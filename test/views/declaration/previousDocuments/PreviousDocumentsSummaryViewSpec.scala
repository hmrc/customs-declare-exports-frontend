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

package views.declaration.previousDocuments

import base.Injector
import forms.common.YesNoAnswer
import forms.declaration.Document
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import play.twirl.api.Html
import services.cache.ExportsDeclarationBuilder
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents_summary

class PreviousDocumentsSummaryViewSpec extends UnitViewSpec with ExportsDeclarationBuilder with Injector {

  private val page = instanceOf[previous_documents_summary]
  private val form = YesNoAnswer.form()
  private val document1 = Document("355", "reference1", Some("3"))
  private val document2 = Document("740", "reference2", None)
  private val documents = Seq(document1, document2)

  private def createView(documents: Seq[Document] = documents)(implicit request: JourneyRequest[_]): Html =
    page(Mode.Normal, form, documents)(request, messages)

  "Previous Documents Summary page" should {

    "have all messages defined" in {
      messages must haveTranslationFor("declaration.previousDocuments.addAnotherDocument")
      messages must haveTranslationFor("declaration.previousDocuments.change.hint")
      messages must haveTranslationFor("declaration.previousDocuments.remove.hint")
      messages must haveTranslationFor("declaration.previousDocuments.summary.header.singular")
      messages must haveTranslationFor("declaration.previousDocuments.summary.header.plural")
      messages must haveTranslationFor("declaration.previousDocuments.summary.documentCode.label")
      messages must haveTranslationFor("declaration.previousDocuments.summary.documentReference.label")
      messages must haveTranslationFor("declaration.previousDocuments.summary.goodsItemIdentifier.label")
      messages must haveTranslationFor("declaration.previousDocuments.summary.warning.text")
      messages must haveTranslationFor("site.warning")
      messages must haveTranslationFor("tariff.declaration.addPreviousDocument.common.text")
      messages must haveTranslationFor("tariff.declaration.addPreviousDocument.common.linkText.0")
      messages must haveTranslationFor("tariff.expander.title.common")
    }

    onEveryDeclarationJourney() { implicit request =>
      "display section header" in {
        createView().getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display singular header" in {
        val view = createView(documents = Seq(document1))
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.summary.header.singular")
      }

      "display plural header" in {
        createView().getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.summary.header.plural", "2")
      }

      "display the warning" in {
        val warningText = s"! ${messages("site.warning")} ${messages("declaration.previousDocuments.summary.warning.text")}"
        createView().getElementsByClass("govuk-warning-text").text mustBe warningText
      }

      "display table headings" in {
        val tableHeader = createView().getElementsByClass("govuk-table__header")
        tableHeader.get(0) must containMessage("declaration.previousDocuments.summary.documentCode.label")
        tableHeader.get(1) must containMessage("declaration.previousDocuments.summary.documentReference.label")
        tableHeader.get(2) must containMessage("declaration.previousDocuments.summary.goodsItemIdentifier.label")
      }

      "have visually hidden header for Change links" in {
        createView().getElementsByClass("govuk-table__header").get(3) must containMessage("site.change.header")
      }

      "have visually hidden header for Remove links" in {
        createView().getElementsByClass("govuk-table__header").get(4) must containMessage("site.remove.header")
      }

      "display documents in table" in {
        val tableRow = createView().getElementsByClass("govuk-table__row")
        tableRow.get(1).child(0).text() mustBe "Entry Summary Declaration (ENS) (355)"
        tableRow.get(1).child(1).text() mustBe "reference1"
        tableRow.get(1).child(2).text() mustBe "3"
        tableRow.get(2).child(0).text() mustBe "Air Waybill (740)"
        tableRow.get(2).child(1).text() mustBe "reference2"
        tableRow.get(2).child(2).text() mustBe ""
      }

      "display 'Add another document' question" in {
        val addAnotherDocument = createView().getElementsByClass("govuk-fieldset__heading").get(1)
        addAnotherDocument must containMessage("declaration.previousDocuments.addAnotherDocument")
      }

      "display radio buttons" in {
        val view = createView()
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Save and continue' button on page" in {
        createView().getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        createView().getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
      }
    }

    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { implicit request =>
      "display 'Back' link to 'Nature of Transaction' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(controllers.declaration.routes.NatureOfTransactionController.displayPage(Mode.Normal))
      }
    }

    onJourney(DeclarationType.CLEARANCE, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { implicit request =>
      "display 'Back' link to 'Office of Exit' page" in {
        val specificRequest = journeyRequest(aDeclaration(withType(request.declarationType), withOfficeOfExit("officeId")))

        val backButton = createView()(specificRequest).getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
      }
    }
  }
}
