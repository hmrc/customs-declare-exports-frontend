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
import forms.declaration.Document
import models.Mode
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.related_documents

class RelatedDocumentsViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {
  private val section = instanceOf[related_documents]

  "Related documents" should {

    "display row with No value with change button" when {

      "documents are empty" in {

        val view = section(Mode.Normal, Seq.empty)(messages)
        val row = view.getElementsByClass("previous-documents-row")

        row must haveSummaryKey(messages("declaration.summary.transaction.previousDocuments"))
        row must haveSummaryValue(messages("site.no"))

        row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.previousDocuments.change")

        row must haveSummaryActionsHref(controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Normal))
      }
    }

    "display documents with change button" when {

      "documents exists" in {

        val data = Seq(Document("325", "123456", None), Document("271", "654321", None))

        val view = section(Mode.Change, data)(messages)
        val table = view.getElementById("previous-documents")

        table.getElementsByTag("caption").text() mustBe messages("declaration.summary.transaction.previousDocuments")
        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.transaction.previousDocuments.type")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.transaction.previousDocuments.reference")
        table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("site.change.header")

        val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
        row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "Proforma Invoice - 325"
        row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "123456"

        val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
        row1ChangeLink must haveHref(controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(Mode.Change))
        row1ChangeLink must containMessage("site.change")
        row1ChangeLink must containMessage("declaration.summary.transaction.previousDocuments.document.change", "123456")

        val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
        row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "Packing List - 271"
        row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "654321"

        val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
        row2ChangeLink must haveHref(controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(Mode.Change))
        row2ChangeLink must containMessage("site.change")
        row2ChangeLink must containMessage("declaration.summary.transaction.previousDocuments.document.change", "654321")
      }
    }
  }
}
