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
import controllers.util.SaveAndReturn
import forms.declaration.FiscalInformation
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.fiscal_information
import views.tags.ViewTest

@ViewTest
class FiscalInformationViewSpec extends UnitViewSpec2 with ExportsTestData with Stubs with Injector {

  private val form: Form[FiscalInformation] = FiscalInformation.form()
  private val page = instanceOf[fiscal_information]
  private def createView(itemId: String = "itemId", form: Form[FiscalInformation] = form)(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form)(request, messages)

  "Fiscal Information View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.fiscalInformation.title")
      messages must haveTranslationFor("declaration.fiscalInformation.details.summary")
      messages must haveTranslationFor("declaration.fiscalInformation.details.item1")
      messages must haveTranslationFor("declaration.fiscalInformation.details.item2")
      messages must haveTranslationFor("declaration.fiscalInformation.details.item3")
      messages must haveTranslationFor("declaration.fiscalInformation.details.item4")
      messages must haveTranslationFor("declaration.fiscalInformation.details.item5")
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

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.fiscalInformation.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.fiscalInformation.header")
      }

      "display two radio buttons with description (not selected)" in {
        val view = createView(form = FiscalInformation.form().fill(FiscalInformation("")))

        view.getElementById("Yes") must not(beSelected)

        val optionOneLabel = view.getElementsByAttributeValue("for", "Yes")
        optionOneLabel must containMessageForElements("site.yes")

        view.getElementById("No") must not(beSelected)

        val optionTwoLabel = view.getElementsByAttributeValue("for", "No")
        optionTwoLabel must containMessageForElements("site.no")
      }

      "display 'Back' button that links to 'Warehouse' page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, "itemId")
        )
      }

      "display 'Save and continue' button" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button" in {
        val saveButton = view.getElementById("submit_and_return")
        saveButton must containMessage("site.save_and_come_back_later")
        saveButton.attr("name") must be(SaveAndReturn.toString)
      }
    }
  }

  "Fiscal Information View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error if nothing is selected" in {

        val view = createView(form = FiscalInformation.form().bind(Map[String, String]()))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#onwardSupplyRelief")

        view must containErrorElementWithMessage(messages("declaration.fiscalInformation.onwardSupplyRelief.empty"))
      }

      "display error if incorrect fiscal information is selected" in {

        val view = createView(form = FiscalInformation.form().fillAndValidate(FiscalInformation("Incorrect")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#onwardSupplyRelief")

        view must containErrorElementWithMessage(messages("declaration.fiscalInformation.onwardSupplyRelief.error"))
      }
    }
  }

  "Dispatch Border Transport View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display selected first radio button - Yes" in {

        val view = createView(form = FiscalInformation.form().fill(FiscalInformation("Yes")))

        view.getElementById("Yes") must beSelected
        view.getElementById("No") must not(beSelected)
      }

      "display selected second radio button - No" in {

        val view = createView(form = FiscalInformation.form().fill(FiscalInformation("No")))

        view.getElementById("Yes") must not(beSelected)
        view.getElementById("No") must beSelected
      }
    }
  }
}
