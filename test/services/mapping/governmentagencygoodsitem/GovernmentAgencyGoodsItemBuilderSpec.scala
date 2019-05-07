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

package services.mapping.governmentagencygoodsitem

import java.util

import forms.declaration.ItemType
import models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Reads
import services.ExportsItemsCacheIds
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem._
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.{GovernmentAgencyGoodsItem => WCOGovernmentAgencyGoodsItem}
class GovernmentAgencyGoodsItemBuilderSpec
    extends WordSpec with Matchers with GovernmentAgencyGoodsItemMocks with GovernmentAgencyGoodsItemData {

  trait SetUp {
    implicit val cacheMap: CacheMap = mock[CacheMap]

    //Set Up Additional Information
    setUpAdditionalInformation
    //setUp Additional Documents
    setUpAdditionalDocuments()
    //Set up ProcedureCodes
    setUpProcedureCodes
    //Set up Commodity Measure
    setUpCommodityMeasure
    //Set up Item Type
    setUpItemType
    //Set up PackageInformation
    setUpPackageInformation

    //setUp GovernmentAgencyGoodsItem
    val governmentAgencyGoodsItem = GovernmentAgencyGoodsItem(sequenceNumeric = 1)

    when(
      cacheMap
        .getEntry[Seq[GovernmentAgencyGoodsItem]](eqTo(ExportsItemsCacheIds.itemsId))(
          any[Reads[Seq[GovernmentAgencyGoodsItem]]]
        )
    ).thenReturn(Some(Seq(governmentAgencyGoodsItem)))

  }

  "GovernmentAgencyGoodsItemBuilder" should {
    "map to WCO model correctly " in new SetUp() {

      val mappedGoodsItemList: List[WCOGovernmentAgencyGoodsItem] = GovernmentAgencyGoodsItemBuilder.build
      mappedGoodsItemList.isEmpty shouldBe false

      val mappedGoodsItem: WCOGovernmentAgencyGoodsItem = mappedGoodsItemList.head

      val additionalDocuments: util.List[AdditionalDocument] = mappedGoodsItem.getAdditionalDocument
      additionalDocuments.isEmpty shouldBe false
      val firstMappedDocument: AdditionalDocument = additionalDocuments.get(0)

      val additionalInformations: util.List[AdditionalInformation] = mappedGoodsItem.getAdditionalInformation
      additionalInformations.isEmpty shouldBe false
      val additionalInformation: AdditionalInformation = additionalInformations.get(0)

      val commodity: Commodity = mappedGoodsItem.getCommodity

      val packagingList: util.List[Packaging] = mappedGoodsItem.getPackaging
      packagingList.isEmpty shouldBe false
      val firstPackaging: Packaging = packagingList.get(0)

      val procedurelist: util.List[GovernmentProcedure] = mappedGoodsItem.getGovernmentProcedure
      val firstProcedure: GovernmentProcedure = procedurelist.get(0)

      validateAdditionalDocuments(firstMappedDocument)
      validateAdditionalInformation(additionalInformation)
      validateCommodity(commodity)
      validatePackaging(firstPackaging)
      validateGovernmentProcedure(firstProcedure)

    }

    "map correctly if ItemType is None " in new SetUp() {

      when(cacheMap.getEntry[ItemType](eqTo(ItemType.id))(any[Reads[ItemType]])).thenReturn(None)
      val mappedGoodsItemList: List[WCOGovernmentAgencyGoodsItem] = GovernmentAgencyGoodsItemBuilder.build
      mappedGoodsItemList.isEmpty shouldBe false
    }
  }

  private def validateGovernmentProcedure(mappedProcedure: GovernmentProcedure) = {
    mappedProcedure.getCurrentCode.getValue shouldBe cachedCode.substring(0, 2)
    mappedProcedure.getPreviousCode.getValue shouldBe cachedCode.substring(2, 4)
  }

  private def validatePackaging(packaging: Packaging) = {
    packaging.getQuantityQuantity.getValue shouldBe BigDecimal(packageQuantity).bigDecimal
    packaging.getMarksNumbersID.getValue shouldBe shippingMarksValue
    packaging.getTypeCode.getValue shouldBe packageTypeValue
  }

  private def validateCommodity(mappedCommodity: WCOGovernmentAgencyGoodsItem.Commodity) = {
    mappedCommodity.getDescription.getValue shouldBe descriptionOfGoods
    mappedCommodity.getDangerousGoods.get(0).getUNDGID.getValue shouldBe unDangerousGoodsCode

    val goodsMeasure = mappedCommodity.getGoodsMeasure

    goodsMeasure.getNetNetWeightMeasure.getValue shouldBe BigDecimal(netMassString).bigDecimal
    goodsMeasure.getNetNetWeightMeasure.getUnitCode shouldBe ExportsItemsCacheIds.defaultMeasureCode

    goodsMeasure.getGrossMassMeasure.getValue shouldBe BigDecimal(grossMassString).bigDecimal
    goodsMeasure.getGrossMassMeasure.getUnitCode shouldBe ExportsItemsCacheIds.defaultMeasureCode

    goodsMeasure.getTariffQuantity.getValue shouldBe BigDecimal(tariffQuantity).bigDecimal
    goodsMeasure.getTariffQuantity.getUnitCode shouldBe ExportsItemsCacheIds.defaultMeasureCode
  }
  private def validateAdditionalInformation(
    additionalInfomation: WCOGovernmentAgencyGoodsItem.AdditionalInformation
  ) = {
    additionalInfomation.getStatementCode.getValue shouldBe statementCode
    additionalInfomation.getStatementDescription.getValue shouldBe descriptionValue
  }

  private def validateAdditionalDocuments(firstMappedDocument: WCOGovernmentAgencyGoodsItem.AdditionalDocument) = {
    firstMappedDocument.getCategoryCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(0, 1)
    firstMappedDocument.getTypeCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(1)
    firstMappedDocument.getID.getValue shouldBe documentIdentifier + documentPart
    firstMappedDocument.getLPCOExemptionCode.getValue shouldBe documentStatus
    firstMappedDocument.getName.getValue shouldBe documentStatus + documentStatusReason
    firstMappedDocument.getSubmitter.getName.getValue shouldBe issusingAuthorityName

    val writeoff = firstMappedDocument.getWriteOff
    writeoff.getAmountAmount shouldBe null
    val writeOffQuantity = writeoff.getQuantityQuantity
    writeOffQuantity.getUnitCode shouldBe measurementUnit
    writeOffQuantity.getValue shouldBe documentQuantity.bigDecimal
  }
}
