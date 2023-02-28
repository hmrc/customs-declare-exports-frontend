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

package controllers.declaration

import base.{ControllerWithoutFormSpec, Injector}
import config.AppConfig
import controllers.routes.RejectedNotificationsController
import forms.declaration.LocationOfGoods
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_FRONTIER
import handlers.ErrorHandler
import mock.ErrorHandlerMocks
import models.declaration.submissions.EnhancedStatus.{ERRORS, RECEIVED}
import models.declaration.submissions.{Action, Submission}
import models.requests.{ExportsSessionKeys, VerifiedEmailRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, GivenWhenThen}
import play.api.i18n.Lang
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testdata.SubmissionsTestData.submission
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.Confirmation
import views.html.declaration.confirmation._
import views.html.error_template

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

class ConfirmationControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach with ErrorHandlerMocks with GivenWhenThen with Injector {
  private val mcc = stubMessagesControllerComponents()
  private val messages = mcc.messagesApi.preferred(List(Lang("en")))

  val lrn = "R12345"
  val ducr = "1GB12121212121212-TRADER-REF-XYZ"
  val submissionId = UUID.randomUUID.toString

  val submissionRecieved = Submission(
    submissionId,
    "eori",
    lrn,
    Some("mrn"),
    Some(ducr),
    Some(RECEIVED),
    Some(ZonedDateTime.now),
    Seq.empty[Action],
    latestDecId = Some(submissionId)
  )
  val submissionWithErrors = Submission(
    submissionId,
    "eori",
    lrn,
    Some("mrn"),
    Some(ducr),
    Some(ERRORS),
    Some(ZonedDateTime.now),
    Seq.empty[Action],
    latestDecId = Some(submissionId)
  )

  trait SetUp {
    val holdingPage = instanceOf[holding_page]
    val confirmationPage = instanceOf[confirmation_page]

    val controller = new ConfirmationController(
      mockAuthAction,
      mockVerifiedEmailAction,
      mockCustomsDeclareExportsConnector,
      mcc,
      new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig]),
      holdingPage,
      confirmationPage
    )

    authorizedUser()
  }

  def buildRequest(queryParam: String = ""): VerifiedEmailRequest[AnyContentAsEmpty.type] = {
    val request = FakeRequest("GET", queryParam).withSession(
      ExportsSessionKeys.submissionDucr -> ducr,
      ExportsSessionKeys.submissionId -> submissionId,
      ExportsSessionKeys.submissionLrn -> lrn
    )

    buildVerifiedEmailRequest(request, exampleUser)
  }

  override def afterEach(): Unit = {
    reset(mockCustomsDeclareExportsConnector)
    super.afterEach()
  }

  "ConfirmationController on displayHoldingPage" should {

    "return the expected page" when {

      "the request does not include a 'js' query parameter" in new SetUp {
        val request = buildRequest()
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK

        val holdingUrl = routes.ConfirmationController.displayHoldingPage.url
        val expectedView = holdingPage(s"$holdingUrl?js=disabled", s"$holdingUrl?js=enabled")(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "the request's query parameter 'js' is equal to 'disabled'" in new SetUp {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val request = buildRequest("/?js=disabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK

        val confirmationUrl = routes.ConfirmationController.displayConfirmationPage.url
        val expectedView = holdingPage(confirmationUrl, "")(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }
    }

    "return 303(SEE_OTHER) status code" when {
      "the request's query parameter 'js' is equal to 'disabled'" in new SetUp {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionRecieved)))

        val request = buildRequest("/?js=disabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConfirmationController.displayConfirmationPage.url)
      }
    }

    "return 200(OK) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in new SetUp {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionRecieved)))

        val request = buildRequest("/?js=enabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe OK
      }
    }

    "return 400(BAD_REQUEST) status code" when {
      "the request's query parameter 'js' is not equal to 'disabled' or 'enabled'" in new SetUp {
        val request = buildRequest("/?js=blabla")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 404(NOT_FOUND) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in new SetUp {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = buildRequest("/?js=enabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe NOT_FOUND
      }
    }
  }

  "ConfirmationController on displayConfirmationPage" should {

    "return 303(SEE_OTHER) status code" when {

      "the request's session does not include the submissionId" in new SetUp {
        val request = buildVerifiedEmailRequest(FakeRequest("GET", ""), exampleUser)
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(toDashboard.url)
      }

      "at least one notification has been received yet" in new SetUp {
        And("the declaration has been rejected")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionWithErrors)))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RejectedNotificationsController.displayPage(submissionId).url)
      }
    }

    "return the expected page" when {

      "no notifications have been received yet" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val submission = Submission(submissionId, "eori", lrn, None, Some(ducr), None, None, Seq.empty[Action], latestDecId = Some(submissionId))
        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, Some(submission), Some(""))
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "at least one notification has been received yet" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionRecieved)))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, Some(submissionRecieved), None)
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "a submission is found but declaration lookup fails" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionRecieved)))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.failed(new Exception()))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, Some(submissionRecieved), None)
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "a submission and declaration are found" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionRecieved)))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withGoodsLocation(LocationOfGoods(""))))))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, Some(submissionRecieved), Some(""))
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }
    }
  }
}
