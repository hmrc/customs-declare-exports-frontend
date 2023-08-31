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

import base.ControllerWithoutFormSpec
import controllers.declaration.amendments.routes.AmendmentOutcomeController
import controllers.declaration.routes.ConfirmationController
import controllers.routes.RootController
import forms.declaration.AmendmentSubmission.reasonKey
import forms.declaration.LegalDeclaration
import forms.declaration.LegalDeclaration._
import mock.ErrorHandlerMocks
import models.ExportsDeclaration
import models.declaration.submissions.EnhancedStatus.RECEIVED
import models.declaration.submissions.Submission
import models.requests.SessionHelper
import models.requests.SessionHelper._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, session, status}
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.declaration.spec.UnitViewSpec
import views.html.declaration.amendments.amendment_submission
import views.html.declaration.summary.legal_declaration

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SubmissionControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with ScalaFutures with UnitViewSpec {

  private val amendmentSubmissionPage = mock[amendment_submission]
  private val legalDeclarationPage = mock[legal_declaration]
  private val mockSubmissionService = mock[SubmissionService]

  private val controller = new SubmissionController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    stubMessagesControllerComponents(),
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
    reset(amendmentSubmissionPage, legalDeclarationPage, mockDeclarationAmendmentsConfig, mockSubmissionService)
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

  "SubmissionController.displaySubmitDeclarationPage" when {

    "return 200 and invoke the expected page" in {
      withNewCaching(aDeclaration())

      val result = controller.displaySubmitDeclarationPage.apply(getJourneyRequest())
      status(result) mustBe OK

      verify(legalDeclarationPage).apply(any())(any(), any())
      verifyNoInteractions(amendmentSubmissionPage)
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
      withNewCaching(aDeclaration())

      val result = controller.displaySubmitAmendmentPage.apply(getJourneyRequest())
      status(result) mustBe OK

      verify(amendmentSubmissionPage).apply(any(), ArgumentMatchers.eq(false))(any(), any())
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
  }

  "SubmissionController.cancelAmendment" when {

    "feature flag is off" should {
      "Redirect to root controller" in {
        when(mockDeclarationAmendmentsConfig.isDisabled).thenReturn(true)
        withNewCaching(aDeclaration())

        val result = controller.displaySubmitAmendmentPage.apply(getJourneyRequest())
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

          when(mockCustomsDeclareExportsConnector.findOrCreateDraftForAmendment(any(), any())(any(), any()))
            .thenReturn(Future.successful("String"))

          val result = controller.cancelAmendment(getRequestWithSession((submissionUuid, "Id")))
          status(result) must be(SEE_OTHER)

          val url = routes.SubmissionController.displayCancelAmendmentPage.url
          redirectLocation(result) must be(Some(url))
        }
      }

      "return 500 (INTERNAL_SERVER_ERROR)" when {

        "no submissionUuid is found in session" in {
          val result = controller.cancelAmendment(getJourneyRequest())
          status(result) mustBe INTERNAL_SERVER_ERROR
        }

        "Backend fails to find/return a matching submission" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(None))

          val result = controller.cancelAmendment(getRequestWithSession((submissionUuid, "Id")))
          status(result) mustBe INTERNAL_SERVER_ERROR
        }

        "latestDecId does not belong to the appropriate submission" in {
          val uuid = UUID.randomUUID().toString
          val expectedSubmission = Submission(uuid, eori = "GB123456", lrn = "123LRN", ducr = Some("ducr"), actions = List.empty, latestDecId = None)

          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(expectedSubmission)))

          val result = controller.cancelAmendment(getRequestWithSession((submissionUuid, "Id")))
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

        val result = controller.submitAmendment(false)(postRequest(body))
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
        await(controller.submitAmendment(false)(postRequestWithSession(body, sessionData)))

        theCacheModelUpdated mustBe declaration.copy(statementDescription = Some("amendReason"))
      }

      "redirect the user if submission is successful" in {
        val actionId = Some("actionId")
        withNewCaching(declaration)
        when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(actionId))

        val sessionData = List(declarationUuid -> declaration.id, submissionUuid -> "submissionUuid")
        val result = controller.submitAmendment(false)(postRequestWithSession(body, sessionData))
        status(result) must be(SEE_OTHER)

        redirectLocation(result) mustBe Some(AmendmentOutcomeController.displayHoldingPage.url)
        result.futureValue.session.get(SessionHelper.submissionActionId) mustBe actionId
      }

      "display an error if the submission fails due to incorrect form" in {
        withNewCaching(declaration)
        val bodyWithoutField = Json.obj(nameKey -> "Test Tester", jobRoleKey -> "Tester", emailKey -> "test@tester.com", confirmationKey -> "true")

        val result = controller.submitAmendment(false)(postRequest(bodyWithoutField))
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
