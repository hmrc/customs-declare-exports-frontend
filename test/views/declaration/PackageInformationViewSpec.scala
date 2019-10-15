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

  val page = new package_information(mainTemplate)
  val packageInformation = PackageInformation("typesOfPackages", 10, "packs")
  private val form: Form[PackageInformation] = PackageInformation.form()

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[PackageInformation] = form,
    packages: Seq[PackageInformation] = Seq(packageInformation)
  ): Document = page(mode, "itemId", form, packages)(journeyRequest(), stubMessages())

  "have proper messages for labels" in {
    val messages = instanceOf[MessagesApi].preferred(journeyRequest())
    messages must haveTranslationFor("supplementary.packageInformation.title")
    messages must haveTranslationFor("supplementary.items")
    messages must haveTranslationFor("supplementary.packageInformation.typesOfPackages")
    messages must haveTranslationFor("supplementary.packageInformation.numberOfPackages")
    messages must haveTranslationFor("supplementary.packageInformation.shippingMarks")
    messages must haveTranslationFor("supplementary.packageInformation.shippingMarks.hint")
    messages must haveTranslationFor("supplementary.packageInformation.table.heading")
    messages must haveTranslationFor("supplementary.packageInformation.numberOfPackages")
    messages must haveTranslationFor("supplementary.packageInformation.table.multiple.heading")
  }

  "Package Information View on empty page" should {
    val view = createView()

    "display back link" in {
      view must containElementWithID("link-back")
      view.getElementById("link-back") must haveHref(controllers.declaration.routes.ItemTypeController.displayPage(Mode.Normal, "itemId"))
    }

    "display page title" in {
      view.getElementById("title").text() mustBe "supplementary.packageInformation.title"
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("supplementary.items")
    }

    "display empty input with label for Types of Packages" in {
      view.getElementById("typesOfPackages-label").text() mustBe "supplementary.packageInformation.typesOfPackages"
      view.getElementById("typesOfPackages").attr("value") mustBe empty
    }

    "display empty input with label for Number of Packages" in {
      view.getElementById("numberOfPackages-label").text() mustBe "supplementary.packageInformation.numberOfPackages"
      view.getElementById("numberOfPackages").attr("value") mustBe empty
    }

    "display empty input with label for Shipping Marks" in {
      view.getElementById("shippingMarks-label").text() mustBe "supplementary.packageInformation.shippingMarks"
      view.getElementById("shippingMarks-hint").text() mustBe "supplementary.packageInformation.shippingMarks.hint"
      view.getElementById("shippingMarks").attr("value") mustBe empty
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      view.getElementById("add").text() mustBe "site.add supplementary.packageInformation.add.hint"
      view.getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }
  }

  "Package Information View when filled" should {

    "display data in Types of Packages input" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("PA", 0, "")))

      view.getElementById("typesOfPackages").attr("value") mustBe "PA"
      view.getElementById("numberOfPackages").attr("value") mustBe "0"
      view.getElementById("shippingMarks").attr("value") mustBe empty
    }

    "display data in Number of Packages input" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("", 100, "")))

      view.getElementById("typesOfPackages").attr("value") mustBe empty
      view.getElementById("numberOfPackages").attr("value") mustBe "100"
      view.getElementById("shippingMarks").attr("value") mustBe empty
    }

    "display data in Shipping Marks" in {

      val view = createView(form = PackageInformation.form().fill(PackageInformation("", 0, "Test")))

      view.getElementById("typesOfPackages").attr("value") mustBe empty
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
      // remove button column
      view.select("form>table>thead>tr>td").text() must be("")

      // check row
      view.select("table>tbody>tr>td:nth-child(1)").text() mustBe "PA"
      view.select("table>tbody>tr>td:nth-child(2)").text() mustBe "100"
      view.select("table>tbody>tr>td:nth-child(3)").text() mustBe "Shipping Mark"
    }

    "display two rows with data in table" in {

      val view = createView(packages = Seq(PackageInformation("PA", 100, "Shipping Mark"), PackageInformation("PB", 101, "Shipping Mark")))

      // check table header
      view.select("table>caption").text() mustBe "supplementary.packageInformation.table.multiple.heading"
      view.select("table>thead>tr>th:nth-child(1)").text() mustBe "supplementary.packageInformation.typesOfPackages"
      view.select("table>thead>tr>th:nth-child(2)").text() mustBe "supplementary.packageInformation.numberOfPackages"
      view.select("table>thead>tr>th:nth-child(3)").text() mustBe "supplementary.packageInformation.shippingMarks"
      // remove button column
      view.select("form>table>thead>tr>td").text() must be("")

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
