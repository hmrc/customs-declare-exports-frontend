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

package views.section1

import base.ExportsTestData._
import base.{Injector, TestHelper}
import controllers.section1.routes.{ConfirmDucrController, DucrEntryController}
import forms.declaration.Ducr.generateDucrPrefix
import forms.declaration.Lrn.form
import forms.declaration.Lrn
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.section1.local_reference_number
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class LocalReferenceNumberViewSpec extends PageWithButtonsSpec with Injector {

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))
  val incorrectDUCR = "7GB000000000000-1234512345123451234512345"
  val page = instanceOf[local_reference_number]

  def createView(implicit request: JourneyRequest[_]): Document =
    createView(Some(form))(request)

  def createView(maybeForm: Option[Form[Lrn]])(implicit request: JourneyRequest[_]): Document =
    page(maybeForm.getOrElse(form))(request, messages)

  def createView(form: Form[Lrn])(implicit request: JourneyRequest[_]): Document =
    createView(Some(form))(request)

  def viewOnInvalidInput(lrn: Lrn)(implicit request: JourneyRequest[_]): Document = {
    val frm = form.fillAndValidate(lrn)
    val view = createView(frm)
    view must haveGovukGlobalErrorSummary
    view
  }

  "Local Reference Number view" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.lrn.header")
      messages must haveTranslationFor("declaration.lrn.body")
      messages must haveTranslationFor("declaration.lrn.hint")
      messages must haveTranslationFor("declaration.lrn.inset")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.length")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.specialCharacter")
    }

    val view = createView

    "display 'Back' button that links to 'Ducr-Entry' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage(backToPreviousQuestionCaption)
      backButton must haveHref(DucrEntryController.displayPage.url)
    }

    "display 'Back' button that links to 'Confirm-Ducr' page" when {
      "the Ducr's prefix is auto-generated" in {
        val ducr = s"${generateDucrPrefix(withRequestOfType(STANDARD))}reference"
        val view = createView(withRequestOfType(STANDARD, withConsignmentReferences(ducr, lrn)))
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(ConfirmDucrController.displayPage.url)
      }
    }

    "display page title" in {
      val h1 = view.getElementById("title")
      h1.text mustBe messages("declaration.lrn.header")
      h1.getElementsByTag("label").first.attr("for") mustBe form("lrn").name
      view.getElementsByClass("govuk-label").size mustBe 1
    }

    "display section header" in {
      view.getElementById("section-header").text() must include(messages("declaration.section.1"))
    }

    "not display 'Exit and return' button" in {
      view.getElementsContainingText("site.exit_and_complete_later") mustBe empty
    }

    checkSaveAndContinueButtonIsDisplayed(createView)

    "not display empty input with label for MRN" in {
      view.getElementsByAttributeValue("for", "mrn") mustBe empty
    }

    "display empty input with hint" in {
      view.getElementById("lrn-hint").text() mustBe messages("declaration.lrn.hint")
      view.getElementById("lrn").attr("value") mustBe empty
    }

    "display inset text for LRN" in {
      view.getElementsByClass("govuk-inset-text").first().text() mustBe messages("declaration.lrn.inset")
    }

    "display error for empty LRN" in {
      val view = viewOnInvalidInput(Lrn(""))
      view must containErrorElementWithTagAndHref("a", "#lrn")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
    }

    "display error when LRN is longer then 22 characters" in {
      val view = viewOnInvalidInput(Lrn(TestHelper.createRandomAlphanumericString(23)))
      view must containErrorElementWithTagAndHref("a", "#lrn")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
    }

    "display error when LRN contains special character" in {
      val view = viewOnInvalidInput(Lrn("#@#$"))
      view must containErrorElementWithTagAndHref("a", "#lrn")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
    }

    "display error LRN empty" in {
      val view = viewOnInvalidInput(Lrn(""))
      view must containErrorElementWithTagAndHref("a", "#lrn")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
    }

    "display error LRN is longer then 22 characters" in {
      val view = viewOnInvalidInput(Lrn(TestHelper.createRandomAlphanumericString(23)))
      view must containErrorElementWithTagAndHref("a", "#lrn")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
    }

    "display error LRN contains special character" in {
      val view = viewOnInvalidInput(Lrn("$$%"))
      view must containErrorElementWithTagAndHref("a", "#lrn")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
    }

    "display data in LRN input" in {
      val frm = form.fill(Lrn(lrn))
      val view = createView(frm)
      view.getElementById("lrn").attr("value") mustBe lrn
    }
  }
}
