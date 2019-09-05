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

package views.declaration

import base.Injector
import forms.declaration.NatureOfTransaction
import models.Mode
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

  private val page = new nature_of_transaction(mainTemplate)
  private val form: Form[NatureOfTransaction] = NatureOfTransaction.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[NatureOfTransaction] = form): Document =
    page(mode, form)(journeyRequest(), stubMessages())

  "Nature Of Transaction View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.natureOfTransaction.title")
      messages must haveTranslationFor("declaration.natureOfTransaction.header")
      messages must haveTranslationFor("declaration.natureOfTransaction.purchase")
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
      view.getElementById("title").text() must be("declaration.natureOfTransaction.title")
    }

    "display section header" in {
      view.getElementById("section-header").text() must be("declaration.natureOfTransaction.header")
    }

    "display radio button with Purchase option" in {
      view.getElementById("Purchase-label").text() must be("declaration.natureOfTransaction.purchase")
    }
    "display radio button with Return option" in {
      view.getElementById("Return-label").text() must be("declaration.natureOfTransaction.return")
    }
    "display radio button with Donation option" in {
      view.getElementById("Donation-label").text() must be("declaration.natureOfTransaction.donation")
    }
    "display radio button with Processing option" in {
      view.getElementById("Processing-label").text() must be("declaration.natureOfTransaction.processing")
    }
    "display radio button with Processed option" in {
      view.getElementById("Processed-label").text() must be("declaration.natureOfTransaction.processed")
    }
    "display radio button with National Purposes option" in {
      view.getElementById("NationalPurposes-label").text() must be("declaration.natureOfTransaction.nationalPurposes")
    }
    "display radio button with Military option" in {
      view.getElementById("Military-label").text() must be("declaration.natureOfTransaction.military")
    }
    "display radio button with Construction option" in {
      view.getElementById("Construction-label").text() must be("declaration.natureOfTransaction.construction")
    }
    "display radio button with Other option" in {
      view.getElementById("Other-label").text() must be("declaration.natureOfTransaction.other")
    }

    "display 'Back' button that links to 'Total Number Of Items' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() must be("site.back")
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.TotalNumberOfItemsController.displayPage(Mode.Normal)
      )
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

  "Nature Of Transaction View for invalid input" should {

    "display error when nature of transaction is empty" in {
      val view = createView(form = NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("")))

      checkErrorsSummary(view)
      haveFieldErrorLink("natureType", "#natureType")

      view.getElementById("error-message-natureType-input").text() must be("declaration.natureOfTransaction.error")
    }

    "display error when nature of transaction is incorrect" in {
      val view = createView(form = NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("ABC")))

      checkErrorsSummary(view)
      haveFieldErrorLink("natureType", "#natureType")

      view.getElementById("error-message-natureType-input").text() must be("declaration.natureOfTransaction.error")
    }
  }
}
