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

import forms.declaration.PackageInformation
import helpers.views.declaration.{CommonMessages, PackageInformationMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.package_information
import views.tags.ViewTest

@ViewTest
class PackageInformationViewSpec extends ViewSpec with PackageInformationMessages with CommonMessages {

  private val form: Form[PackageInformation] = PackageInformation.form()
  private val packageInformationPage = app.injector.instanceOf[package_information]
  private def createView(form: Form[PackageInformation] = form): Html =
    packageInformationPage(Mode.Normal, itemId, form, Seq())(fakeRequest, messages)

  "Package Information View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Items")
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Types of Packages" in {

      val view = createView()

      view.getElementById("typesOfPackages-label").text() must be(messages(typesOfPackages))
      view.getElementById("typesOfPackages").attr("value") must be("")
    }

    "display empty input with label for Number of Packages" in {

      val view = createView()

      view.getElementById("numberOfPackages-label").text() must be(messages(numberOfPackages))
      view.getElementById("numberOfPackages").attr("value") must be("")
    }

    "display empty input with label for Shipping Marks" in {

      val view = createView()

      view.getElementById("shippingMarks-label").text() must be(messages(shippingMarks))
      view.getElementById("shippingMarks-hint").text() must be(messages(shippingMarksHint))
      view.getElementById("shippingMarks").attr("value") must be("")
    }

    "display 'Back' button that links to 'Item Type' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/item-type")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() must be(messages(addCaption))

      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = createView().getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Package Information View when filled" should {

    "display data in Types of Packages input" in {

      val view = createView(PackageInformation.form().fill(PackageInformation("PA", 0, "")))

      view.getElementById("typesOfPackages").attr("value") must be("PA")
      view.getElementById("numberOfPackages").attr("value") must be("0")
      view.getElementById("shippingMarks").attr("value") must be("")
    }

    "display data in Number of Packages input" in {

      val view = createView(PackageInformation.form().fill(PackageInformation("", 100, "")))

      view.getElementById("typesOfPackages").attr("value") must be("")
      view.getElementById("numberOfPackages").attr("value") must be("100")
      view.getElementById("shippingMarks").attr("value") must be("")
    }

    "display data in Shipping Marks" in {

      val view = createView(PackageInformation.form().fill(PackageInformation("", 0, "Test")))

      view.getElementById("typesOfPackages").attr("value") must be("")
      view.getElementById("numberOfPackages").attr("value") must be("0")
      view.getElementById("shippingMarks").attr("value") must be("Test")
    }

    "display data in all inputs" in {

      val view = createView(PackageInformation.form().fill(PackageInformation("PA", 100, "Test")))

      view.getElementById("typesOfPackages").attr("value") must be("PA")
      view.getElementById("numberOfPackages").attr("value") must be("100")
      view.getElementById("shippingMarks").attr("value") must be("Test")
    }

    "display one row with data in table" in {

      val packages = Seq(PackageInformation("PA", 100, "Shipping Mark"))
      val view = packageInformationPage(Mode.Normal, "12345", form, packages)(fakeRequest, messages)

      // check table header
      getElementByCss(view, "table>caption").text() must be(messages(tableHeading))
      getElementByCss(view, "table>thead>tr>th:nth-child(1)").text() must be(messages(typesOfPackages))
      getElementByCss(view, "table>thead>tr>th:nth-child(2)").text() must be(messages(numberOfPackages))
      getElementByCss(view, "table>thead>tr>th:nth-child(3)").text() must be(messages(shippingMarks))
      getElementByCss(view, "table>thead>tr>th:nth-child(4)").text() must be(messages(remove))

      // check row
      getElementByCss(view, "table>tbody>tr>td:nth-child(1)").text() must be("PA")
      getElementByCss(view, "table>tbody>tr>td:nth-child(2)").text() must be("100")
      getElementByCss(view, "table>tbody>tr>td:nth-child(3)").text() must be("Shipping Mark")
    }

    "display two rows with data in table" in {

      val packages = Seq(PackageInformation("PA", 100, "Shipping Mark"), PackageInformation("PB", 101, "Shipping Mark"))
      val view = packageInformationPage(Mode.Normal, "12345", form, packages)(fakeRequest, messages)

      // check table header
      getElementByCss(view, "table>caption").text() must be("2 Packages added")
      getElementByCss(view, "table>thead>tr>th:nth-child(1)").text() must be(messages(typesOfPackages))
      getElementByCss(view, "table>thead>tr>th:nth-child(2)").text() must be(messages(numberOfPackages))
      getElementByCss(view, "table>thead>tr>th:nth-child(3)").text() must be(messages(shippingMarks))
      getElementByCss(view, "table>thead>tr>th:nth-child(4)").text() must be(messages(remove))

      // check rows
      getElementByCss(view, "table>tbody>tr>td:nth-child(1)").text() must be("PA")
      getElementByCss(view, "table>tbody>tr>td:nth-child(2)").text() must be("100")
      getElementByCss(view, "table>tbody>tr>td:nth-child(3)").text() must be("Shipping Mark")

      getElementByCss(view, "table>tbody>tr:nth-child(2)>td:nth-child(1)").text() must be("PB")
      getElementByCss(view, "table>tbody>tr:nth-child(2)>td:nth-child(2)").text() must be("101")
      getElementByCss(view, "table>tbody>tr:nth-child(2)>td:nth-child(3)").text() must be("Shipping Mark")
    }
  }
}
