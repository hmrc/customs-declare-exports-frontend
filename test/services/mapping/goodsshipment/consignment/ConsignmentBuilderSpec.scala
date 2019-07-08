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

package services.mapping.goodsshipment.consignment

import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.declaration._
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class ConsignmentBuilderSpec extends WordSpec with Matchers {

  "ConsignmentBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.Consignment instance" when {
      "on the Supplementary journey" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              GoodsLocation.formId -> GoodsLocationTestData.correctGoodsLocationJSON,
              BorderTransport.formId -> Json.toJson(BorderTransport("3", "10", Some("123112yu78"))),
              TransportDetails.formId -> Json.toJson(TransportDetails(Some("Portugal"), true, "40", Some("1234567878ui"), Some("A"))),
              WarehouseIdentification.formId -> WarehouseIdentificationSpec.correctWarehouseIdentificationJSON,
              Seal.formId -> Json.toJson(Seq(Seal("first"), Seal("second")))
            )
          )
        val consignment = ConsignmentBuilder.build(cacheMap, Choice(AllowedChoiceValues.SupplementaryDec))

        consignment.getContainerCode.getValue should be("1")

        consignment.getGoodsLocation.getID.getValue should be("LOC")
        consignment.getGoodsLocation.getName.getValue should be("9GB1234567ABCDEF")
        consignment.getGoodsLocation.getAddress.getLine.getValue should be("Address Line")
        consignment.getGoodsLocation.getAddress.getCityName.getValue should be("Town or City")
        consignment.getGoodsLocation.getAddress.getCountryCode.getValue should be("PL")
        consignment.getGoodsLocation.getAddress.getPostcodeID.getValue should be("AB12 CD3")

        consignment.getDepartureTransportMeans.getID.getValue should be("123112yu78")
        consignment.getDepartureTransportMeans.getIdentificationTypeCode.getValue should be("10")

        consignment.getArrivalTransportMeans.getModeCode.getValue should be("2")
        consignment.getTransportEquipment.size() should be(0)
      }

      "on the Standard journey" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              GoodsLocation.formId ->
                GoodsLocationTestData.correctGoodsLocationJSON,
              BorderTransport.formId ->
                Json.toJson(BorderTransport("3", "10", Some("123112yu78"))),
              TransportDetails.formId -> Json
                .toJson(TransportDetails(Some("Portugal"), true, "40", Some("1234567878ui"), Some("A"))),
              WarehouseIdentification.formId -> WarehouseIdentificationSpec.correctWarehouseIdentificationJSON,
              Seal.formId -> Json.toJson(Seq(Seal("first"), Seal("second")))
            )
          )
        val consignment = ConsignmentBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))

        consignment.getContainerCode.getValue should be("1")

        consignment.getGoodsLocation.getID.getValue should be("LOC")
        consignment.getGoodsLocation.getName.getValue should be("9GB1234567ABCDEF")
        consignment.getGoodsLocation.getAddress.getLine.getValue should be("Address Line")
        consignment.getGoodsLocation.getAddress.getCityName.getValue should be("Town or City")
        consignment.getGoodsLocation.getAddress.getCountryCode.getValue should be("PL")
        consignment.getGoodsLocation.getAddress.getPostcodeID.getValue should be("AB12 CD3")

        consignment.getDepartureTransportMeans.getID.getValue should be("123112yu78")
        consignment.getDepartureTransportMeans.getIdentificationTypeCode.getValue should be("10")

        consignment.getArrivalTransportMeans.getModeCode.getValue should be("2")

        consignment.getTransportEquipment.size() should be(1)
        consignment.getTransportEquipment.get(0).getSeal.size() should be(2)
        consignment.getTransportEquipment.get(0).getSequenceNumeric.intValue() should be(2)

        consignment.getTransportEquipment.get(0).getSeal.get(0).getID.getValue should be("first")
        consignment.getTransportEquipment.get(0).getSeal.get(0).getSequenceNumeric.intValue() should be(1)
        consignment.getTransportEquipment.get(0).getSeal.get(1).getID.getValue should be("second")
        consignment.getTransportEquipment.get(0).getSeal.get(1).getSequenceNumeric.intValue() should be(2)
      }
    }
  }
}
