/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.declaration.TransportDetails
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.transport_details
import views.tags.ViewTest

@ViewTest
class TransportDetailsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new transport_details(mainTemplate)
  private val form: Form[TransportDetails] = TransportDetails.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[TransportDetails] = form): Document =
    page(mode, form)(journeyRequest(), stubMessages())

  "TransportDetails View" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.transportInfo.active.title")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header")
      messages must haveTranslationFor(
        "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header"
      )
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.nameOfVessel")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.wagonNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.IATAFlightNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.reference.header")
      messages must haveTranslationFor("supplementary.transportInfo.container")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.notPrePaid")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.other")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.accHolder")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.cash")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.creditCard")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.cheque")
      messages must haveTranslationFor("standard.transportDetails.paymentMethod.eFunds")
    }

    "display page title" in {
      view.getElementById("title").text() mustBe "supplementary.transportInfo.active.title"
    }

    "display header" in {
      view.select("legend>h1").text() mustBe "supplementary.transportInfo.active.title"
    }

    "display 'Back' button that links to 'border-transport' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.BorderTransportController.displayPage(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      view.getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }

    "have labels for all fields" in {
      view
        .getElementById("meansOfTransportCrossingTheBorderType-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.crossingTheBorder.header"
      view
        .getElementById("meansOfTransportCrossingTheBorderNationality-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header"
      view
        .getElementById("meansOfTransportCrossingTheBorderType-hint")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint"
      view
        .getElementById("Border_IMOShipIDNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.IMOShipIDNumber"
      view
        .getElementById("Border_NameOfVessel-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.nameOfVessel"
      view
        .getElementById("Border_WagonNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.wagonNumber"
      view
        .getElementById("Border_VehicleRegistrationNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber"
      view
        .getElementById("Border_IATAFlightNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.IATAFlightNumber"
      view
        .getElementById("Border_AircraftRegistrationNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber"
      view
        .getElementById("Border_EuropeanVesselIDNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber"
      view
        .getElementById("Border_NameOfInlandWaterwayVessel-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel"
      view
        .getElementById("meansOfTransportCrossingTheBorderIDNumber-label")
        .text() mustBe "supplementary.transportInfo.meansOfTransport.reference.header"
      view.getElementById("container-label").text() mustBe "supplementary.transportInfo.container"
      view
        .getElementById("standard.transportDetails.paymentMethod.notPrePaid-label")
        .text() mustBe "standard.transportDetails.paymentMethod.notPrePaid"
      view
        .getElementById("standard.transportDetails.paymentMethod.other-label")
        .text() mustBe "standard.transportDetails.paymentMethod.other"
      view
        .getElementById("standard.transportDetails.paymentMethod.accHolder-label")
        .text() mustBe "standard.transportDetails.paymentMethod.accHolder"
      view
        .getElementById("standard.transportDetails.paymentMethod.cash-label")
        .text() mustBe "standard.transportDetails.paymentMethod.cash"
      view
        .getElementById("standard.transportDetails.paymentMethod.creditCard-label")
        .text() mustBe "standard.transportDetails.paymentMethod.creditCard"
      view
        .getElementById("standard.transportDetails.paymentMethod.cheque-label")
        .text() mustBe "standard.transportDetails.paymentMethod.cheque"
      view
        .getElementById("standard.transportDetails.paymentMethod.eFunds-label")
        .text() mustBe "standard.transportDetails.paymentMethod.eFunds"
    }
  }
}
