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

package forms.supplementary

import forms.supplementary.TransportInformation.MeansOfTransportTypeCodes.NameOfVessel
import forms.supplementary.TransportInformation.ModeOfTransportCodes.Road
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue}

class TransportInformationSpec extends WordSpec with MustMatchers {
  import TransportInformationSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val transportInformation = correctTransportInformation
      val expectedTransportInformationProperties: Map[String, String] = Map(
        "declaration.goodsShipment.consignment.arrivalTransportMeans.modeCode" -> transportInformation.inlandModeOfTransportCode.get,
        "declaration.borderTransportMeans.modeCode" -> transportInformation.borderModeOfTransportCode,
        "declaration.goodsShipment.consignment.departureTransportMeans.identificationTypeCode" -> transportInformation.meansOfTransportOnDepartureType,
        "declaration.goodsShipment.consignment.departureTransportMeans.id" -> transportInformation.meansOfTransportOnDepartureIDNumber.get,
        "declaration.borderTransportMeans.identificationTypeCode" -> transportInformation.meansOfTransportCrossingTheBorderType,
        "declaration.borderTransportMeans.id" -> transportInformation.meansOfTransportCrossingTheBorderIDNumber.get,
        "declaration.borderTransportMeans.registrationNationalityCode" -> "GB",
        "declaration.goodsShipment.consignment.containerCode" -> "1",
        "declaration.goodsShipment.governmentAgencyGoodsItem.commodity.transportEquipment.id" -> transportInformation.containerId.get
      )

      transportInformation.toMetadataProperties() must equal(expectedTransportInformationProperties)
    }
  }

}

object TransportInformationSpec {
  val correctTransportInformation = TransportInformation(
    inlandModeOfTransportCode = Some(Road),
    borderModeOfTransportCode = Road,
    meansOfTransportOnDepartureType = NameOfVessel,
    meansOfTransportOnDepartureIDNumber = Some("123ABC"),
    meansOfTransportCrossingTheBorderType = NameOfVessel,
    meansOfTransportCrossingTheBorderIDNumber = Some("QWERTY"),
    meansOfTransportCrossingTheBorderNationality = Some("United Kingdom"),
    container = true,
    containerId = Some("ContainerID")
  )
  val emptyTransportInformation = TransportInformation(
    inlandModeOfTransportCode = None,
    borderModeOfTransportCode = "",
    meansOfTransportOnDepartureType = "",
    meansOfTransportOnDepartureIDNumber = None,
    meansOfTransportCrossingTheBorderType = "",
    meansOfTransportCrossingTheBorderIDNumber = None,
    meansOfTransportCrossingTheBorderNationality = None,
    container = false,
    containerId = None
  )

  val correctTransportInformationJSON: JsValue = JsObject(
    Map(
      "inlandModeOfTransportCode" -> JsString(Road),
      "borderModeOfTransportCode" -> JsString(Road),
      "meansOfTransportOnDepartureType" -> JsString(NameOfVessel),
      "meansOfTransportOnDepartureIDNumber" -> JsString("123ABC"),
      "meansOfTransportCrossingTheBorderType" -> JsString(NameOfVessel),
      "meansOfTransportCrossingTheBorderIDNumber" -> JsString("QWERTY"),
      "meansOfTransportCrossingTheBorderNationality" -> JsString("United Kingdom"),
      "container" -> JsBoolean(true),
      "containerId" -> JsString("ContainerID")
    )
  )
  val emptyTransportInformationJSON: JsValue = JsObject(
    Map(
      "inlandModeOfTransportCode" -> JsString(""),
      "borderModeOfTransportCode" -> JsString(""),
      "meansOfTransportOnDepartureType" -> JsString(""),
      "meansOfTransportOnDepartureIDNumber" -> JsString(""),
      "meansOfTransportCrossingTheBorderType" -> JsString(""),
      "meansOfTransportCrossingTheBorderIDNumber" -> JsString(""),
      "meansOfTransportCrossingTheBorderNationality" -> JsString(""),
      "container" -> JsBoolean(false),
      "containerId" -> JsString("")
    )
  )
}
