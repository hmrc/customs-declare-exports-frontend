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
import config.AppConfig
import controllers.declaration.routes.{NatureOfTransactionController, OfficeOfExitController, PreviousDocumentsSummaryController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, Choice1040, ChoiceOthers}
import forms.declaration.{Document, PreviousDocumentsData}
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.scalatest.Assertion
import play.api.data.Form
import play.twirl.api.Html
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents
import views.tags.ViewTest

@ViewTest
class PreviousDocumentsViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  private val appConfig = instanceOf[AppConfig]

  private val page = instanceOf[previous_documents]
  private val form: Form[Document] = Document.form

  private def createView(implicit request: JourneyRequest[_]): Html =
    page(Mode.Normal, form)(request, messages)

  "Previous Documents View" should {

    "have all messages defined" in {
      messages must haveTranslationFor("declaration.previousDocuments.documentCode.error")
      messages must haveTranslationFor("declaration.previousDocuments.documentCode.empty")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.error")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.empty")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.error")
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, CLEARANCE)(
      aDeclaration(withAuthorisationProcedureCodeChoice(Choice1040), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))
    ) { implicit request =>
      val view = createView

      "display the expected page V1 title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.v1.title")
      }

      "display the expected under-title V1 body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(0).text mustBe messages("declaration.previousDocuments.v1.body")
        verifyBodyBulletList(view)
      }

      "display the expected under-body V1 inset text" in {
        verifyBodyInsetText(view)
      }

      "display label, body help and empty input for V1 Document type" in {
        verifyFieldDocumentType(view, 1)
      }

      "display label, body help and empty input for V1 Document reference" in {
        verifyFieldDocumentReference(view, 1)
      }

      "NOT display label and input for V1 Goods Item Identifier" in {
        view.getElementsByAttributeValue("for", "goodsItemIdentifier").size mustBe 0
        Option(view.getElementById("goodsItemIdentifier")) mustBe None
      }
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, CLEARANCE)(
      aDeclaration(withAuthorisationProcedureCodeChoice(Choice1007), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))
    ) { implicit request =>
      val view = createView

      "display the expected page V2 title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.v2.title")
      }

      "display the expected under-title V2 body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(0).text mustBe messages("declaration.previousDocuments.v2.body.1")
        paragraphs.get(1).text mustBe messages("declaration.previousDocuments.v2.body.2")
        verifyBodyBulletList(view)
      }

      "display the expected under-body V2 inset text" in {
        verifyBodyInsetText(view)

        val insetTextParagraphs = view.getElementsByClass("govuk-inset-text").get(0).children
        val thirdParagraph = insetTextParagraphs.get(2)
        thirdParagraph.text mustBe messages("declaration.previousDocuments.inset.text.3", messages("declaration.previousDocuments.inset.text.3.link"))
        thirdParagraph.child(0) must haveHref(appConfig.simplifiedDeclPreviousDoc)
      }

      "display label, body help and empty input for V2 Document type" in {
        verifyFieldDocumentType(view, 2)
      }

      "display label, body help and empty input for V2 Document reference" in {
        verifyFieldDocumentReference(view, 2)
      }

      "display label, body help and empty input for V2 Goods Item Identifier" in {
        verifyFieldGoodsItemIdentifier(view, 2)
      }
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, CLEARANCE)(
      aDeclaration(withAuthorisationProcedureCodeChoice(ChoiceOthers), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))
    ) { implicit request =>
      val view = createView

      "display the expected page V3 title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.v3.title")
      }

      "display the expected under-title V3 body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(0).text mustBe messages("declaration.previousDocuments.v3.body.1")
        paragraphs.get(1).text mustBe messages("declaration.previousDocuments.v3.body.2")
        verifyBodyBulletList(view)
      }

      "display the expected under-body V3 inset text" in {
        verifyBodyInsetText(view)
      }

      "display label, body help, hint help and empty input for V3 Document type" in {
        verifyFieldDocumentType(view, 3)
      }

      "display label, body help, hint help and empty input for V3 Document reference" in {
        verifyFieldDocumentReference(view, 3)
      }

      "display label, body help and empty input for V3 Goods Item Identifier" in {
        verifyFieldGoodsItemIdentifier(view, 3)
      }
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, CLEARANCE)(aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.yes))) { implicit request =>
      val view = createView

      "display the expected page V4 title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.v4.title")
      }

      "display the expected under-title V4 body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(0).text mustBe messages("declaration.previousDocuments.v4.body")
        verifyBodyBulletList(view)
      }

      "display the expected under-body V4 inset text" in {
        verifyBodyInsetText(view)
      }

      "display label, body help and empty input for V4 Document type" in {
        verifyFieldDocumentType(view, 4)
      }

      "display label, body help, hint help and empty input for V4 Document reference" in {
        verifyFieldDocumentReference(view, 4)
      }

      "display label, body help and empty input for V4 Goods Item Identifier" in {
        verifyFieldGoodsItemIdentifier(view, 4)
      }
    }

    onClearance { implicit request =>
      val view = createView

      "display the expected page V5 title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.v5.title")
      }

      "display the expected under-title V5 body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(0).text mustBe messages("declaration.previousDocuments.v5.body")
        verifyBodyBulletList(view)
      }

      "display the expected under-body V5 inset text" in {
        verifyBodyInsetText(view)
      }

      "display label, body help and empty input for V5 Document type" in {
        verifyFieldDocumentType(view, 5)
      }

      "display label, body help, hint help and empty input for V5 Document reference" in {
        verifyFieldDocumentReference(view, 5)
      }

      "display label, body help and empty input for V5 Goods Item Identifier" in {
        verifyFieldGoodsItemIdentifier(view, 5)
      }
    }

    onOccasional { implicit request =>
      val view = createView

      "display the expected page V6 title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.previousDocuments.v6.title")
      }

      "display the expected under-title V6 body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(0).text mustBe messages("declaration.previousDocuments.v6.body")
        verifyBodyBulletList(view)
      }

      "display the expected under-body V6 inset text" in {
        verifyBodyInsetText(view)
      }

      "display label, body help and empty input for V6 Document type" in {
        verifyFieldDocumentType(view, 6)
      }

      "display label, body help and empty input for V6 Document reference" in {
        verifyFieldDocumentReference(view, 6)
      }

      "display label, body help and empty input for V6 Goods Item Identifier" in {
        verifyFieldGoodsItemIdentifier(view, 6)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to 'Nature of Transaction' page" in {
        val backButton = createView.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton must haveHref(NatureOfTransactionController.displayPage(Mode.Normal))
      }
    }

    onJourney(CLEARANCE, OCCASIONAL, SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'Office of Exit' page" in {
        val backButton = createView.getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(OfficeOfExitController.displayPage(Mode.Normal))
      }
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display back button to the previous documents summary" when {
        "there are documents in the cache" in {
          val previousDocuments = PreviousDocumentsData(Seq(Document("MCR", "reference", None)))
          val requestWithPreviousDocuments = journeyRequest(request.cacheModel.copy(previousDocuments = Some(previousDocuments)))

          val backButton = createView(requestWithPreviousDocuments).getElementById("back-link")

          backButton must containMessage("site.back")
          backButton must haveHref(PreviousDocumentsSummaryController.displayPage(Mode.Normal))
        }
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView.getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = createView.getElementById("submit_and_return")
        saveAndReturnButton must containMessage("site.save_and_come_back_later")
      }
    }

    def verifyBodyBulletList(view: Html): Assertion = {
      val bulletItems = view.getElementsByClass("govuk-list--bullet").get(0).children
      bulletItems.get(0).text mustBe messages("declaration.previousDocuments.body.bullet.1")
      bulletItems.get(1).text mustBe messages("declaration.previousDocuments.body.bullet.2")
    }

    def verifyBodyInsetText(view: Html): Assertion = {
      val paragraphs = view.getElementsByClass("govuk-inset-text").get(0).children
      paragraphs.get(0).text mustBe messages("declaration.previousDocuments.inset.text.1")
      paragraphs.get(1).text mustBe messages("declaration.previousDocuments.inset.text.2")
    }

    def verifyFieldDocumentType(view: Html, version: Int): Assertion = {
      val label = view.getElementsByTag("label").get(0)
      label.attr("for") mustBe "documentType"
      label.text mustBe messages("declaration.previousDocuments.documentCode")

      val bodyHelp = label.nextElementSibling
      bodyHelp.text mustBe messages(s"declaration.previousDocuments.v$version.documentCode.body")

      if (version == 3) {
        val hintHelp = bodyHelp.nextElementSibling
        hintHelp.text mustBe messages("declaration.previousDocuments.v3.documentCode.hint")
      }

      view.getElementById("documentType").attr("value") mustBe empty
    }

    def verifyFieldDocumentReference(view: Html, version: Int): Assertion = {
      val label = view.getElementsByTag("label").get(1)
      label.attr("for") mustBe "documentReference"
      label.text mustBe messages("declaration.previousDocuments.documentReference")

      val bodyHelp = label.nextElementSibling
      bodyHelp.text mustBe messages(s"declaration.previousDocuments.v$version.documentReference.body")

      val hintHelp = bodyHelp.nextElementSibling
      hintHelp.text mustBe messages("declaration.previousDocuments.documentReference.hint")

      view.getElementById("documentReference").attr("value") mustBe empty
    }

    def verifyFieldGoodsItemIdentifier(view: Html, version: Int): Assertion = {
      val label = view.getElementsByTag("label").last
      label.attr("for") mustBe "goodsItemIdentifier"
      label.text mustBe messages("declaration.previousDocuments.goodsItemIdentifier")

      val bodyHelp = label.nextElementSibling
      bodyHelp.text mustBe messages(s"declaration.previousDocuments.v$version.goodsItemIdentifier.body")

      view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
    }
  }
}
