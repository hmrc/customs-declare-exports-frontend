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
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.carrier.CarrierEoriNumber
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.carrier_eori_number
import views.tags.ViewTest

@ViewTest
class CarrierEoriNumberViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page: carrier_eori_number = instanceOf[carrier_eori_number]

  private def createView(mode: Mode = Mode.Normal, form: Form[CarrierEoriNumber] = CarrierEoriNumber.form())(
    implicit request: JourneyRequest[_]
  ): Document =
    page(mode, form)(request, messages)

  "Carrier Eori Number View" should {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView()
      "display answer input" in {
        val carrierEoriNumber = CarrierEoriNumber.form().fill(CarrierEoriNumber(Some(Eori("GB123456789")), YesNoAnswers.yes))
        val view = createView(form = carrierEoriNumber)

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.carrierEori.title")
        messages must haveTranslationFor("declaration.carrierEori.eori.label")
        messages must haveTranslationFor("declaration.carrierEori.hasEori.empty")
        messages must haveTranslationFor("tariff.declaration.locationOfGoods.clearance.text")
      }

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.carrierEori.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display eori question" in {
        view.getElementsByClass("govuk-label") must containMessageForElements("declaration.carrierEori.eori.label")
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Exporter Details' page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.RepresentativeStatusController.displayPage(Mode.Normal))
      }

      "display 'Save and continue' button" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage("site.save_and_come_back_later")
      }

      "handle invalid input" should {
        "display errors when all inputs are incorrect" in {
          val data = CarrierEoriNumber(Some(Eori("123456789")), YesNoAnswers.yes)
          val form = CarrierEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }

        "display errors when eori contains special characters" in {
          val data = CarrierEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = YesNoAnswers.yes)
          val form = CarrierEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }
      }
    }
  }
}
