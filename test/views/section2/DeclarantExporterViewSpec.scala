/*
 * Copyright 2023 HM Revenue & Customs
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

package views.section2

import base.Injector
import controllers.section1.routes.DeclarantDetailsController
import controllers.section2.routes.EntryIntoDeclarantsRecordsController
import controllers.summary.routes.SectionSummaryController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.DeclarantIsExporter
import forms.section2.DeclarantIsExporter.form
import models.DeclarationType._
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.common.PageWithButtonsSpec
import views.components.gds.Styles
import views.html.section2.declarant_exporter
import views.tags.ViewTest

@ViewTest
class DeclarantExporterViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[declarant_exporter]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[DeclarantIsExporter] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Declarant Exporter View" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.declarant.exporter.title")
      messages must haveTranslationFor("declaration.declarant.exporter.body")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.yes")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.no")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.no.hint")
      messages must haveTranslationFor("declaration.declarant.exporter.error")
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
      "display 'Back' button that links to the /summary-section/1 page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(SectionSummaryController.displayPage(1).url)
      }
    }

    onSupplementary { implicit request =>
      "display 'Back' button that links to the /summary-section/1 page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(SectionSummaryController.displayPage(1).url)
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Declarant Details' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DeclarantDetailsController.displayPage.url)
      }
    }
  }

  "Declarant Exporter View" when {

    "the declaration is in AMENDMENT_DRAFT mode" should {
      val status = withStatus(AMENDMENT_DRAFT)

      "display 'Back' button that links to '/entry-into-declarants-records' page" when {
        "on CLEARANCE journey" in {
          implicit val request = withRequestOfType(CLEARANCE, status)
          val backButton = createView().getElementById("back-link")

          backButton must containMessage(backToPreviousQuestionCaption)
          backButton must haveHref(EntryIntoDeclarantsRecordsController.displayPage.url)
        }
      }

      "not display the 'Back' button" when {
        nonClearanceJourneys.foreach { declarationType =>
          s"the journey is $declarationType" in {
            implicit val request = withRequestOfType(declarationType, status)
            Option(createView().getElementById("back-link")) mustBe None
          }
        }
      }
    }
  }

  "Declarant Exporter View with invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when answer is empty" in {
        val view = createView(DeclarantIsExporter.form.fillAndValidate(DeclarantIsExporter("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.exporter.error")
      }

      "display error when EORI is provided, but is incorrect" in {
        val view = createView(form.fillAndValidate(DeclarantIsExporter("wrong")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.exporter.error")
      }
    }
  }

  "Declarant Exporter View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display answer input" in {
        val form = DeclarantIsExporter.form.fill(DeclarantIsExporter(YesNoAnswers.yes))
        val view = createView(form)

        view.getElementById("code_yes") must beSelected
      }
    }
  }
}
