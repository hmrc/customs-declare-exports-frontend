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
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.package_information
import views.tags.ViewTest

@ViewTest
class PackageInformationViewSpec extends ViewSpec with PackageInformationMessages with CommonMessages {

  private val form: Form[PackageInformation] = PackageInformation.form()
  private def createView(form: Form[PackageInformation] = form): Html =
    package_information(form, Seq())(fakeRequest, messages, appConfig)

  "Package Information View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Package Information")
      assertMessage(remove, "Remove Packaging Information")
      assertMessage(tableHeading, "1 Package added")
      assertMessage(tableMultipleHeading, "{0} Packages added")
      assertMessage(typesOfPackages, "6/9 How was this item packaged?")
      assertMessage(typesOfPackagesHint, "Enter the 2 digit code, for example PA for pallets")
      assertMessage(numberOfPackages, "6/10 Number of this package type")
      assertMessage(shippingMarks, "6/11 Shipping marks")
      assertMessage(shippingMarksHint, "Any mark or numbers on transport units and packages which can help us find it")
    }

    "have proper messages for error labels" in {

      assertMessage(typesOfPackagesEmpty, "Type of package can not be empty")
      assertMessage(typesOfPackagesError, "Type of package should be a 2 character code")
      assertMessage(numberOfPackagesEmpty, "Number of packages can not be empty")
      assertMessage(numberOfPackagesError, "Number of packages must be greater than 0 and less than 99999")
      assertMessage(shippingMarksEmpty, "Shipping marks can not be empty")
      assertMessage(shippingMarksError, "Shipping marks can only be up to 42 characters")
    }
  }

  "Package Information View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Items")
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Types of Packages" in {

      val view = createView()

      getElementById(view, "typesOfPackages-label").text() must be(messages(typesOfPackages))
      getElementById(view, "typesOfPackages-hint").text() must be(messages(typesOfPackagesHint))
      getElementById(view, "typesOfPackages").attr("value") must be("")
    }

    "display empty input with label for Number of Packages" in {

      val view = createView()

      getElementById(view, "numberOfPackages-label").text() must be(messages(numberOfPackages))
      getElementById(view, "numberOfPackages").attr("value") must be("")
    }

    "display empty input with label for Shipping Marks" in {

      val view = createView()

      getElementById(view, "shippingMarks-label").text() must be(messages(shippingMarks))
      getElementById(view, "shippingMarks-hint").text() must be(messages(shippingMarksHint))
      getElementById(view, "shippingMarks").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Item Type\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/item-type")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Package Information View when filled" should {

    "display data in Types of Packages input" in {

      val view = createView(PackageInformation.form().fill(PackageInformation(Some("PA"), None, None)))

      getElementById(view, "typesOfPackages").attr("value") must be("PA")
      getElementById(view, "numberOfPackages").attr("value") must be("")
      getElementById(view, "shippingMarks").attr("value") must be("")
    }

    "display data in Number of Packages input" in {

      val view = createView(PackageInformation.form().fill(PackageInformation(None, Some(100), None)))

      getElementById(view, "typesOfPackages").attr("value") must be("")
      getElementById(view, "numberOfPackages").attr("value") must be("100")
      getElementById(view, "shippingMarks").attr("value") must be("")
    }

    "display data in Shipping Marks" in {

      val view = createView(PackageInformation.form().fill(PackageInformation(None, None, Some("Test"))))

      getElementById(view, "typesOfPackages").attr("value") must be("")
      getElementById(view, "numberOfPackages").attr("value") must be("")
      getElementById(view, "shippingMarks").attr("value") must be("Test")
    }

    "display data in all inputs" in {

      val view = createView(PackageInformation.form().fill(PackageInformation(Some("PA"), Some(100), Some("Test"))))

      getElementById(view, "typesOfPackages").attr("value") must be("PA")
      getElementById(view, "numberOfPackages").attr("value") must be("100")
      getElementById(view, "shippingMarks").attr("value") must be("Test")
    }

    "display one row with data in table" in {

      val packages = Seq(PackageInformation(Some("PA"), Some(100), Some("Shipping Mark")))
      val view = package_information(form, packages)(fakeRequest, messages, appConfig)

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

      val packages = Seq(
        PackageInformation(Some("PA"), Some(100), Some("Shipping Mark")),
        PackageInformation(Some("PB"), Some(101), Some("Shipping Mark"))
      )
      val view = package_information(form, packages)(fakeRequest, messages, appConfig)

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
