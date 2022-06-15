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
import forms.common.YesNoAnswer._
import forms.declaration.{AuthorisationProcedureCodeChoice, NactCode}
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.zero_rated_for_vat

class ZeroRatedForVatViewSpec extends UnitViewSpec with Stubs with Injector {

  private val itemId = "itemId"
  private val page = instanceOf[zero_rated_for_vat]
  private def createView(mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, itemId, NactCode.form())

  "Which export procedure are you using Page" must {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.authorisations.zeroRatedForVat.title")
      messages must haveTranslationFor("declaration.authorisations.zeroRatedForVat.error.empty")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedYes")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedReduced")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedReduced.hint")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedExempt")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedExempt")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedExempt.hint")
      messages must haveTranslationFor(
        "declaration.zeroRatedForVat.radio.VatZeroRatedExempt.hintdeclaration.zeroRatedForVat.radio.VatZeroRatedExempt.hint"
      )
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1")
      messages must haveTranslationFor("declaration.authorisations.procedureCodeChoice.readMoreExpander.paragraph.1.linkText")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.zeroRatedForVat.title")
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

      "display expander" in {
        view.getElementsByClass("govuk-details__summary-text").first() must containHtml(
          messages("declaration.authorisations.procedureCodeChoice.readMoreExpander.header")
        )
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Authorisations Required' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(routes.AdditionalActorsSummaryController.displayPage())
      }

    }

    onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))) { implicit request =>
      val view = createView()

      "EIDR is true" must {
        "display 'Back' button that links to 'Consignee Details' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.back")
          backButton must haveHref(routes.ConsigneeDetailsController.displayPage())
        }
      }
    }

    onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.no))) { implicit request =>
      val view = createView()

      "EIDR is false" must {
        "display 'Back' button that links to 'Other parties' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.back")
          backButton must haveHref(routes.AdditionalActorsSummaryController.displayPage())
        }
      }
    }
  }
}
