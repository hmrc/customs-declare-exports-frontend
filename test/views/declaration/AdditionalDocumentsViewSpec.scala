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

package views.declaration

import base.Injector
import controllers.declaration.routes
import controllers.helpers.SaveAndReturn
import forms.common.YesNoAnswer
import forms.declaration.AdditionalDocumentSpec._
import forms.declaration.additionaldocuments.AdditionalDocument
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import tools.Stubs
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_documents
import views.tags.ViewTest

@ViewTest
class AdditionalDocumentsViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val additionalDocumentsPage = instanceOf[additional_documents]
  private def createView(form: Form[YesNoAnswer] = form, cachedDocuments: Seq[AdditionalDocument] = Seq())(
    implicit request: JourneyRequest[_]
  ): Document =
    additionalDocumentsPage(mode, itemId, form, cachedDocuments)(request, messages)

  "additional_documents view" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.additionalDocument.table.heading")
      messages must haveTranslationFor("declaration.additionalDocument.table.multiple.heading")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier.body")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier.error")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatusReason")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatusReason.error")
      messages must haveTranslationFor("declaration.additionalDocument.documentQuantity")
      messages must haveTranslationFor("declaration.additionalDocument.documentQuantity.error")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatus")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatus.error")
      messages must haveTranslationFor("declaration.additionalDocument.issuingAuthorityName")
      messages must haveTranslationFor("declaration.additionalDocument.issuingAuthorityName.error.length")
      messages must haveTranslationFor("declaration.additionalDocument.error.maximumAmount")
      messages must haveTranslationFor("declaration.additionalDocument.error.duplicated")
      messages must haveTranslationFor("declaration.additionalDocument.error.notDefined")
    }
  }

  "additional_documents view on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.additionalDocument.table.multiple.heading", "0")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display 'Back' button that links to 'Additional Information Required' page when no additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalInformationRequiredController.displayPage(mode, itemId))
      }

      "display 'Save and continue' button on page" in {
        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton must containMessage(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage(saveAndReturnCaption)
        saveAndReturnButton must haveAttribute("name", SaveAndReturn.toString)
      }
    }
  }

  "additional_documents view on empty page with cached Additional Information" should {
    onEveryDeclarationJourney(withItem(anItem(withItemId(itemId), withAdditionalInformation("1234", "Description")))) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Additional Information' page when additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalInformationController.displayPage(mode, itemId))
      }

    }
  }

  "additional_documents view for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error" in {

        val view = createView(YesNoAnswer.form().fillAndValidate(YesNoAnswer("invalid")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("error.yesNo.required")
      }
    }
  }

  "additional_documents view when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display a table with previously entered document" which {

        val view = createView(cachedDocuments = Seq(correctAdditionalDocument))

        "have header row" that {
          val header = view.getElementById("additional_documents")

          "have header for Document Type" in {
            header.getElementsByClass("govuk-table__header").get(0) must containMessage("declaration.additionalDocument.summary.documentTypeCode")
          }

          "have header for Document Identifier" in {
            header.getElementsByClass("govuk-table__header").get(1) must containMessage("declaration.additionalDocument.summary.documentIdentifier")
          }

          "have visually hidden header for Change links" in {
            header.getElementsByClass("govuk-table__header").get(3) must containMessage("site.change.header")
          }

          "have visually hidden header for Remove links" in {
            header.getElementsByClass("govuk-table__header").get(4) must containMessage("site.remove.header")
          }

        }

        "have data row" that {

          val row = view.selectFirst("#additional_documents tbody tr")

          "have Document Type" in {
            row.selectFirst(".document-type").text() must equal(correctAdditionalDocument.documentTypeCode.get)
          }

          "have Document Identifier" in {
            row.selectFirst(".document-identifier").text() must equal(correctAdditionalDocument.documentIdentifier.get)
          }

          "have change link" in {
            val removeLink = row.select(".govuk-link").get(0)

            removeLink must containMessage("site.change")
            removeLink must containMessage("declaration.additionalDocument.table.change.hint", "ABCDEF1234567890")
            removeLink must haveHref(
              routes.AdditionalDocumentChangeController.displayPage(Mode.Normal, itemId, ListItem.createId(0, correctAdditionalDocument))
            )
          }

          "have remove link" in {
            val removeLink = row.select(".govuk-link").get(1)

            removeLink must containMessage("site.remove")
            removeLink must containMessage("declaration.additionalDocument.table.remove.hint", "ABCDEF1234567890")
            removeLink must haveHref(
              routes.AdditionalDocumentRemoveController.displayPage(Mode.Normal, itemId, ListItem.createId(0, correctAdditionalDocument))
            )
          }
        }
      }
    }
  }
}
