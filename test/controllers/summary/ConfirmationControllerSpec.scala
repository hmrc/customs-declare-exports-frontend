/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.summary

import base.ControllerWithoutFormSpec
import controllers.summary.routes.ConfirmationController
import controllers.timeline.routes.RejectedNotificationsController
import forms.section1.AdditionalDeclarationType.STANDARD_FRONTIER
import forms.section3.LocationOfGoods
import models.declaration.submissions.EnhancedStatus.{ERRORS, RECEIVED}
import models.declaration.submissions.{Action, Submission}
import models.requests.{SessionHelper, VerifiedEmailRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.GivenWhenThen
import play.api.i18n.Lang
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testdata.SubmissionsTestData.{createSubmission, submission}
import views.helpers.Confirmation
import views.html.summary._

import java.time.ZonedDateTime
import java.util.{Locale, UUID}
import scala.concurrent.Future

class ConfirmationControllerSpec extends ControllerWithoutFormSpec with GivenWhenThen {

  private val messages = mcc.messagesApi.preferred(List(Lang(Locale.ENGLISH)))

  val lrn = "R12345"
  val ducr = "1GB12121212121212-TRADER-REF-XYZ"
  val submissionId = UUID.randomUUID.toString

  val submissionReceived = Submission(
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
      errorHandler,
      holdingPage,
      confirmationPage
    )

    authorizedUser()
  }

  def buildRequest(queryParam: String = ""): VerifiedEmailRequest[AnyContentAsEmpty.type] = {
    val request = FakeRequest("GET", queryParam).withSession(
      SessionHelper.submissionDucr -> ducr,
      SessionHelper.submissionUuid -> submissionId,
      SessionHelper.submissionLrn -> lrn
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

        val holdingUrl = ConfirmationController.displayHoldingPage.url
        val redirectToUrl = ConfirmationController.displayConfirmationPage.url
        val expectedView = holdingPage(redirectToUrl, s"$holdingUrl?js=disabled", s"$holdingUrl?js=enabled")(request, messages)

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

        val redirectToUrl = ConfirmationController.displayConfirmationPage.url
        val expectedView = holdingPage(redirectToUrl, redirectToUrl, "")(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }
    }

    "return 303(SEE_OTHER) status code" when {
      "the request's query parameter 'js' is equal to 'disabled'" in new SetUp {
        And("at least one notification has been received yet")
        val submission = createSubmission(statuses = List(RECEIVED))
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val request = buildRequest("/?js=disabled")
        val result = controller.displayHoldingPage(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ConfirmationController.displayConfirmationPage.url)
      }
    }

    "return 200(OK) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in new SetUp {
        And("at least one notification has been received yet")
        val submission = createSubmission(statuses = List(RECEIVED))
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

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
      "at least one notification has been received" in new SetUp {
        And("the declaration has been rejected")
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionWithErrors)))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RejectedNotificationsController.displayPage(submissionId).url)
      }
    }

    "return 500(INTERNAL_SERVER_ERROR) status code" when {

      "the request's session does not include the submissionUuid" in new SetUp {
        val request = buildVerifiedEmailRequest(FakeRequest("GET", ""), exampleUser)
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "a submission is NOT found" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(None))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "a submission is found but the declaration lookup fails" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionReceived)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(None))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return the expected page" when {

      "no notifications have been received yet" in new SetUp {
        val submission = Submission(submissionId, "eori", lrn, None, Some(ducr), None, None, Seq.empty[Action], latestDecId = Some(submissionId))
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, submission, None)
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "at least one notification has been received" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionReceived)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, submissionReceived, None)
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "submission and declaration are found" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submissionReceived)))

        val declaration = aDeclaration(withAdditionalDeclarationType(), withGoodsLocation(LocationOfGoods("")))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        val request = buildRequest()
        val result = controller.displayConfirmationPage(request)

        status(result) mustBe OK

        val confirmation = Confirmation(request.email, STANDARD_FRONTIER.toString, submissionReceived, Some(""))
        val expectedView = confirmationPage(confirmation)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }
    }
  }
}
