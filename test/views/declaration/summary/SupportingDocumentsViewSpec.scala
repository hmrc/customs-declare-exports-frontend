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

package views.declaration.summary

import base.Injector
import forms.declaration.additionaldocuments.DocumentsProduced
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.supporting_documents

class SupportingDocumentsViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  private val section = instanceOf[supporting_documents]
  private val documents = Seq(
    DocumentsProduced(Some("typ1"), Some("identifier1"), None, None, None, None, None),
    DocumentsProduced(Some("typ2"), Some("identifier2"), None, None, None, None, None)
  )

  "Supporting documents view" should {

    "display title only and change link" when {

      "there is no documents" in {

        val view = section(Mode.Normal, "itemId", 1, Seq.empty)(messages)
        val row = view.getElementsByClass("supporting-documents-1-row")

        row must haveSummaryKey(messages("declaration.summary.items.item.supportingDocuments"))
        row must haveSummaryValue("")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.supportingDocuments.changeAll")

        row must haveSummaryActionsHref(controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, "itemId"))
      }
    }

    "display all supporting documents with change buttons" in {

      val view = section(Mode.Normal, "itemId", 1, documents)(messages)
      val table = view.getElementById("supporting-documents-1-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.supportingDocuments")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.supportingDocuments.code")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.items.item.supportingDocuments.information")
      table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("site.change.header")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ1"
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier1"
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, "itemId"))
      row1ChangeLink
        .text() mustBe s"${messages("site.change")} ${messages("declaration.summary.items.item.supportingDocuments.change", "typ1", "identifier1", 1)}"

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ2"
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier2"
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, "itemId"))
      row2ChangeLink
        .text() mustBe s"${messages("site.change")} ${messages("declaration.summary.items.item.supportingDocuments.change", "typ2", "identifier2", 1)}"
    }

    "display all supporting documents without change buttons" when {

      "actionsEnabled is false" in {

        val view = section(Mode.Normal, "itemId", 1, documents, actionsEnabled = false)(messages)
        val table = view.getElementById("supporting-documents-1-table")

        table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.supportingDocuments")

        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.supportingDocuments.code")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages(
          "declaration.summary.items.item.supportingDocuments.information"
        )

        val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
        row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ1"
        row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier1"
        val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2)
        row1ChangeLink.attr("href") mustBe empty
        row1ChangeLink.text() mustBe empty

        val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
        row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ2"
        row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier2"
        val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2)
        row2ChangeLink.attr("href") mustBe empty
        row2ChangeLink.text() mustBe empty
      }

    }
  }
}
