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
import controllers.section2.routes.{DeclarantExporterController, PersonPresentingGoodsDetailsController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori, YesNoAnswer}
import forms.section2.EntityDetails
import forms.section2.exporter.ExporterEoriNumber.form
import forms.section2.consignor.ConsignorDetails
import forms.section2.exporter.ExporterEoriNumber
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import views.components.gds.Styles
import views.html.section2.exporter_eori_number
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class ExporterEoriNumberViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val page: exporter_eori_number = instanceOf[exporter_eori_number]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[ExporterEoriNumber] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  onEveryDeclarationJourney() { implicit request =>
    "ExporterEoriNumber Eori Number View" should {
      val view = createView()
      "display answer input" in {
        val view = createView(form.fill(ExporterEoriNumber(Some(Eori("GB123456789")), YesNoAnswers.yes)))

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.exporterEori.title")
        messages must haveTranslationFor("declaration.exporterEori.eori.label")
        messages must haveTranslationFor("declaration.exporterEori.hasEori.empty")
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

      checkAllSaveButtonsAreDisplayed(createView())

      "handle invalid input" should {
        "display errors when all inputs are incorrect" in {
          val data = ExporterEoriNumber(Some(Eori("123456789")), YesNoAnswers.yes)
          val view = createView(form.fillAndValidate(data))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }

        "display errors when eori contains special characters" in {
          val data = ExporterEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = YesNoAnswers.yes)
          val view = createView(form.fillAndValidate(data))

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

      backButton must containMessage("site.backToPreviousQuestion")
      backButton.getElementById("back-link") must haveHref(PersonPresentingGoodsDetailsController.displayPage)
    }
  }

  onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
    "display 'Back' button that links to the correct page" in {
      val backButton = createView().getElementById("back-link")

      backButton must containMessage("site.backToPreviousQuestion")
      backButton.getElementById("back-link") must haveHref(DeclarantExporterController.displayPage)
    }
  }
}
