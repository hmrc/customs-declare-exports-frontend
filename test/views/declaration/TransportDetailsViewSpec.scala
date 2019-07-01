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

import base.TestHelper._
import forms.Choice.AllowedChoiceValues
import forms.declaration.TransportCodes._
import forms.declaration.TransportDetails
import helpers.views.declaration.CommonMessages
import play.api.data.Form
import play.twirl.api.Html
import services.Countries
import services.model.AutoCompleteItem
import utils.RadioOption
import views.declaration.spec.ViewSpec
import views.html.components.{input_radio, input_text}
import views.html.components.fields.field_autocomplete
import views.html.declaration.transport_details
import views.tags.ViewTest

@ViewTest
class TransportDetailsViewSpec extends TransportDetailsFields with CommonMessages {

  def createView(form: Form[TransportDetails] = form): Html =
    transport_details(form)(journeyRequest(fakeRequest, AllowedChoiceValues.StandardDec), appConfig, messages)

  "TransportDetails View" should {

    "display page title" in {
      val view = createView()

      getElementById(view, "title").text() must be(messages("supplementary.transportInfo.title"))
    }

    "display header" in {
      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages("supplementary.transportInfo.title"))
    }

    "display 'Back' button that links to 'border-transport' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/border-transport")
    }

    "display 'Save and continue' button on page" in {
      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "have labels for all fields" in {
      val view = createView()

      view.body must include(meansOfTransportCrossingTheBorderNationality)
      view.body must include(meansOfTransportCrossingTheBorderType)
      view.body must include(meansOfTransportCrossingTheBorderIDNumber)
      view.body must include(container)
      view.body must include(paymentMethod)
    }
  }

}

trait TransportDetailsFields extends ViewSpec {
  val form: Form[TransportDetails] = TransportDetails.form()

  val meansOfTransportCrossingTheBorderNationality = field_autocomplete(
    form("meansOfTransportCrossingTheBorderNationality"),
    "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header",
    None,
    messages("declaration.destinationCountries.countriesOfRouting.empty"),
    AutoCompleteItem.from(Countries.allCountries),
    'autocomplete -> "off",
    '_inputClass -> "form-control form-control--block",
    'spellcheck -> "false",
    'ariaautocomplete -> "list",
    'ariahaspopup -> "true",
    'ariaowns -> "suggestions-list",
    'ariaactivedescendant -> "true",
    'otherErrorFields -> Seq("countryCode")
  ).body

  val meansOfTransportCrossingTheBorderType = input_radio(
    field = form("meansOfTransportCrossingTheBorderType"),
    legend = "7/14 What was the active means of transport",
    hint = Some(messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint")),
    inputs = Seq(
      RadioOption(
        "Border_IMOShipIDNumber",
        IMOShipIDNumber,
        messages("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber")
      ),
      RadioOption(
        "Border_NameOfVessel",
        NameOfVessel,
        messages("supplementary.transportInfo.meansOfTransport.nameOfVessel")
      ),
      RadioOption(
        "Border_WagonNumber",
        WagonNumber,
        messages("supplementary.transportInfo.meansOfTransport.wagonNumber")
      ),
      RadioOption(
        "Border_VehicleRegistrationNumber",
        VehicleRegistrationNumber,
        messages("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber")
      ),
      RadioOption(
        "Border_IATAFlightNumber",
        IATAFlightNumber,
        messages("supplementary.transportInfo.meansOfTransport.IATAFlightNumber")
      ),
      RadioOption(
        "Border_AircraftRegistrationNumber",
        AircraftRegistrationNumber,
        messages("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber")
      ),
      RadioOption(
        "Border_EuropeanVesselIDNumber",
        EuropeanVesselIDNumber,
        messages("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber")
      ),
      RadioOption(
        "Border_NameOfInlandWaterwayVessel",
        NameOfInlandWaterwayVessel,
        messages("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel")
      )
    )
  ).body

  val meansOfTransportCrossingTheBorderIDNumber =
    input_text(field = form("meansOfTransportCrossingTheBorderIDNumber"), label = "Reference").body

  val container = input_radio(
    field = form("container"),
    legend = "7/2 Were the goods in a container?",
    inputs = Seq(RadioOption("Yes", "true", messages("site.yes")), RadioOption("No", "false", messages("site.no")))
  ).body

  val paymentMethod = input_radio(
    field = form("paymentMethod"),
    legend = "4/2 Enter transport charges method of payment",
    inputs = paymentMethods.toSeq.map { case (a, b) => RadioOption(messages(b), a, messages(b)) }
  ).body
}
