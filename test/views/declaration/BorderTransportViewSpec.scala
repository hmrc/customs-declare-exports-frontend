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
import forms.declaration.BorderTransport
import helpers.views.declaration.CommonMessages
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.border_transport
import views.tags.ViewTest

@ViewTest
class BorderTransportViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector with CommonMessages {

  private val page = new border_transport(mainTemplate)
  private val form: Form[BorderTransport] = BorderTransport.form()
  private val realMessages = validatedMessages

  def borderView(view: Document): Unit = {
    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.transportInfo.active.title")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.nameOfVessel")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.wagonNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.IATAFlightNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel")
      messages must haveTranslationFor("supplementary.transportInfo.meansOfTransport.CrossingTheBorder.IDNumber.header")
    }

    "display page title" in {
      view.getElementById("title") must containText(realMessages("supplementary.transportInfo.active.title"))
    }

    "display 'Save and continue' button on page" in {
      view.getElementById("submit") must containText(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return") must containText(realMessages(saveAndReturnCaption))
    }
  }

  def havingMeansOfTransport(view: Document): Unit =
    "display 'Means of Transport' section" which {
      "nationality picker" in {
        view
          .getElementById("meansOfTransportCrossingTheBorderNationality-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header")
        )
      }
      "has label" in {
        view
          .getElementById("meansOfTransportCrossingTheBorderType-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header")
        )
      }
      "has hint" in {
        view
          .getElementById("meansOfTransportCrossingTheBorderType-hint") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint")
        )
      }
      "has 'Ship' section" in {
        view
          .getElementById("Border_IMOShipIDNumber-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber")
        )
      }
      "has 'Vessel' section" in {
        view
          .getElementById("Border_NameOfVessel-label") must containText(realMessages("supplementary.transportInfo.meansOfTransport.nameOfVessel"))
      }
      "has 'Vagon' section" in {
        view
          .getElementById("Border_WagonNumber-label") must containText(realMessages("supplementary.transportInfo.meansOfTransport.wagonNumber"))
      }
      "has 'Register Vehicle' section" in {
        view
          .getElementById("Border_VehicleRegistrationNumber-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber")
        )
      }
      "has 'Fligh number' section" in {
        view
          .getElementById("Border_IATAFlightNumber-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.IATAFlightNumber")
        )
      }
      "has 'Aircraft Number' section" in {
        view
          .getElementById("Border_AircraftRegistrationNumber-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber")
        )
      }
      "has 'European Vessel' section" in {
        view
          .getElementById("Border_EuropeanVesselIDNumber-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber")
        )
      }
      "has 'Inland waterway vessel' section " in {
        view
          .getElementById("Border_NameOfInlandWaterwayVessel-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel")
        )
      }
      "has Reference input text" in {
        view
          .getElementById("meansOfTransportCrossingTheBorderIDNumber-label") must containText(
          realMessages("supplementary.transportInfo.meansOfTransport.CrossingTheBorder.IDNumber.header")
        )
      }
    }

  private def createView(mode: Mode = Mode.Normal, form: Form[BorderTransport] = form, request: JourneyRequest[_] = journeyRequest()): Document =
    page(mode, form)(request, realMessages)

  "TransportDetails View" when {
    "we are on Standard journey" should {
      val requrestOnStandard = journeyRequest(DeclarationType.STANDARD)

      val view = createView(request = requrestOnStandard)
      "display 'Back' button that links to 'Departure' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containText(realMessages(backCaption))
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage(Mode.Normal))
      }

      behave like borderView(view)
      behave like havingMeansOfTransport(view)
    }

    "we are on Supplementary journey" should {
      val requrestOnSupplementary = journeyRequest(DeclarationType.SUPPLEMENTARY)
      val view = createView(request = requrestOnSupplementary)

      "display 'Back' button that links to 'Departure' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containText(realMessages(backCaption))
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage(Mode.Normal))
      }

      behave like havingMeansOfTransport(view)
      behave like borderView(view)
    }

    "we are on Simplified journey" should {
      val requestOnSimplified = journeyRequest(DeclarationType.SIMPLIFIED)
      val view = createView(request = requestOnSimplified)

      "display 'Back' button that links to 'Supervising Customs Office' page" in {
        val viewForStandard = view
        val backButton = viewForStandard.getElementById("back-link")

        backButton must containText(realMessages(backCaption))
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage(Mode.Normal)
        )
      }
      behave like borderView(view)
      behave like havingMeansOfTransport(view)
    }
  }
}
