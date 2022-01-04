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

package controllers.declaration

import base.ControllerWithoutFormSpec
import forms.declaration.LegalDeclaration
import mock.ErrorHandlerMocks
import models.{ExportsDeclaration, Mode}
import models.declaration.submissions.Submission
import models.requests.ExportsSessionKeys
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.declaration.summary._

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with OptionValues {

  private val normalSummaryPage = mock[normal_summary_page]
  private val draftSummaryPage = mock[draft_summary_page]
  private val amendSummaryPage = mock[amend_summary_page]
  private val mockSummaryPageNoData = mock[summary_page_no_data]
  private val mockSubmissionService = mock[SubmissionService]

  private val controller = new SummaryController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    mockSubmissionService,
    stubMessagesControllerComponents(),
    normalSummaryPage,
    amendSummaryPage,
    draftSummaryPage,
    mockSummaryPageNoData
  )(ec, appConfig)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(normalSummaryPage.apply(any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(draftSummaryPage.apply()(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(amendSummaryPage.apply()(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSummaryPageNoData.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(normalSummaryPage, draftSummaryPage, amendSummaryPage, mockSummaryPageNoData, mockSubmissionService)
    super.afterEach()
  }

  "SummaryController.displayPage" should {

    "return 200 (OK)" when {

      "declaration contains mandatory data" in {

        withNewCaching(aDeclaration(withConsignmentReferences()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(normalSummaryPage, times(1)).apply(any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
      }

      "declaration doesn't contain mandatory data" in {

        withNewCaching(aDeclaration())

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(normalSummaryPage, times(0)).apply(any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(1)).apply()(any(), any())
      }
    }
  }

  "SummaryController.submitDeclaration" should {

    "Redirect to confirmation" when {
      "valid submission" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)

        val expectedSubmission: Submission = Submission(eori = "GB123456", lrn = "123LRN", ducr = Some("ducr"), actions = List.empty)
        when(mockSubmissionService.submit(any(), any[ExportsDeclaration], any[LegalDeclaration])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(expectedSubmission)))

        val formData = List(("fullName", "Test Tester"), ("jobRole", "Tester"), ("email", "test@tester.com"), ("confirmation", "true"))
        val result = controller.submitDeclaration(postRequestAsFormUrlEncoded(formData: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.ConfirmationController.displayHoldingPage.url))

        val actualSession = session(result)
        actualSession.get(ExportsSessionKeys.declarationId) must be(None)
        actualSession.get(ExportsSessionKeys.submissionDucr) must be(expectedSubmission.ducr)
        actualSession.get(ExportsSessionKeys.submissionId) must be(Some(expectedSubmission.uuid))
        actualSession.get(ExportsSessionKeys.submissionLrn) must be(Some(expectedSubmission.lrn))

        verify(mockSubmissionService).submit(any(), any[ExportsDeclaration], any[LegalDeclaration])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "Return 400 (Bad Request) during submission" when {
      "missing legal declaration data" in {
        withNewCaching(aDeclaration())
        val partialForm = List(("fullName", "Test Tester"), ("jobRole", "Tester"), ("email", "test@tester.com"))
        val result = controller.submitDeclaration(postRequestAsFormUrlEncoded(partialForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR) during submission" when {
      "lrn is not returned from submission service" in {
        withNewCaching(aDeclaration())
        when(mockSubmissionService.submit(any(), any(), any())(any(), any())).thenReturn(Future.successful(None))

        val correctForm = List(("fullName", "Test Tester"), ("jobRole", "Tester"), ("email", "test@tester.com"), ("confirmation", "true"))
        val result = controller.submitDeclaration(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe INTERNAL_SERVER_ERROR
        verify(normalSummaryPage, times(0)).apply(any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
      }
    }
  }
}
