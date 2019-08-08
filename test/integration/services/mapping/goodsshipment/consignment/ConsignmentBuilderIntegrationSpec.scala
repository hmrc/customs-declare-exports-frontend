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

package integration.services.mapping.goodsshipment.consignment

import forms.declaration.GoodsLocationTestData._
import forms.declaration.TransportCodes.Maritime
import forms.declaration._
import models.ExportsDeclaration
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.cache.ExportsDeclarationBuilder
import services.mapping.goodsshipment.consignment.ConsignmentBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class ConsignmentBuilderIntegrationSpec
    extends WordSpec with Matchers with ExportsDeclarationBuilder with GuiceOneAppPerSuite {

  private val builder = app.injector.instanceOf[ConsignmentBuilder]

  "ConsignmentBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.Consignment instance" when {
      "correct data is present" in {
        val borderModeOfTransportCode = "BCode"
        val meansOfTransportOnDepartureType = "T"
        val meansOfTransportOnDepartureIDNumber = "12345"

        val model: ExportsDeclaration =
          aDeclaration(
            withGoodsLocation(GoodsLocationTestData.correctGoodsLocation),
            withBorderTransport(
              borderModeOfTransportCode,
              meansOfTransportOnDepartureType,
              Some(meansOfTransportOnDepartureIDNumber)
            ),
            withWarehouseIdentification(WarehouseIdentificationSpec.correctWarehouseIdentification),
            withTransportDetails(Some("Portugal"), container = true, "40", Some("1234567878ui"), Some("A")),
            withSeals(Seq(Seal("id1"), Seal("id2")))
          )

        val goodsShipment: Declaration.GoodsShipment = new Declaration.GoodsShipment

        builder.buildThenAdd(model, goodsShipment)

        val consignment: GoodsShipment.Consignment = goodsShipment.getConsignment

        consignment.getContainerCode.getValue should be("1")

        consignment.getGoodsLocation.getID.getValue should be("LOC")
        consignment.getGoodsLocation.getName.getValue should be(additionalQualifier)
        consignment.getGoodsLocation.getAddress.getLine.getValue should be(addressLine)
        consignment.getGoodsLocation.getAddress.getCityName.getValue should be(city)
        consignment.getGoodsLocation.getAddress.getCountryCode.getValue should be(countryCode)
        consignment.getGoodsLocation.getAddress.getPostcodeID.getValue should be(postcode)

        consignment.getDepartureTransportMeans.getID.getValue should be(meansOfTransportOnDepartureIDNumber)
        consignment.getDepartureTransportMeans.getIdentificationTypeCode.getValue should be(
          meansOfTransportOnDepartureType
        )

        consignment.getArrivalTransportMeans.getModeCode.getValue should be(Maritime)

        consignment.getTransportEquipment.size() should be(1)
        consignment.getTransportEquipment.get(0).getSeal.size() should be(2)
        consignment.getTransportEquipment.get(0).getSequenceNumeric.intValue() should be(2)

        consignment.getTransportEquipment.get(0).getSeal.get(0).getID.getValue should be("id1")
        consignment.getTransportEquipment.get(0).getSeal.get(0).getSequenceNumeric.intValue() should be(1)
        consignment.getTransportEquipment.get(0).getSeal.get(1).getID.getValue should be("id2")
        consignment.getTransportEquipment.get(0).getSeal.get(1).getSequenceNumeric.intValue() should be(2)

      }
    }
  }
}
