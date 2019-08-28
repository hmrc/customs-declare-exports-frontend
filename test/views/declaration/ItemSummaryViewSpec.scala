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

import controllers.declaration.routes
import forms.declaration.{ItemType, PackageInformation}
import helpers.views.declaration.ItemSummaryMessages
import models.Mode
import models.declaration.ProcedureCodesData
import org.jsoup.nodes.Document
import services.cache.ExportItem
import views.declaration.spec.ViewSpec
import views.html.declaration.items_summary
import views.tags.ViewTest

@ViewTest
class ItemSummaryViewSpec extends ViewSpec with ItemSummaryMessages {

  private val confirmationPage = app.injector.instanceOf[items_summary]
  private def view(items: List[ExportItem]): Document = confirmationPage(Mode.Normal, items)(fakeRequest, messages)

  "Item Summary Page View" should {

    "have proper messages for labels" in {
      messages(title) must be("Items")
      messages(header) must be("Items")
      messages(noItemsAddedHeader) must be("Declare all items involved in this export")
      messages(oneItemAddedHeader) must be("You have added 1 item")
      messages(manyItemsAddedHeader, 2) must be("You have added 2 items")
      messages(hint) must be(
        "An item is an individual goods type that is identified by a commodity code, for example a computer or an aluminium pipe"
      )
      messages(tableItemNumber) must be("Item number")
      messages(tableProcedureCode) must be("Procedure code")
      messages(tableCommodityCode) must be("Commodity code")
      messages(tablePackageCount) must be("Number of packages")
      messages(changeItem) must be("Change")
      messages(removeItem) must be("Remove")
      messages(addItem) must be("Add item")
      messages(addAnotherItem) must be("Add another item")
      messages(continue) must be("Save and continue")
    }

    "render back button" in {
      val doc = view(List.empty)
      doc.getElementById("link-back") must haveAttribute("href", routes.PreviousDocumentsController.displayForm().url)
    }

    "render title" when {
      "no items" in {
        val doc = view(List.empty)
        doc.getElementById("title") must containText(messages(noItemsAddedHeader))
      }

      "one item" in {
        val doc = view(List(ExportItem("id")))
        doc.getElementById("title") must containText(messages(oneItemAddedHeader))
      }

      "many items" in {
        val doc = view(List(ExportItem("id1"), ExportItem("id2")))
        doc.getElementById("title") must containText(messages(manyItemsAddedHeader, 2))
      }
    }

    "not render item table" when {
      "no items" in {
        val doc = view(List.empty)

        doc must not(containElementWithID("item_table"))
      }
    }

    "render item table sorted by sequenceId" when {
      "some items" in {
        val doc = view(
          List(
            ExportItem(
              "id2",
              sequenceId = 2,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code2"), Seq.empty)),
              itemType = Some(ItemType("item-type2", Seq.empty, Seq.empty, "", None, None, "")),
              packageInformation = List(PackageInformation("", 2, ""))
            ),
            ExportItem(
              "id1",
              sequenceId = 1,
              procedureCodes = Some(ProcedureCodesData(Some("procedure-code1"), Seq.empty)),
              itemType = Some(ItemType("item-type1", Seq.empty, Seq.empty, "", None, None, "")),
              packageInformation = List(PackageInformation("", 1, ""))
            )
          )
        )

        doc must containElementWithID("item_table")

        val rows = doc.getElementsByTag("tr")
        rows must have(size(3))

        rows.get(1) must haveId("item_0")
        rows.get(1).getElementById("item_0--sequence_id") must containText("1")
        rows.get(1).getElementById("item_0--procedure_code") must containText("procedure-code1")
        rows.get(1).getElementById("item_0--item_type") must containText("item-type1")
        rows.get(1).getElementById("item_0--package_count") must containText("1")
        rows.get(1).getElementById("item_0--change") must haveHref(
          routes.ProcedureCodesController.displayPage(Mode.Normal, "id1")
        )
        rows.get(1).getElementById("item_0--remove") must haveHref(
          routes.ItemsSummaryController.removeItem(Mode.Normal, "id1")
        )

        rows.get(2) must haveId("item_1")
        rows.get(2).getElementById("item_1--sequence_id") must containText("2")
        rows.get(2).getElementById("item_1--procedure_code") must containText("procedure-code2")
        rows.get(2).getElementById("item_1--item_type") must containText("item-type2")
        rows.get(2).getElementById("item_1--package_count") must containText("2")
        rows.get(2).getElementById("item_1--change") must haveHref(
          routes.ProcedureCodesController.displayPage(Mode.Normal, "id2")
        )
        rows.get(2).getElementById("item_1--remove") must haveHref(
          routes.ItemsSummaryController.removeItem(Mode.Normal, "id2")
        )
      }
    }

    "render actions section" when {
      "no items" in {
        val doc = view(List.empty)

        doc.getElementById("add") must containText(messages("site.add.item"))
        doc must not(containElementWithID("submit"))
        doc must not(containElementWithID("submit_and_return"))
      }

      "some items" in {
        val doc = view(List(ExportItem("id")))

        doc.getElementById("add") must containText(messages("site.add.anotherItem"))
        doc must containElementWithID("submit")
        doc must containElementWithID("submit_and_return")
      }

    }
  }

}
