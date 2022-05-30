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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori, YesNoAnswer}
import forms.declaration.EntityDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.exporter.ExporterEoriNumber
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.exporter_eori_number
import views.tags.ViewTest

@ViewTest
class ExporterEoriNumberViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page: exporter_eori_number = instanceOf[exporter_eori_number]

  private def createView(mode: Mode = Mode.Normal, form: Form[ExporterEoriNumber] = ExporterEoriNumber.form())(
    implicit request: JourneyRequest[_]
  ): Document =
    page(mode, form)(request, messages)

  onEveryDeclarationJourney() { implicit request =>
    "ExporterEoriNumber Eori Number View" should {
      val view = createView()
      "display answer input" in {
        val exporterEoriNumber = ExporterEoriNumber.form().fill(ExporterEoriNumber(Some(Eori("GB123456789")), YesNoAnswers.yes))
        val view = createView(form = exporterEoriNumber)

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.exporterEori.title")
        messages must haveTranslationFor("declaration.exporterEori.eori.label")
        messages must haveTranslationFor("declaration.exporterEori.hasEori.empty")

        val titleKey = request.declarationType match {
          case CLEARANCE => "tariff.declaration.areYouTheExporter.clearance.text"
          case _         => "tariff.declaration.areYouTheExporter.common.text"
        }
        messages must haveTranslationFor(titleKey)
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.exporterEori.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display eori question" in {
        view.getElementsByClass("govuk-label") must containMessageForElements("declaration.exporterEori.eori.label")
        view.getElementById("eori").attr("value") mustBe empty
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

      "handle invalid input" should {
        "display errors when all inputs are incorrect" in {
          val data = ExporterEoriNumber(Some(Eori("123456789")), YesNoAnswers.yes)
          val form = ExporterEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }

        "display errors when eori contains special characters" in {
          val data = ExporterEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = YesNoAnswers.yes)
          val form = ExporterEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }
      }
    }
  }

  onJourney(CLEARANCE) { implicit request =>
    "display 'Back' button that links to the correct page" in {
      val cachedParties = Parties(
        isEntryIntoDeclarantsRecords = YesNoAnswer.Yes,
        consignorDetails =
          Some(ConsignorDetails(EntityDetails(None, address = Some(Address("fullName", "addressLine", "townOrCity", "postCode", "country")))))
      )
      val req = journeyRequest(request.cacheModel.copy(parties = cachedParties))

      val view = createView()(req)
      val backButton = view.getElementById("back-link")

      backButton must containMessage("site.back")
      backButton.getElementById("back-link") must haveHref(
        controllers.declaration.routes.PersonPresentingGoodsDetailsController.displayPage(Mode.Normal)
      )
    }
  }

  onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
    "display 'Back' button that links to the correct page" in {
      val backButton = createView().getElementById("back-link")

      backButton must containMessage("site.back")
      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.DeclarantExporterController.displayPage(Mode.Normal))
    }
  }
}
