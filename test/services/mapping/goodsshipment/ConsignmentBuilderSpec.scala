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

package services.mapping.goodsshipment

import forms.declaration.{CarrierDetails, CarrierDetailsSpec, TransportInformation, TransportInformationSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class ConsignmentBuilderSpec extends WordSpec with Matchers {

  "ConsignmentBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.Consignment instance" when {
      "all data has been supplied with eori only" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id -> CarrierDetailsSpec.correctCarrierDetailsEORIOnlyJSON,
              TransportInformation.id -> TransportInformationSpec.correctTransportInformationJSON
            )
          )
        val consignment = ConsignmentBuilder.build(cacheMap)
        consignment.getGoodsLocation.getID.getValue should be("9GB1234567ABCDEF")

        consignment.getGoodsLocation.getName should be(null)
        consignment.getGoodsLocation.getAddress should be(null)

        consignment.getContainerCode.getValue should be("1")

        consignment.getArrivalTransportMeans.getModeCode.getValue should be("1")
        consignment.getDepartureTransportMeans.getID.getValue should be("123112yu78")
        consignment.getDepartureTransportMeans.getIdentificationTypeCode.getValue should be("40")
      }
      "all data has been supplied with address only" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id -> CarrierDetailsSpec.correctCarrierDetailsAddressOnlyJSON,
              TransportInformation.id -> TransportInformationSpec.correctTransportInformationJSON
            )
          )
        val consignment = ConsignmentBuilder.build(cacheMap)
        consignment.getGoodsLocation.getID should be(null)
        consignment.getGoodsLocation.getName.getValue should be("Full Name")

        consignment.getGoodsLocation.getAddress.getLine.getValue should be("Address Line")
        consignment.getGoodsLocation.getAddress.getCityName.getValue should be("Town or City")
        consignment.getGoodsLocation.getAddress.getCountryCode.getValue should be("PL")
        consignment.getGoodsLocation.getAddress.getPostcodeID.getValue should be("AB12 34CD")
        consignment.getGoodsLocation.getAddress.getPostcodeID.getValue should be("AB12 34CD")

        consignment.getContainerCode.getValue should be("1")

        consignment.getArrivalTransportMeans.getModeCode.getValue should be("1")
        consignment.getDepartureTransportMeans.getID.getValue should be("123112yu78")
        consignment.getDepartureTransportMeans.getIdentificationTypeCode.getValue should be("40")
      }

      "no carrier data has been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id -> CarrierDetailsSpec.emptyCarrierDetailsJSON,
              TransportInformation.id -> TransportInformationSpec.correctTransportInformationJSON
            )
          )
        val consignment = ConsignmentBuilder.build(cacheMap)
        consignment.getGoodsLocation should be(null)

        consignment.getContainerCode.getValue should be("1")

        consignment.getArrivalTransportMeans.getModeCode.getValue should be("1")
        consignment.getDepartureTransportMeans.getID.getValue should be("123112yu78")
        consignment.getDepartureTransportMeans.getIdentificationTypeCode.getValue should be("40")
      }

      "no transport information data has been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id -> CarrierDetailsSpec.correctCarrierDetailsEORIOnlyJSON,
              TransportInformation.id -> TransportInformationSpec.emptyTransportInformationJSON
            )
          )
        val consignment = ConsignmentBuilder.build(cacheMap)
        consignment.getGoodsLocation.getID.getValue should be("9GB1234567ABCDEF")

        consignment.getGoodsLocation.getName should be(null)
        consignment.getGoodsLocation.getAddress should be(null)

        consignment.getContainerCode.getValue should be("0")

        consignment.getArrivalTransportMeans should be(null)
        consignment.getDepartureTransportMeans should be(null)
      }
    }
  }
}
