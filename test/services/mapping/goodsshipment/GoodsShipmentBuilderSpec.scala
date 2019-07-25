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

import forms.ChoiceSpec.supplementaryChoice
import forms.declaration.ConsigneeDetailsSpec.correctConsigneeDetailsFull
import forms.declaration.NatureOfTransactionSpec.correctNatureOfTransaction
import forms.declaration.GoodsLocationTestData.correctGoodsLocation
import forms.declaration.DestinationCountriesSpec.correctDestinationCountries
import forms.declaration.ConsignmentReferencesSpec.correctConsignmentReferences
import forms.declaration.PreviousDocumentsData
import forms.declaration.WarehouseIdentificationSpec.correctWarehouseIdentification
import forms.declaration.DocumentSpec.correctPreviousDocument
import forms.declaration.DeclarationAdditionalActorsSpec.correctAdditionalActors1
import forms.declaration.DeclarationAdditionalActorsSpec.correctAdditionalActors2
import models.declaration.{DeclarationAdditionalActorsData, Locations}
import models.declaration.SupplementaryDeclarationTestData._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.verify
import org.mockito.ArgumentMatchers.{any, refEq}
import org.scalatest.{Matchers, WordSpec}
import services.cache.CacheTestData
import wco.datamodel.wco.dec_dms._2.Declaration

class GoodsShipmentBuilderSpec extends WordSpec with Matchers with CacheTestData with MockitoSugar {

  private val mockGoodsShipmentNatureOfTransactionBuilder = mock[GoodsShipmentNatureOfTransactionBuilder]
  private val mockConsigneeBuilder = mock[ConsigneeBuilder]

  private def builder: GoodsShipmentBuilder =
    new GoodsShipmentBuilder(mockGoodsShipmentNatureOfTransactionBuilder, mockConsigneeBuilder)

  "GoodsShipmentBuilder" should {

    "correctly map to the WCO-DEC GoodsShipment instance" in {
      val goodsShipment = GoodsShipmentBuilder.build(cacheMapAllRecords, supplementaryChoice)
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
      goodsShipment.getPreviousDocument.get(0).getTypeCode.getValue should be("MCR")

      goodsShipment.getAEOMutualRecognitionParty.size should be(1)
      goodsShipment.getAEOMutualRecognitionParty.get(0).getID.getValue should be("eori1")
      goodsShipment.getAEOMutualRecognitionParty.get(0).getRoleCode.getValue should be("CS")
    }

    "correctly map to the WCO-DEC GoodsShipment instance, using ExportCacheModel" in {

      val declaration = new Declaration()
      builder.buildThenAdd(
        createEmptyExportsModel.copy(
          natureOfTransaction = Some(correctNatureOfTransaction),
          parties = createEmptyParties().copy(
            consigneeDetails = Some(correctConsigneeDetailsFull),
            declarationAdditionalActorsData =
              Some(DeclarationAdditionalActorsData(Seq(correctAdditionalActors1, correctAdditionalActors2)))
          ),
          locations = Locations().copy(
            goodsLocation = Some(correctGoodsLocation),
            destinationCountries = Some(correctDestinationCountries),
            warehouseIdentification = Some(correctWarehouseIdentification)
          ),
          consignmentReferences = Some(correctConsignmentReferences),
          previousDocuments = Some(PreviousDocumentsData(Seq(correctPreviousDocument)))
        ),
        declaration
      )

      verify(mockGoodsShipmentNatureOfTransactionBuilder)
        .buildThenAdd(refEq(correctNatureOfTransaction), any[Declaration.GoodsShipment])

      val goodsShipment = declaration.getGoodsShipment

      verify(mockConsigneeBuilder)
        .buildThenAdd(refEq(correctConsigneeDetailsFull), any[Declaration.GoodsShipment])

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
