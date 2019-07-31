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

import forms.declaration.TotalNumberOfItems
import helpers.views.declaration.{CommonMessages, TotalNumberOfItemsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.total_number_of_items
import views.tags.ViewTest

@ViewTest
class TotalNumberOfItemsViewSpec extends ViewSpec with TotalNumberOfItemsMessages with CommonMessages {

  private val form: Form[TotalNumberOfItems] = TotalNumberOfItems.form()
  private val totalNumberOfItemsPage = app.injector.instanceOf[total_number_of_items]
  private def createView(form: Form[TotalNumberOfItems] = form): Html =
    totalNumberOfItemsPage(form)(fakeRequest, messages)

  "Total Number Of Items View" should {

    "have proper messages for labels" in {

      assertMessage(totalAmountInvoiced, "4/11 What was the total amount invoiced?")
      assertMessage(totalAmountInvoicedHint, "The total price of all the goods in the declaration e.g. 1234567.12")

      assertMessage(exchangeRate, "4/15 What was the exchange rate used?")
      assertMessage(
        exchangeRateHint,
        "The rate of exchange fixed in advance by a contract between the parties e.g. 1234567.12345"
      )

      assertMessage(totalPackageQuantity, "6/18 Total number of packages")
      assertMessage(totalPackageQuantityHint, "Include all items in this shipment")
    }

    "have proper messages for error labels" in {

      assertMessage(totalAmountInvoicedError, "Total amount of invoiced items is incorrect")

      assertMessage(exchangeRateError, "Exchange rate is incorrect")

      assertMessage(totalPackageQuantityEmpty, "Total number of packages cannot be empty")
      assertMessage(totalPackageQuantityError, "Total number of packages is incorrect")
    }
  }

  "Total Number Of Items View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(totalNoOfItemsTitle))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Items")
    }

    "display header" in {

      getElementById(createView(), "title").text() must be(messages(valueOfItems))
    }

    "display empty input with label for Total Amount Invoiced" in {

      val view = createView()

      getElementById(view, "totalAmountInvoiced-label").text() must be(messages(totalAmountInvoiced))
      getElementById(view, "totalAmountInvoiced-hint").text() must be(messages(totalAmountInvoicedHint))
      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
    }

    "display empty input with label for Exchange Rate" in {

      val view = createView()

      getElementById(view, "exchangeRate-label").text() must be(messages(exchangeRate))
      getElementById(view, "exchangeRate-hint").text() must be(messages(exchangeRateHint))
      getElementById(view, "exchangeRate").attr("value") must be("")
    }

    "display empty input with label for Total Package" in {

      val view = createView()

      getElementById(view, "totalPackage-label").text() must be(messages(totalPackageQuantity))
      getElementById(view, "totalPackage-hint").text() must be(messages(totalPackageQuantityHint))
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display 'Back' button that links to 'Transport Information' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/office-of-exit")
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Total Number Of Items View for invalid input" should {

    "display errors when nothing is entered" in {

      val view = createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(None, None, "")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityEmpty, "#totalPackage")

      getElementById(view, "error-message-totalPackage-input").text() must be(messages(totalPackageQuantityEmpty))
    }

    "display error when all entered input is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("abcd"), "abcd")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalAmountInvoiced-error", totalAmountInvoicedError, "#totalAmountInvoiced")
      checkErrorLink(view, "exchangeRate-error", exchangeRateError, "#exchangeRate")
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityError, "#totalPackage")

      getElementById(view, "error-message-totalAmountInvoiced-input").text() must be(messages(totalAmountInvoicedError))
      getElementById(view, "error-message-exchangeRate-input").text() must be(messages(exchangeRateError))
      getElementById(view, "error-message-totalPackage-input").text() must be(messages(totalPackageQuantityError))
    }

    "display error when Total Amount Invoiced is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("123.12345"), "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalAmountInvoiced-error", totalAmountInvoicedError, "#totalAmountInvoiced")

      getElementById(view, "error-message-totalAmountInvoiced-input").text() must be(messages(totalAmountInvoicedError))
    }

    "display error when Exchange Rate is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("abcd"), "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, "exchangeRate-error", exchangeRateError, "#exchangeRate")

      getElementById(view, "error-message-exchangeRate-input").text() must be(messages(exchangeRateError))
    }

    "display error when Total Package is empty" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("123.12345"), "")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityEmpty, "#totalPackage")

      getElementById(view, "error-message-totalPackage-input").text() must be(messages(totalPackageQuantityEmpty))
    }

    "display error when Total Package is incorrect" in {

      val view =
        createView(
          TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("123.12345"), "abcd"))
        )

      checkErrorsSummary(view)
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityError, "#totalPackage")

      getElementById(view, "error-message-totalPackage-input").text() must be(messages(totalPackageQuantityError))
    }
  }

  "Total Number Of Items View when filled" should {

    "display data in Total Amount Invoiced input" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), None, "")))

      getElementById(view, "totalAmountInvoiced").attr("value") must be("123.123")
      getElementById(view, "exchangeRate").attr("value") must be("")
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display data in Exchange Rate input" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(None, Some("123.12345"), "")))

      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
      getElementById(view, "exchangeRate").attr("value") must be("123.12345")
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display data in Total Package input" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(None, None, "1")))

      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
      getElementById(view, "exchangeRate").attr("value") must be("")
      getElementById(view, "totalPackage").attr("value") must be("1")
    }

    "display data in all inputs" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), Some("123.12345"), "1")))

      getElementById(view, "totalAmountInvoiced").attr("value") must be("123.123")
      getElementById(view, "exchangeRate").attr("value") must be("123.12345")
      getElementById(view, "totalPackage").attr("value") must be("1")
    }
  }
}
