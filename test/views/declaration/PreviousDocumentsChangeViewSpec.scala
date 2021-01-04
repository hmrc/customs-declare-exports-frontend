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

package views.declaration

import base.Injector
import forms.declaration.Document
import models.Mode
import models.declaration.DocumentCategory.SimplifiedDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents_change

class PreviousDocumentsChangeViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[previous_documents_change]
  private val document = Document("750", "reference", SimplifiedDeclaration, Some("3"))
  private val form = Document.form().fill(document)

  private def createView(mode: Mode = Mode.Normal, documentId: String = ListItem.createId(0, document), form: Form[Document] = form)(
    implicit request: JourneyRequest[_]
  ) = page(mode, documentId, form)(request, messages)

  "Previous Documents Change page" should {

    "have all messages defined" in {
      messages must haveTranslationFor("declaration.previousDocuments.title")
      messages must haveTranslationFor("declaration.previousDocuments.hint")
      messages must haveTranslationFor("declaration.previousDocuments.documentCategory.error.empty")
      messages must haveTranslationFor("declaration.previousDocuments.documentCategory.error.incorrect")
      messages must haveTranslationFor("declaration.previousDocuments.documentType.error")
      messages must haveTranslationFor("declaration.previousDocuments.documentType.empty")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.hint")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.error")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.empty")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.label")
      messages must haveTranslationFor("declaration.previousDocuments.Y")
      messages must haveTranslationFor("declaration.previousDocuments.Z")
      messages must haveTranslationFor("declaration.previousDocuments.documentType")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.hint")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.error")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display same page title as header" in {

        val viewWithMessage = createView()

        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {

        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display two radio buttons with description" in {

        val view = createView()

        view.getElementById("simplified-declaration") must beSelected
        view.getElementsByAttributeValue("for", "simplified-declaration") must containMessageForElements("declaration.previousDocuments.Y")

        view.getElementById("related-document") mustNot beSelected
        view.getElementsByAttributeValue("for", "related-document") must containMessageForElements("declaration.previousDocuments.Z")
      }

      "display input with label for Previous document code" in {

        view.getElementsByAttributeValue("for", "documentType") must containMessageForElements("declaration.previousDocuments.documentType")
        view.getElementById("documentType").attr("value") mustBe "750"
      }

      "display input with label for Previous DUCR or MUCR" in {

        view.getElementsByAttributeValue("for", "documentReference") must containMessageForElements("declaration.previousDocuments.documentReference")
        view.getElementById("documentReference").attr("value") mustBe "reference"
      }

      "display input with label for Previous Goods Identifier" in {

        view.getElementsByAttributeValue("for", "goodsItemIdentifier") must containMessageForElements(
          "declaration.previousDocuments.goodsItemIdentifier"
        )
        view.getElementById("goodsItemIdentifier").attr("value") mustBe "3"
      }

      "display 'Back' button that links to 'Previous Documents Summary' page" when {
        "has a valid back button" in {

          val backButton = createView().getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(Mode.Normal)
          )
        }
      }
    }
  }
}
