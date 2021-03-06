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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.AuthorisationProcedureCodeChoice
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.authorisation_procedure_code_choice

class AuthorisationProcedureCodeChoiceViewSpec extends UnitViewSpec with Stubs with Injector {

  private val page = instanceOf[authorisation_procedure_code_choice]
  private def view(implicit request: JourneyRequest[_]): Document = page(AuthorisationProcedureCodeChoice.form(), Mode.Normal)

  "Which export procedure are you using Page" must {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.title")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.error.empty")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.radio.1040")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.radio.1007")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.radio.other")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.inset.title")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.inset.paragraph1")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.inset.bullet1")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.inset.bullet2")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.inset.bullet3")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.inset.paragraph2")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.2")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.3")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.3.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.4")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.4.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.5")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.5.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.6")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.6.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.7")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.8.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.9.linkText")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.10")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.10.linkText")
    }

    onEveryDeclarationJourney() { implicit request =>
      "display 'Back' button that links to 'Authorisations Required' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(routes.AdditionalActorsSummaryController.displayPage())
      }

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

      "display inset text" in {
        val inset = view.getElementsByClass("govuk-inset-text")
        val expected = Seq(
          messages("declaration.authorisations.procedureCodeChoice.inset.title"),
          messages("declaration.authorisations.procedureCodeChoice.inset.paragraph1"),
          messages("declaration.authorisations.procedureCodeChoice.inset.bullet1"),
          messages("declaration.authorisations.procedureCodeChoice.inset.bullet2"),
          messages("declaration.authorisations.procedureCodeChoice.inset.bullet3"),
          messages("declaration.authorisations.procedureCodeChoice.inset.paragraph2")
        ).mkString(" ")
        inset.get(0) must containText(expected)
      }

      "display non-standard procedures expander" in {
        view.getElementsByClass("govuk-details__summary-text").first() must containHtml(
          messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")
        )
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturn = view.getElementById("submit_and_return")
        saveAndReturn must containMessage("site.save_and_come_back_later")
        saveAndReturn must haveAttribute("name", SaveAndReturn.toString)
      }
    }
  }
}
