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

package views.declaration.summary

import base.Injector
import forms.declaration.Document
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.related_documents_gds

class RelatedDocumentsViewSpec extends UnitViewSpec with ExportsTestData with Injector {
  private val section = instanceOf[related_documents_gds]

  "Related documents" should {

    "display row with No value with change button" when {

      "documents are empty" in {


        val view = section(Mode.Normal, Seq.empty)(messages, journeyRequest())
        val row = view.getElementsByClass("previous-documents-row")

        row must haveSummaryKey(messages("declaration.summary.parties.holders"))
        row must haveSummaryValue(messages("site.no"))

        row must haveSummaryActionsText("site.change declaration.summary.transaction.previousDocuments.change")

        row must haveSummaryActionsHref(controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Normal))

//        val view = related_documents_gds(Mode.Change, Seq.empty)(messages, journeyRequest())
//
//        view.getElementById("previous-documents-label").text() mustBe messages("declaration.summary.transaction.previousDocuments")
//        view.getElementById("previous-documents").text() mustBe messages("site.no")
//
//        val List(change, accessibleChange) = view.getElementById("previous-documents-change").text().split(" ").toList
//
//        change mustBe messages("site.change")
//        accessibleChange mustBe messages("declaration.summary.transaction.previousDocuments.change")
//
//        view.getElementById("previous-documents-change") must haveHref(
//          controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Change)
//        )
      }
    }

    "display documents with change button" when {

      "documents exists" in {

        val data = Seq(Document("X", "325", "123456", None), Document("X", "271", "654321", None))

       // val view = section(Mode.Normal, data)(messages, journeyRequest())

//        val view = related_documents(Mode.Change, data)(messages, journeyRequest())
//
//        view.getElementById("previous-documents-label").text() mustBe messages("declaration.summary.transaction.previousDocuments")
//        view.getElementById("previous-documents-type").text() mustBe messages("declaration.summary.transaction.previousDocuments.type")
//        view.getElementById("previous-documents-reference").text() mustBe messages("declaration.summary.transaction.previousDocuments.reference")
//        view.getElementById("previous-document-0-type").text() mustBe "Proforma Invoice - 325"
//        view.getElementById("previous-document-0-reference").text() mustBe "123456"
//
//        val List(change1, accessibleChange1) = view.getElementById("previous-document-0-change").text().split(" ").toList
//
//        change1 mustBe messages("site.change")
//        accessibleChange1 mustBe messages("declaration.summary.transaction.previousDocuments.document.change", 0)
//
//        view.getElementById("previous-document-0-change") must haveHref(
//          controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Change)
//        )
        view.getElementById("previous-document-1-type").text() mustBe "Packing List - 271"
        view.getElementById("previous-document-1-reference").text() mustBe "654321"

        val List(change2, accessibleChange2) = view.getElementById("previous-document-1-change").text().split(" ").toList

//        change2 mustBe messages("site.change")
//        accessibleChange2 mustBe messages("declaration.summary.transaction.previousDocuments.document.change", 1)
//
//        view.getElementById("previous-document-1-change") must haveHref(
//          controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Change)
//        )

        val view = section(Mode.Change, data)(messages, journeyRequest())
        val table = view.getElementById("previous-documents")

        table.getElementsByTag("caption").text() mustBe messages("declaration.summary.transaction.previousDocuments")
        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.transaction.previousDocuments.type")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.transaction.previousDocuments.reference")
        table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("")
        table.getElementsByClass("govuk-table__header-0-type").text() mustBe messages("Proforma Invoice - 325") //needs changing
        table.getElementsByClass("govuk-table__header-0-reference").text() mustBe messages("123456") //needs changing

        val List(change1, accessibleChange1) = view.getElementById("previous-document-0-change").text().split(" ").toList

        table mustBe haveSummaryActionsText("site.change declaration.summary.transaction.previousDocuments.document.change",0)

        view.getElementsByClass mustBe  haveSummaryActionsHref(controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Change))






        val row = view.getElementsByClass("previous-documents-row")
        row must haveSummaryKey(messages("declaration.summary.transaction.previousDocuments"))
        row must haveSummaryValue("123")

        row must haveSummaryActionsText("site.change declaration.summary.locations.officeOfExit.change")

        row must haveSummaryActionsHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Change))

      }
    }
  }
}
