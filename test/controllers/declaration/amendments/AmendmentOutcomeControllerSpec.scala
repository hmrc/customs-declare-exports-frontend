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

package controllers.declaration.amendments

import base.ControllerWithoutFormSpec
import config.AppConfig
import controllers.routes.RootController
import handlers.ErrorHandler
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{AmendmentCancellationRequest, AmendmentRequest}
import models.declaration.submissions.{Action, NotificationSummary, Submission}
import models.requests.{SessionHelper, VerifiedEmailRequest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.Lang
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.helpers.Confirmation
import views.html.declaration.amendments._
import views.html.declaration.confirmation.holding_page
import views.html.error_template

import java.time.ZonedDateTime
import java.util.{Locale, UUID}
import scala.concurrent.Future

class AmendmentOutcomeControllerSpec extends ControllerWithoutFormSpec with GivenWhenThen with ScalaFutures {

  private val mcc = stubMessagesControllerComponents()
  private val messages = mcc.messagesApi.preferred(List(Lang(Locale.ENGLISH)))

  val title = "declaration.amendment.holding.title"

  val actionId = UUID.randomUUID.toString
  val submissionId = UUID.randomUUID.toString

  trait SetUp {
    val holdingPage = instanceOf[holding_page]
    val amendment_accepted = mock[amendment_accepted]
    val amendment_cancelled = mock[amendment_cancelled]
    val amendment_rejection = mock[amendment_rejection]
    val amendment_failed = mock[amendment_failed]
    val amendment_pending = mock[amendment_pending]

    val controller = new AmendmentOutcomeController(
      mockAuthAction,
      mockVerifiedEmailAction,
      mockCustomsDeclareExportsConnector,
      mcc,
      new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig]),
      holdingPage,
      amendment_accepted,
      amendment_cancelled,
      amendment_rejection,
      amendment_failed,
      amendment_pending,
      mockDeclarationAmendmentsConfig
    )

    authorizedUser()
  }

  def action(maybeStatus: Option[EnhancedStatus] = None): Action =
    Action(
      id = actionId,
      requestType = AmendmentRequest,
      requestTimestamp = ZonedDateTime.now,
      notifications = maybeStatus.map { status =>
        Some(List(NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, status)))
      }.getOrElse(None),
      decId = Some(actionId),
      versionNo = 1
    )

  def submission(actions: List[Action] = List.empty) = Submission(
    submissionId,
    "eori",
    lrn = "R12345",
    Some("mrn"),
    ducr = Some("1GB12121212121212-TRADER-REF-XYZ"),
    Some(RECEIVED),
    Some(ZonedDateTime.now),
    actions,
    latestDecId = Some(submissionId)
  )

  def buildRequest(queryParam: String = ""): VerifiedEmailRequest[AnyContentAsEmpty.type] = {
    val request =
      FakeRequest("GET", queryParam).withSession(SessionHelper.submissionActionId -> actionId, SessionHelper.submissionUuid -> submissionId)

    buildVerifiedEmailRequest(request, exampleUser)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockCustomsDeclareExportsConnector, mockDeclarationAmendmentsConfig)

    when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
    when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(None))
  }

  "AmendmentOutcomeController on displayHoldingPage" should {

    "return the expected page" when {

      "the request does not include a 'js' query parameter" in new SetUp {
        val request = buildRequest()
        val result = controller.displayHoldingPage(false)(request)

        status(result) mustBe OK

        val holdingUrl = routes.AmendmentOutcomeController.displayHoldingPage(false).url
        val redirectToUrl = routes.AmendmentOutcomeController.displayOutcomePage.url
        val expectedView = holdingPage(redirectToUrl, s"$holdingUrl?js=disabled", s"$holdingUrl?js=enabled", title)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "the request's query parameter 'js' is equal to 'disabled'" in new SetUp {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findAction(any())(any(), any()))
          .thenReturn(Future.successful(Some(action())))

        val request = buildRequest("/?js=disabled")
        val result = controller.displayHoldingPage(false)(request)

        status(result) mustBe OK

        val redirectToUrl = routes.AmendmentOutcomeController.displayOutcomePage.url
        val expectedView = holdingPage(redirectToUrl, redirectToUrl, "", title)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }

      "the op is a cancellation" in new SetUp {
        val request = buildRequest()
        val result = controller.displayHoldingPage(true)(request)

        status(result) mustBe OK

        val holdingUrl = routes.AmendmentOutcomeController.displayHoldingPage(true).url
        val redirectToUrl = routes.AmendmentOutcomeController.displayOutcomePage.url
        val title = "declaration.cancel.amendment.holding.title"
        val expectedView = holdingPage(redirectToUrl, s"$holdingUrl?js=disabled", s"$holdingUrl?js=enabled", title)(request, messages)

        val actualView = viewOf(result)
        actualView mustBe expectedView
      }
    }

    "return 303(SEE_OTHER) status code" when {
      "the request's query parameter 'js' is equal to 'disabled'" in new SetUp {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findAction(any())(any(), any()))
          .thenReturn(Future.successful(Some(action(Some(CUSTOMS_POSITION_GRANTED)))))

        val result = controller.displayHoldingPage(false)(buildRequest("/?js=disabled"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AmendmentOutcomeController.displayOutcomePage.url)
      }
    }

    "return 200(OK) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in new SetUp {
        And("at least one notification has been received yet")
        when(mockCustomsDeclareExportsConnector.findAction(any())(any(), any()))
          .thenReturn(Future.successful(Some(action(Some(CUSTOMS_POSITION_GRANTED)))))

        val result = controller.displayHoldingPage(false)(buildRequest("/?js=enabled"))

        status(result) mustBe OK
      }
    }

    "return 400(BAD_REQUEST) status code" when {
      "the request's query parameter 'js' is not equal to 'disabled' or 'enabled'" in new SetUp {
        val result = controller.displayHoldingPage(false)(buildRequest("/?js=blabla"))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 404(NOT_FOUND) status code" when {
      "the request's query parameter 'js' is equal to 'enabled'" in new SetUp {
        And("no notifications have been received yet")
        when(mockCustomsDeclareExportsConnector.findAction(any())(any(), any()))
          .thenReturn(Future.successful(Some(action())))

        val result = controller.displayHoldingPage(false)(buildRequest("/?js=enabled"))

        status(result) mustBe NOT_FOUND
      }
    }
  }

  "AmendmentOutcomeController on displayOutcomePage" should {

    "redirect to the RootController" when {
      "the 'declarationAmendmentsConfig' flag is disabled" in new SetUp {
        when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(false)

        val result = controller.displayOutcomePage(buildRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "return 500(INTERNAL_SERVER_ERROR) status code" when {

      "the request's session does not include the submissionUuid" in new SetUp {
        val request = FakeRequest("GET", "").withSession(SessionHelper.submissionActionId -> actionId)
        val result = controller.displayOutcomePage(buildVerifiedEmailRequest(request, exampleUser))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the request's session does not include the submissionActionId" in new SetUp {
        val request = FakeRequest("GET", "").withSession(SessionHelper.submissionUuid -> submissionId)
        val result = controller.displayOutcomePage(buildVerifiedEmailRequest(request, exampleUser))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the submission specified via Session.submissionUuid cannot be retrieved" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the action specified via Session.submissionActionId cannot be retrieved" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission())))

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 200(OK) status code" when {

      "the notification against the amendment request has CUSTOMS_POSITION_DENIED status (failed)" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission(List(action(Some(CUSTOMS_POSITION_DENIED)))))))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        when(amendment_failed.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe OK

        val confirmationCaptor = ArgumentCaptor.forClass(classOf[Confirmation])
        verify(amendment_failed).apply(confirmationCaptor.capture())(any(), any())
        confirmationCaptor.getValue.asInstanceOf[Confirmation].submission.uuid mustBe submissionId
      }

      "the notification against the amendment request has CUSTOMS_POSITION_GRANTED status (accepted)" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission(List(action(Some(CUSTOMS_POSITION_GRANTED)))))))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        when(amendment_accepted.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe OK

        val confirmationCaptor = ArgumentCaptor.forClass(classOf[Confirmation])
        verify(amendment_accepted).apply(confirmationCaptor.capture())(any(), any())
        confirmationCaptor.getValue.asInstanceOf[Confirmation].submission.uuid mustBe submissionId
      }

      "the notification against the amendment cancellation has CUSTOMS_POSITION_GRANTED status (accepted)" in new SetUp {
        val cancellation = action(Some(CUSTOMS_POSITION_GRANTED)).copy(requestType = AmendmentCancellationRequest)
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission(List(cancellation)))))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        when(amendment_cancelled.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe OK

        val confirmationCaptor = ArgumentCaptor.forClass(classOf[Confirmation])
        verify(amendment_cancelled).apply(confirmationCaptor.capture())(any(), any())
        confirmationCaptor.getValue.asInstanceOf[Confirmation].submission.uuid mustBe submissionId
      }

      "the notification against the amendment request has ERRORS status (rejected)" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission(List(action(Some(ERRORS)))))))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        when(amendment_rejection.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe OK

        val confirmationCaptor = ArgumentCaptor.forClass(classOf[Confirmation])
        verify(amendment_rejection).apply(confirmationCaptor.capture())(any(), any())
        confirmationCaptor.getValue.asInstanceOf[Confirmation].submission.uuid mustBe submissionId
      }

      "no notification against the amendment request has been received yet" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission(List(action())))))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withAdditionalDeclarationType()))))

        when(amendment_pending.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.displayOutcomePage(buildRequest())

        status(result) mustBe OK

        val confirmationCaptor = ArgumentCaptor.forClass(classOf[Confirmation])
        verify(amendment_pending).apply(confirmationCaptor.capture())(any(), any())
        confirmationCaptor.getValue.asInstanceOf[Confirmation].submission.uuid mustBe submissionId
      }
    }
  }
}
