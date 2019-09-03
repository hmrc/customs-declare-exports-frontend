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
import forms.declaration.BorderTransport
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
import views.html.declaration.border_transport
import views.tags.ViewTest

@ViewTest
class BorderTransportViewSpec extends BorderTransportFields with CommonMessages with Stubs {

  private val borderTransportPage = new border_transport(mainTemplate)
  def createView(form: Form[BorderTransport] = form): Html =
    borderTransportPage(Mode.Normal, form)(request, messages)

  "BorderTransport View" should {

    "display page title" in {
      val view = createView()

      view.getElementById("title").text() mustBe messages("supplementary.transportInfo.title")
    }

    "display header" in {
      val view = createView()

      view.select("legend>h1").text() mustBe messages("supplementary.transportInfo.title")
    }

    "display 'Back' button that links to 'Warehouse' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.WarehouseIdentificationController.displayPage().url
    }

    "display 'Save and continue' button on page" in {
      val view: Document = createView()
      view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val view: Document = createView()
      view.getElementById("submit_and_return").text() mustBe messages(saveAndReturnCaption)
      view.getElementById("submit_and_return").attr("name") mustBe SaveAndReturn.toString
    }

    "have labels for all fields" in {
      val view = createView()
      view
        .getElementById("borderModeOfTransportCode")
        .getElementsByClass("form-hint")
        .text() mustBe messages("supplementary.transportInfo.borderTransportMode.header.hint")

      view
        .getElementById("borderModeOfTransportCode")
        .getElementsByTag("legend")
        .text()
        .startsWith(messages("supplementary.transportInfo.borderTransportMode.header")) mustBe true

      view.getElementById("Border_Sea-label").text() mustBe "supplementary.transportInfo.transportMode.sea"
      view.getElementById("Border_Road-label").text() mustBe "supplementary.transportInfo.transportMode.road"
      view.getElementById("Border_Rail-label").text() mustBe "supplementary.transportInfo.transportMode.rail"
      view.getElementById("Border_Air-label").text() mustBe "supplementary.transportInfo.transportMode.air"
      view
        .getElementById("Border_PostalOrMail-label")
        .text() mustBe "supplementary.transportInfo.transportMode.postalOrMail"

      view.body must include(expBorderModeOfTransportCode.body)
      view.body must include(expMeansOfTransportOnDepartureType.body)
      view.body must include(expMeansOfTransportOnDepartureIDNumber.body)
    }
  }
}

trait BorderTransportFields extends UnitViewSpec {
  val form: Form[BorderTransport] = BorderTransport.form()

  val expBorderModeOfTransportCode = field_radio(
    field = form("borderModeOfTransportCode"),
    legend = messages("supplementary.transportInfo.borderTransportMode.header"),
    hint = Some(messages("supplementary.transportInfo.borderTransportMode.header.hint")),
    inputs = Seq(
      RadioOption("Border_Sea", Maritime, messages("supplementary.transportInfo.transportMode.sea")),
      RadioOption("Border_Rail", Rail, messages("supplementary.transportInfo.transportMode.rail")),
      RadioOption("Border_Road", Road, messages("supplementary.transportInfo.transportMode.road")),
      RadioOption("Border_Air", Air, messages("supplementary.transportInfo.transportMode.air")),
      RadioOption(
        "Border_PostalOrMail",
        PostalConsignment,
        messages("supplementary.transportInfo.transportMode.postalOrMail")
      ),
      RadioOption(
        "Border_FixedTransportInstallations",
        FixedTransportInstallations,
        messages("supplementary.transportInfo.transportMode.fixedTransportInstallations")
      ),
      RadioOption(
        "Border_InlandWaterway",
        InlandWaterway,
        messages("supplementary.transportInfo.transportMode.inlandWaterway")
      ),
      RadioOption("Border_Unknown", Unknown, messages("supplementary.transportInfo.transportMode.unknown"))
    )
  )

  val expMeansOfTransportOnDepartureType = field_radio(
    field = form("meansOfTransportOnDepartureType"),
    legend = messages("supplementary.transportInfo.meansOfTransport.departure.header"),
    hint = Some(messages("supplementary.transportInfo.meansOfTransport.departure.header.hint")),
    inputs = Seq(
      RadioOption(
        "Departure_IMOShipIDNumber",
        IMOShipIDNumber,
        messages("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber")
      ),
      RadioOption(
        "Departure_NameOfVessel",
        NameOfVessel,
        messages("supplementary.transportInfo.meansOfTransport.nameOfVessel")
      ),
      RadioOption(
        "Departure_WagonNumber",
        WagonNumber,
        messages("supplementary.transportInfo.meansOfTransport.wagonNumber")
      ),
      RadioOption(
        "Departure_VehicleRegistrationNumber",
        VehicleRegistrationNumber,
        messages("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber")
      ),
      RadioOption(
        "Departure_IATAFlightNumber",
        IATAFlightNumber,
        messages("supplementary.transportInfo.meansOfTransport.IATAFlightNumber")
      ),
      RadioOption(
        "Departure_AircraftRegistrationNumber",
        AircraftRegistrationNumber,
        messages("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber")
      ),
      RadioOption(
        "Departure_EuropeanVesselIDNumber",
        EuropeanVesselIDNumber,
        messages("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber")
      ),
      RadioOption(
        "Departure_NameOfInlandWaterwayVessel",
        NameOfInlandWaterwayVessel,
        messages("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel")
      )
    )
  )

  val expMeansOfTransportOnDepartureIDNumber = field_text(
    field = form("meansOfTransportOnDepartureIDNumber"),
    label = messages("supplementary.transportInfo.meansOfTransport.reference.header")
  )

}
