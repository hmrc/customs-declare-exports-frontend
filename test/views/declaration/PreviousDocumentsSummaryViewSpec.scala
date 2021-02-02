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
import forms.common.YesNoAnswer
import forms.declaration.Document
import models.declaration.DocumentCategory.{RelatedDocument, SimplifiedDeclaration}
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import play.api.data.Form
import services.cache.ExportsDeclarationBuilder
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents_summary

class PreviousDocumentsSummaryViewSpec extends UnitViewSpec with ExportsDeclarationBuilder with Injector {

  private val page = instanceOf[previous_documents_summary]
  private val form = YesNoAnswer.form()
  private val document1 = Document("355", "reference1", SimplifiedDeclaration, Some("3"))
  private val document2 = Document("740", "reference2", RelatedDocument, None)
  private val documents = Seq(document1, document2)

  private def createView(mode: Mode = Mode.Normal, form: Form[YesNoAnswer] = form, documents: Seq[Document] = documents)(
    implicit request: JourneyRequest[_]
  ) =
    page(mode, form, documents)(request, messages)

  "Previous Documents Summary page" should {

    "have all messages defined" in {
      messages must haveTranslationFor("declaration.previousDocuments.summary.header.singular")
      messages must haveTranslationFor("declaration.previousDocuments.summary.header.plural")
      messages must haveTranslationFor("tariff.declaration.addPreviousDocument.common.text")
      messages must haveTranslationFor("tariff.declaration.addPreviousDocument.common.linkText.0")
      messages must haveTranslationFor("declaration.previousDocuments.change.hint")
      messages must haveTranslationFor("declaration.previousDocuments.remove.hint")
      messages must haveTranslationFor("declaration.previousDocuments.documentType.label")
      messages must haveTranslationFor("declaration.previousDocuments.documentReference.label")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.label")
      messages must haveTranslationFor("declaration.previousDocuments.goodsItemIdentifier.label")
      messages must haveTranslationFor("declaration.previousDocuments.title")
      messages must haveTranslationFor("declaration.previousDocuments.addAnotherDocument")
      messages must haveTranslationFor("tariff.expander.title.common")
    }

    onEveryDeclarationJourney() { implicit request =>
      "display section header" in {

        createView().getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display singular header" in {

        val view = createView(documents = Seq(document1))

        view.getElementsByClass("govuk-fieldset__heading").first() must containMessage("declaration.previousDocuments.summary.header.singular")
      }

      "display plural header" in {

        val view = createView()

        view.getElementsByClass("govuk-fieldset__heading").first() must containMessage("declaration.previousDocuments.summary.header.plural", "2")
      }

      "display table headings" in {

        val view = createView()

        view.getElementsByClass("govuk-table__header").get(0) must containMessage("declaration.previousDocuments.documentType.summary.label")
        view.getElementsByClass("govuk-table__header").get(1) must containMessage("declaration.previousDocuments.documentReference.summary.label")
        view.getElementsByClass("govuk-table__header").get(2) must containMessage("declaration.previousDocuments.goodsItemIdentifier.summary.label")
      }

      "have visually hidden header for Change links" in {
        val view = createView()
        view.getElementsByClass("govuk-table__header").get(3) must containMessage("site.change.header")
      }

      "have visually hidden header for Remove links" in {
        val view = createView()
        view.getElementsByClass("govuk-table__header").get(4) must containMessage("site.remove.header")
      }

      "display documents in table" in {

        val view = createView()

        view.getElementsByClass("govuk-table__row").get(1).child(0).text() mustBe "Entry Summary Declaration (ENS) (355)"
        view.getElementsByClass("govuk-table__row").get(1).child(1).text() mustBe "reference1"
        view.getElementsByClass("govuk-table__row").get(1).child(2).text() mustBe "3"
        view.getElementsByClass("govuk-table__row").get(2).child(0).text() mustBe "Air Waybill (740)"
        view.getElementsByClass("govuk-table__row").get(2).child(1).text() mustBe "reference2"
        view.getElementsByClass("govuk-table__row").get(2).child(2).text() mustBe ""
      }

      "display add another question" in {

        val view = createView()

        view.getElementsByClass("govuk-fieldset__heading").get(1) must containMessage("declaration.previousDocuments.addAnotherDocument")
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
      "display 'Back' link to 'Office of Exit' page" when {

        "Office of exit has answer Yes" in {

          val specificRequest = journeyRequest(aDeclaration(withType(request.declarationType), withOfficeOfExit("officeId", "Yes")))
          val backButton = createView()(specificRequest).getElementById("back-link")

          backButton must containMessage("site.back")
          backButton must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
        }
      }

      "display 'Back' link to 'Office of Exit Outside UK' page" when {

        "Office of exit has answer No" in {

          val specificRequest = journeyRequest(aDeclaration(withType(request.declarationType), withOfficeOfExit("", "No")))
          val backButton = createView()(specificRequest).getElementById("back-link")

          backButton must containMessage("site.back")
          backButton must haveHref(controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage(Mode.Normal))
        }
      }
    }
  }
}
