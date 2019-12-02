/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.controllers.declaration

import config.AppConfig
import controllers.declaration.SummaryController
import forms.declaration.LegalDeclaration
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import models.{ExportsDeclaration, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.summary.{summary_page, summary_page_no_data}

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  private val mockSummaryPage = mock[summary_page]
  private val mockSummaryPageNoData = mock[summary_page_no_data]
  private val mockSubmissionService = mock[SubmissionService]
  private val appConfig = mock[AppConfig]

  private val controller = new SummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    mockSubmissionService,
    stubMessagesControllerComponents(),
    mockSummaryPage,
    mockSummaryPageNoData
  )(ec, appConfig)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockSummaryPage.apply(any(), any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSummaryPageNoData.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockSummaryPage, mockSummaryPageNoData, mockSubmissionService)
    super.afterEach()
  }

  "Display" should {

    "return 200 (OK)" when {

      "declaration contains mandatory data" in {

        withNewCaching(aDeclaration(withConsignmentReferences()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockSummaryPage, times(1)).apply(any(), any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
      }

      "declaration doesn't contain mandatory data" in {

        withNewCaching(aDeclaration())

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockSummaryPage, times(0)).apply(any(), any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(1)).apply()(any(), any())
      }
    }
  }

  "Submit" should {
    "return 500 (INTERNAL_SERVER_ERROR) during submission" when {
      "lrn is not returned from submission service" in {
        withNewCaching(aDeclaration())
        when(mockSubmissionService.submit(any(), any(), any())(any(), any())).thenReturn(Future.successful(None))

        val correctForm = Seq(("fullName", "Test Tester"), ("jobRole", "Tester"), ("email", "test@tester.com"), ("confirmation", "true"))
        val result = controller.submitDeclaration()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe INTERNAL_SERVER_ERROR
        verify(mockSummaryPage, times(0)).apply(any(), any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
      }
    }

    "Redirect to confirmation" when {
      "valid submission" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        when(
          mockSubmissionService
            .submit(any(), any[ExportsDeclaration], any[LegalDeclaration])(any[HeaderCarrier], any[ExecutionContext])
        ).thenReturn(Future.successful(Some("123LRN")))

        val correctForm = Seq(("fullName", "Test Tester"), ("jobRole", "Tester"), ("email", "test@tester.com"), ("confirmation", "true"))
        val result = controller.submitDeclaration()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
        session(result).get(ExportsSessionKeys.declarationId) must be(None)
        redirectLocation(result) must be(Some(controllers.declaration.routes.ConfirmationController.displaySubmissionConfirmation().url))

        flash(result).get(FlashKeys.lrn) must be(Some("123LRN"))
        flash(result).get(FlashKeys.decType) must be(Some(declaration.`type`.toString))

        verify(mockSubmissionService).submit(any(), any[ExportsDeclaration], any[LegalDeclaration])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "Return 400 (Bad Request) during submission" when {
      "missing legal declaration data" in {
        withNewCaching(aDeclaration())
        val partialForm = Seq(("fullName", "Test Tester"), ("jobRole", "Tester"), ("email", "test@tester.com"))
        val result = controller.submitDeclaration()(postRequestAsFormUrlEncoded(partialForm: _*))

        status(result) must be(BAD_REQUEST)

      }
    }
  }
}
