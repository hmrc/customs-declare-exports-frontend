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
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.shipOrRoroImoNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.shipOrRoroImoNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.shipOrRoroImoNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber.hint")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.flightNumber")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.flightNumber.label")
        messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.flightNumber.hint")
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
          view.getElementById("shipOrRoroImoNumber").attr("value") mustBe shipOrRoroImoNumber
          view
            .getElementsByAttributeValue("for", "shipOrRoroImoNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.shipOrRoroImoNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$shipOrRoroImoNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.shipOrRoroImoNumber.label"
          )
        }

        "have 'Name of vessel' option" in {
          view.getElementById("nameOfVessel").attr("value") mustBe nameOfVessel
          view
            .getElementsByAttributeValue("for", "nameOfVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfVessel"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$nameOfVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfVessel.label"
          )
        }

        "have 'Wagon number' option" in {
          view.getElementById("wagonNumber").attr("value") mustBe wagonNumber
          view
            .getElementsByAttributeValue("for", "wagonNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.wagonNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$wagonNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.wagonNumber.label"
          )
        }

        "have 'Vehicle number' option" in {
          view.getElementById("vehicleRegistrationNumber").attr("value") mustBe vehicleRegistrationNumber
          view
            .getElementsByAttributeValue("for", "vehicleRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$vehicleRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber.label"
          )
        }

        "have 'flight number' option" in {
          view.getElementById("flightNumber").attr("value") mustBe flightNumber
          view
            .getElementsByAttributeValue("for", "flightNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.flightNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$flightNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.flightNumber.label"
          )
        }

        "have 'aircraft registration' option" in {
          view.getElementById("aircraftRegistrationNumber").attr("value") mustBe aircraftRegistrationNumber
          view
            .getElementsByAttributeValue("for", "aircraftRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$aircraftRegistrationNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber.label"
          )
        }

        "have 'european vessel id' option" in {
          view.getElementById("europeanVesselIDNumber").attr("value") mustBe europeanVesselIDNumber
          view
            .getElementsByAttributeValue("for", "europeanVesselIDNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.europeanVesselIDNumber"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$europeanVesselIDNumber") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.europeanVesselIDNumber.label"
          )
        }

        "have 'inland waterway' option" in {
          view.getElementById("nameOfInlandWaterwayVessel").attr("value") mustBe nameOfInlandWaterwayVessel
          view
            .getElementsByAttributeValue("for", "nameOfInlandWaterwayVessel") must containMessageForElements(
            "declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel"
          )
          view
            .getElementsByAttributeValue("for", s"meansOfTransportOnDepartureIDNumber_$nameOfInlandWaterwayVessel") must containMessageForElements(
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
          view.getElementById("notApplicable").attr("value") mustBe notApplicable
          view
            .getElementsByAttributeValue("for", "notApplicable") must containMessageForElements(
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
