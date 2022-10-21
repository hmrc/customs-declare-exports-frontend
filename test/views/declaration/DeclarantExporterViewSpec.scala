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
import controllers.declaration.routes.{ConsignmentReferencesController, DeclarantDetailsController, LinkDucrToMucrController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantIsExporter
import forms.declaration.DeclarantIsExporter.form
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.components.gds.Styles
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.declarant_exporter
import views.tags.ViewTest

@ViewTest
class DeclarantExporterViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[declarant_exporter]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[DeclarantIsExporter] = form())(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Declarant Exporter View on empty page" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.declarant.exporter.title")
      messages must haveTranslationFor("declaration.declarant.exporter.body")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.yes")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.no")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.no.hint")
      messages must haveTranslationFor("declaration.declarant.exporter.error")
      messages must haveTranslationFor("tariff.declaration.areYouTheExporter.clearance.text")
    }
  }

  "Declarant Exporter View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        createView().getElementsByClass(Styles.gdsPageHeading) must containMessageForElements("declaration.declarant.exporter.title")
      }

      "display section header" in {
        createView().getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display paragraph body" in {
        createView().getElementsByTag("p") must containMessageForElements("declaration.declarant.exporter.body")
      }

      "display radio button with Yes option" in {
        val view = createView()
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("declaration.declarant.exporter.answer.yes")
      }

      "display radio button with No option" in {
        val view = createView()
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("declaration.declarant.exporter.answer.no")
        view.getElementById("code_no-item-hint") must containMessage("declaration.declarant.exporter.answer.no.hint")
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Link DUCR to a MUCR' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(LinkDucrToMucrController.displayPage().url)
      }
    }

    onSupplementary { implicit request =>
      "display 'Back' button that links to 'Are you the exporter' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(ConsignmentReferencesController.displayPage().url)
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Declarant Details' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DeclarantDetailsController.displayPage().url)
      }
    }
  }

  "Declarant Exporter View with invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when answer is empty" in {
        val view = createView(DeclarantIsExporter.form().fillAndValidate(DeclarantIsExporter("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.exporter.error")
      }

      "display error when EORI is provided, but is incorrect" in {
        val view = createView(form().fillAndValidate(DeclarantIsExporter("wrong")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.exporter.error")
      }
    }
  }

  "Declarant Exporter View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display answer input" in {
        val form = DeclarantIsExporter.form().fill(DeclarantIsExporter(YesNoAnswers.yes))
        val view = createView(form)

        view.getElementById("code_yes") must beSelected
      }
    }
  }
}
