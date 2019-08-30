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
import models.Mode
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
    totalNumberOfItemsPage(Mode.Normal, form)(fakeRequest, messages)

  "Total Number Of Items View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(totalNoOfItemsTitle))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Items")
    }

    "display header" in {

      createView().getElementById("title").text() must be(messages(valueOfItems))
    }

    "display empty input with label for Total Amount Invoiced" in {

      val view = createView()

      view.getElementById("totalAmountInvoiced-label").text() must be(messages(totalAmountInvoiced))
      view.getElementById("totalAmountInvoiced-hint").text() must be(messages(totalAmountInvoicedHint))
      view.getElementById("totalAmountInvoiced").attr("value") must be("")
    }

    "display empty input with label for Exchange Rate" in {

      val view = createView()

      view.getElementById("exchangeRate-label").text() must be(messages(exchangeRate))
      view.getElementById("exchangeRate-hint").text() must be(messages(exchangeRateHint))
      view.getElementById("exchangeRate").attr("value") must be("")
    }

    "display empty input with label for Total Package" in {

      val view = createView()

      view.getElementById("totalPackage-label").text() must be(messages(totalPackageQuantity))
      view.getElementById("totalPackage").attr("value") must be("")
    }

    "display 'Back' button that links to 'Transport Information' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/office-of-exit")
    }

    "display 'Save and continue' button on page" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = createView().getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Total Number Of Items View for invalid input" should {

    "display errors when nothing is entered" in {

      val view = createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(None, None, "")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityEmpty, "#totalPackage")

      view.getElementById("error-message-totalPackage-input").text() must be(messages(totalPackageQuantityEmpty))
    }

    "display error when all entered input is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("abcd"), "abcd")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalAmountInvoiced-error", totalAmountInvoicedError, "#totalAmountInvoiced")
      checkErrorLink(view, "exchangeRate-error", exchangeRateError, "#exchangeRate")
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityError, "#totalPackage")

      view.getElementById("error-message-totalAmountInvoiced-input").text() must be(messages(totalAmountInvoicedError))
      view.getElementById("error-message-exchangeRate-input").text() must be(messages(exchangeRateError))
      view.getElementById("error-message-totalPackage-input").text() must be(messages(totalPackageQuantityError))
    }

    "display error when Total Amount Invoiced is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("abcd"), Some("123.12345"), "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalAmountInvoiced-error", totalAmountInvoicedError, "#totalAmountInvoiced")

      view.getElementById("error-message-totalAmountInvoiced-input").text() must be(messages(totalAmountInvoicedError))
    }

    "display error when Exchange Rate is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("abcd"), "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, "exchangeRate-error", exchangeRateError, "#exchangeRate")

      view.getElementById("error-message-exchangeRate-input").text() must be(messages(exchangeRateError))
    }

    "display error when Total Package is empty" in {

      val view =
        createView(TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("123.12345"), "")))

      checkErrorsSummary(view)
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityEmpty, "#totalPackage")

      view.getElementById("error-message-totalPackage-input").text() must be(messages(totalPackageQuantityEmpty))
    }

    "display error when Total Package is incorrect" in {

      val view =
        createView(
          TotalNumberOfItems.form.fillAndValidate(TotalNumberOfItems(Some("123.12"), Some("123.12345"), "abcd"))
        )

      checkErrorsSummary(view)
      checkErrorLink(view, "totalPackage-error", totalPackageQuantityError, "#totalPackage")

      view.getElementById("error-message-totalPackage-input").text() must be(messages(totalPackageQuantityError))
    }
  }

  "Total Number Of Items View when filled" should {

    "display data in Total Amount Invoiced input" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), None, "")))

      view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
      view.getElementById("exchangeRate").attr("value") must be("")
      view.getElementById("totalPackage").attr("value") must be("")
    }

    "display data in Exchange Rate input" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(None, Some("123.12345"), "")))

      view.getElementById("totalAmountInvoiced").attr("value") must be("")
      view.getElementById("exchangeRate").attr("value") must be("123.12345")
      view.getElementById("totalPackage").attr("value") must be("")
    }

    "display data in Total Package input" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(None, None, "1")))

      view.getElementById("totalAmountInvoiced").attr("value") must be("")
      view.getElementById("exchangeRate").attr("value") must be("")
      view.getElementById("totalPackage").attr("value") must be("1")
    }

    "display data in all inputs" in {

      val view = createView(TotalNumberOfItems.form.fill(TotalNumberOfItems(Some("123.123"), Some("123.12345"), "1")))

      view.getElementById("totalAmountInvoiced").attr("value") must be("123.123")
      view.getElementById("exchangeRate").attr("value") must be("123.12345")
      view.getElementById("totalPackage").attr("value") must be("1")
    }
  }
}
