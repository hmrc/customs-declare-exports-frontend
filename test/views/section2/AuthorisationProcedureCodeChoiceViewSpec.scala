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
import controllers.section2.routes.{AdditionalActorsSummaryController, ConsigneeDetailsController}
import forms.common.YesNoAnswer._
import forms.section2.AuthorisationProcedureCodeChoice.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import views.common.PageWithButtonsSpec
import views.components.gds.Styles
import views.html.section2.authorisation_procedure_code_choice

class AuthorisationProcedureCodeChoiceViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[authorisation_procedure_code_choice]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView()(implicit request: JourneyRequest[_]): Document = page(form)

  "Which export procedure are you using Page" must {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.title")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.error.empty")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.radio.1040")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.radio.1007")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.radio.other")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.2")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.2")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.3")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.4")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.4.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.5")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.6")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.6.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.7")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.8.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.9.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.10")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.11.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.12.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.13")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.13.linkText")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.authorisations.procedureCodeChoice.title")
      }

      "display 'Mode of Transport' section" which {
        "have '1040' option" in {
          view.getElementsByAttributeValue("for", "Code1040") must containMessageForElements(
            "declaration.authorisations.procedureCodeChoice.radio.1040"
          )
        }

        "have '1007' option" in {
          view.getElementsByAttributeValue("for", "Code1007") must containMessageForElements(
            "declaration.authorisations.procedureCodeChoice.radio.1007"
          )
        }

        "have 'Other' option" in {
          view.getElementsByAttributeValue("for", "CodeOther") must containMessageForElements(
            "declaration.authorisations.procedureCodeChoice.radio.other"
          )
        }
      }

      "display non-standard procedures expander" in {
        view.getElementsByClass("govuk-details__summary-text").first() must containHtml(
          messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")
        )
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Authorisations Required' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(AdditionalActorsSummaryController.displayPage)
      }

    }

    onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))) { implicit request =>
      "EIDR is true" must {
        "display 'Back' button that links to 'Consignee Details' page" in {
          val backButton = createView().getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton must haveHref(ConsigneeDetailsController.displayPage)
        }
      }
    }

    onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.no))) { implicit request =>
      "EIDR is false" must {
        "display 'Back' button that links to 'Other parties' page" in {
          val backButton = createView().getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton must haveHref(AdditionalActorsSummaryController.displayPage)
        }
      }
    }

    "display the expander for Procedure choice" in {

      val expander = createView().getElementById("authorisationProcedureCodeChoice-readMore")
      expander.children.size mustBe 2

      val title = expander.child(0).text
      title mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")

      val link1 = expander.getElementById("link1")
      val link2 = expander.getElementById("link2")

      link1.text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.11.linkText")
      link2.text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.12.linkText")

      val paragraphs = expander.child(1).getElementsByClass("govuk-body")
      paragraphs.size mustBe 11

      val firstParagraph = paragraphs.get(0)
      firstParagraph.text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1")
      paragraphs.get(1).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.2")
      paragraphs.get(2).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.3")
      paragraphs.get(3).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.4", messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.4.linkText"))
      paragraphs.get(4).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.5")
      paragraphs.get(5).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.6", messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.6.linkText"))
      paragraphs.get(6).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.7", messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.7.linkText"))
      paragraphs.get(7).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.8", messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.8.linkText"))
      paragraphs.get(8).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.9", messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.9.linkText"))
      paragraphs.get(9).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.10")
      paragraphs.get(10).text mustBe messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.13", messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.13.linkText"))
    }


  }
}
