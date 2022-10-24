/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.declaration.routes.{
  CommodityDetailsController,
  NactCodeSummaryController,
  StatisticalValueController,
  UNDangerousGoodsCodeController
}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import forms.declaration.{IsExs, PackageInformation}
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.packageInformation.package_information
import views.tags.ViewTest

@ViewTest
class PackageInformationViewSpec extends PageWithButtonsSpec with Injector {

  override val typeAndViewInstance =
    (STANDARD, page(itemId, form(), List(PackageInformationViewSpec.packageInformation))(_, _))

  val page = instanceOf[package_information]

  def createView(frm: Form[YesNoAnswer] = form(), packages: Seq[PackageInformation] = List(PackageInformationViewSpec.packageInformation))(
    implicit request: JourneyRequest[_]
  ): Document = page(itemId, frm, packages)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.packageInformation.title")
    messages must haveTranslationFor("declaration.packageInformation.typesOfPackages.label")
    messages must haveTranslationFor("declaration.packageInformation.typesOfPackages.hint.1")
    messages must haveTranslationFor("declaration.packageInformation.numberOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.shippingMark")
    messages must haveTranslationFor("declaration.packageInformation.shippingMark.paragraph")
    messages must haveTranslationFor("declaration.packageInformation.table.heading")
    messages must haveTranslationFor("declaration.packageInformation.numberOfPackages")
    messages must haveTranslationFor("declaration.packageInformation.table.multiple.heading")
  }

  "Package Information View back link" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(StatisticalValueController.displayPage(itemId))
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(NactCodeSummaryController.displayPage(itemId))
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      def viewHasBackLinkForExsStatus(exsStatus: String, call: Call): Assertion = {
        val requestWithCache = journeyRequest(aDeclarationAfter(request.cacheModel, withIsExs(IsExs(exsStatus))))
        val view = createView()(requestWithCache)
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(call)
      }

      "display back link when Is EXS is 'Yes'" in {
        viewHasBackLinkForExsStatus("Yes", UNDangerousGoodsCodeController.displayPage(itemId))
      }

      "display back link when Is EXS is 'No'" in {
        viewHasBackLinkForExsStatus("No", CommodityDetailsController.displayPage(itemId))
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

      checkAllSaveButtonsAreDisplayed(createView())
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
        // change & remove button column
        view.select("table>thead>tr>th:nth-child(4)") must containMessageForElements("site.change.header")
        view.select("table>thead>tr>th:nth-child(5)") must containMessageForElements("site.remove.header")

        // check row
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe "Packet (PA)"
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "100"
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").text() mustBe "Shipping Mark"
      }

      "display two rows with data in table" in {
        val view = createView(packages =
          Seq(
            PackageInformation("ID1", Some("PA"), Some(100), Some("Shipping Mark")),
            PackageInformation("ID2", Some("PB"), Some(101), Some("Shipping Mark"))
          )
        )

        // check table header
        view.select("table>thead>tr>th:nth-child(1)") must containMessageForElements("declaration.packageInformation.table.heading.typesOfPackages")
        view.select("table>thead>tr>th:nth-child(2)") must containMessageForElements("declaration.packageInformation.table.heading.numberOfPackages")
        view.select("table>thead>tr>th:nth-child(3)") must containMessageForElements("declaration.packageInformation.table.heading.shippingMarks")
        // change & remove button column
        view.select("table>thead>tr>th:nth-child(4)") must containMessageForElements("site.change.header")
        view.select("table>thead>tr>th:nth-child(5)") must containMessageForElements("site.remove.header")

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

  val packageInformation: PackageInformation = PackageInformation("pkgId", Some("1A"), Some(1), Some("Marks"))
}
