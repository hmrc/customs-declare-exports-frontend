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
  private def createView(form: Form[TotalNumberOfItems] = form): Html =
    total_number_of_items(appConfig, form)(fakeRequest, messages)

  "Total Number Of Items View" should {

    "have proper messages for labels" in {

      assertMessage(totalNumberOfItems, "1/9 What is the total number of items?")
      assertMessage(tnoiTitle, "Total number of items")
      assertMessage(tnoiHint, "Up to a maximum of 999 items")

      assertMessage(totalAmountInvoiced, "4/11 What is the total amount invoiced?")
      assertMessage(taiHint, "The total price of all the goods in the declaration e.g. 1234567.12")

      assertMessage(exchangeRate, "4/15 What was the exchange rate used?")
      assertMessage(
        erHint,
        "The rate of exchange fixed in advance by a contract between the parties e.g. 1234567.12345"
      )

      assertMessage(totalPackageQuantity, "6/18 Total number of packages")
      assertMessage(tpqHint, "Include all items in this shipment")
    }

    "have proper messages for error labels" in {

      assertMessage(tnoiEmpty, "Total number of items cannot be empty")
      assertMessage(tnoiError, "Total number of items is incorrect")

      assertMessage(taiEmpty, "Total amount of invoiced items cannot be empty")
      assertMessage(taiError, "Total amount of invoiced items is incorrect")

      assertMessage(erEmpty, "Exchange rate cannot be empty")
      assertMessage(erError, "Exchange rate is incorrect")

      assertMessage(tpqEmpty, "Total number of packages cannot be empty")
      assertMessage(tpqError, "Total number of packages is incorrect")
    }
  }

  "Total Number Of Items View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(tnoiTitle))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(totalNumberOfItems))
    }

    "display empty input with label for Total Number of Items" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(
        messages(totalNumberOfItems)
      )
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(messages(tnoiHint))
      getElementById(view, "itemsQuantity").attr("value") must be("")
    }

    "display empty input with label for Total Amount Invoiced" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(
        messages(totalAmountInvoiced)
      )
      getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(taiHint))
      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
    }

    "display empty input with label for Exchange Rate" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(5)>label>span:nth-child(1)").text() must be(messages(exchangeRate))
      getElementByCss(view, "form>div:nth-child(5)>label>span.form-hint").text() must be(messages(erHint))
      getElementById(view, "exchangeRate").attr("value") must be("")
    }

    "display empty input with label for Total Package" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(6)>label>span:nth-child(1)").text() must be(
        messages(totalPackageQuantity)
      )
      getElementByCss(view, "form>div:nth-child(6)>label>span.form-hint").text() must be(messages(tpqHint))
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Transport Information\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/transport-information")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Total Number Of Items View for invalid input" should {

    "display errors when nothing is entered" in {

      val view = createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("", "", "", "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, tnoiEmpty, "#itemsQuantity")
      checkErrorLink(view, 2, taiEmpty, "#totalAmountInvoiced")
      checkErrorLink(view, 3, erEmpty, "#exchangeRate")
      checkErrorLink(view, 4, tpqEmpty, "#totalPackage")

      getElementByCss(view, "#error-message-itemsQuantity-input").text() must be(messages(tnoiEmpty))
      getElementByCss(view, "#error-message-totalAmountInvoiced-input").text() must be(messages(taiEmpty))
      getElementByCss(view, "#error-message-exchangeRate-input").text() must be(messages(erEmpty))
      getElementByCss(view, "#error-message-totalPackage-input").text() must be(messages(tpqEmpty))
    }

    "display error when all entered input is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("abcd", "abcd", "abcd", "abcd")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, tnoiError, "#itemsQuantity")
      checkErrorLink(view, 2, taiError, "#totalAmountInvoiced")
      checkErrorLink(view, 3, erError, "#exchangeRate")
      checkErrorLink(view, 4, tpqError, "#totalPackage")

      getElementByCss(view, "#error-message-itemsQuantity-input").text() must be(messages(tnoiError))
      getElementByCss(view, "#error-message-totalAmountInvoiced-input").text() must be(messages(taiError))
      getElementByCss(view, "#error-message-exchangeRate-input").text() must be(messages(erError))
      getElementByCss(view, "#error-message-totalPackage-input").text() must be(messages(tpqError))
    }

    "display error when Total Number of Items is empty" in {

      val view =
        createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("", "123.123", "123.12345", "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, tnoiEmpty, "#itemsQuantity")

      getElementByCss(view, "#error-message-itemsQuantity-input").text() must be(messages(tnoiEmpty))
    }

    "display error when Total Number of Items is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("abcd", "123.123", "123.12345", "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, tnoiError, "#itemsQuantity")

      getElementByCss(view, "#error-message-itemsQuantity-input").text() must be(messages(tnoiError))
    }

    "display error when Total Amount Invoiced is empty" in {

      val view = createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("100", "", "123.12345", "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, taiEmpty, "#totalAmountInvoiced")

      getElementByCss(view, "#error-message-totalAmountInvoiced-input").text() must be(messages(taiEmpty))
    }

    "display error when Total Amount Invoiced is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("100", "abcd", "123.12345", "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, taiError, "#totalAmountInvoiced")

      getElementByCss(view, "#error-message-totalAmountInvoiced-input").text() must be(messages(taiError))
    }

    "display error when Exchange Rate is empty" in {

      val view = createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("100", "123.12", "", "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, erEmpty, "#exchangeRate")

      getElementByCss(view, "#error-message-exchangeRate-input").text() must be(messages(erEmpty))
    }

    "display error when Exchange Rate is incorrect" in {

      val view = createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("100", "123.12", "abcd", "1")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, erError, "#exchangeRate")

      getElementByCss(view, "#error-message-exchangeRate-input").text() must be(messages(erError))
    }

    "display error when Total Package is empty" in {

      val view =
        createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("100", "123.12", "123.12345", "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, tpqEmpty, "#totalPackage")

      getElementByCss(view, "#error-message-totalPackage-input").text() must be(messages(tpqEmpty))
    }

    "display error when Total Package is incorrect" in {

      val view =
        createView(TotalNumberOfItems.form().fillAndValidate(TotalNumberOfItems("100", "123.12", "123.12345", "abcd")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, tpqError, "#totalPackage")

      getElementByCss(view, "#error-message-totalPackage-input").text() must be(messages(tpqError))
    }
  }

  "Total Number Of Items View when filled" should {

    "display data in Total Number of Items input" in {

      val view = createView(TotalNumberOfItems.form().fill(TotalNumberOfItems("1", "", "", "")))

      getElementById(view, "itemsQuantity").attr("value") must be("1")
      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
      getElementById(view, "exchangeRate").attr("value") must be("")
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display data in Total Amount Invoiced input" in {

      val view = createView(TotalNumberOfItems.form().fill(TotalNumberOfItems("", "123.123", "", "")))

      getElementById(view, "itemsQuantity").attr("value") must be("")
      getElementById(view, "totalAmountInvoiced").attr("value") must be("123.123")
      getElementById(view, "exchangeRate").attr("value") must be("")
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display data in Exchange Rate input" in {

      val view = createView(TotalNumberOfItems.form().fill(TotalNumberOfItems("", "", "123.12345", "")))

      getElementById(view, "itemsQuantity").attr("value") must be("")
      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
      getElementById(view, "exchangeRate").attr("value") must be("123.12345")
      getElementById(view, "totalPackage").attr("value") must be("")
    }

    "display data in Total Package input" in {

      val view = createView(TotalNumberOfItems.form().fill(TotalNumberOfItems("", "", "", "1")))

      getElementById(view, "itemsQuantity").attr("value") must be("")
      getElementById(view, "totalAmountInvoiced").attr("value") must be("")
      getElementById(view, "exchangeRate").attr("value") must be("")
      getElementById(view, "totalPackage").attr("value") must be("1")
    }

    "display data in all inputs" in {

      val view = createView(TotalNumberOfItems.form().fill(TotalNumberOfItems("1", "123.123", "123.12345", "1")))

      getElementById(view, "itemsQuantity").attr("value") must be("1")
      getElementById(view, "totalAmountInvoiced").attr("value") must be("123.123")
      getElementById(view, "exchangeRate").attr("value") must be("123.12345")
      getElementById(view, "totalPackage").attr("value") must be("1")
    }
  }
}
