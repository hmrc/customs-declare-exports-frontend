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
import forms.declaration.LegalDeclaration
import mock.ErrorHandlerMocks
import models.ExportsDeclaration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.declaration.summary.legal_declaration_page

import scala.concurrent.Future

class AmendmentSummaryControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks {

  private val legalDeclarationPage = mock[legal_declaration_page]
  private val mockSubmissionService = mock[SubmissionService]

  private val controller = new AmendmentSummaryController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockExportsCacheService,
    mockSubmissionService,
    legalDeclarationPage
  )(ec, appConfig)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(legalDeclarationPage.apply(any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSubmissionService.amend(any[String], any[ExportsDeclaration], any[LegalDeclaration]))
      .thenReturn(Future.successful(None))
  }

  override protected def afterEach(): Unit = {
    reset(legalDeclarationPage, mockSubmissionService)
    super.afterEach()
  }

  "AmendmentSummaryController.displayDeclarationPage" should {
     "return 200" in {
       withNewCaching(aDeclaration())
       val result = controller.displayDeclarationPage.apply(getJourneyRequest())
       status(result) mustBe OK
     }
  }

  "AmendmentSummaryController.submit" should {

    val declaration = aDeclaration()
    val amendReason = "amendReason"
    val body = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com", amendReason -> amendReason, "confirmation" -> "true")

    "update the statementDescription field in the cache model" in {
      withNewCaching(declaration)
      await(controller.submitAmendment()(postRequest(body)))
      theCacheModelUpdated mustBe declaration.copy(statementDescription = Some(amendReason))
    }

    "invoke the submission service amend method" in {
      withNewCaching(declaration)
      await(controller.submitAmendment()(postRequest(body)))
      verify(mockSubmissionService).amend(any(), any[ExportsDeclaration], any[LegalDeclaration])
    }

    "redirect the user if submission is successful"in {
      withNewCaching(declaration)
      val result = controller.submitAmendment()(postRequest(body))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.AmendConfirmationController.displayHoldingPage.url)
    }

    "display an error if the submission fails" in {
      val bodyWithoutField = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com", "confirmation" -> "true")

      withNewCaching(declaration)
      val result = controller.submitAmendment()(postRequest(bodyWithoutField))
      status(result) must be(BAD_REQUEST)
      verify(mockErrorHandler).displayErrorPage(any())
    }
  }
}
