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

import controllers.declaration.SummaryController
import models.declaration.SupplementaryDeclarationData
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.{ExportsDeclaration, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.summary.{summary_page, summary_page_no_data}

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  val mockSummaryPage = mock[summary_page]
  val mockSummaryPageNoData = mock[summary_page_no_data]
  val mockSubmissionService = mock[SubmissionService]

  val controller = new SummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    mockSubmissionService,
    stubMessagesControllerComponents(),
    mockSummaryPage,
    mockSummaryPageNoData
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSummaryPageNoData.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockSummaryPage, mockSummaryPageNoData, mockSubmissionService)
    super.afterEach()
  }

  def theResponseData: SupplementaryDeclarationData = {
    val captor = ArgumentCaptor.forClass(classOf[SupplementaryDeclarationData])
    verify(mockSummaryPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Display" should {

    "return 200 (OK)" when {

      "declaration contains mandatory data" in {

        withNewCaching(aDeclaration(withConsignmentReferences()))

        val result = controller.displayPage(Mode.NormalMode)(getRequest())

        status(result) mustBe OK
        verify(mockSummaryPage, times(1)).apply(any(), any())(any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())

        theResponseData.consignmentReferences.value.lrn mustBe LRN
      }

      "declaration doesn't contain mandatory data" in {

        withNewCaching(aDeclaration())

        val result = controller.displayPage(Mode.NormalMode)(getRequest())

        status(result) mustBe OK
        verify(mockSummaryPage, times(0)).apply(any(), any())(any(), any())
        verify(mockSummaryPageNoData, times(1)).apply()(any(), any())
      }
    }
  }

  "Submit" should {
    "return 500 (INTERNAL_SERVER_ERROR) during submission" when {
      "lrn is not returned from submission service" in {
        val declaration = aDeclaration()
        withNewCaching(aDeclaration())
        when(mockSubmissionService.submit(any())(any(), any(), any())).thenReturn(Future.successful(None))

        val result = controller.submitDeclaration()(postRequest(Json.toJson(declaration)))

        status(result) mustBe INTERNAL_SERVER_ERROR
        verify(mockSummaryPage, times(0)).apply(any(), any())(any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
      }
    }

    "Redirect to confirmation" when {
      "valid submission" in {
        val declaration = aDeclaration()
        withNewCaching(aDeclaration())
        when(
          mockSubmissionService.submit(any[ExportsDeclaration])(
            any[JourneyRequest[_]],
            any[HeaderCarrier],
            any[ExecutionContext]
          )
        ).thenReturn(Future.successful(Some("123LRN")))

        val result = controller.submitDeclaration()(postRequest(Json.toJson(declaration)))

        status(result) must be(SEE_OTHER)
        session(result).get(ExportsSessionKeys.declarationId) must be(None)
        redirectLocation(result) must be(Some(controllers.declaration.routes.ConfirmationController.displaySubmissionConfirmation().url))
        flash(result).get("LRN") must be(Some("123LRN"))
        verify(mockSubmissionService).submit(any[ExportsDeclaration])(any[JourneyRequest[_]], any[HeaderCarrier], any[ExecutionContext])
      }
    }
  }
}
