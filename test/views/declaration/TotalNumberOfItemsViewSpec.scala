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
  private val page = new total_number_of_items(mainTemplate)
  private val form: Form[TotalNumberOfItems] = TotalNumberOfItems.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[TotalNumberOfItems] = form, messages: Messages = stubMessages()): Document =
    page(mode, form)(journeyRequest(), messages)

  "Total Number Of Items View on empty page" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.items")
      messages must haveTranslationFor("supplementary.valueOfItems")
      messages must haveTranslationFor("supplementary.totalAmountInvoiced")
      messages must haveTranslationFor("supplementary.totalAmountInvoiced.hint")
      messages must haveTranslationFor("supplementary.totalAmountInvoiced.error")
      messages must haveTranslationFor("supplementary.exchangeRate")
      messages must haveTranslationFor("supplementary.exchangeRate.error")
      messages must haveTranslationFor("supplementary.exchangeRate.hint")
      messages must haveTranslationFor("supplementary.totalPackageQuantity")
      messages must haveTranslationFor("supplementary.totalPackageQuantity.empty")
      messages must haveTranslationFor("supplementary.totalPackageQuantity.error")
      messages must haveTranslationFor("error.summary.title")
    }

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("supplementary.items")
    }

    "display header" in {
      view.getElementById("title").text() must be("supplementary.valueOfItems")
    }

    "display empty input with label for Total Amount Invoiced" in {
      view.getElementById("totalAmountInvoiced-label").text() must be("supplementary.totalAmountInvoiced")
      view.getElementById("totalAmountInvoiced-hint").text() must be("supplementary.totalAmountInvoiced.hint")
      view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
    }

    "display empty input with label for Exchange Rate" in {
      view.getElementById("exchangeRate-label").text() must be("supplementary.exchangeRate")
      view.getElementById("exchangeRate-hint").text() must be("supplementary.exchangeRate.hint")
      view.getElementById("exchangeRate").attr("value") mustBe empty
    }

    "display empty input with label for Total Package" in {
      view.getElementById("totalPackage-label").text() must be("supplementary.totalPackageQuantity")
      view.getElementById("totalPackage").attr("value") mustBe empty
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

  "Total Number Of Items View for invalid input" should {

    "display errors when nothing is entered" in {

      val view =
        createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(None, None, Some(""))))

      checkErrorsSummary(view)
      haveFieldErrorLink("totalPackage", "#totalPackage")

      view.getElementById("error-message-totalPackage-input").text() must be("supplementary.totalPackageQuantity.empty")
    }

    "display error when all entered input is incorrect" in {

      val view =
        createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("abcd"), Some("abcd")))).outerHtml()

      checkErrorsSummary(view)
      haveFieldErrorLink("totalAmountInvoiced", "#totalAmountInvoiced")
      haveFieldErrorLink("exchangeRate", "#exchangeRate")
      haveFieldErrorLink("totalPackage", "#totalPackage")

      view.getElementById("error-message-totalAmountInvoiced-input").text() must be("supplementary.totalAmountInvoiced.error")
      view.getElementById("error-message-exchangeRate-input").text() must be("supplementary.exchangeRate.error")
      view.getElementById("error-message-totalPackage-input").text() must be("supplementary.totalPackageQuantity.error")
    }

    "display error when Total Amount Invoiced is incorrect" in {

      val view =
        createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("123.12345"), Some("1")))).outerHtml()

      checkErrorsSummary(view)
      haveFieldErrorLink("totalAmountInvoiced", "#totalAmountInvoiced")

      view.getElementById("error-message-totalAmountInvoiced-input").text() must be("supplementary.totalAmountInvoiced.error")
    }

    "display error when Exchange Rate is incorrect" in {

      val view =
        createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("abcd"), Some("1")))).outerHtml()

      checkErrorsSummary(view)
      haveFieldErrorLink("exchangeRate", "#exchangeRate")

      view.getElementById("error-message-exchangeRate-input").text() must be("supplementary.exchangeRate.error")
    }

    "display error when Total Package is empty" in {

      val view =
        createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("123.12345"), Some("")))).outerHtml()

      checkErrorsSummary(view)
      haveFieldErrorLink("totalPackage", "#totalPackage")

      view.getElementById("error-message-totalPackage-input").text() must be("supplementary.totalPackageQuantity.empty")
    }

    "display error when Total Package is incorrect" in {

      val view =
        createView(form = TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("123.12345"), Some("abcd")))).outerHtml()

      checkErrorsSummary(view)
      haveFieldErrorLink("totalPackage", "#totalPackage")

      view.getElementById("error-message-totalPackage-input").text() must be("supplementary.totalPackageQuantity.error")
    }
  }

  "Total Number Of Items View when filled" should {

    "display data in Total Amount Invoiced input" in {

      val view = createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), None, Some(""))))

      view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
      view.getElementById("exchangeRate").attr("value") mustBe empty
      view.getElementById("totalPackage").attr("value") mustBe empty
    }

    "display data in Exchange Rate input" in {

      val view = createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(None, Some("123.12345"), Some(""))))

      view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
      view.getElementById("exchangeRate").attr("value") must be("123.12345")
      view.getElementById("totalPackage").attr("value") mustBe empty
    }

    "display data in Total Package input" in {

      val view = createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(None, None, Some("1"))))

      view.getElementById("totalAmountInvoiced").attr("value") mustBe empty
      view.getElementById("exchangeRate").attr("value") mustBe empty
      view.getElementById("totalPackage").attr("value") must be("1")
    }

    "display data in all inputs" in {

      val view =
        createView(form = TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), Some("123.12345"), Some("1"))))

      view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
      view.getElementById("exchangeRate").attr("value") must be("123.12345")
      view.getElementById("totalPackage").attr("value") must be("1")
    }
  }
}
