/*
 * Copyright 2022 HM Revenue & Customs
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

import base.{ControllerWithoutFormSpec, Injector}
import mock.ErrorHandlerMocks
import models.declaration.submissions.EnhancedStatus.{CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED, EnhancedStatus, QUERY_NOTIFICATION_MESSAGE}
import models.declaration.submissions.RequestType.CancellationRequest
import models.declaration.submissions.{Action, NotificationSummary, Submission}
import models.requests.{ExportsSessionKeys, VerifiedEmailRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.GivenWhenThen
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, SEE_OTHER}
import play.api.i18n.Lang
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, session, status, OK}
import testdata.SubmissionsTestData.{eori, lrn, mrn, uuid}
import views.html.{cancellation_holding, cancellation_result}

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

class CancellationResultControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with Injector with GivenWhenThen {

  private val mcc = stubMessagesControllerComponents()
  private val messages = mcc.messagesApi.preferred(List(Lang("en")))

  val holdingPage = instanceOf[cancellation_holding]
  val resultPage = instanceOf[cancellation_result]

  val controller = new CancellationResultController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockCustomsDeclareExportsConnector,
    mcc,
    mockErrorHandler,
    holdingPage,
    resultPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
  }

  override protected def afterEach(): Unit =
    reset(mockCustomsDeclareExportsConnector)

  def buildRequest(queryParam: String = "", submissionId: Option[String] = None): VerifiedEmailRequest[AnyContentAsEmpty.type] = {
    val session = if (submissionId.isDefined) ExportsSessionKeys.submissionId -> submissionId.get else ("", "")
    val request = FakeRequest("GET", queryParam).withSession(session)
    buildVerifiedEmailRequest(request, exampleUser)
  }

  def submissionWithStatus(status: Option[EnhancedStatus]): Option[Submission] = {
    val action = Seq(if (status.isDefined) actionWithNotificationSummary(status.get) else actionWithoutNotificationSummary)
    Some(Submission(eori = eori, lrn = lrn, actions = action))
  }

  private def notificationSummary(status: EnhancedStatus) =
    NotificationSummary(UUID.randomUUID(), ZonedDateTime.now(), status)

  private def actionWithNotificationSummary(status: EnhancedStatus) =
    Action(id = "id", requestType = CancellationRequest, notifications = Some(Seq(notificationSummary(status))))

  private val actionWithoutNotificationSummary =
    Action(id = "id", requestType = CancellationRequest, notifications = None)

  "CancellationResultController on displayHoldingPage" should {

    "return the expected page" when {

      "the request does not include a 'js' query parameter" in {
        val request = buildRequest(submissionId = Some(uuid))
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK

        val holdingUrl = routes.CancellationResultController.displayHoldingPage.url
        val expectedView = holdingPage(s"$holdingUrl?js=disabled", s"$holdingUrl?js=enabled", mrn)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "the request's query parameter 'js' is equal to 'disabled'" in {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(None)))

        val request = buildRequest("/?js=disabled", Some(uuid))
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK

        val confirmationUrl = routes.CancellationResultController.displayResultPage.url
        val expectedView = holdingPage(confirmationUrl, "", mrn)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }
    }

    "return 303(SEE_OTHER) status code" when {
      "the request's query parameter 'js' is equal to 'disabled'" in {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(Some(CUSTOMS_POSITION_GRANTED))))

        val request = buildRequest("/?js=disabled", Some(uuid))
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

        val request = buildRequest("/?js=enabled", Some(uuid))
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK
      }
    }

    "return 400(BAD_REQUEST) status code" when {
      "the request's query parameter 'js' is not equal to 'disabled' or 'enabled'" in {
        val request = buildRequest("/?js=blabla", Some(uuid))
        val result = controller.displayHoldingPage(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 404(NOT_FOUND) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(submissionWithStatus(None)))

        val request = buildRequest("/?js=enabled", Some(uuid))
        val result = controller.displayHoldingPage(request)

        status(result) mustBe NOT_FOUND
      }
    }

    "return 400 status code" when {

      "the request's session does not include the submissionId" in {
        val request = buildRequest()
        val result = controller.displayHoldingPage(request)

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "CancellationResultController on displayResultPage" should {

    "return 400 status code" when {

      "the request's session does not include the submissionId" in {
        val request = buildRequest()
        val result = controller.displayResultPage(request)

        status(result) mustBe BAD_REQUEST
        session(result).data.keys mustNot contain(ExportsSessionKeys.submissionId)
      }
    }

    "return the expected page" when {

      for (notificationStatus <- List(None, Some(CUSTOMS_POSITION_GRANTED), Some(CUSTOMS_POSITION_DENIED), Some(QUERY_NOTIFICATION_MESSAGE)))
        s"submission with notification status $notificationStatus has been received" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(submissionWithStatus(notificationStatus)))

          val request = buildRequest(submissionId = Some(uuid))
          val result = controller.displayResultPage(request)

          status(result) mustBe OK
          session(result).data.keys mustNot contain(ExportsSessionKeys.submissionId)

          val expectedView = resultPage(notificationStatus, mrn)(request, messages)

          val actualView = viewOf(result)
          actualView mustBe expectedView
        }
    }
  }
}
