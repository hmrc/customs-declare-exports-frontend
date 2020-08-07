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

package views.declaration

import base.Injector
import forms.common.YesNoAnswer
import forms.declaration.Document
import models.Mode
import models.declaration.DocumentCategory.SimplifiedDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents_remove

class PreviousDocumentsRemoveViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[previous_documents_remove]
  private val form = YesNoAnswer.form()
  private val documentWithRelatesTo = Document("355", "reference", SimplifiedDeclaration, Some("3"))
  private val documentWithoutRelatesTo = Document("355", "reference", SimplifiedDeclaration, None)

  private def createView(
    mode: Mode = Mode.Normal,
    documentId: String = ListItem.createId(0, documentWithRelatesTo),
    document: Document = documentWithRelatesTo,
    form: Form[YesNoAnswer] = form
  )(implicit request: JourneyRequest[_]) = page(mode, documentId, document, form)(request, messages)

  "Previous Documents Remove page" should {

    "have all messages defined" in {
      messages must haveTranslationFor("declaration.type.previousDocumentsSummaryText")
      messages must haveTranslationFor("declaration.previousDocuments.documentType.label")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.summary.label")
      messages must haveTranslationFor("declaration.previousDocuments.documentCategory.summary.label")
      messages must haveTranslationFor("declaration.previousDocuments.Y")
      messages must haveTranslationFor("declaration.previousDocuments.Z")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.summary.label")
      messages must haveTranslationFor("declaration.previousDocuments.title")
      messages must haveTranslationFor("declaration.previousDocuments.remove.title")
      messages must haveTranslationFor("declaration.previousDocuments.remove.title")
    }

    onEveryDeclarationJourney() { implicit request =>
      "display section header" in {

        createView().getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display same page title as header" in {

        val viewWithMessage = createView()

        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display header" in {

        val view = createView()

        view.getElementsByClass("govuk-fieldset__heading").first() must containMessage("declaration.previousDocuments.remove.title")
      }

      "display summary list when document contains Relates to" in {

        val view = createView()

        view.getElementsByClass("govuk-summary-list__key").get(0) must containMessage("declaration.previousDocuments.documentType.label")
        view.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe ("Entry Summary Declaration (ENS) (355)")
        view.getElementsByClass("govuk-summary-list__key").get(1) must containMessage("declaration.previousDocuments.documentReference.summary.label")
        view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe ("reference")
        view.getElementsByClass("govuk-summary-list__key").get(2) must containMessage("declaration.previousDocuments.documentCategory.summary.label")
        view.getElementsByClass("govuk-summary-list__value").get(2) must containMessage("declaration.previousDocuments.Y")
        view.getElementsByClass("govuk-summary-list__key").get(3) must containMessage(
          "declaration.previousDocuments.goodsItemIdentifier.summary.label"
        )
        view.getElementsByClass("govuk-summary-list__value").get(3).text() mustBe "3"
      }

      "display summary list when document doesn't contain Relates to" in {

        val view = createView(document = documentWithoutRelatesTo)

        view.getElementsByClass("govuk-summary-list__key").get(0) must containMessage("declaration.previousDocuments.documentType.label")
        view.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe "Entry Summary Declaration (ENS) (355)"
        view.getElementsByClass("govuk-summary-list__key").get(1) must containMessage("declaration.previousDocuments.documentReference.summary.label")
        view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe "reference"
        view.getElementsByClass("govuk-summary-list__key").get(2) must containMessage("declaration.previousDocuments.documentCategory.summary.label")
        view.getElementsByClass("govuk-summary-list__value").get(2) must containMessage("declaration.previousDocuments.Y")
        intercept[IndexOutOfBoundsException] {
          view.getElementsByClass("govuk-summary-list__key").get(3).text()
        }
        intercept[IndexOutOfBoundsException] {
          view.getElementsByClass("govuk-summary-list__value").get(3).text()
        }
      }

      "display radio buttons" in {

        val view = createView()

        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Back' link to 'Previous Documents Summary' page" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage("site.back")
        backButton must haveHref(controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(Mode.Normal))
      }

      "display 'Save and continue' button on page" in {

        createView().getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {

        createView().getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
      }
    }
  }
}
