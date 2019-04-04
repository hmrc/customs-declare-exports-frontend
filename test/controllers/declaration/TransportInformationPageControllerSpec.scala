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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.TransportInformation
import forms.declaration.TransportInformation.MeansOfTransportTypeCodes.NameOfVessel
import forms.declaration.TransportInformation.ModeOfTransportCodes.Road
import forms.declaration.TransportInformationSpec._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue}
import play.api.test.Helpers._

class TransportInformationPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  import TransportInformationPageControllerSpec._

  private val uri = uriWithContextPath("/declaration/transport-information")

  before {
    authorizedUser()
    withCaching[TransportInformation](None, TransportInformation.id)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Transport Information Page Controller on display" should {

    "display the whole content" in {
      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("supplementary.transportInfo.title"))
      resultAsString must include(messages("supplementary.transportInfo.borderTransportMode.header"))
      resultAsString must include(messages("supplementary.transportInfo.borderTransportMode.header.hint"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.departure.header"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.departure.header.hint"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header"))
      resultAsString must include(
        messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.header.hint")
      )
      resultAsString must include(
        messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.header")
      )
    }

    "display all radio button options" in {
      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("supplementary.transportInfo.transportMode.sea"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.rail"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.road"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.air"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.postalOrMail"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.fixedTransportInstallations"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.inlandWaterway"))
      resultAsString must include(messages("supplementary.transportInfo.transportMode.unknown"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.IMOShipIDNumber"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.nameOfVessel"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.wagonNumber"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.vehicleRegistrationNumber"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.IATAFlightNumber"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.aircraftRegistrationNumber"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.europeanVesselIDNumber"))
      resultAsString must include(messages("supplementary.transportInfo.meansOfTransport.nameOfInlandWaterwayVessel"))
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "display \"Back\" button that links to \"Office-of-exit\" page" in {
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/office-of-exit")
    }
  }

  "TransportInformationPageController on submit" should {

    "display the form page with error" when {

      "no value provided for mode of transport at the border" in {
        val emptyForm = buildTransportInformationForm(
          meansOfTransportOnDepartureType = NameOfVessel,
          meansOfTransportCrossingTheBorderType = NameOfVessel
        )
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(messages("supplementary.transportInfo.borderTransportMode.error.empty"))
      }

      "no value provided for means of transport on departure type" in {
        val emptyForm = buildTransportInformationForm(
          borderModeOfTransportCode = Road,
          meansOfTransportCrossingTheBorderType = NameOfVessel
        )
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messages("supplementary.transportInfo.meansOfTransport.departure.error.empty")
        )
      }

      "means of transport on departure ID number is longer than 27 characters" in {
        val emptyForm =
          buildTransportInformationForm(meansOfTransportOnDepartureIDNumber = "12345678901234567890ABCDEFGH")
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messages("supplementary.transportInfo.meansOfTransport.idNumber.error.length")
        )
      }

      "means of transport on departure ID number contains special characters" in {
        val emptyForm = buildTransportInformationForm(meansOfTransportOnDepartureIDNumber = "123$%&ABC")
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messages("supplementary.transportInfo.meansOfTransport.idNumber.error.specialCharacters")
        )
      }

      "no value provided for means of transport crossing the border type" in {
        val emptyForm = buildTransportInformationForm(
          borderModeOfTransportCode = Road,
          meansOfTransportOnDepartureType = NameOfVessel
        )
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messages("supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.empty")
        )
      }

      "means of transport crossing the border ID number is longer than 35 characters" in {
        val emptyForm = buildTransportInformationForm(
          meansOfTransportCrossingTheBorderIDNumber = "123456789012345678901234567890ABCDEF"
        )
        val result = route(app, postRequest(uri, emptyForm)).get
        contentAsString(result) must include(
          messages("supplementary.transportInfo.meansOfTransport.idNumber.error.length")
        )
      }

      "means of transport crossing the border ID number contains special characters" in {
        val emptyForm = buildTransportInformationForm(meansOfTransportCrossingTheBorderIDNumber = "123$%&ABC")
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messages("supplementary.transportInfo.meansOfTransport.idNumber.error.specialCharacters")
        )
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[TransportInformation](None, TransportInformation.id)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
      route(app, postRequest(uri, correctTransportInformationJSON)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[TransportInformation](any(), ArgumentMatchers.eq(TransportInformation.id), any())(any(), any(), any())
    }

    "redirect to 'Total number of items' page" in {
      val result = route(app, postRequest(uri, correctTransportInformationJSON)).get
      status(result) must be(SEE_OTHER)

      val header = result.futureValue.header

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/add-transport-containers"))
    }
  }

}

object TransportInformationPageControllerSpec {

  def buildTransportInformationForm(
    borderModeOfTransportCode: String = "",
    meansOfTransportOnDepartureType: String = "",
    meansOfTransportOnDepartureIDNumber: String = "",
    meansOfTransportCrossingTheBorderType: String = "",
    meansOfTransportCrossingTheBorderIDNumber: String = "",
    meansOfTransportCrossingTheBorderNationality: String = "",
    container: Boolean = false
  ): JsValue = JsObject(
    Map(
      "borderModeOfTransportCode" -> JsString(borderModeOfTransportCode),
      "meansOfTransportOnDepartureType" -> JsString(meansOfTransportOnDepartureType),
      "meansOfTransportOnDepartureIDNumber" -> JsString(meansOfTransportOnDepartureIDNumber),
      "meansOfTransportCrossingTheBorderType" -> JsString(meansOfTransportCrossingTheBorderType),
      "meansOfTransportCrossingTheBorderIDNumber" -> JsString(meansOfTransportCrossingTheBorderIDNumber),
      "meansOfTransportCrossingTheBorderNationality" -> JsString(meansOfTransportCrossingTheBorderNationality),
      "container" -> JsBoolean(container)
    )
  )

}
