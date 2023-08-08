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
import config.AppConfig
import controllers.declaration.SummaryController.{continuePlaceholder, lrnDuplicateError}
import controllers.declaration.SummaryControllerSpec.{expectedHref, fakeSummaryPage}
import controllers.routes.SavedDeclarationsController
import forms.{Lrn, LrnValidator}
import handlers.ErrorHandler
import mock.ErrorHandlerMocks
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.FormError
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.helpers.ActionItemBuilder.lastUrlPlaceholder
import views.html.declaration.amendments.amendment_summary
import views.html.declaration.summary._
import views.html.error_template

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with OptionValues {

  private val amendmentSummaryPage = mock[amendment_summary]
  private val normalSummaryPage = mock[normal_summary_page]
  private val mockSummaryPageNoData = mock[summary_page_no_data]
  private val mockLrnValidator = mock[LrnValidator]
  private val mockSubmissionsService = mock[SubmissionService]

  private val normalModeBackLink = SavedDeclarationsController.displayDeclarations()

  private val mcc = stubMessagesControllerComponents()

  private val controller = new SummaryController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig]),
    mockSubmissionsService,
    mockExportsCacheService,
    mcc,
    amendmentSummaryPage,
    normalSummaryPage,
    mockSummaryPageNoData,
    mockLrnValidator
  )(ec, appConfig)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(amendmentSummaryPage.apply()(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(normalSummaryPage.apply(any(), any(), any())(any(), any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSummaryPageNoData.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockLrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(false))
  }

  override protected def afterEach(): Unit = {
    reset(amendmentSummaryPage, normalSummaryPage, mockSummaryPageNoData, mockLrnValidator)
    super.afterEach()
  }

  "SummaryController.displayOutcomePage" should {

    "return 200 (OK)" when {

      "declaration contains mandatory data" when {

        "ready for submission" in {
          val declaration = aDeclaration(withConsignmentReferences())
          withNewCaching(declaration.copy(declarationMeta = declaration.declarationMeta.copy(readyForSubmission = Some(true))))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(normalSummaryPage, times(1)).apply(eqTo(normalModeBackLink), any(), any())(any(), any(), any())
          verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
        }

        "saved declaration" when {

          "readyForSubmission exists" in {
            val declaration = aDeclaration(withConsignmentReferences())
            withNewCaching(declaration.copy(declarationMeta = declaration.declarationMeta.copy(readyForSubmission = Some(false))))

            val result = controller.displayPage(getRequest())

            status(result) mustBe OK
            verify(normalSummaryPage, times(1)).apply(eqTo(normalModeBackLink), any(), any())(any(), any(), any())
            verify(mockSummaryPageNoData, times(0)).apply()(any(), any())
          }

          "readyForSubmission does not exist" in {
            val declaration = aDeclaration(withConsignmentReferences())
            withNewCaching(declaration.copy(declarationMeta = declaration.declarationMeta.copy(readyForSubmission = None)))

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

      val declaration = aDeclaration(withConsignmentReferences())
      withNewCaching(declaration.copy(declarationMeta = declaration.declarationMeta.copy(readyForSubmission = Some(true))))

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
