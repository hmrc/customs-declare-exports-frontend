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
import controllers.declaration.routes.PreviousDocumentsSummaryController
import forms.common.YesNoAnswer.form
import forms.declaration.Document
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import play.twirl.api.Html
import utils.ListItem
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.previousDocuments.previous_documents_remove
import views.tags.ViewTest

@ViewTest
class PreviousDocumentsRemoveViewSpec extends PageWithButtonsSpec with Injector {

  val documentWithRelatesTo = Document("355", "reference", Some("3"))
  val documentWithoutRelatesTo = Document("355", "reference", None)
  val documentId = ListItem.createId(0, documentWithRelatesTo)

  val page = instanceOf[previous_documents_remove]

  override val typeAndViewInstance = (STANDARD, page(documentId, documentWithRelatesTo, form())(_, _))

  def createView(document: Document = documentWithRelatesTo)(implicit request: JourneyRequest[_]): Html =
    page(documentId, document, form())(request, messages)

  "Previous Documents Remove page" should {

    "have all messages defined" in {
      messages must haveTranslationFor("declaration.previousDocuments.remove.title")
      messages must haveTranslationFor("declaration.previousDocuments.summary.documentCode.label")
      messages must haveTranslationFor("declaration.previousDocuments.summary.documentReference.label")
      messages must haveTranslationFor("declaration.previousDocuments.summary.goodsItemIdentifier.label")
      messages must haveTranslationFor("tariff.declaration.addPreviousDocument.clearance.text")
    }

    onEveryDeclarationJourney() { implicit request =>
      "display section header" in {
        createView().getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display same page title as header" in {
        val view = createView()
        createView().title must include(view.getElementsByTag("h1").text())
      }

      "display the expected page title" in {
        createView().getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.remove.title")
      }

      "display summary list when document contains Relates to" in {
        val view = createView()

        val keyClasses = view.getElementsByClass("govuk-summary-list__key")
        keyClasses.size mustBe 3
        keyClasses.get(0) must containMessage("declaration.previousDocuments.summary.documentCode.label")
        keyClasses.get(1) must containMessage("declaration.previousDocuments.summary.documentReference.label")
        keyClasses.get(2) must containMessage("declaration.previousDocuments.summary.goodsItemIdentifier.label")

        val valueClasses = view.getElementsByClass("govuk-summary-list__value")
        valueClasses.size mustBe 3
        valueClasses.get(0).text() mustBe "Entry Summary Declaration (ENS) (355)"
        valueClasses.get(1).text() mustBe "reference"
        valueClasses.get(2).text() mustBe "3"
      }

      "display summary list when document doesn't contain Relates to" in {
        val view = createView(document = documentWithoutRelatesTo)

        val keyClasses = view.getElementsByClass("govuk-summary-list__key")
        keyClasses.size mustBe 2
        keyClasses.get(0) must containMessage("declaration.previousDocuments.summary.documentCode.label")
        keyClasses.get(1) must containMessage("declaration.previousDocuments.summary.documentReference.label")

        val valueClasses = view.getElementsByClass("govuk-summary-list__value")
        valueClasses.size mustBe 2
        valueClasses.get(0).text() mustBe "Entry Summary Declaration (ENS) (355)"
        valueClasses.get(1).text() mustBe "reference"
      }

      "display radio buttons" in {
        val view = createView()
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Back' link to 'Previous Documents Summary' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(PreviousDocumentsSummaryController.displayPage)
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }
}
