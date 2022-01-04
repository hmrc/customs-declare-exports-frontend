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

package views.declaration.summary

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer.Yes
import forms.declaration.additionaldocuments.AdditionalDocument
import models.Mode
import models.declaration.AdditionalDocuments
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.additional_documents

class AdditionalDocumentsViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  private val additionalDocumentsSection = instanceOf[additional_documents]
  private val documents = Seq(
    AdditionalDocument(Some("typ1"), Some("identifier1"), None, None, None, None, None),
    AdditionalDocument(Some("typ2"), Some("identifier2"), None, None, None, None, None)
  )

  "Supporting documents view" should {

    "display title only and change link" when {

      "there is no documents" in {

        val view = additionalDocumentsSection(Mode.Normal, "itemId", 1, AdditionalDocuments(None, Seq.empty))(messages)
        val row = view.getElementsByClass("additional-documents-1-row")

        row must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments"))
        row must haveSummaryValue("No")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.changeAll", "1")

        row must haveSummaryActionsHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, "itemId"))
      }
    }

    "display all additional documents with change buttons" in {

      val view = additionalDocumentsSection(Mode.Normal, "itemId", 1, AdditionalDocuments(Yes, documents))(messages)
      val table = view.getElementById("additional-documents-1-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.additionalDocuments")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalDocuments.code")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.items.item.additionalDocuments.identifier")
      table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("site.change.header")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ1"
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier1"
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, "itemId"))
      row1ChangeLink must containMessage("site.change")
      row1ChangeLink must containMessage("declaration.summary.items.item.additionalDocuments.change", "typ1", "identifier1", 1)

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ2"
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier2"
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, "itemId"))
      row2ChangeLink must containMessage("site.change")
      row2ChangeLink must containMessage("declaration.summary.items.item.additionalDocuments.change", "typ2", "identifier2", 1)
    }

    "display all additional documents without change buttons" when {

      "actionsEnabled is false" in {

        val view =
          additionalDocumentsSection(Mode.Normal, "itemId", 1, AdditionalDocuments(Yes, documents), actionsEnabled = false)(messages)
        val table = view.getElementById("additional-documents-1-table")

        table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.additionalDocuments")

        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalDocuments.code")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.items.item.additionalDocuments.identifier")

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
