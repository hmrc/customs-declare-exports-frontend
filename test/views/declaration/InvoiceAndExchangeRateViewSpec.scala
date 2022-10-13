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

import base.Injector
import controllers.declaration.routes.InvoiceAndExchangeRateChoiceController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.InvoiceAndExchangeRate
import forms.declaration.InvoiceAndExchangeRate.form
import forms.declaration.officeOfExit.OfficeOfExit
import models.DeclarationType.STANDARD
import models.Mode
import models.Mode.Normal
import models.declaration.Locations
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.invoice_and_exchange_rate
import views.tags.ViewTest

@ViewTest
class InvoiceAndExchangeRateViewSpec extends PageWithButtonsSpec with Injector {

  val validCurrencyCode = "GBP"

  val page = instanceOf[invoice_and_exchange_rate]

  override val typeAndViewInstance = (STANDARD, page(Normal, form)(_, _))

  def createView(frm: Form[InvoiceAndExchangeRate] = form, mode: Mode = Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, frm)(request, messages)

  "Total Number Of Items View on empty page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.invoice.details.title")
      messages must haveTranslationFor("declaration.invoice.details.body.1")
      messages must haveTranslationFor("declaration.invoice.details.body.2")
      messages must haveTranslationFor("declaration.totalAmountInvoiced")
      messages must haveTranslationFor("declaration.totalAmountInvoiced.hint")
      messages must haveTranslationFor("declaration.totalAmountInvoiced.error")
      messages must haveTranslationFor("declaration.exchangeRate")
      messages must haveTranslationFor("declaration.exchangeRate.error")
      messages must haveTranslationFor("declaration.exchangeRate.input.hint")
      messages must haveTranslationFor("error.summary.title")
      messages must haveTranslationFor("declaration.totalAmountInvoicedCurrency.error.empty")
      messages must haveTranslationFor("declaration.totalAmountInvoicedCurrency.exchangeRateMissing.error.invalid")
      messages must haveTranslationFor("declaration.totalAmountInvoicedCurrency.exchangeRatePresent.error.invalid")
      messages must haveTranslationFor("declaration.totalAmountInvoiced.amount.label")
      messages must haveTranslationFor("declaration.totalAmountInvoiced.currency.label")
      messages must haveTranslationFor("declaration.exchangeRate.paragraph1.text")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display same page title as header" in {
        val viewWithMessage = createView()
        viewWithMessage.title must include(viewWithMessage.getElementsByTag("h1").text)
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display header" in {
        view.getElementById("title") must containMessage("declaration.invoice.details.title")
      }

      "display empty input with label for Total Amount Invoiced" in {
        view.getElementsByAttributeValue("for", "totalAmountInvoiced") must containMessageForElements("declaration.totalAmountInvoiced")
        view.getElementById("totalAmountInvoiced-hint") must containMessage("declaration.totalAmountInvoiced.hint")
        view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
      }

      "display empty input with label for Currency code" in {
        view.getElementsByAttributeValue("for", "totalAmountInvoicedCurrency") must containMessageForElements(
          "declaration.totalAmountInvoiced.currency.label"
        )
        view.getElementById("totalAmountInvoicedCurrency").attr("value") mustBe empty
      }

      "display empty input with label for Exchange Rate" in {
        view.getElementsByAttributeValue("for", "exchangeRate") must containMessageForElements("declaration.exchangeRate.input.label")
        view.getElementById("exchangeRate-hint") must containMessage("declaration.exchangeRate.input.hint")
        view.getElementById("exchangeRate").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Office of Exit' page" in {
        val officeOfExitInsideUK = Locations(officeOfExit = Some(OfficeOfExit("id")))
        val requestWithOfficeOfExitInsideUK = journeyRequest(request.cacheModel.copy(locations = officeOfExitInsideUK))

        val backButton = createView()(requestWithOfficeOfExitInsideUK).getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(InvoiceAndExchangeRateChoiceController.displayPage(Normal))
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

    }
  }

  "Total Number Of Items View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when all entered input is incorrect" in {
        val view = createView(form.fillAndValidate(InvoiceAndExchangeRate("abcd", Some("dsfsd"), "kjf", Some("abcd"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#totalAmountInvoiced")
        view must containErrorElementWithTagAndHref("a", "#exchangeRate")

        view must containErrorElementWithMessageKey("declaration.totalAmountInvoiced.error")
        view must containErrorElementWithMessageKey("declaration.exchangeRate.error")
      }

      "display error when Total Amount Invoiced is incorrect" in {
        val invoiceAndExchangeRate = InvoiceAndExchangeRate("abcd", Some(validCurrencyCode), YesNoAnswers.yes, Some("123.12345"))
        val view = createView(form.fillAndValidate(invoiceAndExchangeRate))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#totalAmountInvoiced")

        view must containErrorElementWithMessageKey("declaration.totalAmountInvoiced.error")
      }

      "display error when Exchange Rate is incorrect" in {
        val invoiceAndExchangeRate = InvoiceAndExchangeRate("123.12", Some(validCurrencyCode), YesNoAnswers.yes, Some("abcd"))
        val view = createView(form.fillAndValidate(invoiceAndExchangeRate))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#exchangeRate")

        view must containErrorElementWithMessageKey("declaration.exchangeRate.error")
      }

      "display error when Currency Code is incorrect" in {
        val invoiceAndExchangeRate = Map("exchangeRate" -> "123.12345", "totalAmountInvoiced" -> "123.12", "totalAmountInvoicedCurrency" -> "US")
        val view = createView(form.bind(invoiceAndExchangeRate))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#totalAmountInvoicedCurrency")

        view must containErrorElementWithMessageKey("declaration.totalAmountInvoicedCurrency.exchangeRatePresent.error.invalid")
      }
    }
  }

  "Total Number Of Items View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in Total Amount Invoiced input" in {
        val invoiceAndExchangeRate = InvoiceAndExchangeRate("123.123", None, YesNoAnswers.no, None)
        val view = createView(form.fill(invoiceAndExchangeRate))

        view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
        view.getElementById("exchangeRate").attr("value") mustBe empty
        view.getElementById("totalAmountInvoicedCurrency").attr("value") mustBe empty
      }

      "display data in Exchange Rate input" in {
        val invoiceAndExchangeRate = InvoiceAndExchangeRate("", None, YesNoAnswers.no, Some("123.12345"))
        val view = createView(form.fill(invoiceAndExchangeRate))

        view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
        view.getElementById("exchangeRate").attr("value") must be("123.12345")
        view.getElementById("totalAmountInvoicedCurrency").attr("value") mustBe empty
      }

      "display data in Currency code input" in {
        val invoiceAndExchangeRate = InvoiceAndExchangeRate("", Some(validCurrencyCode), YesNoAnswers.yes, None)
        val view = createView(form.fill(invoiceAndExchangeRate))

        view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
        view.getElementById("exchangeRate").attr("value") mustBe empty
        view.getElementById("totalAmountInvoicedCurrency").attr("value") must be(validCurrencyCode)
      }

      "display data in all inputs" in {
        val invoiceAndExchangeRate = InvoiceAndExchangeRate("123.123", Some(validCurrencyCode), YesNoAnswers.yes, Some("123.12345"))
        val view = createView(form.fill(invoiceAndExchangeRate))

        view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
        view.getElementById("exchangeRate").attr("value") must be("123.12345")
        view.getElementById("totalAmountInvoicedCurrency").attr("value") must be(validCurrencyCode)
      }
    }
  }
}
