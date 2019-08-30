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
import forms.declaration.PackageInformation
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.package_information
import views.tags.ViewTest

@ViewTest
class PackageInformationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  val packageInformation = PackageInformation("typesOfPackages", 10, "packs")
  private val form: Form[PackageInformation] = PackageInformation.form()

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[PackageInformation] = form,
    packages: Seq[PackageInformation] = Seq(packageInformation)
  ): Document =
    new package_information(mainTemplate)(mode, "itemId", form, packages)(journeyRequest, stubMessages())

  "have proper messages for labels" in {
    val messages = instanceOf[MessagesApi].preferred(journeyRequest)
    messages("supplementary.packageInformation.title") mustBe "Package Information"
    messages("supplementary.items") mustBe "Items"
    messages("supplementary.packageInformation.typesOfPackages") mustBe "Package Type"
    messages("supplementary.packageInformation.numberOfPackages") mustBe "Number of this package type"
    messages("supplementary.packageInformation.shippingMarks") mustBe "Shipping marks"
    messages("supplementary.packageInformation.shippingMarks.hint") mustBe "Any mark or numbers on transport units and packages which can help us find it"
    messages("supplementary.packageInformation.table.heading") mustBe "1 Package added"
    messages("supplementary.packageInformation.numberOfPackages") mustBe "Number of this package type"
    messages("supplementary.packageInformation.table.multiple.heading") mustBe "{0} Packages added"
    messages("supplementary.packageInformation.remove") mustBe "Remove Packaging Information"
  }

  "Package Information View on empty page" should {
    "display back link" in {
      val document = createView()
      document must containElementWithID("link-back")
      document.getElementById("link-back") must haveHref(
        controllers.declaration.routes.ItemTypeController.displayPage(Mode.Normal, "itemId")
      )
    }

    "display page title" in {
      createView().getElementById("title").text() mustBe "supplementary.packageInformation.title"
    }

    "display section header" in {
      createView().getElementById("section-header").text() mustBe "supplementary.items"
    }

    "display header" in {
      createView().select("legend>h1").text() mustBe "supplementary.packageInformation.title"
    }

    "display empty input with label for Types of Packages" in {
      val view = createView()

      view.getElementById("typesOfPackages-label").text() mustBe "supplementary.packageInformation.typesOfPackages"
      view.getElementById("typesOfPackages").attr("value") mustBe ""
    }

    "display empty input with label for Number of Packages" in {
      val view = createView()

      view.getElementById("numberOfPackages-label").text() mustBe "supplementary.packageInformation.numberOfPackages"
      view.getElementById("numberOfPackages").attr("value") mustBe ""
    }

    "display empty input with label for Shipping Marks" in {

      val view = createView()

      view.getElementById("shippingMarks-label").text() mustBe "supplementary.packageInformation.shippingMarks"
      view.getElementById("shippingMarks-hint").text() mustBe "supplementary.packageInformation.shippingMarks.hint"
      view.getElementById("shippingMarks").attr("value") mustBe ""
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      view.getElementById("add").text() mustBe "site.add"
      view.getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      createView().getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }
  }

  "Package Information View when filled" should {

    "display data in Types of Packages input" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("PA", 0, "")))

      view.getElementById("typesOfPackages").attr("value") mustBe "PA"
      view.getElementById("numberOfPackages").attr("value") mustBe "0"
      view.getElementById("shippingMarks").attr("value") mustBe ""
    }

    "display data in Number of Packages input" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("", 100, "")))

      view.getElementById("typesOfPackages").attr("value") mustBe ""
      view.getElementById("numberOfPackages").attr("value") mustBe "100"
      view.getElementById("shippingMarks").attr("value") mustBe ""
    }

    "display data in Shipping Marks" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("", 0, "Test")))

      view.getElementById("typesOfPackages").attr("value") mustBe ""
      view.getElementById("numberOfPackages").attr("value") mustBe "0"
      view.getElementById("shippingMarks").attr("value") mustBe "Test"
    }

    "display data in all inputs" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("PA", 100, "Test")))

      view.getElementById("typesOfPackages").attr("value") mustBe "PA"
      view.getElementById("numberOfPackages").attr("value") mustBe "100"
      view.getElementById("shippingMarks").attr("value") mustBe "Test"
    }

    "display one row with data in table" in {

      val view = createView(packages = Seq(PackageInformation("PA", 100, "Shipping Mark")))

      // check table header
      view.select("table>caption").text() mustBe "supplementary.packageInformation.table.heading"
      view.select("table>thead>tr>th:nth-child(1)").text() mustBe "supplementary.packageInformation.typesOfPackages"
      view.select("table>thead>tr>th:nth-child(2)").text() mustBe "supplementary.packageInformation.numberOfPackages"
      view.select("table>thead>tr>th:nth-child(3)").text() mustBe "supplementary.packageInformation.shippingMarks"
      view.select("table>thead>tr>th:nth-child(4)").text() mustBe "supplementary.packageInformation.remove"

      // check row
      view.select("table>tbody>tr>td:nth-child(1)").text() mustBe "PA"
      view.select("table>tbody>tr>td:nth-child(2)").text() mustBe "100"
      view.select("table>tbody>tr>td:nth-child(3)").text() mustBe "Shipping Mark"
    }

    "display two rows with data in table" in {

      val view = createView(
        packages = Seq(PackageInformation("PA", 100, "Shipping Mark"), PackageInformation("PB", 101, "Shipping Mark"))
      )

      // check table header
      view.select("table>caption").text() mustBe "supplementary.packageInformation.table.multiple.heading"
      view.select("table>thead>tr>th:nth-child(1)").text() mustBe "supplementary.packageInformation.typesOfPackages"
      view.select("table>thead>tr>th:nth-child(2)").text() mustBe "supplementary.packageInformation.numberOfPackages"
      view.select("table>thead>tr>th:nth-child(3)").text() mustBe "supplementary.packageInformation.shippingMarks"
      view.select("table>thead>tr>th:nth-child(4)").text() mustBe "supplementary.packageInformation.remove"

      // check rows
      view.select("table>tbody>tr>td:nth-child(1)").text() must include("PA")
      view.select("table>tbody>tr>td:nth-child(2)").text() must include("100")
      view.select("table>tbody>tr>td:nth-child(3)").text() must include("Shipping Mark")

      view.select("table>tbody>tr:nth-child(2)>td:nth-child(1)").text() must include("PB")
      view.select("table>tbody>tr:nth-child(2)>td:nth-child(2)").text() must include("101")
      view.select("table>tbody>tr:nth-child(2)>td:nth-child(3)").text() must include("Shipping Mark")
    }
  }
}
