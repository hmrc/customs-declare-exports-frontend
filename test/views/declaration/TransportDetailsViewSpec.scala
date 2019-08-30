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

  val form: Form[TransportDetails] = TransportDetails.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[TransportDetails] = form): Document =
    new transport_details(mainTemplate)(mode, form)(journeyRequest, stubMessages())

  "TransportDetails View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest)
      messages("supplementary.transportInfo.active.title") mustBe "Active transport details"
      messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header") mustBe "What is the active means of transport"
      messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header") mustBe "What nationality is the active means of transport?"
      messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint") mustBe "Select the type of identification used for the chosen transport and enter the reference"
      messages("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber") mustBe "IMO Ship identification number"
      messages("supplementary.transportInfo.meansOfTransport.nameOfVessel") mustBe "Name of the seagoing vessel"
      messages("supplementary.transportInfo.meansOfTransport.wagonNumber") mustBe "Wagon number"
      messages("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber") mustBe "Vehicle registration number"
      messages("supplementary.transportInfo.meansOfTransport.IATAFlightNumber") mustBe "IATA flight number"
      messages("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber") mustBe "Aircrafts registration number"
      messages("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber") mustBe "European vessel identification number (ENI code)"
      messages("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel") mustBe "Name of the inland waterway’s vessel"
      messages("supplementary.transportInfo.meansOfTransport.reference.header") mustBe "Reference"
      messages("supplementary.transportInfo.container") mustBe "Were the goods in a container?"
      messages("standard.transportDetails.paymentMethod.notPrePaid") mustBe "Not pre-paid"
      messages("standard.transportDetails.paymentMethod.other") mustBe "Other (e.g. Direct debit to cash account)"
      messages("standard.transportDetails.paymentMethod.accHolder") mustBe "Account holder with carrier"
      messages("standard.transportDetails.paymentMethod.cash") mustBe "Payment in cash"
      messages("standard.transportDetails.paymentMethod.creditCard") mustBe "Payment by credit card"
      messages("standard.transportDetails.paymentMethod.cheque") mustBe "Payment by cheque"
      messages("standard.transportDetails.paymentMethod.eFunds") mustBe "Electronic funds transfer"
    }

    "display page title" in {
      val view = createView()

      view.getElementById("title").text() mustBe "supplementary.transportInfo.active.title"
    }

    "display header" in {
      val view = createView()

      view.select("legend>h1").text() mustBe "supplementary.transportInfo.active.title"
    }

    "display 'Back' button that links to 'border-transport' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.BorderTransportController.displayPage(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      createView().getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      createView().getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }

    "have labels for all fields" in {
      val view = createView()

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
