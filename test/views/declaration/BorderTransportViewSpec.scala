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
import forms.declaration.BorderTransport
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
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

  private val page = instanceOf[border_transport]
  private val form: Form[BorderTransport] = BorderTransport.form()
  private val realMessages = validatedMessages

  def borderView(view: Document): Unit = {
    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.transportInformation.active.title")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.crossingTheBorder.header")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.crossingTheBorder.nationality.header")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.crossingTheBorder.header.hint")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IMOShipIDNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.nameOfVessel")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.wagonNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.vehicleRegistrationNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.IATAFlightNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.aircraftRegistrationNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.europeanVesselIDNumber")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.nameOfInlandWaterwayVessel")
      messages must haveTranslationFor("declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.header")
    }

    "display page title" in {
      view.getElementsByTag("h1").first() must containText(realMessages("declaration.transportInformation.active.title"))
    }

    "display 'Save and continue' button on page" in {
      view.getElementById("submit") must containText(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return") must containText(realMessages(saveAndReturnCaption))
    }
  }

  val havingMeansOfTransport: Document => Unit = (view: Document) => {
    def hasSectionFor(view: Document, transportType: String) = {
      view
        .getElementsByAttributeValue("for", transportType)
        .first() must containText(realMessages(s"declaration.transportInformation.meansOfTransport.$transportType"))
      view
        .getElementsByAttributeValue("for", s"borderTransportReference_$transportType")
        .first() must containText(realMessages(s"declaration.transportInformation.meansOfTransport.$transportType.label"))
    }

    "display 'Means of Transport' section" which {
      "nationality picker" in {
        view
          .getElementById("borderTransportNationality-label") must containText(
          realMessages("declaration.transportInformation.meansOfTransport.crossingTheBorder.nationality.header")
        )
      }
      "has label" in {
        view
          .getElementById("borderTransportType-fieldSet")
          .getElementsByClass("govuk-fieldset__legend")
          .first() must containText(realMessages("declaration.transportInformation.meansOfTransport.crossingTheBorder.header"))
      }
      "has hint" in {
        view
          .getElementById("borderTransportType-hint") must containText(
          realMessages("declaration.transportInformation.meansOfTransport.crossingTheBorder.header.hint")
        )
      }
      "has 'Ship' section" in {
        hasSectionFor(view, "IMOShipIDNumber")
      }
      "has 'Vessel' section" in {
        hasSectionFor(view, "nameOfVessel")
      }
      "has 'Wagon' section" in {
        hasSectionFor(view, "wagonNumber")
      }
      "has 'Register Vehicle' section" in {
        hasSectionFor(view, "vehicleRegistrationNumber")
      }
      "has 'Flight number' section" in {
        hasSectionFor(view, "IATAFlightNumber")
      }
      "has 'Aircraft Number' section" in {
        hasSectionFor(view, "aircraftRegistrationNumber")
      }
      "has 'European Vessel' section" in {
        hasSectionFor(view, "europeanVesselIDNumber")
      }
      "has 'Inland waterway vessel' section " in {
        hasSectionFor(view, "nameOfInlandWaterwayVessel")
      }
    }
  }

  private def createView(mode: Mode = Mode.Normal, form: Form[BorderTransport] = form, request: JourneyRequest[_] = journeyRequest()): Document =
    page(mode, form)(request, realMessages)

  "TransportDetails View" must {

    onStandard { standard =>
      val view = createView(request = standard)
      "display 'Back' button that links to 'Departure' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containText(realMessages(backCaption))
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage(Mode.Normal))
      }

      behave like borderView(view)
      behave like havingMeansOfTransport(view)
    }

    onSupplementary { supplementary =>
      val view = createView(request = supplementary)

      "display 'Back' button that links to 'Departure' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containText(realMessages(backCaption))
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage(Mode.Normal))
      }

      behave like havingMeansOfTransport(view)
      behave like borderView(view)
    }
  }
}
