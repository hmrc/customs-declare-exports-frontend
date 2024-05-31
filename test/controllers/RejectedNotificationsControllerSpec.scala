/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import base.ControllerWithoutFormSpec
import mock.FeatureFlagMocks
import models.declaration.notifications.Notification
import models.declaration.submissions.Action
import models.requests.SessionHelper.submissionUuid
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, BodyParser, Request, Result, Action => PlayAction}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.rejected_notification_errors

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class RejectedNotificationsControllerSpec extends ControllerWithoutFormSpec with OptionValues with FeatureFlagMocks {

  private val mockErrorPage = mock[rejected_notification_errors]
  private val mockErrorsReportedController = mock[ErrorsReportedController]

  private val controller = new RejectedNotificationsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    errorHandler,
    mockCustomsDeclareExportsConnector,
    mcc,
    mockNewErrorReportConfig,
    mockErrorsReportedController,
    mockErrorPage,
    mockTdrFeatureFlags
  )(global)

  private val declarationId = "DeclarationId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()

    when(mockErrorPage.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockNewErrorReportConfig.isNewErrorReportEnabled).thenReturn(false)
    when(mockTdrFeatureFlags.showErrorPageVersionForTdr).thenReturn(false)

    val fakeAction = new PlayAction[AnyContent] {
      override def parser: BodyParser[AnyContent] = ???
      override def apply(request: Request[AnyContent]): Future[Result] = Future.successful(Ok(""))
      override def executionContext: ExecutionContext = ???
    }
    when(mockErrorsReportedController.displayPage(any())).thenReturn(fakeAction)
    when(mockErrorsReportedController.displayPageOnUnacceptedAmendment(any(), any())).thenReturn(fakeAction)
  }

  override protected def afterEach(): Unit = {
    reset(mockErrorPage, mockNewErrorReportConfig)
    super.afterEach()
  }

  "RejectedNotificationsController.displayPage" should {

    "return 200 (OK) and the expected page" when {

      "declaration and notifications are found" in {
        fetchDeclaration(declarationId)
        findNotifications(declarationId)

        verifyResult(controller.displayPage(declarationId)(getRequest()), None, None)
      }

      "the declaration is found but the Submission has no notifications" in {
        fetchDeclaration(declarationId)

        when(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any()))
          .thenReturn(Future.successful(List.empty))

        verifyResult(controller.displayPage(declarationId)(getRequest()), None, None)
      }

      "newExportsReport flag is enabled" in {
        when(mockNewErrorReportConfig.isNewErrorReportEnabled).thenReturn(true)
        val result = controller.displayPage(declarationId)(getRequest())

        status(result) mustBe OK
        verify(mockErrorsReportedController).displayPage(any())
      }

      "the showErrorPageVersionForTdr is enabled" in {
        when(mockTdrFeatureFlags.showErrorPageVersionForTdr).thenReturn(true)

        fetchDeclaration(declarationId)
        findNotifications(declarationId)

      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "the declaration cannot be found" in {
        declarationNotFound

        val result = controller.displayPage(declarationId)(getRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "RejectedNotificationsController.displayPageOnUnacceptedAmendment" should {

    "return 200 (OK) and the expected page" when {
      val submissionId = "submissionId"
      val request = FakeRequest("GET", "").withSession(submissionUuid -> submissionId)
      val draftDeclarationId = "1234"

      "Action, declaration and notification are found" in {
        fetchAction(failedAction)
        fetchDeclaration(failedAction.decId.value)
        fetchLatestNotification(failedNotification)

        val result = controller.displayPageOnUnacceptedAmendment(failedAction.id)(request)
        verifyResult(result, failedAction.decId, Some(submissionId))
      }

      "for a draft declaration of an unaccepted amendment" in {
        fetchAction(failedAction)
        fetchDeclaration(draftDeclarationId)
        fetchLatestNotification(failedNotification)

        val result = controller.displayPageOnUnacceptedAmendment(failedAction.id, Some(draftDeclarationId))(request)
        verifyResult(result, Some(draftDeclarationId), Some(submissionId))
      }

      "newExportsReport flag is enabled" in {
        when(mockNewErrorReportConfig.isNewErrorReportEnabled).thenReturn(true)
        val result = controller.displayPageOnUnacceptedAmendment(failedAction.id, Some(draftDeclarationId))(getRequest())

        status(result) mustBe OK
        verify(mockErrorsReportedController).displayPageOnUnacceptedAmendment(any(), any())
      }

      "the showErrorPageVersionForTdr is enabled" in {
        when(mockTdrFeatureFlags.showErrorPageVersionForTdr).thenReturn(true)

        fetchAction(failedAction)
        fetchDeclaration(failedAction.decId.value)
        fetchLatestNotification(failedNotification)

      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {

      "the Action cannot be found" in {
        when(mockCustomsDeclareExportsConnector.findAction(any())(any(), any())).thenReturn(Future.successful(None))
        val result = controller.displayPageOnUnacceptedAmendment(failedAction.id)(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "Action.decId is None" in {
        fetchAction(failedAction.copy(decId = None))
        val result = controller.displayPageOnUnacceptedAmendment(failedAction.id)(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the notification cannot be found" in {
        fetchAction(failedAction)
        fetchDeclaration(failedAction.decId.value)
        when(mockCustomsDeclareExportsConnector.findLatestNotification(any())(any(), any())).thenReturn(Future.successful(None))

        val result = controller.displayPageOnUnacceptedAmendment(failedAction.id)(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  private def verifyResult(result: Future[Result], expectedDec: Option[String], expectedSub: Option[String]): Assertion = {
    status(result) mustBe OK
    val captorDec = ArgumentCaptor.forClass(classOf[Option[String]])
    val captorSub = ArgumentCaptor.forClass(classOf[Option[String]])
    verify(mockErrorPage).apply(captorSub.capture(), any(), any(), captorDec.capture(), any())(any(), any())
    captorDec.getValue.asInstanceOf[Option[String]] mustBe expectedDec
    captorSub.getValue.asInstanceOf[Option[String]] mustBe expectedSub
  }

  val failedAction: Action =
    Json
      .parse(s"""{
      |  "id" : "7f73dc47-6aaa-4122-b0ef-547e500c81e7",
      |  "requestType" : "AmendmentRequest",
      |  "decId" : "3dd4bb59-6174-429e-8386-b876b558b35d",
      |  "versionNo" : 2,
      |  "notifications" : [
      |    {
      |      "notificationId" : "6629e651-8352-4454-a8c2-76b6ea200ab3",
      |      "dateTimeIssued" : "2023-03-28T12:51:15Z[UTC]",
      |      "enhancedStatus" : "CUSTOMS_POSITION_DENIED"
      |    }
      |  ],
      |  "requestTimestamp" : "2023-03-27T12:46:13.936473Z[UTC]"
      |}""".stripMargin)
      .as[Action]

  val failedNotification: Notification =
    Json
      .parse(s"""{
      |  "actionId" : "7f73dc47-6aaa-4122-b0ef-547e500c81e7",
      |  "mrn" : "23GB6580NC91061096",
      |  "dateTimeIssued" : "2023-05-16T12:28:20Z[UTC]",
      |  "status" : "CUSTOMS_POSITION_DENIED",
      |  "errors" : [
      |    {
      |      "validationCode" : "CDS10020",
      |      "pointer" : "declaration.items.#1.additionalDocument.#2.documentStatus"
      |    }
      |  ]
      |}""".stripMargin)
      .as[Notification]
}
