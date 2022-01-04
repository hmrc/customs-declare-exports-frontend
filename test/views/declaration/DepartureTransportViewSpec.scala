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

import base.ExportsTestData.itemWith1040AsPC
import base.Injector
import controllers.declaration.routes
import controllers.helpers.SaveAndReturn
import forms.declaration.DepartureTransport
import forms.declaration.TransportCodes._
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.twirl.api.Html
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.departure_transport
import views.tags.ViewTest

@ViewTest
class DepartureTransportViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val borderTransportPage = instanceOf[departure_transport]

  def createView(form: Option[Form[DepartureTransport]] = None)(implicit request: JourneyRequest[_]): Html =
    borderTransportPage(Mode.Normal, form.getOrElse(DepartureTransport.form(request.declarationType)))(request, messages)

  "Departure Transport View" must {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "have defined translation for used labels" in {
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.departure.title")
        messages must haveTranslationFor(backCaption)
        messages must haveTranslationFor(saveAndContinueCaption)
        messages must haveTranslationFor(saveAndReturnCaption)

        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.departure.title")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IATAFlightNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IATAFlightNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IATAFlightNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.europeanVesselIDNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.europeanVesselIDNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.europeanVesselIDNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel")

        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.reference.header")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.reference.hint")
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend).text() mustBe messages("declaration.transportInformation.meansOfTransport.departure.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveAndReturn = view.getElementById("submit_and_return")
        saveAndReturn.text() mustBe messages(saveAndReturnCaption)
        saveAndReturn must haveAttribute("name", SaveAndReturn.toString)
      }

      "display 'Transport details type' radio section " which {

        "have 'Ship number' option" in {
          view.getElementById("Departure_IMOShipIDNumber").attr("value") mustBe IMOShipIDNumber
          view
            .getElementsByAttributeValue("for", "Departure_IMOShipIDNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.IMOShipIDNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$IMOShipIDNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.IMOShipIDNumber.label"
          )
        }

        "have 'Name of vessel' option" in {
          view.getElementById("Departure_NameOfVessel").attr("value") mustBe NameOfVessel
          view
            .getElementsByAttributeValue("for", "Departure_NameOfVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfVessel"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$NameOfVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfVessel.label"
          )
        }

        "have 'Wagon number' option" in {
          view.getElementById("Departure_WagonNumber").attr("value") mustBe WagonNumber
          view
            .getElementsByAttributeValue("for", "Departure_WagonNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.wagonNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$WagonNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.wagonNumber.label"
          )
        }

        "have 'Vehicle number' option" in {
          view.getElementById("Departure_VehicleRegistrationNumber").attr("value") mustBe VehicleRegistrationNumber
          view
            .getElementsByAttributeValue("for", "Departure_VehicleRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$VehicleRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber.label"
          )
        }

        "have 'flight number' option" in {
          view.getElementById("Departure_IATAFlightNumber").attr("value") mustBe IATAFlightNumber
          view
            .getElementsByAttributeValue("for", "Departure_IATAFlightNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.IATAFlightNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$IATAFlightNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.IATAFlightNumber.label"
          )
        }

        "have 'aircraft registration' option" in {
          view.getElementById("Departure_AircraftRegistrationNumber").attr("value") mustBe AircraftRegistrationNumber
          view
            .getElementsByAttributeValue("for", "Departure_AircraftRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$AircraftRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber.label"
          )
        }

        "have 'european vessel id' option" in {
          view.getElementById("Departure_EuropeanVesselIDNumber").attr("value") mustBe EuropeanVesselIDNumber
          view
            .getElementsByAttributeValue("for", "Departure_EuropeanVesselIDNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.europeanVesselIDNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$EuropeanVesselIDNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.europeanVesselIDNumber.label"
          )
        }

        "have 'inland waterway' option" in {
          view.getElementById("Departure_NameOfInlandWaterwayVessel").attr("value") mustBe NameOfInlandWaterwayVessel
          view
            .getElementsByAttributeValue("for", "Departure_NameOfInlandWaterwayVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$NameOfInlandWaterwayVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel.label"
          )
        }
      }
    }

    onJourney(SUPPLEMENTARY, STANDARD) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Inland Transport Details' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.InlandTransportDetailsController.displayPage())
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Supervising Customs Office' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.SupervisingCustomsOfficeController.displayPage())
      }

      "display radio section " which {

        "has 'none' option" in {
          view.getElementById("Departure_NotApplicable").attr("value") mustBe OptionNone
          view
            .getElementsByAttributeValue("for", "Departure_NotApplicable") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.notApplicable"
          )
        }
      }
    }

    onJourney(CLEARANCE)(aDeclaration(withEntryIntoDeclarantsRecords(), withItem(itemWith1040AsPC))) { implicit request =>
      "display 'Back' button to the 'Warehouse' page" when {
        "declaration is EIDR and all declaration's items have '1040' as PC and '000' as unique APC" in {
          val view = createView()
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(routes.WarehouseIdentificationController.displayPage())
        }
      }
    }
  }
}
