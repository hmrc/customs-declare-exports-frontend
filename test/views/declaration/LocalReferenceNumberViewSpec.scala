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

package views.declaration

import base.ExportsTestData._
import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.declaration.routes.DeclarantDetailsController
import forms.Lrn
import forms.Lrn.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.local_reference_number
import views.tags.ViewTest

@ViewTest
class LocalReferenceNumberViewSpec extends PageWithButtonsSpec with Injector {

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))
  val incorrectDUCR = "7GB000000000000-1234512345123451234512345"
  val page = instanceOf[local_reference_number]

  def createView()(implicit request: JourneyRequest[_]): Document =
    createView(Some(form))(request)

  def createView(maybeForm: Option[Form[Lrn]])(implicit request: JourneyRequest[_]): Document =
    page(maybeForm.getOrElse(form))(request, messages)

  def viewOnInvalidInput(lrn: Lrn)(implicit request: JourneyRequest[_]): Document = {
    val frm = form.fillAndValidate(lrn)
    val view = createView(frm)
    view must haveGovukGlobalErrorSummary
    view
  }

  private def createView(form: Form[Lrn])(implicit request: JourneyRequest[_]): Document =
    createView(Some(form))(request)

  "Local Reference Number" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.consignmentReferences.header")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.info")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet2")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet3")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet4")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet5")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.inset.1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.invalid")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.ducr.hint1")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.inset")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.length")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.specialCharacter")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.lrn.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.hint1")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.hint2")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.error.invalid")
    }
  }

  "Lrn View" should {

    "display page title" in {
      createView().getElementById("title").text() mustBe messages("declaration.lrn.header")
    }

    "display section header" in {
      createView().getElementById("section-header").text() must include(messages("declaration.section.1"))
    }

    "not display 'Exit and return' button" in {
      createView.getElementsContainingText("site.exit_and_complete_later") mustBe empty
    }

    checkSaveAndContinueButtonIsDisplayed(createView)
  }

  "not display empty input with label for MRN" in {
    createView().getElementsByAttributeValue("for", "mrn") mustBe empty
  }

  "display empty input with label for LRN" in {
    createView().getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.lrn.body")
    createView().getElementById("lrn-hint").text() mustBe messages("declaration.lrn.hint")
    createView().getElementById("lrn").attr("value") mustBe empty
  }

  "display inset text for LRN" in {
    createView().getElementsByClass("govuk-inset-text").first().text() mustBe messages("declaration.lrn.inset")
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

  "Lrn View" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Declarant Details' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DeclarantDetailsController.displayPage().url)
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Declaration Type' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.AdditionalDeclarationTypeController.displayPage().url)
      }
    }
  }

}
