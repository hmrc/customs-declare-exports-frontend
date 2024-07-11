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

package controllers.summary

import base.ControllerWithoutFormSpec
import controllers.amendments.routes.AmendmentOutcomeController
import controllers.general.routes.RootController
import controllers.routes.ChoiceController
import controllers.summary.routes.{ConfirmationController, SubmissionController, SummaryController}
import forms.summary.LegalDeclaration
import forms.summary.LegalDeclaration._
import forms.timeline.AmendmentSubmission.reasonKey
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.{AMENDMENT_DRAFT, COMPLETE}
import models.declaration.submissions.EnhancedStatus.RECEIVED
import models.declaration.submissions.Submission
import models.requests.SessionHelper
import models.requests.SessionHelper._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, session, status}
import play.twirl.api.HtmlFormat
import services.SubmissionService
import services.view.AmendmentAction.{Cancellation, Resubmission, Submission => SubmissionAmendment}
import uk.gov.hmrc.http.HeaderCarrier
import views.common.UnitViewSpec
import views.html.amendments.amendment_submission
import views.html.summary.legal_declaration

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SubmissionControllerSpec extends ControllerWithoutFormSpec with ScalaFutures with UnitViewSpec {

  private val amendmentSubmissionPage = mock[amendment_submission]
  private val legalDeclarationPage = mock[legal_declaration]
  private val mockSubmissionService = mock[SubmissionService]

  private val controller = new SubmissionController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    mcc,
    mockCustomsDeclareExportsConnector,
    mockExportsCacheService,
    mockSubmissionService,
    amendmentSubmissionPage,
    legalDeclarationPage,
    mockDeclarationAmendmentsConfig
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(amendmentSubmissionPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(legalDeclarationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(None))
    when(mockDeclarationAmendmentsConfig.isDisabled).thenReturn(false)
  }

  override protected def afterEach(): Unit = {
    reset(amendmentSubmissionPage, legalDeclarationPage, mockCustomsDeclareExportsConnector, mockDeclarationAmendmentsConfig, mockSubmissionService)
    super.afterEach()
  }

  val uuid = UUID.randomUUID().toString

  val expectedSubmission = Submission(
    uuid,
    eori = "GB123456",
    lrn = "123LRN",
    ducr = Some("ducr"),
    actions = List.empty,
    latestDecId = Some(uuid),
    latestEnhancedStatus = Some(RECEIVED)
  )

  "SubmissionController.displaySubmitDeclarationPage" should {

    "return 200 and invoke the expected page" in {
      withNewCaching(aDeclaration(withItems(anItem(withAdditionalInformation("code", "description")))))

      val result = controller.displaySubmitDeclarationPage.apply(getJourneyRequest())
      status(result) mustBe OK

      verify(legalDeclarationPage).apply(any())(any(), any())
      verifyNoInteractions(amendmentSubmissionPage)
    }

    "redirect to /saved-summary-no-items" when {
      "trying to submit a declaration without items" in {
        withNewCaching(aDeclaration())

        val result = controller.displaySubmitDeclarationPage.apply(getJourneyRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SummaryController.displayPageOnNoItems.url)
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "in error-fix mode" in {
        val declaration = aDeclaration()
        when(mockExportsCacheService.get(any())(any())).thenReturn(Future.successful(Some(declaration)))

        val request = getJourneyRequest(declaration, errorFixModeSessionKey -> "true")
        val result = controller.displaySubmitDeclarationPage.apply(request)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "SubmissionController.displaySubmitAmendmentPage" should {

    "return 200 and invoke the expected page" in {
      withNewCaching(aDeclaration(withStatus(AMENDMENT_DRAFT)))

      val result = controller.displaySubmitAmendmentPage.apply(getJourneyRequest())
      status(result) mustBe OK

      verify(amendmentSubmissionPage).apply(any(), eqTo(SubmissionAmendment))(any(), any())
      verifyNoInteractions(legalDeclarationPage)
    }

    "Redirect to root controller" when {
      "feature flag is off" in {
        when(mockDeclarationAmendmentsConfig.isDisabled).thenReturn(true)
        withNewCaching(aDeclaration())

        val result = controller.displaySubmitAmendmentPage.apply(getJourneyRequest())
        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoInteractions(amendmentSubmissionPage)
      }
    }

    "Redirect to the '/choice' page" when {
      "the amendments to the declaration were already submitted" in {
        withNewCaching(aDeclaration(withStatus(COMPLETE)))

        val result = controller.displaySubmitAmendmentPage.apply(getJourneyRequest())
        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(ChoiceController.displayPage.url)
        verifyNoInteractions(amendmentSubmissionPage)
      }
    }
  }

  "SubmissionController.displayResubmitAmendmentPage" should {

    "return 200 and invoke the expected page" in {
      withNewCaching(aDeclaration())

      val result = controller.displayResubmitAmendmentPage.apply(getJourneyRequest())
      status(result) mustBe OK

      verify(amendmentSubmissionPage).apply(any(), eqTo(Resubmission))(any(), any())
      verifyNoInteractions(legalDeclarationPage)
    }

    "Redirect to root controller" when {
      "feature flag is off" in {
        when(mockDeclarationAmendmentsConfig.isDisabled).thenReturn(true)
        withNewCaching(aDeclaration())

        val result = controller.displayResubmitAmendmentPage.apply(getJourneyRequest())
        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoInteractions(amendmentSubmissionPage)
      }
    }
  }

  "SubmissionController.cancelAmendment" when {
    val declaration = aDeclaration()

    "feature flag is off" should {
      "Redirect to root controller" in {
        when(mockDeclarationAmendmentsConfig.isDisabled).thenReturn(true)

        val result = controller.cancelAmendment(declaration.id)(getJourneyRequest())
        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoInteractions(amendmentSubmissionPage)
      }
    }

    "feature flag is on" should {

      "Redirect to the Cancel Amendment page" when {
        "Backend returns a Submission with a defined latestDecId and findOrCreateDraftForAmendment is successful" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(expectedSubmission)))

          when(mockCustomsDeclareExportsConnector.findDeclaration(eqTo(declaration.id))(any(), any()))
            .thenReturn(Future.successful(Some(declaration)))

          when(mockCustomsDeclareExportsConnector.findOrCreateDraftForAmendment(any(), any(), any(), eqTo(declaration))(any(), any()))
            .thenReturn(Future.successful("declarationId"))

          val result = controller.cancelAmendment(declaration.id)(getRequestWithSession((submissionUuid, "Id")))
          status(result) must be(SEE_OTHER)

          val url = SubmissionController.displayCancelAmendmentPage.url
          redirectLocation(result) must be(Some(url))
        }
      }

      "return 500 (INTERNAL_SERVER_ERROR)" when {

        "no submissionUuid is found in session" in {
          val result = controller.cancelAmendment(declaration.id)(getJourneyRequest())
          status(result) mustBe INTERNAL_SERVER_ERROR
        }

        "Backend fails to find/return a matching submission" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(None))

          val result = controller.cancelAmendment(declaration.id)(getRequestWithSession((submissionUuid, "Id")))
          status(result) mustBe INTERNAL_SERVER_ERROR
        }

        "no declaration is found for the provided declarationId" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(expectedSubmission)))

          when(mockCustomsDeclareExportsConnector.findDeclaration(eqTo(declaration.id))(any(), any()))
            .thenReturn(Future.successful(None))

          val result = controller.cancelAmendment(declaration.id)(getRequestWithSession((submissionUuid, "Id")))
          status(result) mustBe INTERNAL_SERVER_ERROR
        }

        "latestDecId does not belong to the appropriate submission" in {
          val uuid = UUID.randomUUID().toString
          val expectedSubmission = Submission(uuid, eori = "GB123456", lrn = "123LRN", ducr = Some("ducr"), actions = List.empty, latestDecId = None)

          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(expectedSubmission)))

          when(mockCustomsDeclareExportsConnector.findDeclaration(eqTo(declaration.id))(any(), any()))
            .thenReturn(Future.successful(Some(declaration)))

          val result = controller.cancelAmendment(declaration.id)(getRequestWithSession((submissionUuid, "Id")))
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "SubmissionController.submitAmendment" when {
    val declaration = aDeclaration()
    val body =
      Json.obj(nameKey -> "Test Tester", jobRoleKey -> "Tester", emailKey -> "test@tester.com", reasonKey -> "amendReason", confirmationKey -> "true")

    "feature flag is off" should {
      "Redirect to root controller" in {
        when(mockDeclarationAmendmentsConfig.isDisabled).thenReturn(true)
        withNewCaching(declaration)

        val result = controller.submitAmendment("Submission")(postRequest(body))
        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "feature flag is on" should {

      "update the statementDescription field in the cache model" in {
        withNewCaching(declaration)
        when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("actionId")))

        val sessionData = List(declarationUuid -> declaration.id, submissionUuid -> "submissionUuid")
        await(controller.submitAmendment("Submission")(postRequestWithSession(body, sessionData)))

        theCacheModelUpdated mustBe declaration.copy(statementDescription = Some("amendReason"))
      }

      "redirect the user if submission is successful" in {
        val actionId = Some("actionId")
        val sessionData = List(declarationUuid -> declaration.id, submissionUuid -> "submissionUuid")
        withNewCaching(declaration)

        List(Cancellation, Resubmission, SubmissionAmendment).foreach { amendmentAction =>
          when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), eqTo(amendmentAction))(any(), any()))
            .thenReturn(Future.successful(actionId))

          val result = controller.submitAmendment(amendmentAction.toString)(postRequestWithSession(body, sessionData))
          status(result) must be(SEE_OTHER)

          redirectLocation(result) mustBe Some(AmendmentOutcomeController.displayHoldingPage(amendmentAction == Cancellation).url)
          result.futureValue.session.get(SessionHelper.submissionActionId) mustBe actionId
        }
      }

      "display an error if the submission fails due to incorrect form" in {
        withNewCaching(declaration)
        val bodyWithoutField = Json.obj(nameKey -> "Test Tester", jobRoleKey -> "Tester", emailKey -> "test@tester.com", confirmationKey -> "true")

        val result = controller.submitAmendment(SubmissionAmendment.toString)(postRequest(bodyWithoutField))
        status(result) must be(BAD_REQUEST)
      }
    }
  }

  "SubmissionController.submitDeclaration" should {

    "Redirect to confirmation" when {
      "the submission is accepted" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)

        when(mockSubmissionService.submitDeclaration(any(), any[ExportsDeclaration], any[LegalDeclaration])(any(), any()))
          .thenReturn(Future.successful(Some(expectedSubmission)))

        val body = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com", "confirmation" -> "true")
        val result = controller.submitDeclaration()(postRequest(body))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(ConfirmationController.displayHoldingPage.url))

        val actualSession = session(result)
        actualSession.get(declarationUuid) must be(None)
        actualSession.get(submissionDucr) must be(expectedSubmission.ducr)
        actualSession.get(submissionUuid) must be(Some(expectedSubmission.uuid))
        actualSession.get(submissionLrn) must be(Some(expectedSubmission.lrn))

        verify(mockSubmissionService).submitDeclaration(any(), any[ExportsDeclaration], any[LegalDeclaration])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      }
    }

    "Return 400 (Bad Request)" when {

      "missing legal declaration data" in {
        withNewCaching(aDeclaration())
        val body = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com")
        val result = controller.submitDeclaration()(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }

      "form is submitted with form errors" in {
        withNewCaching(aDeclaration())
        val result = controller.submitDeclaration()(postRequestWithSubmissionError)

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "lrn is not returned from submission service" in {
        withNewCaching(aDeclaration())
        when(mockSubmissionService.submitDeclaration(any(), any(), any())(any(), any())).thenReturn(Future.successful(None))

        val body = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com", "confirmation" -> "true")
        val result = controller.submitDeclaration()(postRequest(body))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
