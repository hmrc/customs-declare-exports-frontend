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
import config.AppConfig
import connectors.CodeListConnector
import handlers.ErrorHandler
import mock.FeatureFlagMocks
import models.declaration.notifications.Notification
import models.declaration.submissions.Action
import models.requests.SessionHelper.submissionUuid
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.helpers.ErrorsReportedHelper
import views.html.{error_template, errors_reported}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ErrorsReportedControllerSpec extends ControllerWithoutFormSpec with OptionValues with FeatureFlagMocks {

  private val mockErrorsReportedPage = mock[errors_reported]
  private val mockErrorsReportedHelper = mock[ErrorsReportedHelper]
  private val codeListConnector = mock[CodeListConnector]

  private val mcc = stubMessagesControllerComponents()

  private val controller = new ErrorsReportedController(
    mockAuthAction,
    mockVerifiedEmailAction,
    new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig]),
    mockCustomsDeclareExportsConnector,
    codeListConnector,
    mcc,
    mockErrorsReportedHelper,
    mockErrorsReportedPage
  )(global)

  private val declarationId = "DeclarationId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockErrorsReportedPage.apply(any(), any(), any(), any(), any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCustomsDeclareExportsConnector.findDraftByParent(any())(any(), any())).thenReturn(Future.successful(None))
  }

  override protected def afterEach(): Unit = {
    reset(mockErrorsReportedPage, mockCustomsDeclareExportsConnector)
    super.afterEach()
  }

  "ErrorsReportedController.displayPage" should {

    "return 200 (OK)" when {

      "declaration, draft declaration and notifications are found" in {
        fetchDeclaration(declarationId)
        fetchDraftByParent(declarationId)
        findNotifications(declarationId)

        verifyResult(controller.displayPage(declarationId)(getRequest()), None, None)
      }

      "declaration and notifications are found but no draft declaration" in {
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
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "the declaration cannot be found" in {
        declarationNotFound

        when(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any()))
          .thenReturn(Future.successful(List.empty))

        val result = controller.displayPage(declarationId)(getRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "ErrorsReportedController.displayPageOnUnacceptedAmendment" should {

    "return 200 (OK)" when {
      val submissionId = "submissionId"
      val request = FakeRequest("GET", "").withSession(submissionUuid -> submissionId)
      val draftDeclarationId = "1234"

      "Action, declaration, draft dec and notification are found" in {
        fetchAction(failedAction)
        fetchDeclaration(failedAction.decId.value)
        fetchDraftByParent(failedAction.decId.value)
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
    verify(mockErrorsReportedPage).apply(captorSub.capture(), any(), any(), captorDec.capture(), any())(any(), any(), any())
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
