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

import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.DepartureTransport
import forms.declaration.TransportCodes._
import helpers.views.declaration.CommonMessages
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.components.fields.{field_radio, field_text}
import views.html.declaration.departure_transport
import views.tags.ViewTest

@ViewTest
class BorderTransportViewSpec extends UnitViewSpec with CommonMessages with Stubs {

  val form: Form[DepartureTransport] = DepartureTransport.form()

  private val borderTransportPage = new departure_transport(mainTemplate)

  def createView(form: Form[DepartureTransport] = form): Html =
    borderTransportPage(Mode.Normal, form)(request, messages)

  "BorderTransport View" should {
    val view = createView()

    "have defined translation for used labels" in {
      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("supplementary.transportInfo.title")
      messages must haveTranslationFor("supplementary.transportInfo.title")
      messages must haveTranslationFor(backCaption)
      messages must haveTranslationFor(saveAndContinueCaption)
      messages must haveTranslationFor(saveAndReturnCaption)
      messages must haveTranslationFor("supplementary.transportInfo.borderTransportMode.header.hint")
      messages must haveTranslationFor("supplementary.transportInfo.borderTransportMode.header")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.sea")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.road")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.rail")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.air")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.postalOrMail")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.fixedTransportInstallations")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.inlandWaterway")
      messages must haveTranslationFor("supplementary.transportInfo.transportMode.unknown")

      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.departure.header")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.departure.header.hint")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.nameOfVessel")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.wagonNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.IATAFlightNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel")

      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.reference.header")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.reference.hint")
    }

    "display page title" in {
      view.getElementById("title").text() mustBe messages("supplementary.transportInfo.title")
    }

    "display 'Back' button that links to 'Warehouse' page" in {
      val backButton = view.getElementById("link-back")
      backButton.text() mustBe messages(backCaption)
      backButton must haveHref(routes.WarehouseIdentificationController.displayPage())
    }

    "display 'Save and continue' button on page" in {
      view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val saveAndReturn = view.getElementById("submit_and_return")
      saveAndReturn.text() mustBe messages(saveAndReturnCaption)
      saveAndReturn must haveAttribute("name", SaveAndReturn.toString)
    }

    "display 'Mode of Transport' section" which {

      val section = view.getElementById("borderModeOfTransportCode")

      "have correct hint" in {
        section
          .getElementsByClass("form-hint")
          .text() mustBe messages("supplementary.transportInfo.borderTransportMode.header.hint")
      }

      "have correct legend" in {
        section
          .getElementsByTag("legend")
          .text() must startWith(messages("supplementary.transportInfo.borderTransportMode.header"))
      }

      "have 'Sea' option" in {
        section.getElementById("Border_Sea-label").text() mustBe "supplementary.transportInfo.transportMode.sea"
      }

      "have 'Road' option" in {
        section.getElementById("Border_Road-label").text() mustBe "supplementary.transportInfo.transportMode.road"
      }

      "have 'Rail' option" in {
        section.getElementById("Border_Rail-label").text() mustBe "supplementary.transportInfo.transportMode.rail"
      }

      "have 'Air' option" in {
        section.getElementById("Border_Air-label").text() mustBe "supplementary.transportInfo.transportMode.air"
      }

      "have 'Postal or Mail' option" in {
        section
          .getElementById("Border_PostalOrMail-label")
          .text() mustBe "supplementary.transportInfo.transportMode.postalOrMail"
      }

      "have 'Fixed transport installations' option" in {
        section
          .getElementById("Border_FixedTransportInstallations-label")
          .text() mustBe "supplementary.transportInfo.transportMode.fixedTransportInstallations"
      }

      "have 'Inland waterway transport' option" in {
        section
          .getElementById("Border_InlandWaterway-label")
          .text() mustBe "supplementary.transportInfo.transportMode.inlandWaterway"
      }

      "have 'Mode unknown' option" in {
        section
          .getElementById("Border_Unknown-label")
          .text() mustBe "supplementary.transportInfo.transportMode.unknown"
      }
    }

    "display 'Transport details type' section " which {

      val section = view.getElementById("meansOfTransportOnDepartureType")

      "have label" in {
        section
          .getElementById("meansOfTransportOnDepartureType-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.departure.header"
      }

      "have hint" in {
        section
          .getElementById("meansOfTransportOnDepartureType-hint")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.departure.header.hint"
      }

      "have 'Ship number' option" in {
        section
          .getElementById("Departure_IMOShipIDNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.IMOShipIDNumber"
      }

      "have 'Name of vessel' option" in {
        section
          .getElementById("Departure_NameOfVessel-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.nameOfVessel"
      }

      "have 'Vagon number' option" in {
        section
          .getElementById("Departure_WagonNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.wagonNumber"
      }

      "have 'Vehice number' option" in {
        section
          .getElementById("Departure_VehicleRegistrationNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber"
      }

      "have 'flight number' option" in {
        section
          .getElementById("Departure_IATAFlightNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.IATAFlightNumber"
      }

      "have 'aircraft registration' option" in {
        section
          .getElementById("Departure_AircraftRegistrationNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber"
      }

      "have 'eni code' optopn" in {
        section
          .getElementById("Departure_EuropeanVesselIDNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber"
      }

      "have 'inland waterway' option" in {
        section
          .getElementById("Departure_NameOfInlandWaterwayVessel-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel"
      }
    }

    "display 'Reference' section" which {
      "have label" in {
        view
          .getElementById("meansOfTransportOnDepartureIDNumber-label")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.reference.header"
      }
      "have hint" in {
        view
          .getElementById("meansOfTransportOnDepartureIDNumber-hint")
          .text() mustBe "supplementary.transportInfo.meansOfTransport.reference.hint"
      }
    }
  }
}
