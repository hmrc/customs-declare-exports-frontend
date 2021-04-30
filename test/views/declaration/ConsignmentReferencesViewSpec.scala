/*
 * Copyright 2021 HM Revenue & Customs
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

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.ConsignmentReferences
import forms.{Ducr, Lrn}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val properDUCR = "7GB000000000000-12345"
  private val incorrectDUCR = "7GB000000000000-1234512345123451234512345"

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()
  private val consignmentReferencesPage = instanceOf[consignment_references]
  private def createView(form: Form[ConsignmentReferences] = form)(implicit request: JourneyRequest[_]): Document =
    consignmentReferencesPage(Mode.Normal, form)(request, messages)

  "Consignment References" should {

    "have correct message keys" in {

      messages must haveTranslationFor("declaration.consignmentReferences.header")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.info")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.length")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.specialCharacter")
    }
  }

  "Consignment References View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {

        createView().getElementById("title").text() mustBe messages("declaration.consignmentReferences.header")
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include(messages("declaration.section.1"))
      }

      "display empty input with label for DUCR" in {

        val view = createView()

        view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementById("ducr_ducr-hint").text() mustBe messages("declaration.consignmentReferences.ducr.hint")
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "display empty input with label for LRN" in {

        val view = createView()

        view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.lrn.info")
        view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.lrn.hint")
        view.getElementById("lrn").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Declaration Type' page" in {

        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.AdditionalDeclarationTypeController.displayPage().url
      }

      "display 'Save and continue' button on page" in {
        val view = createView()
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val view = createView()
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() mustBe messages(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }

  "Consignment References View for invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error for empty LRN" in {

        val view =
          createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), Lrn(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when LRN is longer then 22 characters" in {

        val view = createView(
          ConsignmentReferences
            .form()
            .fillAndValidate(ConsignmentReferences(Ducr(properDUCR), Lrn(TestHelper.createRandomAlphanumericString(23))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when LRN contains special character" in {

        val view =
          createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), Lrn("#@#$"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }

      "display error when DUCR is incorrect and LRN empty" in {

        val view =
          createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("error.ducr")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when DUCR is incorrect and LRN is longer then 22 characters" in {

        val view = createView(
          ConsignmentReferences
            .form()
            .fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn(TestHelper.createRandomAlphanumericString(23))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("error.ducr")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when DUCR is incorrect and LRN contains special character" in {

        val view =
          createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn("$$%"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("error.ducr")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }
    }
  }

  "Consignment References View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in DUCR input" in {

        val view =
          createView(ConsignmentReferences.form().fill(ConsignmentReferences(Ducr("9GB12345678901234-SHIP1234-1"), Lrn(""))))

        view.getElementById("ducr_ducr").attr("value") mustBe "9GB12345678901234-SHIP1234-1"
        view.getElementById("lrn").attr("value") mustBe empty
      }

      "display data in LRN input" in {

        val view = createView(ConsignmentReferences.form().fill(ConsignmentReferences(Ducr(""), Lrn("test1"))))

        view.getElementById("ducr_ducr").attr("value") mustBe empty
        view.getElementById("lrn").attr("value") mustBe "test1"
      }

      "display data in all inputs" in {

        val view =
          createView(ConsignmentReferences.form().fill(ConsignmentReferences(Ducr("GB/ABC4-ASIUDYFAHSDJF"), Lrn("test1"))))

        view.getElementById("ducr_ducr").attr("value") mustBe "GB/ABC4-ASIUDYFAHSDJF"
        view.getElementById("lrn").attr("value") mustBe "test1"
      }
    }
  }
}
