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
import controllers.util.SaveAndReturn
import forms.declaration.FiscalInformation
import models.Mode
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.fiscal_information
import views.tags.ViewTest

@ViewTest
class FiscalInformationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val form: Form[FiscalInformation] = FiscalInformation.form()
  private val page = new fiscal_information(mainTemplate)
  private def createView(itemId: String = "itemId", form: Form[FiscalInformation] = form): Html =
    page(Mode.Normal, itemId, form)(request, stubMessages())

  "Fiscal Information View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.fiscalInformation.title")
      messages must haveTranslationFor("declaration.fiscalInformation.question")
      messages must haveTranslationFor("declaration.fiscalInformation.onwardSupplyRelief.error")
      messages must haveTranslationFor("declaration.fiscalInformation.header")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.title")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.numbers.header")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.country")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.country.empty")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.country.error")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.reference")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.reference.empty")
      messages must haveTranslationFor("declaration.additionalFiscalReferences.reference.error")
    }

    val view = createView()

    "display page title" in {
      view.getElementById("title").text() mustBe "declaration.fiscalInformation.title"
    }

    "display section header" in {
      view.getElementById("section-header").text() mustBe "declaration.fiscalInformation.header"
    }

    "display two radio buttons with description (not selected)" in {
      val view = createView(form = FiscalInformation.form().fill(FiscalInformation("")))

      val optionOne = view.getElementById("Yes")
      optionOne.attr("checked") mustBe empty

      val optionOneLabel = view.getElementById("Yes-label")
      optionOneLabel.text() mustBe "site.yes"

      val optionTwo = view.getElementById("No")
      optionTwo.attr("checked") mustBe empty

      val optionTwoLabel = view.getElementById("No-label")
      optionTwoLabel.text() mustBe "site.no"
    }

    "display 'Back' button that links to 'Warehouse' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, "itemId")
      )
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button" in {
      val saveButton = view.getElementById("submit_and_return")
      saveButton.text() mustBe "site.save_and_come_back_later"
      saveButton.attr("name") must be(SaveAndReturn.toString)
    }

  }

  "Fiscal Information View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(form = FiscalInformation.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      haveFieldErrorLink("onwardSupplyRelief", "#onwardSupplyRelief")

      view.select("#error-message-onwardSupplyRelief-input").text() mustBe "error.required"
    }

    "display error if incorrect fiscal information is selected" in {

      val view = createView(form = FiscalInformation.form().fillAndValidate(FiscalInformation("Incorrect")))

      checkErrorsSummary(view)
      haveFieldErrorLink("onwardSupplyRelief", "#onwardSupplyRelief")

      view
        .select("#error-message-onwardSupplyRelief-input")
        .text() mustBe "declaration.fiscalInformation.onwardSupplyRelief.error"
    }

  }

  "Dispatch Border Transport View when filled" should {

    "display selected first radio button - Yes" in {

      val view = createView(form = FiscalInformation.form().fill(FiscalInformation("Yes")))

      val optionOne = view.getElementById("Yes")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("No")
      optionTwo.attr("checked") mustBe empty
    }

    "display selected second radio button - No" in {

      val view = createView(form = FiscalInformation.form().fill(FiscalInformation("No")))

      val optionOne = view.getElementById("Yes")
      optionOne.attr("checked") mustBe empty

      val optionTwo = view.getElementById("No")
      optionTwo.attr("checked") must be("checked")
    }
  }
}
