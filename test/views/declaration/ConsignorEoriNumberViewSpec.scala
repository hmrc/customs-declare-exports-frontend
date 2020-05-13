/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.{DeclarantDetails, EntityDetails, ExporterDetails}
import forms.declaration.consignor.ConsignorEoriNumber
import models.DeclarationType.CLEARANCE
import models.declaration.Parties
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.consignor_eori_number
import views.tags.ViewTest

@ViewTest
class ConsignorEoriNumberViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page: consignor_eori_number = instanceOf[consignor_eori_number]

  private def createView(mode: Mode = Mode.Normal, form: Form[ConsignorEoriNumber] = ConsignorEoriNumber.form())(
    implicit journeyRequest: JourneyRequest[_]
  ): Document =
    page(mode, form)(journeyRequest, stubMessages())

  "Consignor Eori Number View" should {

    onJourney(CLEARANCE) { implicit request =>
      "display answer input" in {

        val consignorEoriNumber = ConsignorEoriNumber.form().fill(ConsignorEoriNumber(Some(Eori("GB123456789")), Some(YesNoAnswers.yes)))
        val view = createView(form = consignorEoriNumber)

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {

        val messages = instanceOf[MessagesApi].preferred(journeyRequest(CLEARANCE))
        messages must haveTranslationFor("declaration.consignorEori.title")
        messages must haveTranslationFor("declaration.summary.parties.header")
        messages must haveTranslationFor("declaration.consignorEori.eori.label")
        messages must haveTranslationFor("declaration.consignorEori.hasEori.empty")
        messages must haveTranslationFor("declaration.consignorEori.help-item1")
      }

      "display page title" in {

        createView().getElementsByClass("govuk-fieldset__heading").text() mustBe "declaration.consignorEori.title"
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include("declaration.summary.parties.header")
      }

      "display eori question" in {

        val view = createView()

        view.getElementsByClass("govuk-label--m").text() mustBe "declaration.consignorEori.eori.label"
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Exporter Details' page" in {

        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ExporterDetailsController.displayPage(Mode.Normal))
      }

      "display 'Back' button that links to 'Are you the Exporter' page" in {

        val testEori = "GB1234567890000"
        val cachedParties = Parties(
          declarantDetails = Some(DeclarantDetails(EntityDetails(eori = Some(Eori(testEori)), None))),
          exporterDetails = Some(ExporterDetails(EntityDetails(eori = Some(Eori(testEori)), None)))
        )
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.DeclarantExporterController.displayPage(Mode.Normal))
      }

      "display 'Save and continue' button" in {

        val saveButton = createView().getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button on page" in {

        val saveAndReturnButton = createView().getElementById("submit_and_return")
        saveAndReturnButton.text() mustBe "site.save_and_come_back_later"
      }

      "handle invalid input" should {
        "display errors when all inputs are incorrect" in {

          val data = ConsignorEoriNumber(Some(Eori("123456789")), Some(YesNoAnswers.yes))
          val form = ConsignorEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessage("declaration.eori.error.format")
        }

        "display errors when eori contains special characters" in {

          val data = ConsignorEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = Some(YesNoAnswers.yes))
          val form = ConsignorEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessage("declaration.eori.error.format")
        }
      }
    }
  }
}
