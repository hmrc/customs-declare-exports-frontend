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

package forms.declaration

import forms.declaration.TransportInformation.MeansOfTransportTypeCodes.NameOfVessel
import forms.declaration.TransportInformation.ModeOfTransportCodes.Road
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue}

class TransportInformationSpec extends WordSpec with MustMatchers {
  import TransportInformationSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val transportInformation = correctTransportInformation
      val expectedTransportInformationProperties: Map[String, String] = Map(
        "declaration.borderTransportMeans.modeCode" -> transportInformation.borderModeOfTransportCode,
        "declaration.goodsShipment.consignment.departureTransportMeans.identificationTypeCode" -> transportInformation.meansOfTransportOnDepartureType,
        "declaration.goodsShipment.consignment.departureTransportMeans.id" -> transportInformation.meansOfTransportOnDepartureIDNumber.get,
        "declaration.borderTransportMeans.identificationTypeCode" -> transportInformation.meansOfTransportCrossingTheBorderType,
        "declaration.borderTransportMeans.id" -> transportInformation.meansOfTransportCrossingTheBorderIDNumber.get,
        "declaration.borderTransportMeans.registrationNationalityCode" -> "GB",
        "declaration.goodsShipment.consignment.containerCode" -> "1"
      )

      transportInformation.toMetadataProperties() must equal(expectedTransportInformationProperties)
    }
  }

  "TransportInformation mapping used for binding data" should {

    "return form with errors for borderModeOfTransportCode" when {
      "provided with empty input for the field" in {
        val transportInformationInputData = JsObject(
          Map(
            "meansOfTransportOnDepartureType" -> JsString(NameOfVessel),
            "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel)
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("supplementary.transportInfo.borderTransportMode.error.empty")
      }

      "provided with a value not defined in ModeOfTransportCodes for the field" in {
        val transportInformationInputData = JsObject(
          Map(
            "borderModeOfTransportCode" -> JsString("Invalid"),
            "meansOfTransportOnDepartureType" -> JsString(NameOfVessel),
            "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel)
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("supplementary.transportInfo.borderTransportMode.error.incorrect")
      }
    }

    "return form with errors for meansOfTransportOnDepartureType" when {
      "provided with empty input for the field" in {
        val transportInformationInputData = JsObject(
          Map(
            "borderModeOfTransportCode" -> JsString(Road),
            "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel)
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("supplementary.transportInfo.meansOfTransport.departure.error.empty")
      }

      "provided with a value not defined in MeansOfTransportTypeCodes for the field" in {
        val transportInformationInputData = JsObject(
          Map(
            "borderModeOfTransportCode" -> JsString(Road),
            "meansOfTransportOnDepartureType" -> JsString("Invalid"),
            "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel)
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("supplementary.transportInfo.meansOfTransport.departure.error.incorrect")
      }
    }

    "return form with errors for meansOfTransportCrossingTheBorderType" when {
      "provided with empty input for the field" in {
        val transportInformationInputData = JsObject(
          Map(
            "borderModeOfTransportCode" -> JsString(Road),
            "meansOfTransportOnDepartureType" -> JsString(NameOfVessel)
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.empty"
        )
      }

      "provided with a value not defined in MeansOfTransportTypeCodes for the field" in {
        val transportInformationInputData = JsObject(
          Map(
            "borderModeOfTransportCode" -> JsString(Road),
            "meansOfTransportOnDepartureType" -> JsString(NameOfVessel),
            "meansOfTransportCrossingTheBorderType" -> JsString("Invalid")
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.incorrect"
        )
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val transportInformationInputData = JsObject(
          Map(
            "borderModeOfTransportCode" -> JsString(Road),
            "meansOfTransportOnDepartureType" -> JsString(NameOfVessel),
            "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel)
          )
        )
        val form = TransportInformation.form().bind(transportInformationInputData)

        form.hasErrors must be(false)
      }
    }
  }

}

object TransportInformationSpec {
  val correctTransportInformation = TransportInformation(
    borderModeOfTransportCode = Road,
    meansOfTransportOnDepartureType = NameOfVessel,
    meansOfTransportOnDepartureIDNumber = Some("123ABC"),
    meansOfTransportCrossingTheBorderType = NameOfVessel,
    meansOfTransportCrossingTheBorderIDNumber = Some("QWERTY"),
    meansOfTransportCrossingTheBorderNationality = Some("United Kingdom"),
    container = true
  )
  val emptyTransportInformation = TransportInformation(
    borderModeOfTransportCode = "",
    meansOfTransportOnDepartureType = "",
    meansOfTransportOnDepartureIDNumber = None,
    meansOfTransportCrossingTheBorderType = "",
    meansOfTransportCrossingTheBorderIDNumber = None,
    meansOfTransportCrossingTheBorderNationality = None,
    container = false
  )

  val correctTransportInformationJSON: JsValue = JsObject(
    Map(
      "borderModeOfTransportCode" -> JsString(Road),
      "meansOfTransportOnDepartureType" -> JsString(NameOfVessel),
      "meansOfTransportOnDepartureIDNumber" -> JsString("123ABC"),
      "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel),
      "meansOfTransportCrossingTheBorderIDNumber" -> JsString("QWERTY"),
      "meansOfTransportCrossingTheBorderNationality" -> JsString("United Kingdom"),
      "container" -> JsBoolean(true)
    )
  )
  val emptyTransportInformationJSON: JsValue = JsObject(
    Map(
      "borderModeOfTransportCode" -> JsString(""),
      "meansOfTransportOnDepartureType" -> JsString(""),
      "meansOfTransportOnDepartureIDNumber" -> JsString(""),
      "meansOfTransportCrossingTheBorderType" -> JsString(""),
      "meansOfTransportCrossingTheBorderIDNumber" -> JsString(""),
      "meansOfTransportCrossingTheBorderNationality" -> JsString(""),
      "container" -> JsBoolean(false)
    )
  )
}
