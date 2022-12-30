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
import controllers.declaration.SummaryController.{continuePlaceholder, lrnDuplicateError}
import controllers.declaration.SummaryControllerSpec.{expectedHref, fakeSummaryPage}
import controllers.routes.SavedDeclarationsController
import forms.declaration.LegalDeclaration
import forms.{Lrn, LrnValidator}
import mock.ErrorHandlerMocks
import models.declaration.submissions.Submission
import models.requests.ExportsSessionKeys
import models.ExportsDeclaration
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.FormError
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.helpers.ActionItemBuilder.lastUrlPlaceholder
import views.html.declaration.summary._

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with OptionValues {

  private val normalSummaryPage = mock[normal_summary_page]
  private val legalDeclarationPage = mock[legal_declaration_page]
  private val mockSummaryPageNoData = mock[summary_page_no_data]
  private val mockSubmissionService = mock[SubmissionService]
  private val mockLrnValidator = mock[LrnValidator]

  private val normalModeBackLink = SavedDeclarationsController.displayDeclarations()

  private val controller = new SummaryController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    mockSubmissionService,
    stubMessagesControllerComponents(),
    normalSummaryPage,
    mockSummaryPageNoData,
    legalDeclarationPage,
    mockLrnValidator
  )(ec, appConfig)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(normalSummaryPage.apply(any(), any(), any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(legalDeclarationPage.apply(any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSummaryPageNoData.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockLrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(false))
  }

  override protected def afterEach(): Unit = {
    reset(normalSummaryPage, legalDeclarationPage, mockSummaryPageNoData, mockSubmissionService)
    super.afterEach()
  }

  "SummaryController.displayPage" should {

    "return 200 (OK)" when {

      "declaration contains mandatory data" when {

        "ready for submission" in {
          withNewCaching(aDeclaration(withConsignmentReferences()).copy(readyForSubmission = Some(true)))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(normalSummaryPage, times(1)).apply(eqTo(normalModeBackLink), any(), any())(any(), any(), any())
          verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
        }

        "saved declaration" when {

          "readyForSubmission exists" in {
            withNewCaching(aDeclaration(withConsignmentReferences()).copy(readyForSubmission = Some(false)))

            val result = controller.displayPage(getRequest())

            status(result) mustBe OK
            verify(normalSummaryPage, times(1)).apply(eqTo(normalModeBackLink), any(), any())(any(), any(), any())
            verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
          }

          "readyForSubmission does not exist" in {
            withNewCaching(aDeclaration(withConsignmentReferences()).copy(readyForSubmission = None))

            val result = controller.displayPage(getRequest())

            status(result) mustBe OK
            verify(normalSummaryPage, times(1)).apply(eqTo(normalModeBackLink), any(), any())(any(), any(), any())
            verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
          }
        }
      }

      "declaration doesn't contain mandatory data" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verify(normalSummaryPage, times(0)).apply(any(), any(), any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(1)).apply()(any(), any())
      }
    }

    "pass an error to page if LRN is a duplicate" in {
      when(mockLrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))
      withNewCaching(aDeclaration(withConsignmentReferences()).copy(readyForSubmission = Some(true)))

      val captor = ArgumentCaptor.forClass(classOf[Seq[FormError]])

      await(controller.displayPage(getRequest()))

      verify(normalSummaryPage, times(1)).apply(any(), captor.capture(), any())(any(), any(), any())
      captor.getValue mustBe List(lrnDuplicateError)
    }

    "return a draft summary page with a 'Continue' button linking to the same page referenced by the last 'Change' link" when {
      "the declaration is not ready for submission yet" in {
        when(normalSummaryPage.apply(any(), any(), any())(any(), any(), any())).thenReturn(fakeSummaryPage)

        withNewCaching(aDeclaration(withConsignmentReferences()))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        val view = Jsoup.parse(contentAsString(result))
        view.getElementById(continuePlaceholder).attr("href") mustBe expectedHref
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

        val body = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com", "confirmation" -> "true")
        val result = controller.submitDeclaration()(postRequest(body))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.ConfirmationController.displayHoldingPage.url))

        val actualSession = session(result)
        actualSession.get(ExportsSessionKeys.declarationId) must be(None)
        actualSession.get(ExportsSessionKeys.declarationType) must be(Some(""))
        actualSession.get(ExportsSessionKeys.submissionDucr) must be(expectedSubmission.ducr)
        actualSession.get(ExportsSessionKeys.submissionId) must be(Some(expectedSubmission.uuid))
        actualSession.get(ExportsSessionKeys.submissionLrn) must be(Some(expectedSubmission.lrn))

        verify(mockSubmissionService).submit(any(), any[ExportsDeclaration], any[LegalDeclaration])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "Return 400 (Bad Request) during submission" when {

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

    "return 500 (INTERNAL_SERVER_ERROR) during submission" when {
      "lrn is not returned from submission service" in {
        withNewCaching(aDeclaration())
        when(mockSubmissionService.submit(any(), any(), any())(any(), any())).thenReturn(Future.successful(None))

        val body = Json.obj("fullName" -> "Test Tester", "jobRole" -> "Tester", "email" -> "test@tester.com", "confirmation" -> "true")
        val result = controller.submitDeclaration()(postRequest(body))

        status(result) mustBe INTERNAL_SERVER_ERROR
        verify(normalSummaryPage, times(0)).apply(any(), any(), any())(any(), any(), any())
        verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
      }
    }
  }
}

object SummaryControllerSpec {

  import controllers.declaration.SummaryController.continuePlaceholder

  val expectedHref = "/customs-declare-exports/declaration/consignment-references"

  val fakeSummaryPage = Html(s"""
       |<!DOCTYPE html>
       |<html lang="en">
       |<body>
       |  <div>
       |    <a href="/customs-declare-exports/declaration/declaration-choice?$lastUrlPlaceholder">Change</a>
       |    <a href="/customs-declare-exports/declaration/type?$lastUrlPlaceholder">Change</a>
       |    <a href="$expectedHref?$lastUrlPlaceholder">Change</a>
       |    <a href="$continuePlaceholder" id="$continuePlaceholder">Continue</a>
       |  </div>
       |</body>
       |</html>
       """.stripMargin)
}
