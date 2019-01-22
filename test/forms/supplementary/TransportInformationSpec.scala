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
import org.scalatest.{MustMatchers, WordSpec}

class TransportInformationSpec extends WordSpec with MustMatchers {

  private val inlandModeOfTransportCode = Some("1")
  private val borderModeOfTransportCode = "2"
  private val meansOfTransportOnDepartureType = "10"
  private val meansOfTransportOnDepartureIDNumber = Some("QWERTY1234567890")
  private val meansOfTransportCrossingTheBorderType = "20"
  private val meansOfTransportCrossingTheBorderIDNumber = Some("ABCDEFGHIJK1234567890")
  private val meansOfTransportCrossingTheBorderNationality = Some("United Kingdom")
  private val meansOfTransportCrossingTheBorderNationalityCode = Some("GB")
  private val container = true
  private val containerId = Some("1234")

  private val transportInformation = TransportInformation(
    inlandModeOfTransportCode = inlandModeOfTransportCode,
    borderModeOfTransportCode = borderModeOfTransportCode,
    meansOfTransportOnDepartureType = meansOfTransportOnDepartureType,
    meansOfTransportOnDepartureIDNumber = meansOfTransportOnDepartureIDNumber,
    meansOfTransportCrossingTheBorderType = meansOfTransportCrossingTheBorderType,
    meansOfTransportCrossingTheBorderIDNumber = meansOfTransportCrossingTheBorderIDNumber,
    meansOfTransportCrossingTheBorderNationality = meansOfTransportCrossingTheBorderNationality,
    container = container,
    containerId = containerId
  )

  private val expectedTransportInformationProperties: Map[String, String] = Map(
    "declaration.goodsShipment.consignment.arrivalTransportMeans.modeCode" -> inlandModeOfTransportCode.get,
    "declaration.borderTransportMeans.modeCode" -> borderModeOfTransportCode,
    "declaration.borderTransportMeans.registrationNationalityCode" -> meansOfTransportCrossingTheBorderNationalityCode.get,
    "declaration.goodsShipment.consignment.containerCode" -> container.toString,
    "declaration.goodsShipment.governmentAgencyGoodsItem.commodity.transportEquipment.id" -> containerId.get
  )

  "TransportInformation" should {
    "convert itself into transport information properties" in {
      transportInformation.toMetadataProperties() must equal(expectedTransportInformationProperties)
    }
  }

}
