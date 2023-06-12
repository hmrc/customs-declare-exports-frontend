/*
 * Copyright 2023 HM Revenue & Customs
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

package views.declaration.declarationitems

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.declaration.{CommodityDetails, PackageInformation, StatisticalValue}
import models.declaration.{ExportItem, ProcedureCodesData}
import org.jsoup.nodes.Document
import play.api.data.FormError
import services.cache.ExportsTestHelper
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationitems.items_summary
import views.tags.ViewTest

@ViewTest
class ItemsSummaryViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[items_summary]
  private val form = YesNoAnswer.form()
  private def createView(items: List[ExportItem] = List.empty, itemErrors: Seq[FormError] = Seq.empty): Document =
    page(form, items, itemErrors)(journeyRequest(), messages)

  "Items Summary Page View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.itemsAdd.titleWithItems")
      messages must haveTranslationFor("declaration.itemsSummary.addAnotherItem.question")
      messages must haveTranslationFor("declaration.itemsSummary.addAnotherItem.error.empty")
      messages must haveTranslationFor("declaration.itemsSummary.item.incorrect")
    }

    val view = createView()

    "render back button" in {
      view.getElementById("back-link") must haveAttribute("href", routes.PreviousDocumentsSummaryController.displayPage.url)
    }

    "render title" in {
      val view = createView(items = List(ExportItem("id1"), ExportItem("id2")))
      view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.itemsAdd.titleWithItems", "2")
    }

    "not render item table" when {
      "no items" in {
        view must not(containElementWithID("item_table"))
      }
    }

    "render item table as supplied to view" when {

      "some items" in {
        val view = createView(items =
          List(
            ExportItem(
              "id1",
              sequenceId = 1,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code1"), Seq.empty)),
              statisticalValue = Some(StatisticalValue("")),
              commodityDetails = Some(CommodityDetails(Some("1234567890"), Some(""))),
              packageInformation = Some(List(PackageInformation(1, "pk1", None, Some(1), None)))
            ),
            ExportItem(
              "id2",
              sequenceId = 2,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code2"), Seq.empty)),
              statisticalValue = Some(StatisticalValue("")),
              commodityDetails = Some(CommodityDetails(Some("1234567890"), Some(""))),
              packageInformation = Some(List(PackageInformation(2, "pk2", None, Some(2), None)))
            )
          )
        )

        view must containElementWithID("item_table")

        val tableHead = view.getElementsByTag("th")

        tableHead.get(0).text() mustBe messages("declaration.itemsSummary.itemNumber")
        tableHead.get(1).text() mustBe messages("declaration.itemsSummary.procedureCode")
        tableHead.get(2).text() mustBe messages("declaration.itemsSummary.commodityCode")
        tableHead.get(3).text() mustBe messages("declaration.itemsSummary.noOfPackages")
        tableHead.get(4).text() mustBe messages("site.change.header")
        tableHead.get(5).text() mustBe messages("site.remove.header")

        val rows = view.getElementsByTag("tr")
        rows must have(size(3))

        rows.get(1).getElementById("item_1--sequence_id") must containText("1")
        rows.get(1).getElementById("item_1--procedure_code") must containText("procedure-code1")
        rows.get(1).getElementById("item_1--item_type") must containText("1234567890")
        rows.get(1).getElementById("item_1--package_count").text() must be("1")
        rows.get(1).getElementById("item_1--change").getElementsByTag("a").get(0) must haveHref(routes.ProcedureCodesController.displayPage("id1"))
        rows.get(1).getElementById("item_1--remove").getElementsByTag("a").get(0) must haveHref(routes.ItemsSummaryController.removeItem("id1"))

        rows.get(2).getElementById("item_2--sequence_id") must containText("2")
        rows.get(2).getElementById("item_2--procedure_code") must containText("procedure-code2")
        rows.get(2).getElementById("item_2--item_type") must containText("1234567890")
        rows.get(2).getElementById("item_2--package_count").text() must be("2")
        rows.get(2).getElementById("item_2--change").getElementsByTag("a").get(0) must haveHref(routes.ProcedureCodesController.displayPage("id2"))
        rows.get(2).getElementById("item_2--remove").getElementsByTag("a").get(0) must haveHref(routes.ItemsSummaryController.removeItem("id2"))
      }

      "item has two package information elements with one having empty number of packages" in {
        val view = createView(items =
          List(
            ExportItem(
              "id1",
              sequenceId = 1,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code1"), Seq.empty)),
              statisticalValue = Some(StatisticalValue("")),
              commodityDetails = Some(CommodityDetails(Some("1234567890"), Some(""))),
              packageInformation =
                Some(List(PackageInformation(1, "pk1", None, Some(1), None), PackageInformation(2, "pk2", None, None, Some("shipping-marks"))))
            )
          )
        )

        view must containElementWithID("item_table")

        val tableHead = view.getElementsByTag("th")

        tableHead.get(0).text() mustBe messages("declaration.itemsSummary.itemNumber")
        tableHead.get(1).text() mustBe messages("declaration.itemsSummary.procedureCode")
        tableHead.get(2).text() mustBe messages("declaration.itemsSummary.commodityCode")
        tableHead.get(3).text() mustBe messages("declaration.itemsSummary.noOfPackages")
        tableHead.get(4).text() mustBe messages("site.change.header")
        tableHead.get(5).text() mustBe messages("site.remove.header")

        val rows = view.getElementsByTag("tr")
        rows must have(size(2))

        rows.get(1).getElementById("item_1--sequence_id") must containText("1")
        rows.get(1).getElementById("item_1--procedure_code") must containText("procedure-code1")
        rows.get(1).getElementById("item_1--item_type") must containText("1234567890")
        rows.get(1).getElementById("item_1--package_count").text() must be("1")
        rows.get(1).getElementById("item_1--change").getElementsByTag("a").get(0) must haveHref(routes.ProcedureCodesController.displayPage("id1"))
        rows.get(1).getElementById("item_1--remove").getElementsByTag("a").get(0) must haveHref(routes.ItemsSummaryController.removeItem("id1"))

      }
    }

    "render Yes - No form" in {
      val view = createView(items = List(ExportItem("id")))

      view.getElementsByClass("govuk-fieldset__legend").get(1) must containMessage("declaration.itemsSummary.addAnotherItem.question")
      view must containElementWithClass("govuk-radios")
      view must containElementWithID("code_yes")
      view must containElementWithID("code_no")
    }

    "render error section" when {
      "there are some errors in items" in {
        val itemSequenceId = "1"
        val items = List(ExportItem(itemSequenceId))
        val errors = Seq(FormError("item_1", Seq("declaration.itemsSummary.item.incorrect"), itemSequenceId))
        val view = createView(items = items, itemErrors = errors)

        view must haveGovukGlobalErrorSummary
        view must containElementWithClass("govuk-error-summary__list")
        view must containErrorElementWithTagAndHref("a", "#item_1")
        view must containErrorElementWithMessage(messages("declaration.itemsSummary.item.incorrect", itemSequenceId))
      }
    }
  }
}
