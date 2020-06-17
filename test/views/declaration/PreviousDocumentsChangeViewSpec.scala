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
import forms.declaration.Document
import models.{DeclarationType, Mode}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents_change

class PreviousDocumentsChangeViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[previous_documents_change]
  private val document = Document("Y", "750", "reference", Some("3"))
  private val form = Document.form().fill(document)

  private def createView(
    mode: Mode = Mode.Normal,
    documentId: String = ListItem.createId(0, document),
    form: Form[Document] = form,
    messages: Messages = stubMessages(),
    request: JourneyRequest[_] = journeyRequest(DeclarationType.STANDARD)
  ) = page(mode, documentId, form)(request, messages)

  "Previous Documents Change page" should {

    "have all messages defined" in {

      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.previousDocuments.title")
      messages must haveTranslationFor("declaration.previousDocuments.hint")
      messages must haveTranslationFor("declaration.previousDocuments.heading")
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
      messages must haveTranslationFor("supplementary.consignmentReferences.heading")
      messages must haveTranslationFor("declaration.previousDocuments.documentType")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.hint")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.error")
    }

    onEveryDeclarationJourney() { request =>
      val view = createView()

      "display same page title as header" in {

        val viewWithMessage = createView(messages = realMessagesApi.preferred(request))

        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {

        view.getElementById("section-header").text() must include("declaration.previousDocuments.heading")
      }

      "display two radio buttons with description" in {

        val view = createView()

        view.getElementById("simplified-declaration") must beSelected
        view.getElementsByAttributeValue("for", "simplified-declaration").text() mustBe messages("declaration.previousDocuments.Y")

        view.getElementById("related-document") mustNot beSelected
        view.getElementsByAttributeValue("for", "related-document").text() mustBe messages("declaration.previousDocuments.Z")
      }

      "display input with label for Previous document code" in {

        view.getElementsByAttributeValue("for", "documentType").text() must be("declaration.previousDocuments.documentType")
        view.getElementById("documentType").attr("value") mustBe "750"
      }

      "display input with label for Previous DUCR or MUCR" in {

        view.getElementsByAttributeValue("for", "documentReference").text() must be("declaration.previousDocuments.documentReference")
        view.getElementById("documentReference").attr("value") mustBe "reference"
      }

      "display input with label for Previous Goods Identifier" in {

        view.getElementsByAttributeValue("for", "goodsItemIdentifier").text() must be("declaration.previousDocuments.goodsItemIdentifier")
        view.getElementById("goodsItemIdentifier").attr("value") mustBe "3"
      }

      "display 'Back' button that links to 'Previous Documents Summary' page" when {
        "has a valid back button" in {

          val backButton = createView(request = request).getElementById("back-link")

          backButton.text() must be("site.back")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(Mode.Normal)
          )
        }
      }
    }
  }
}
