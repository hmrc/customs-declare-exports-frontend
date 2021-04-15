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
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.{Document, PreviousDocumentsData}
import models.declaration.DocumentCategory.{RelatedDocument, SimplifiedDeclaration}
import models.declaration.Locations
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.{Document => JsonDocument}
import play.api.data.Form
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents
import views.tags.ViewTest

@ViewTest
class PreviousDocumentsViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  private val page = instanceOf[previous_documents]
  private val form: Form[Document] = Document.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[Document] = form)(implicit request: JourneyRequest[_]): JsonDocument =
    page(mode, form)(request, messages)

  "Previous Documents View on empty page" should {

    "have proper messages for labels" in {
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
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display two radio buttons with description (not selected)" in {

        val view = createView(form = Document.form.bindFromRequest(Map.empty))

        view.getElementById("simplified-declaration") mustNot beSelected
        view.getElementsByAttributeValue("for", "simplified-declaration").text() mustBe messages("declaration.previousDocuments.Y")

        view.getElementById("related-document") mustNot beSelected
        view.getElementsByAttributeValue("for", "related-document").text() mustBe messages("declaration.previousDocuments.Z")
      }

      "display empty input with label for Previous document code" in {
        view.getElementsByAttributeValue("for", "documentType") must containMessageForElements("declaration.previousDocuments.documentType")
        view.getElementById("documentType").attr("value") mustBe empty
      }

      "display empty input with label for Previous DUCR or MUCR" in {
        view.getElementsByAttributeValue("for", "documentReference") must containMessageForElements("declaration.previousDocuments.documentReference")
        view.getElementById("documentReference").attr("value") mustBe empty
      }

      "display empty input with label for Previous Goods Identifier" in {
        view.getElementsByAttributeValue("for", "goodsItemIdentifier") must containMessageForElements(
          "declaration.previousDocuments.goodsItemIdentifier"
        )
        view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
      }
    }

    "display 'Back' button that links to 'Transaction Type' page" when {
      onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { implicit request =>
        "has a valid back button" in {

          val backButton = createView().getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.NatureOfTransactionController.displayPage(Mode.Normal))
        }
      }

      onJourney(DeclarationType.CLEARANCE, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { implicit request =>
        " with Office Of Exit Inside UK" in {

          val officeOfExitInsideUK = Locations(officeOfExit = Some(OfficeOfExit("id")))
          val requestWithOfficeOfExitInsideUK = journeyRequest(request.cacheModel.copy(locations = officeOfExitInsideUK))

          val backButton = createView()(requestWithOfficeOfExitInsideUK).getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
        }
      }
    }

    onEveryDeclarationJourney() { implicit request =>
      "display back button to the previous documents summary" when {

        "there are documents in the cache" in {

          val previousDocuments = PreviousDocumentsData(Seq(Document("MCR", "reference", SimplifiedDeclaration, None)))
          val requestWithPreviousDocuments = journeyRequest(request.cacheModel.copy(previousDocuments = Some(previousDocuments)))

          val backButton = createView()(requestWithPreviousDocuments).getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(Mode.Normal)
          )
        }
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView().getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = createView().getElementById("submit_and_return")
        saveAndReturnButton must containMessage("site.save_and_come_back_later")
      }
    }

  }

  "Previous Documents View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display selected second radio button - Simplified Declaration (Y)" in {

        val view = createView(form = Document.form.fill(Document("", "", SimplifiedDeclaration, Some(""))))

        view.getElementById("simplified-declaration") must beSelected
        view.getElementById("related-document") mustNot beSelected
      }

      "display selected third radio button - Previous Documents (Z)" in {

        val view = createView(form = Document.form.fill(Document("", "", RelatedDocument, Some(""))))

        view.getElementById("simplified-declaration") mustNot beSelected
        view.getElementById("related-document") must beSelected
      }

      "display data in Document type input" in {

        val view = createView(form = Document.form.fill(Document("Test", "", RelatedDocument, Some(""))))

        view.getElementById("documentType").attr("value") must be("Test")
        view.getElementById("documentReference").attr("value") mustBe empty
        view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
      }

      "display data in Previous DUCR or MUCR input" in {

        val view = createView(form = Document.form.fill(Document("", "Test", RelatedDocument, Some(""))))

        view.getElementById("documentType").attr("value") mustBe empty
        view.getElementById("documentReference").attr("value") must be("Test")
        view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
      }

      "display data in Previous Goods Identifier input" in {

        val view = createView(form = Document.form.fill(Document("", "", RelatedDocument, Some("Test"))))

        view.getElementById("documentType").attr("value") mustBe empty
        view.getElementById("documentReference").attr("value") mustBe empty
        view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
      }

      "display all data entered" in {

        val view = createView(form = Document.form.fill(Document("Test", "Test", SimplifiedDeclaration, Some("Test"))))

        view.getElementById("simplified-declaration") must beSelected
        view.getElementById("related-document") mustNot beSelected

        view.getElementById("documentType").attr("value") must be("Test")
        view.getElementById("documentReference").attr("value") must be("Test")
        view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
      }
    }
  }
}
