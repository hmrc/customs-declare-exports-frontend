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
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import unit.tools.Stubs
import views.components.inputs.RadioOption
import views.declaration.spec.UnitViewSpec
import views.html.components.fields.{field_radio, field_text}
import views.html.declaration.departure_transport
import views.tags.ViewTest

@ViewTest
class DepartureTransportViewSpec extends UnitViewSpec with CommonMessages with Stubs {

  val form: Form[DepartureTransport] = DepartureTransport.form()

  private val borderTransportPage = new departure_transport(mainTemplate)

  def createView(form: Form[DepartureTransport] = form): Html =
    borderTransportPage(Mode.Normal, form)(request, messages)

  "BorderTransport View" should {
    val view = createView()

    "have defined translation for used labels" in {
      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.transportInformation.title")
      messages must haveTranslationFor("declaration.transportInformation.title")
      messages must haveTranslationFor(backCaption)
      messages must haveTranslationFor(saveAndContinueCaption)
      messages must haveTranslationFor(saveAndReturnCaption)
      messages must haveTranslationFor("declaration.transportInformation.borderTransportMode.header.hint")
      messages must haveTranslationFor("declaration.transportInformation.borderTransportMode.header")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.sea")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.road")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.rail")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.air")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.postalOrMail")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.fixedTransportInstallations")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.inlandWaterway")
      messages must haveTranslationFor("declaration.transportInformation.transportMode.unknown")

      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.departure.header")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.departure.header.hint")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.nameOfVessel")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IATAFlightNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.europeanVesselIDNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel")

      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.reference.header")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.reference.hint")
    }

    "display page title" in {
      view.getElementById("title").text() mustBe messages("declaration.transportInformation.title")
    }

    "display 'Back' button that links to 'Inland Transport Details' page" in {
      val backButton = view.getElementById("link-back")
      backButton.text() mustBe messages(backCaption)
      backButton must haveHref(routes.InlandTransportDetailsController.displayPage())
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
          .text() mustBe messages("declaration.transportInformation.borderTransportMode.header.hint")
      }

      "have correct legend" in {
        section
          .getElementsByTag("legend")
          .text() must startWith(messages("declaration.transportInformation.borderTransportMode.header"))
      }

      "have 'Sea' option" in {
        section.getElementById("Border_Sea-label").text() mustBe "declaration.transportInformation.transportMode.sea"
      }

      "have 'Road' option" in {
        section.getElementById("Border_Road-label").text() mustBe "declaration.transportInformation.transportMode.road"
      }

      "have 'Rail' option" in {
        section.getElementById("Border_Rail-label").text() mustBe "declaration.transportInformation.transportMode.rail"
      }

      "have 'Air' option" in {
        section.getElementById("Border_Air-label").text() mustBe "declaration.transportInformation.transportMode.air"
      }

      "have 'Postal or Mail' option" in {
        section
          .getElementById("Border_PostalOrMail-label")
          .text() mustBe "declaration.transportInformation.transportMode.postalOrMail"
      }

      "have 'Fixed transport installations' option" in {
        section
          .getElementById("Border_FixedTransportInstallations-label")
          .text() mustBe "declaration.transportInformation.transportMode.fixedTransportInstallations"
      }

      "have 'Inland waterway transport' option" in {
        section
          .getElementById("Border_InlandWaterway-label")
          .text() mustBe "declaration.transportInformation.transportMode.inlandWaterway"
      }

      "have 'Mode unknown' option" in {
        section
          .getElementById("Border_Unknown-label")
          .text() mustBe "declaration.transportInformation.transportMode.unknown"
      }
    }

    "display 'Transport details type' section " which {

      val section = view.getElementById("meansOfTransportOnDepartureType")

      "have label" in {
        section
          .getElementById("meansOfTransportOnDepartureType-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.departure.header"
      }

      "have hint" in {
        section
          .getElementById("meansOfTransportOnDepartureType-hint")
          .text() mustBe "declaration.transportInformation.meansOfTransport.departure.header.hint"
      }

      "have 'Ship number' option" in {
        section
          .getElementById("Departure_IMOShipIDNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.IMOShipIDNumber"
      }

      "have 'Name of vessel' option" in {
        section
          .getElementById("Departure_NameOfVessel-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.nameOfVessel"
      }

      "have 'Vagon number' option" in {
        section
          .getElementById("Departure_WagonNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.wagonNumber"
      }

      "have 'Vehice number' option" in {
        section
          .getElementById("Departure_VehicleRegistrationNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber"
      }

      "have 'flight number' option" in {
        section
          .getElementById("Departure_IATAFlightNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.IATAFlightNumber"
      }

      "have 'aircraft registration' option" in {
        section
          .getElementById("Departure_AircraftRegistrationNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber"
      }

      "have 'eni code' optopn" in {
        section
          .getElementById("Departure_EuropeanVesselIDNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.europeanVesselIDNumber"
      }

      "have 'inland waterway' option" in {
        section
          .getElementById("Departure_NameOfInlandWaterwayVessel-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel"
      }
    }

    "display 'Reference' section" which {
      "have label" in {
        view
          .getElementById("meansOfTransportOnDepartureIDNumber-label")
          .text() mustBe "declaration.transportInformation.meansOfTransport.reference.header"
      }
      "have hint" in {
        view
          .getElementById("meansOfTransportOnDepartureIDNumber-hint")
          .text() mustBe "declaration.transportInformation.meansOfTransport.reference.hint"
      }
    }
  }
}
