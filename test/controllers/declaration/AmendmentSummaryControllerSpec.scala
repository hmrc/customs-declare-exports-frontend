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
import config.featureFlags.DeclarationAmendmentsConfig
import forms.declaration.LegalDeclaration._
import mock.ErrorHandlerMocks
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.legal_declaration_page

import scala.concurrent.Future

class AmendmentSummaryControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with UnitViewSpec {

  private val legalDeclarationPage = mock[legal_declaration_page]
  private val mockSubmissionService = mock[SubmissionService]
  private val declarationAmendmentsConfig = mock[DeclarationAmendmentsConfig]

  private val controller = new AmendmentSummaryController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockExportsCacheService,
    mockSubmissionService,
    legalDeclarationPage,
    declarationAmendmentsConfig
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(legalDeclarationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSubmissionService.amend).thenReturn(Future.successful(None))
    when(declarationAmendmentsConfig.isEnabled).thenReturn(true)
  }

  override protected def afterEach(): Unit = {
    reset(legalDeclarationPage, mockSubmissionService)
    super.afterEach()
  }

  "AmendmentSummaryController.displayDeclarationPage" should {
    "return 200 and invoke page with correct amend parameter" in {
      val req = getJourneyRequest()
      withNewCaching(aDeclaration())
      val result = controller.displayDeclarationPage.apply(req)
      status(result) mustBe OK
      verify(legalDeclarationPage).apply(any(), ArgumentMatchers.eq(true))(any(), any())
    }

    "return 303 when amend feature flag is off" in {
      when(declarationAmendmentsConfig.isEnabled).thenReturn(false)
      withNewCaching(aDeclaration())
      val result = controller.displayDeclarationPage.apply(getJourneyRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)
    }
  }

  "AmendmentSummaryController.submit" should {
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
        withNewCaching(declaration)
        await(controller.submitAmendment()(postRequest(body)))
        theCacheModelUpdated mustBe declaration.copy(statementDescription = Some("amendReason"))
      }

      "invoke the submission service amend method" in {
        withNewCaching(declaration)
        await(controller.submitAmendment()(postRequest(body)))
        verify(mockSubmissionService).amend
      }

      "redirect the user if submission is successful" in {
        withNewCaching(declaration)
        val result = controller.submitAmendment()(postRequest(body))
        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(routes.AmendmentConfirmationController.displayHoldingPage.url)
      }

      "display an error if the submission fails due to incorrect form" in {
        val bodyWithoutField = Json.obj(nameKey -> "Test Tester", jobRoleKey -> "Tester", emailKey -> "test@tester.com", confirmationKey -> "true")

        withNewCaching(declaration)
        val result = controller.submitAmendment()(postRequest(bodyWithoutField))
        status(result) must be(BAD_REQUEST)
        verify(mockErrorHandler).displayErrorPage(any())
      }
    }

    "when feature flag is off" when {
      "Redirect to root controller" in {
        when(declarationAmendmentsConfig.isEnabled).thenReturn(false)
        withNewCaching(declaration)
        val result = controller.submitAmendment()(postRequest(body))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)
      }
    }
  }
}
