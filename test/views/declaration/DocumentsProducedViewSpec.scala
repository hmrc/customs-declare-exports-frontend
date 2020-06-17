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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.YesNoAnswer
import forms.declaration.DocumentsProducedSpec._
import forms.declaration.additionaldocuments.DocumentsProduced
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.documentsProduced.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val documentsProducedPage = instanceOf[documents_produced]
  private def createView(form: Form[YesNoAnswer] = form, cachedDocuments: Seq[DocumentsProduced] = Seq())(
    implicit request: JourneyRequest[_]
  ): Document =
    documentsProducedPage(mode, itemId, form, cachedDocuments)(request, messages)

  "Document Produced" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)
      messages must haveTranslationFor("declaration.addDocument.table.heading")
      messages must haveTranslationFor("declaration.addDocument.table.multiple.heading")
      messages must haveTranslationFor("declaration.addDocument.documentTypeCode")
      messages must haveTranslationFor("declaration.addDocument.documentIdentifier")
      messages must haveTranslationFor("declaration.addDocument.documentIdentifier.hint")
      messages must haveTranslationFor("declaration.addDocument.documentIdentifier.error")
      messages must haveTranslationFor("declaration.addDocument.documentStatusReason")
      messages must haveTranslationFor("declaration.addDocument.documentStatusReason.error")
      messages must haveTranslationFor("declaration.addDocument.documentQuantity")
      messages must haveTranslationFor("declaration.addDocument.documentQuantity.error")
      messages must haveTranslationFor("declaration.addDocument.documentStatus")
      messages must haveTranslationFor("declaration.addDocument.documentStatus.error")
      messages must haveTranslationFor("declaration.addDocument.issuingAuthorityName")
      messages must haveTranslationFor("declaration.addDocument.issuingAuthorityName.error.length")
      messages must haveTranslationFor("declaration.addDocument.error.maximumAmount")
      messages must haveTranslationFor("declaration.addDocument.error.duplicated")
      messages must haveTranslationFor("declaration.addDocument.error.notDefined")
    }
  }

  "Documents Produced View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe messagesKey("declaration.addDocument.table.multiple.heading")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("supplementary.items")
      }

      "display 'Back' button that links to 'Additional Information Required' page when no additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messagesKey(backCaption)
        backButton must haveHref(routes.AdditionalInformationRequiredController.displayPage(mode, itemId))
      }

      "display 'Save and continue' button on page" in {
        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton.text() mustBe messagesKey(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text() mustBe messagesKey(saveAndReturnCaption)
        saveAndReturnButton must haveAttribute("name", SaveAndReturn.toString)
      }
    }
  }

  "Documents Produced View on empty page with cached Additional Information" should {
    onEveryDeclarationJourney(withItem(anItem(withItemId(itemId), withAdditionalInformation("1234", "Description")))) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Additional Information' page when additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messagesKey(backCaption)
        backButton must haveHref(routes.AdditionalInformationController.displayPage(mode, itemId))
      }

    }
  }

  "Documents Produced View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error" in {

        val view = createView(YesNoAnswer.form().fillAndValidate(YesNoAnswer("invalid")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#yesNo")

        view must containErrorElementWithMessage("error.yesNo.required")
      }
    }
  }

  "Documents Produced View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display a table with previously entered document" which {

        val view = createView(cachedDocuments = Seq(correctDocumentsProduced))

        "have header row" that {
          val header = view.getElementById("documents_produced")

          "have header for Document Type" in {
            header.getElementsByClass("govuk-table__header").get(0).text() mustBe messagesKey("declaration.addDocument.summary.documentTypeCode")
          }

          "have header for Document Identifier" in {
            header.getElementsByClass("govuk-table__header").get(1).text() mustBe messagesKey("declaration.addDocument.summary.documentIdentifier")
          }

        }

        "have data row" that {

          val row = view.selectFirst("#documents_produced tbody tr")

          "have Document Type" in {
            row.selectFirst(".document-type").text() must equal(correctDocumentsProduced.documentTypeCode.get)
          }

          "have Document Identifier" in {
            row.selectFirst(".document-identifier").text() must equal(correctDocumentsProduced.documentIdentifier.get)
          }

          "have change link" in {
            val removeLink = row.select(".govuk-link").get(0)
            removeLink.text() mustBe messages("site.change") + messages("declaration.addDocument.table.update.hint")
            removeLink must haveHref(
              controllers.declaration.routes.DocumentsProducedChangeController
                .displayPage(Mode.Normal, itemId, ListItem.createId(0, correctDocumentsProduced))
            )
          }

          "have remove link" in {
            val removeLink = row.select(".govuk-link").get(1)
            removeLink.text() mustBe messages("site.remove") + messages("declaration.addDocument.table.update.hint")
            removeLink must haveHref(
              controllers.declaration.routes.DocumentsProducedRemoveController
                .displayPage(Mode.Normal, itemId, ListItem.createId(0, correctDocumentsProduced))
            )
          }
        }
      }
    }
  }
}
