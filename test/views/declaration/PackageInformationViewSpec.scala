/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.common.YesNoAnswer
import forms.declaration.{IsExs, PackageInformation}
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.package_information
import views.tags.ViewTest

@ViewTest
class PackageInformationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[package_information]
  private val packageInformation = PackageInformation("ID", Some("1A"), Some(10), Some("packs"))

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[YesNoAnswer] = YesNoAnswer.form(),
    packages: Seq[PackageInformation] = Seq(packageInformation)
  )(implicit request: JourneyRequest[_]): Document = page(mode, "itemId", form, packages)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.packageInformation.title")
    messages must haveTranslationFor("declaration.packageInformation.typesOfPackages.label")
    messages must haveTranslationFor("declaration.packageInformation.typesOfPackages.hint.1")
    messages must haveTranslationFor("declaration.packageInformation.numberOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.shippingMark")
    messages must haveTranslationFor("declaration.packageInformation.shippingMark.hint")
    messages must haveTranslationFor("declaration.packageInformation.table.heading")
    messages must haveTranslationFor("declaration.packageInformation.numberOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.table.multiple.heading")
  }

  "Package Information View back link" should {
    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, "itemId"))
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, "itemId"))
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      def viewHasBackLinkForExsStatus(exsStatus: String, call: Call) = {
        val requestWithCache =
          journeyRequest(aDeclarationAfter(request.cacheModel, withIsExs(IsExs(exsStatus))))
        val view = createView()(requestWithCache)
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(call)
      }

      "display back link when Is EXS is 'Yes'" in {
        viewHasBackLinkForExsStatus("Yes", controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, "itemId"))
      }

      "display back link when Is EXS is 'No'" in {
        viewHasBackLinkForExsStatus("No", controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, "itemId"))
      }
    }
  }

  "Package Information View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe messages("declaration.packageInformation.table.heading")
      }

      "display page title for multiple items" in {
        createView(packages = Seq.empty).getElementsByTag("h1").text() mustBe messages("declaration.packageInformation.table.multiple.heading", "0")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display'Save and continue' button on page" in {
        view.getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
      }
    }
  }

  "Package Information View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display one row with data in table" in {

        val view = createView(packages = Seq(PackageInformation("ID", Some("PA"), Some(100), Some("Shipping Mark"))))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)") must containMessageForElements("declaration.packageInformation.table.heading.typesOfPackages")
        view.select("table>thead>tr>th:nth-child(2)") must containMessageForElements("declaration.packageInformation.table.heading.numberOfPackages")
        view.select("table>thead>tr>th:nth-child(3)") must containMessageForElements("declaration.packageInformation.table.heading.shippingMarks")
        // remove button column
        view.select("table>thead>tr>th:nth-child(4)") must containMessageForElements("site.remove.header")

        // check row
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe "Packet (PA)"
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "100"
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").text() mustBe "Shipping Mark"
      }

      "display two rows with data in table" in {

        val view = createView(
          packages = Seq(
            PackageInformation("ID1", Some("PA"), Some(100), Some("Shipping Mark")),
            PackageInformation("ID2", Some("PB"), Some(101), Some("Shipping Mark"))
          )
        )

        // check table header
        view.select("table>thead>tr>th:nth-child(1)") must containMessageForElements("declaration.packageInformation.table.heading.typesOfPackages")
        view.select("table>thead>tr>th:nth-child(2)") must containMessageForElements("declaration.packageInformation.table.heading.numberOfPackages")
        view.select("table>thead>tr>th:nth-child(3)") must containMessageForElements("declaration.packageInformation.table.heading.shippingMarks")
        // remove button column
        view.select("table>thead>tr>th:nth-child(4)") must containMessageForElements("site.remove.header")

        // check rows
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe "Packet (PA)"
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "100"
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").text() mustBe "Shipping Mark"

        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(1)").text() mustBe "Pallet, box Combined open-ended box and pallet (PB)"
        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(2)").text() mustBe "101"
        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(3)").text() mustBe "Shipping Mark"
      }
    }
  }
}

object PackageInformationViewSpec {
  val packageInformation: PackageInformation = PackageInformation("ID", Some("1A"), Some(1), Some("Marks"))
}
