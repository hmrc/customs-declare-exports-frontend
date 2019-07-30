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

package integration.services.mapping.goodsshipment

import forms.declaration.ConsigneeDetailsSpec.correctConsigneeDetailsFull
import forms.declaration.ConsignmentReferencesSpec.correctConsignmentReferences
import forms.declaration.DeclarationAdditionalActorsSpec.{correctAdditionalActors1, correctAdditionalActors2}
import forms.declaration.DestinationCountriesSpec.correctDestinationCountries
import forms.declaration.DocumentSpec.correctPreviousDocument
import forms.declaration.GoodsLocationTestData.correctGoodsLocation
import forms.declaration.NatureOfTransactionSpec.correctNatureOfTransaction
import forms.declaration.PreviousDocumentsData
import forms.declaration.WarehouseIdentificationSpec.correctWarehouseIdentification
import models.declaration.DeclarationAdditionalActorsData
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.cache.ExportsCacheModelBuilder
import services.mapping.goodsshipment.GoodsShipmentBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class GoodsShipmentBuilderIntegrationSpec
    extends WordSpec with Matchers with ExportsCacheModelBuilder with GuiceOneAppPerSuite {

  private def builder = app.injector.instanceOf[GoodsShipmentBuilder]

  "GoodsShipmentBuilder" should {

    "correctly map to the WCO-DEC GoodsShipment instance" in {

      val cacheModel = aCacheModel(
        withNatureOfTransaction(correctNatureOfTransaction.natureType),
        withConsigneeDetails(correctConsigneeDetailsFull),
        withDeclarationAdditionalActors(
          DeclarationAdditionalActorsData(Seq(correctAdditionalActors1, correctAdditionalActors2))
        ),
        withDestinationCountries(correctDestinationCountries),
        withGoodsLocation(correctGoodsLocation),
        withWarehouseIdentification(correctWarehouseIdentification),
        withConsignmentReferences(correctConsignmentReferences),
        withPreviousDocuments(PreviousDocumentsData(Seq(correctPreviousDocument)))
      )

      val declaration = new Declaration()
      builder.buildThenAdd(cacheModel, declaration)

      val goodsShipment = declaration.getGoodsShipment

      goodsShipment.getTransactionNatureCode.getValue should be("1")

      goodsShipment.getConsignee.getID.getValue should be("9GB1234567ABCDEF")
      goodsShipment.getConsignee.getID.getValue should be("9GB1234567ABCDEF")
      goodsShipment.getConsignee.getName.getValue should be("Full Name")
      goodsShipment.getConsignee.getAddress.getLine.getValue should be("Address Line")
      goodsShipment.getConsignee.getAddress.getCityName.getValue should be("Town or City")
      goodsShipment.getConsignee.getAddress.getPostcodeID.getValue should be("AB12 34CD")
      goodsShipment.getConsignee.getAddress.getCountryCode.getValue should be("PL")

      goodsShipment.getConsignment.getGoodsLocation.getID.getValue should be("LOC")

      goodsShipment.getDestination.getCountryCode.getValue should be("PL")

      goodsShipment.getExportCountry.getID.getValue should be("PL")

      goodsShipment.getUCR.getID should be(null)
      goodsShipment.getUCR.getTraderAssignedReferenceID.getValue should be("8GB123456789012-1234567890QWERTYUIO")

      goodsShipment.getWarehouse.getID.getValue should be("1234567GB")
      goodsShipment.getWarehouse.getTypeCode.getValue should be("R")

      goodsShipment.getPreviousDocument.size should be(1)
      goodsShipment.getPreviousDocument.get(0).getID.getValue should be("DocumentReference")
      goodsShipment.getPreviousDocument.get(0).getCategoryCode.getValue should be("X")
      goodsShipment.getPreviousDocument.get(0).getLineNumeric.intValue() should be(123)
      goodsShipment.getPreviousDocument.get(0).getTypeCode.getValue should be("ABC")

      goodsShipment.getAEOMutualRecognitionParty.size should be(2)
      goodsShipment.getAEOMutualRecognitionParty.get(0).getID.getValue should be("eori1")
      goodsShipment.getAEOMutualRecognitionParty.get(0).getRoleCode.getValue should be("CS")
      goodsShipment.getAEOMutualRecognitionParty.get(1).getID.getValue should be("eori99")
      goodsShipment.getAEOMutualRecognitionParty.get(1).getRoleCode.getValue should be("FW")
    }
  }
}
