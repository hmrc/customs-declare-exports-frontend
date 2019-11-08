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
import controllers.declaration.routes
import forms.declaration.{CommodityDetails, PackageInformation}
import models.Mode
import models.declaration.{ExportItem, ItemType, ProcedureCodesData}
import org.jsoup.nodes.Document
import play.api.data.FormError
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.items_summary
import views.tags.ViewTest

@ViewTest
class ItemSummaryViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new items_summary(mainTemplate)
  private def createView(mode: Mode = Mode.Normal, items: List[ExportItem] = List.empty, itemErrors: Seq[FormError] = Seq.empty): Document =
    page(mode, items, itemErrors)(journeyRequest(), stubMessages())

  "Item Summary Page View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.itemsAdd.title")
      messages must haveTranslationFor("supplementary.itemsAdd.titleWithItems")
      messages must haveTranslationFor("site.add.item")
      messages must haveTranslationFor("site.add.anotherItem")
      messages must haveTranslationFor("declaration.itemsSummary.item.incorrect")
    }

    val view = createView()

    "render back button" in {

      view.getElementById("link-back") must haveAttribute("href", routes.PreviousDocumentsController.displayPage().url)
    }

    "render title" when {
      "no items" in {
        view.getElementById("title").text() mustBe "supplementary.itemsAdd.title"
      }

      "one item" in {
        view.getElementById("title").text() mustBe "supplementary.itemsAdd.title"
      }

      "many items" in {
        val view = createView(items = List(ExportItem("id1"), ExportItem("id2")))
        view.getElementById("title").text() mustBe "supplementary.itemsAdd.titleWithItems"
      }
    }

    "not render item table" when {
      "no items" in {
        view must not(containElementWithID("item_table"))
      }
    }

    "render item table sorted by sequenceId" when {
      "some items" in {
        val view = createView(
          items = List(
            ExportItem(
              "id2",
              sequenceId = 2,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code2"), Seq.empty)),
              itemType = Some(ItemType(Seq.empty, Seq.empty, None, "")),
              commodityDetails = Some(CommodityDetails(Some("item-type2"), "")),
              packageInformation = List(PackageInformation("", 2, ""))
            ),
            ExportItem(
              "id1",
              sequenceId = 1,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code1"), Seq.empty)),
              itemType = Some(ItemType(Seq.empty, Seq.empty, None, "")),
              commodityDetails = Some(CommodityDetails(Some("item-type1"), "")),
              packageInformation = List(PackageInformation("", 1, ""))
            )
          )
        )

        view must containElementWithID("item_table")

        val rows = view.getElementsByTag("tr")
        rows must have(size(3))

        rows.get(1) must haveId("item_0")
        rows.get(1).getElementById("item_0--sequence_id") must containText("1")
        rows.get(1).getElementById("item_0--procedure_code") must containText("procedure-code1")
        rows.get(1).getElementById("item_0--item_type") must containText("item-type1")
        rows.get(1).getElementById("item_0--package_count") must containText("1")
        rows.get(1).getElementById("item_0--change") must haveHref(routes.ProcedureCodesController.displayPage(Mode.Normal, "id1"))
        rows.get(1).getElementById("item_0--remove") must haveHref(routes.ItemsSummaryController.removeItem(Mode.Normal, "id1"))

        rows.get(2) must haveId("item_1")
        rows.get(2).getElementById("item_1--sequence_id") must containText("2")
        rows.get(2).getElementById("item_1--procedure_code") must containText("procedure-code2")
        rows.get(2).getElementById("item_1--item_type") must containText("item-type2")
        rows.get(2).getElementById("item_1--package_count") must containText("2")
        rows.get(2).getElementById("item_1--change") must haveHref(routes.ProcedureCodesController.displayPage(Mode.Normal, "id2"))
        rows.get(2).getElementById("item_1--remove") must haveHref(routes.ItemsSummaryController.removeItem(Mode.Normal, "id2"))
      }
    }

    "render actions section" when {
      "no items" in {
        view.getElementById("add").text() mustBe "site.add.item site.add.item"
        view must not(containElementWithID("submit"))
        view must not(containElementWithID("submit_and_return"))
      }

      "some items" in {
        val view = createView(items = List(ExportItem("id")))

        view.getElementById("add").text() mustBe "site.add.anotherItem site.add.anotherItem"
        view must containElementWithID("submit")
        view must containElementWithID("submit_and_return")
      }
    }

    "render error section" when {

      "there are some errors in items" in {

        val itemSequenceId = "1"
        val items = List(ExportItem(itemSequenceId))
        val errors = Seq(FormError("item_0", Seq("declaration.itemsSummary.item.incorrect"), itemSequenceId))
        val view = createView(items = items, itemErrors = errors)

        view must haveGlobalErrorSummary
        view must containElementWithID("item_0-error")
        view.getElementById("item_0-error").text() mustBe messages("declaration.itemsSummary.item.incorrect", itemSequenceId)
        view.getElementById("item_0-error") must haveHref("#item_0")
      }
    }
  }
}
