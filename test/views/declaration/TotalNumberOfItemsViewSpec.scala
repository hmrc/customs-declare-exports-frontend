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
import forms.declaration.TotalNumberOfItems
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.total_number_of_items
import views.tags.ViewTest

@ViewTest
class TotalNumberOfItemsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[total_number_of_items]
  private val form: Form[TotalNumberOfItems] = TotalNumberOfItems.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[TotalNumberOfItems] = form, messages: Messages = stubMessages())(
    implicit request: JourneyRequest[_]
  ): Document =
    page(mode, form)(request, messages)

  "Total Number Of Items View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.invoice.details.title")
      messages must haveTranslationFor("declaration.invoice.details.header")
      messages must haveTranslationFor("declaration.totalAmountInvoiced")
      messages must haveTranslationFor("declaration.totalAmountInvoiced.hint")
      messages must haveTranslationFor("declaration.totalAmountInvoiced.error")
      messages must haveTranslationFor("declaration.exchangeRate")
      messages must haveTranslationFor("declaration.exchangeRate.error")
      messages must haveTranslationFor("declaration.exchangeRate.hint")
      messages must haveTranslationFor("error.summary.title")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display same page title as header" in {
        val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("declaration.invoice.details.header")
      }

      "display header" in {
        view.getElementById("title").text() must be("declaration.invoice.details.title")
      }

      "display empty input with label for Total Amount Invoiced" in {
        view.getElementsByAttributeValue("for", "totalAmountInvoiced").text() mustBe messages("declaration.totalAmountInvoiced")
        view.getElementById("totalAmountInvoiced-hint").text() must be("declaration.totalAmountInvoiced.hint")
        view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
      }

      "display empty input with label for Exchange Rate" in {
        view.getElementsByAttributeValue("for", "exchangeRate").text() mustBe messages("declaration.exchangeRate")
        view.getElementById("exchangeRate-hint").text() must be("declaration.exchangeRate.hint")
        view.getElementById("exchangeRate").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Transport Information' page" in {
        val backButton = view.getElementById("back-link")

        backButton.text() must be("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
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

  "Total Number Of Items View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when all entered input is incorrect" in {

        val view =
          createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("abcd"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#totalAmountInvoiced")
        view must containErrorElementWithTagAndHref("a", "#exchangeRate")

        view must containErrorElementWithMessage("declaration.totalAmountInvoiced.error")
        view must containErrorElementWithMessage("declaration.exchangeRate.error")
      }

      "display error when Total Amount Invoiced is incorrect" in {

        val view =
          createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("123.12345"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#totalAmountInvoiced")

        view must containErrorElementWithMessage("declaration.totalAmountInvoiced.error")
      }

      "display error when Exchange Rate is incorrect" in {

        val view =
          createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("abcd"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#exchangeRate")

        view must containErrorElementWithMessage("declaration.exchangeRate.error")
      }
    }

  }

  "Total Number Of Items View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in Total Amount Invoiced input" in {

        val view = createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), None)))

        view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
        view.getElementById("exchangeRate").attr("value") mustBe empty
      }

      "display data in Exchange Rate input" in {

        val view = createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(None, Some("123.12345"))))

        view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
        view.getElementById("exchangeRate").attr("value") must be("123.12345")
      }

      "display data in all inputs" in {

        val view =
          createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), Some("123.12345"))))

        view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
        view.getElementById("exchangeRate").attr("value") must be("123.12345")
      }
    }
  }
}
