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
import forms.declaration.LegalDeclaration
import forms.declaration.LegalDeclaration._
import mock.ErrorHandlerMocks
import models.ExportsDeclaration
import models.declaration.submissions.Submission
import models.requests.SessionHelper
import models.requests.SessionHelper._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, session, status}
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.legal_declaration

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SubmissionControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with ScalaFutures with UnitViewSpec {

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
    legalDeclarationPage,
    mockDeclarationAmendmentsConfig
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(legalDeclarationPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(None))
    when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
  }

  override protected def afterEach(): Unit = {
    reset(legalDeclarationPage, mockSubmissionService)
    super.afterEach()
  }

  val uuid = UUID.randomUUID().toString
  val expectedSubmission =
    Submission(uuid, eori = "GB123456", lrn = "123LRN", ducr = Some("ducr"), actions = List.empty, latestDecId = Some(uuid))

  "SubmissionController.displayLegalDeclarationPage" when {

    "for an amendment" should {

      "return 200 and invoke page with the amend parameter to 'true'" in {
        withNewCaching(aDeclaration())
        val result = controller.displayLegalDeclarationPage(true, false).apply(getJourneyRequest())
        status(result) mustBe OK
        verify(legalDeclarationPage).apply(any(), ArgumentMatchers.eq(true), any())(any(), any())
      }

      "return 303 when amend feature flag is off" in {
        when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(false)
        withNewCaching(aDeclaration())
        val result = controller.displayLegalDeclarationPage(true, false).apply(getJourneyRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "for a submission" should {

      "return 200 and invoke page with with the amend parameter to 'false'" in {
        withNewCaching(aDeclaration())
        val result = controller.displayLegalDeclarationPage(false, false).apply(getJourneyRequest())
        status(result) mustBe OK
        verify(legalDeclarationPage).apply(any(), ArgumentMatchers.eq(false), any())(any(), any())
      }
    }
  }

  "SubmissionController.submitAmendment" should {
    val declaration = aDeclaration()
    val body =
      Json.obj(
        nameKey -> "Test Tester",
        jobRoleKey -> "Tester",
        emailKey -> "test@tester.com",
        amendReasonKey -> "amendReason",
        confirmationKey -> "true"
      )

    "when feature flag is on" when {

      "update the statementDescription field in the cache model" in {
        when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("actionId")))

        withNewCaching(declaration)
        val sessionData = List(declarationUuid -> declaration.id, submissionUuid -> "submissionUuid")
        await(controller.submitAmendment(false)(postRequestWithSession(body, sessionData)))
        theCacheModelUpdated mustBe declaration.copy(statementDescription = Some("amendReason"))
      }

      "redirect the user if submission is successful" in {
        val actionId = Some("actionId")
        when(mockSubmissionService.submitAmendment(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(actionId))

        withNewCaching(declaration)
        val sessionData = List(declarationUuid -> declaration.id, submissionUuid -> "submissionUuid")
        val result = controller.submitAmendment(false)(postRequestWithSession(body, sessionData))
        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(AmendmentOutcomeController.displayHoldingPage.url)
        result.futureValue.session.get(SessionHelper.submissionActionId) mustBe actionId
      }

      "display an error if the submission fails due to incorrect form" in {
        val bodyWithoutField = Json.obj(nameKey -> "Test Tester", jobRoleKey -> "Tester", emailKey -> "test@tester.com", confirmationKey -> "true")

        withNewCaching(declaration)
        val result = controller.submitAmendment(false)(postRequest(bodyWithoutField))
        status(result) must be(BAD_REQUEST)
      }
    }

    "when feature flag is off" when {
      "Redirect to root controller" in {
        when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(false)
        withNewCaching(declaration)
        val result = controller.submitAmendment(false)(postRequest(body))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "SubmissionController.submitDeclaration" should {

    "Redirect to confirmation" when {
      "the submission is accepted" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)

        when(
          mockSubmissionService.submitDeclaration(any(), any[ExportsDeclaration], any[LegalDeclaration])(any[HeaderCarrier], any[ExecutionContext])
        )
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

  "SubmissionController.cancelAmendment" should {

    "Redirect to Legal Declaration page" when {
      "Backend returns a Submission to send a latestDecId to findOrCreateDraftForAmendment" in {

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(expectedSubmission)))

        when(mockCustomsDeclareExportsConnector.findOrCreateDraftForAmendment(any(), any())(any(), any()))
          .thenReturn(Future.successful("String"))

        val result = controller.cancelAmendment()(getRequestWithSession(("submission.uuid", "Id")))
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.SubmissionController.displayLegalDeclarationPage(true, true).url))

      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "no submissionUuid is found in session" in {

        val result = controller.cancelAmendment()(getJourneyRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
