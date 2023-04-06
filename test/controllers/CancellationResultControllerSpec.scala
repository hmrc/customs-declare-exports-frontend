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
import mock.ErrorHandlerMocks
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.CancellationRequest
import models.declaration.submissions.{Action, NotificationSummary, Submission}
import models.requests.{SessionHelper, VerifiedEmailRequest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.GivenWhenThen
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, session, status, OK}
import play.twirl.api.HtmlFormat
import testdata.SubmissionsTestData.{eori, lrn, mrn, uuid}
import views.html.{cancellation_holding, cancellation_result}

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

class CancellationResultControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with GivenWhenThen {

  val holdingPage = mock[cancellation_holding]
  val resultPage = mock[cancellation_result]

  val controller = new CancellationResultController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockCustomsDeclareExportsConnector,
    stubMessagesControllerComponents(),
    mockErrorHandler,
    holdingPage,
    resultPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(holdingPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(resultPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit =
    reset(holdingPage, mockCustomsDeclareExportsConnector, resultPage)

  def buildRequest(
    queryParam: String = "",
    submissionId: Option[String] = Some(uuid),
    submissionMrn: Option[String] = Some(mrn)
  ): VerifiedEmailRequest[AnyContentAsEmpty.type] = {
    val session = List(
      submissionId.fold("dummyKey1" -> "dummyVal")(uuid => SessionHelper.submissionUuid -> uuid),
      submissionMrn.fold("dummyKey2" -> "dummyVal")(mrn => SessionHelper.submissionMrn -> mrn)
    )
    val request = FakeRequest("GET", queryParam).withSession(session: _*)
    buildVerifiedEmailRequest(request, exampleUser)
  }

  def submissionWithStatus(status: Option[EnhancedStatus]): Option[Submission] = {
    val action = Seq(if (status.isDefined) actionWithNotificationSummary(status.get) else actionWithoutNotificationSummary)
    val uuid = UUID.randomUUID().toString
    Some(Submission(uuid, eori = eori, lrn = lrn, actions = action, latestDecId = Some(uuid)))
  }

  private def notificationSummary(status: EnhancedStatus) =
    NotificationSummary(UUID.randomUUID(), ZonedDateTime.now(), status)

  private def actionWithNotificationSummary(status: EnhancedStatus) =
    Action(id = "id", requestType = CancellationRequest, notifications = Some(Seq(notificationSummary(status))), decId = Some(uuid), versionNo = 1)

  private val actionWithoutNotificationSummary =
    Action(id = "id", requestType = CancellationRequest, notifications = None, decId = Some(uuid), versionNo = 1)

  "CancellationResultController.displayHoldingPage" should {

    "return the expected page" when {

      "the request does not include a 'js' query parameter" in {
        implicit val request = buildRequest()
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK

        val jsDisabledCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsEnabledCaptor = ArgumentCaptor.forClass(classOf[String])
        val mrnCaptor = ArgumentCaptor.forClass(classOf[String])

        verify(holdingPage).apply(jsDisabledCaptor.capture(), jsEnabledCaptor.capture(), mrnCaptor.capture())(any(), any())

        val holdingUrl = routes.CancellationResultController.displayHoldingPage.url

        jsDisabledCaptor.getValue.asInstanceOf[String] mustBe s"$holdingUrl?js=disabled"
        jsEnabledCaptor.getValue.asInstanceOf[String] mustBe s"$holdingUrl?js=enabled"
        mrnCaptor.getValue.asInstanceOf[String] mustBe mrn
      }

      "the request's query parameter 'js' is equal to 'disabled'" in {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(None)))

        implicit val request = buildRequest("/?js=disabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK

        val jsDisabledCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsEnabledCaptor = ArgumentCaptor.forClass(classOf[String])
        val mrnCaptor = ArgumentCaptor.forClass(classOf[String])

        verify(holdingPage).apply(jsDisabledCaptor.capture(), jsEnabledCaptor.capture(), mrnCaptor.capture())(any(), any())

        jsDisabledCaptor.getValue.asInstanceOf[String] mustBe routes.CancellationResultController.displayResultPage.url
        jsEnabledCaptor.getValue.asInstanceOf[String] mustBe ""
        mrnCaptor.getValue.asInstanceOf[String] mustBe mrn
      }
    }

    "return 303(SEE_OTHER) status code" when {
      "the request's query parameter 'js' is equal to 'disabled'" in {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(Some(CUSTOMS_POSITION_GRANTED))))

        val request = buildRequest("/?js=disabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CancellationResultController.displayResultPage.url)
      }
    }

    "return 200(OK) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(Some(CUSTOMS_POSITION_GRANTED))))

        val request = buildRequest("/?js=enabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK
      }
    }

    "return 400(BAD_REQUEST) status code" when {
      "the request's query parameter 'js' is not equal to 'disabled' or 'enabled'" in {
        val request = buildRequest("/?js=blabla")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 404(NOT_FOUND) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(None)))

        val request = buildRequest("/?js=enabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe NOT_FOUND
      }
    }

    "return 400 status code" when {

      "the request's session does not include the submissionUuid" in {
        val request = buildRequest(submissionId = None)
        val result = controller.displayHoldingPage(request)

        status(result) mustBe BAD_REQUEST
        session(result).data.keys mustNot contain(SessionHelper.submissionUuid)
      }

      "the request's session does not include the submission's mrn" in {
        val request = buildRequest(submissionMrn = None)
        val result = controller.displayHoldingPage(request)

        status(result) mustBe BAD_REQUEST
        session(result).data.keys mustNot contain(SessionHelper.submissionMrn)
      }
    }
  }

  "CancellationResultController.displayResultPage" should {

    "return 400 status code" when {

      "the request's session does not include the submissionUuid" in {
        val request = buildRequest(submissionId = None)
        val result = controller.displayResultPage(request)

        status(result) mustBe BAD_REQUEST
        session(result).data.keys mustNot contain(SessionHelper.submissionUuid)
      }

      "the request's session does not include the submission's mrn" in {
        val request = buildRequest(submissionMrn = None)
        val result = controller.displayResultPage(request)

        status(result) mustBe BAD_REQUEST
        session(result).data.keys mustNot contain(SessionHelper.submissionMrn)
      }
    }

    "return the expected page" when {

      for (notificationStatus <- List(None, Some(CUSTOMS_POSITION_GRANTED), Some(CUSTOMS_POSITION_DENIED), Some(QUERY_NOTIFICATION_MESSAGE)))
        s"submission with notification status $notificationStatus has been received" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(submissionWithStatus(notificationStatus)))

          implicit val request = buildRequest()
          val result = controller.displayResultPage(request)

          status(result) mustBe OK
          session(result).data.keys mustNot contain(SessionHelper.submissionUuid)

          val statusCaptor = ArgumentCaptor.forClass(classOf[Option[EnhancedStatus]])
          val mrnCaptor = ArgumentCaptor.forClass(classOf[String])

          verify(resultPage).apply(statusCaptor.capture(), mrnCaptor.capture())(any(), any())

          val expectedStatus = notificationStatus match {
            case Some(CUSTOMS_POSITION_DENIED) | Some(CUSTOMS_POSITION_GRANTED) => notificationStatus
            case _                                                              => None
          }
          statusCaptor.getValue.asInstanceOf[Option[EnhancedStatus]] mustBe expectedStatus
          mrnCaptor.getValue.asInstanceOf[String] mustBe mrn
        }
    }
  }
}
