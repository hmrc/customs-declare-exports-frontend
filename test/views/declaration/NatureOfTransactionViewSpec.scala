/*
 * Copyright 2020 HM Revenue & Customs
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

import base.Injector
import forms.declaration.NatureOfTransaction
import forms.declaration.NatureOfTransaction._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.nature_of_transaction
import views.tags.ViewTest

@ViewTest
class NatureOfTransactionViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[nature_of_transaction]
  private val form: Form[NatureOfTransaction] = NatureOfTransaction.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[NatureOfTransaction] = form)(implicit request: JourneyRequest[_]): Document =
    page(mode, form)(request, stubMessages())

  "Nature Of Transaction View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "have proper messages for labels" in {
        val messages = instanceOf[MessagesApi].preferred(journeyRequest())
        messages must haveTranslationFor("declaration.natureOfTransaction.title")
        messages must haveTranslationFor("declaration.natureOfTransaction.header")
        messages must haveTranslationFor("declaration.natureOfTransaction.sale")
        messages must haveTranslationFor("declaration.natureOfTransaction.return")
        messages must haveTranslationFor("declaration.natureOfTransaction.donation")
        messages must haveTranslationFor("declaration.natureOfTransaction.processing")
        messages must haveTranslationFor("declaration.natureOfTransaction.processed")
        messages must haveTranslationFor("declaration.natureOfTransaction.nationalPurposes")
        messages must haveTranslationFor("declaration.natureOfTransaction.military")
        messages must haveTranslationFor("declaration.natureOfTransaction.construction")
        messages must haveTranslationFor("declaration.natureOfTransaction.other")
        messages must haveTranslationFor("declaration.natureOfTransaction.empty")
        messages must haveTranslationFor("declaration.natureOfTransaction.error")
      }

      val view = createView()
      "display page title" in {
        view.getElementsByTag("h1").text() must be("declaration.natureOfTransaction.title")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("declaration.natureOfTransaction.header")
      }

      "display radio button with Sale option" in {
        view.getElementById("Sale").attr("value") mustBe Sale
        view.getElementsByAttributeValue("for", "Sale").text() mustBe "declaration.natureOfTransaction.sale"
      }
      "display radio button with Return option" in {
        view.getElementById("Return").attr("value") mustBe Return
        view.getElementsByAttributeValue("for", "Return").text() mustBe "declaration.natureOfTransaction.return"
      }
      "display radio button with Donation option" in {
        view.getElementById("Donation").attr("value") mustBe Donation
        view.getElementsByAttributeValue("for", "Donation").text() mustBe "declaration.natureOfTransaction.donation"
      }
      "display radio button with Processing option" in {
        view.getElementById("Processing").attr("value") mustBe Processing
        view.getElementsByAttributeValue("for", "Processing").text() mustBe "declaration.natureOfTransaction.processing"
      }
      "display radio button with Processed option" in {
        view.getElementById("Processed").attr("value") mustBe Processed
        view.getElementsByAttributeValue("for", "Processed").text() mustBe "declaration.natureOfTransaction.processed"
      }
      "display radio button with National Purposes option" in {
        view.getElementById("NationalPurposes").attr("value") mustBe NationalPurposes
        view.getElementsByAttributeValue("for", "NationalPurposes").text() mustBe "declaration.natureOfTransaction.nationalPurposes"
      }
      "display radio button with Military option" in {
        view.getElementById("Military").attr("value") mustBe Military
        view.getElementsByAttributeValue("for", "Military").text() mustBe "declaration.natureOfTransaction.military"
      }
      "display radio button with Construction option" in {
        view.getElementById("Construction").attr("value") mustBe Construction
        view.getElementsByAttributeValue("for", "Construction").text() mustBe "declaration.natureOfTransaction.construction"
      }
      "display radio button with Other option" in {
        view.getElementById("Other").attr("value") mustBe Other
        view.getElementsByAttributeValue("for", "Other").text() mustBe "declaration.natureOfTransaction.other"
      }

      "display 'Back' button that links to 'Total Number Of Items' page" in {

        val backButton = view.getElementById("back-link")

        backButton.text() must be("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.TotalPackageQuantityController.displayPage(Mode.Normal))
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementById("submit")
        saveButton.text() must be("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text() must be("site.save_and_come_back_later")
      }
    }
  }

  "Nature Of Transaction View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when nature of transaction is empty" in {
        val view = createView(form = NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#natureType")

        view must containErrorElementWithMessage("declaration.natureOfTransaction.error")
      }

      "display error when nature of transaction is incorrect" in {
        val view = createView(form = NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("ABC")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#natureType")

        view must containErrorElementWithMessage("declaration.natureOfTransaction.error")
      }
    }
  }
}
