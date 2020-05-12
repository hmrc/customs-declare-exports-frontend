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
import forms.declaration.PackageInformation
import models.DeclarationType.{DeclarationType, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.{DeclarationType, Mode}
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

  private val page = instanceOf[package_information]
  private val packageInformation = PackageInformation(Some("typesOfPackages"), Some(10), Some("packs"))
  private val form: Form[PackageInformation] = PackageInformation.form(STANDARD)

  private def createView(
    mode: Mode = Mode.Normal,
    decType: DeclarationType = STANDARD,
    form: Form[PackageInformation] = form,
    packages: Seq[PackageInformation] = Seq(packageInformation)
  ): Document = page(mode, "itemId", form, packages)(journeyRequest(decType), stubMessages())

  "have proper messages for labels" in {
    val messages = instanceOf[MessagesApi].preferred(journeyRequest())
    messages must haveTranslationFor("declaration.packageInformation.title")
    messages must haveTranslationFor("supplementary.items")
    messages must haveTranslationFor("declaration.packageInformation.typesOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.numberOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.shippingMarks")
    messages must haveTranslationFor("declaration.packageInformation.shippingMarks.hint")
    messages must haveTranslationFor("declaration.packageInformation.table.heading")
    messages must haveTranslationFor("declaration.packageInformation.numberOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.table.multiple.heading")
  }

  "Package Information View back link" should {

    "display back link for Standard declaration" in {
      val view = createView(decType = STANDARD)
      view must containElementWithID("back-link")
      view.getElementById("back-link") must haveHref(controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, "itemId"))
    }

    "display back link for Supplementary declaration" in {
      val view = createView(decType = DeclarationType.SUPPLEMENTARY)
      view must containElementWithID("back-link")
      view.getElementById("back-link") must haveHref(controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, "itemId"))
    }

    "display back link for Simplified declaration" in {
      val view = createView(decType = DeclarationType.SIMPLIFIED)
      view must containElementWithID("back-link")
      view.getElementById("back-link") must haveHref(controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, "itemId"))
    }
  }

  "Package Information View on empty page" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text() mustBe "declaration.packageInformation.title"
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("supplementary.items")
    }

    "display empty input with label for Types of Packages" in {
      view.getElementById("typesOfPackages-label").text() mustBe "declaration.packageInformation.typesOfPackages"
      view.getElementById("typesOfPackages").attr("value") mustBe empty
    }

    "display empty input with label for Number of Packages" in {
      view.getElementsByAttributeValue("for", "numberOfPackages").first().text() mustBe "declaration.packageInformation.numberOfPackages"
      view.getElementById("numberOfPackages").attr("value") mustBe empty
    }

    "display empty input with label for Shipping Marks" in {
      view.getElementsByAttributeValue("for", "shippingMarks").first().text() mustBe "declaration.packageInformation.shippingMarks"
      view.getElementById("shippingMarks-hint").text() mustBe "declaration.packageInformation.shippingMarks.hint"
      view.getElementById("shippingMarks").attr("value") mustBe empty
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      view.getElementById("add") must containMessage("site.add")
      view.getElementById("add") must containMessage("declaration.packageInformation.add.hint")
      view.getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }
  }

  "Package Information View when filled" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "display data in Types of Packages input" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(Some("PA"), Some(0), Some(""))))

        view.getElementById("typesOfPackages").attr("value") mustBe "PA"
        view.getElementById("numberOfPackages").attr("value") mustBe "0"
        view.getElementById("shippingMarks").attr("value") mustBe empty
      }

      "display data in Number of Packages input" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(Some(""), Some(100), Some(""))))

        view.getElementById("typesOfPackages").attr("value") mustBe empty
        view.getElementById("numberOfPackages").attr("value") mustBe "100"
        view.getElementById("shippingMarks").attr("value") mustBe empty
      }

      "display data in Shipping Marks" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(Some(""), Some(0), Some("Test"))))

        view.getElementById("typesOfPackages").attr("value") mustBe empty
        view.getElementById("numberOfPackages").attr("value") mustBe "0"
        view.getElementById("shippingMarks").attr("value") mustBe "Test"
      }

      "display data in all inputs" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(Some("PA"), Some(100), Some("Test"))))

        view.getElementById("typesOfPackages").attr("value") mustBe "PA"
        view.getElementById("numberOfPackages").attr("value") mustBe "100"
        view.getElementById("shippingMarks").attr("value") mustBe "Test"
      }
    }

    onClearance { request =>
      "display data in Types of Packages input" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(Some("PA"), None, None)))

        view.getElementById("typesOfPackages").attr("value") mustBe "PA"
        view.getElementById("numberOfPackages").attr("value") mustBe empty
        view.getElementById("shippingMarks").attr("value") mustBe empty
      }

      "display data in Number of Packages input" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(None, Some(100), None)))

        view.getElementById("typesOfPackages").attr("value") mustBe empty
        view.getElementById("numberOfPackages").attr("value") mustBe "100"
        view.getElementById("shippingMarks").attr("value") mustBe empty
      }

      "display data in Shipping Marks" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(None, None, Some("Test"))))

        view.getElementById("typesOfPackages").attr("value") mustBe empty
        view.getElementById("numberOfPackages").attr("value") mustBe empty
        view.getElementById("shippingMarks").attr("value") mustBe "Test"
      }

      "display data in all inputs" in {

        val view = createView(form = PackageInformation.form(request.declarationType).fill(PackageInformation(Some("PA"), Some(100), Some("Test"))))

        view.getElementById("typesOfPackages").attr("value") mustBe "PA"
        view.getElementById("numberOfPackages").attr("value") mustBe "100"
        view.getElementById("shippingMarks").attr("value") mustBe "Test"
      }
    }

    "display one row with data in table" in {

      val view = createView(packages = Seq(PackageInformation(Some("PA"), Some(100), Some("Shipping Mark"))))

      // check table header
      view.select("table>caption").text() mustBe "declaration.packageInformation.table.heading"
      view.select("table>thead>tr>th:nth-child(1)").text() mustBe "declaration.packageInformation.typesOfPackages"
      view.select("table>thead>tr>th:nth-child(2)").text() mustBe "declaration.packageInformation.numberOfPackages"
      view.select("table>thead>tr>th:nth-child(3)").text() mustBe "declaration.packageInformation.shippingMarks"
      // remove button column
      view.select("form>table>thead>tr>td").text() must be("")

      // check row
      view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe "PA"
      view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "100"
      view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").text() mustBe "Shipping Mark"
    }

    "display two rows with data in table" in {

      val view = createView(
        packages =
          Seq(PackageInformation(Some("PA"), Some(100), Some("Shipping Mark")), PackageInformation(Some("PB"), Some(101), Some("Shipping Mark")))
      )

      // check table header
      view.select("table>caption").text() mustBe "declaration.packageInformation.table.multiple.heading"
      view.select("table>thead>tr>th:nth-child(1)").text() mustBe "declaration.packageInformation.typesOfPackages"
      view.select("table>thead>tr>th:nth-child(2)").text() mustBe "declaration.packageInformation.numberOfPackages"
      view.select("table>thead>tr>th:nth-child(3)").text() mustBe "declaration.packageInformation.shippingMarks"
      // remove button column
      view.select("form>table>thead>tr>td").text() must be("")

      // check rows
      view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe "PA"
      view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "100"
      view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").text() mustBe "Shipping Mark"

      view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(1)").text() mustBe "PB"
      view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(2)").text() mustBe "101"
      view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(3)").text() mustBe "Shipping Mark"
    }
  }
}
