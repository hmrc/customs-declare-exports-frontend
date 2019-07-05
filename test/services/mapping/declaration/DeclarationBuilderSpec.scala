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

package services.mapping.declaration
import forms.ChoiceSpec.supplementaryChoice
import models.declaration.SupplementaryDeclarationDataSpec
import org.scalatest.{Matchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarationBuilderSpec extends WordSpec with Matchers {

  "DeclarationBuilder" should {
    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance" in {
      val declaration =
        DeclarationBuilder.build(SupplementaryDeclarationDataSpec.cacheMapAllRecords, supplementaryChoice)

      declaration.getAgent.getID.getValue should be("9GB1234567ABCDEF")
      declaration.getAgent.getName shouldBe null
      declaration.getAgent.getAddress shouldBe null
      declaration.getAgent.getFunctionCode.getValue should be("2")

      declaration.getBorderTransportMeans.getID.getValue should be("1234567878ui")
      declaration.getBorderTransportMeans.getIdentificationTypeCode.getValue should be("40")
      declaration.getBorderTransportMeans.getRegistrationNationalityCode.getValue should be("PT")
      declaration.getBorderTransportMeans.getModeCode.getValue should be("3")
      declaration.getBorderTransportMeans.getName shouldBe null
      declaration.getBorderTransportMeans.getTypeCode shouldBe null

      declaration.getCurrencyExchange.get(0).getRateNumeric.doubleValue() should be(1212121.12345)

      declaration.getDeclarant.getID.getValue should be("9GB1234567ABCDEF")
      declaration.getDeclarant.getName shouldBe null
      declaration.getDeclarant.getAddress shouldBe null

      declaration.getExitOffice.getID.getValue should be("123qwe12")

      declaration.getExporter.getID.getValue should be("9GB1234567ABCDEF")
      declaration.getExporter.getName.getValue should be("Full Name")
      declaration.getExporter.getAddress.getLine.getValue should be("Address Line")
      declaration.getExporter.getAddress.getCityName.getValue should be("Town or City")
      declaration.getExporter.getAddress.getCountryCode.getValue should be("PL")
      declaration.getExporter.getAddress.getPostcodeID.getValue should be("AB12 34CD")

      declaration.getFunctionalReferenceID.getValue should be("123LRN")

      declaration.getFunctionCode.getValue should be("9")

      declaration.getGoodsItemQuantity.getValue.intValue() should be(1)

      declaration.getInvoiceAmount.getValue.doubleValue() should be(1212312.12)
      declaration.getInvoiceAmount.getCurrencyID should be("GBP")

      declaration.getPresentationOffice shouldBe null

      declaration.getSpecificCircumstancesCodeCode shouldBe null

      declaration.getSupervisingOffice.getID.getValue should be("12345678")

      declaration.getTotalPackageQuantity.getValue.intValue() should be(123)

      declaration.getTypeCode.getValue should be("EXY")

      assertGoodsShipment(declaration)
    }
  }

  private def assertGoodsShipment(declaration: Declaration) = {
    declaration.getGoodsShipment.getTransactionNatureCode.getValue should be("1")

    declaration.getGoodsShipment.getConsignee.getID.getValue should be("9GB1234567ABCDEF")
    declaration.getGoodsShipment.getConsignee.getName.getValue should be("Full Name")
    declaration.getGoodsShipment.getConsignee.getAddress.getLine.getValue should be("Address Line")
    declaration.getGoodsShipment.getConsignee.getAddress.getCityName.getValue should be("Town or City")
    declaration.getGoodsShipment.getConsignee.getAddress.getPostcodeID.getValue should be("AB12 34CD")
    declaration.getGoodsShipment.getConsignee.getAddress.getCountryCode.getValue should be("PL")

    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getID.getValue should be("LOC")
    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getName.getValue should be("9GB1234567ABCDEF")
    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getAddress.getLine.getValue should be("Address Line")
    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getAddress.getCityName.getValue should be(
      "Town or City"
    )
    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getAddress.getPostcodeID.getValue should be("AB12 CD3")
    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getAddress.getCountryCode.getValue should be("PL")
    declaration.getGoodsShipment.getConsignment.getGoodsLocation.getAddress.getTypeCode.getValue shouldBe "Y"

    declaration.getGoodsShipment.getDestination.getCountryCode.getValue should be("PL")

    declaration.getGoodsShipment.getExportCountry.getID.getValue should be("PL")

    declaration.getGoodsShipment.getUCR.getID shouldBe null
    declaration.getGoodsShipment.getUCR.getTraderAssignedReferenceID.getValue should be(
      "8GB123456789012-1234567890QWERTYUIO"
    )

    declaration.getGoodsShipment.getWarehouse.getID.getValue should be("1234567GB")
    declaration.getGoodsShipment.getWarehouse.getTypeCode.getValue should be("R")

    declaration.getGoodsShipment.getPreviousDocument.size should be(1)
    declaration.getGoodsShipment.getPreviousDocument.get(0).getID.getValue should be("DocumentReference")
    declaration.getGoodsShipment.getPreviousDocument.get(0).getCategoryCode.getValue should be("X")
    declaration.getGoodsShipment.getPreviousDocument.get(0).getLineNumeric.intValue() should be(123)
    declaration.getGoodsShipment.getPreviousDocument.get(0).getTypeCode.getValue should be("MCR")

    declaration.getGoodsShipment.getAEOMutualRecognitionParty.size should be(1)
    declaration.getGoodsShipment.getAEOMutualRecognitionParty.get(0).getID.getValue should be("eori1")
    declaration.getGoodsShipment.getAEOMutualRecognitionParty.get(0).getRoleCode.getValue should be("CS")

    declaration.getGoodsShipment.getConsignment.getTransportEquipment.size() should be(0)
  }
}
