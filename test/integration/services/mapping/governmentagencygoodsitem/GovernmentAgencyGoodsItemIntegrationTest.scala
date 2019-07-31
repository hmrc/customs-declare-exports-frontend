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

package integration.services.mapping.governmentagencygoodsitem

import forms.common.Date
import forms.declaration.additionaldocuments.{DocumentIdentifierAndPart, DocumentWriteOff, DocumentsProduced}
import models.declaration.DocumentsProducedData
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.cache.{ExportsCacheItemBuilder, ExportsCacheModelBuilder}
import services.mapping.governmentagencygoodsitem.{GovernmentAgencyGoodsItemBuilder, GovernmentAgencyGoodsItemData}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem

class GovernmentAgencyGoodsItemIntegrationTest
    extends WordSpec with Matchers with ExportsCacheModelBuilder with ExportsCacheItemBuilder with GuiceOneAppPerSuite
    with GovernmentAgencyGoodsItemData {

  private val builder = app.injector.instanceOf[GovernmentAgencyGoodsItemBuilder]

  "GovernmentAgencyGoodsItemBuilder" should {

    "correctly map to the WCO-DEC GoodsShipment instance" in {
      val document = DocumentsProduced(
        documentTypeCode = Some(documentAndAdditionalDocumentTypeCode),
        documentIdentifierAndPart = Some(DocumentIdentifierAndPart(Some("123"), Some("456"))),
        documentStatus = Some(documentStatus),
        documentStatusReason = Some(documentStatusReason),
        issuingAuthorityName = Some(issusingAuthorityName),
        dateOfValidity = Some(Date(Some(12), Some(12), Some(12))),
        documentWriteOff = Some(DocumentWriteOff(Some(measurementUnit), Some(documentQuantity)))
      )

      val additionalProcedureCodes = Seq("additionalCode1", "additionalCode2")
      val model = aCachedItem(
        withDocumentsProducedData(DocumentsProducedData(Seq(document))),
        withAdditionalInformation("code", "description"),
        withItemType(statisticalValue = "123"),
        withPackageInformation(packageInformation),
        withProcedureCodes(Some("AB12344"), additionalProcedureCodes)
      )

      val goodsShipment = new GoodsShipment
      builder.buildThenAdd(model, goodsShipment)

      validateGovernmentAgencyGoodsItem(goodsShipment.getGovernmentAgencyGoodsItem.get(0))

    }
  }

  def validateGovernmentProcedure(item: GovernmentAgencyGoodsItem) = {
    val procedureCodes = item.getGovernmentProcedure

    procedureCodes.get(0).getCurrentCode.getValue shouldBe "AB"
    procedureCodes.get(0).getPreviousCode.getValue shouldBe "12"
    procedureCodes.get(1).getCurrentCode.getValue shouldBe "additionalCode1"
    procedureCodes.get(1).getPreviousCode shouldBe null
    procedureCodes.get(2).getCurrentCode.getValue shouldBe "additionalCode2"
    procedureCodes.get(2).getPreviousCode shouldBe null

  }

  private def validateGovernmentAgencyGoodsItem(item: GovernmentAgencyGoodsItem) = {
    validateAdditionalDocument(item)
    validateAdditionalInformation(item)
    validateStatisticalValueAmount(item)
    validatePackingInformation(item)
    validateGovernmentProcedure(item)
  }

  private def validateAdditionalInformation(item: GovernmentAgencyGoodsItem) = {
    val additionalInformation = item.getAdditionalInformation
    additionalInformation shouldNot be(empty)
    additionalInformation.get(0).getStatementCode.getValue shouldBe "code"
    additionalInformation.get(0).getStatementDescription.getValue shouldBe "description"
  }

  private def validateAdditionalDocument(governmentAgencyGoodsItem: GovernmentAgencyGoodsItem): Unit = {
    val mappedDocuments = governmentAgencyGoodsItem.getAdditionalDocument
    mappedDocuments
      .get(0)
      .getCategoryCode
      .getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(0, 1)
    mappedDocuments.get(0).getTypeCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(1)
    mappedDocuments.get(0).getID.getValue shouldBe "123456"
    mappedDocuments.get(0).getLPCOExemptionCode.getValue shouldBe documentStatus
    mappedDocuments.get(0).getName.getValue shouldBe documentStatusReason
    mappedDocuments.get(0).getSubmitter.getName.getValue shouldBe issusingAuthorityName

    val writeoff = mappedDocuments.get(0).getWriteOff
    writeoff.getAmountAmount shouldBe null
    val writeOffQuantity = writeoff.getQuantityQuantity
    writeOffQuantity.getUnitCode shouldBe measurementUnit
    writeOffQuantity.getValue shouldBe documentQuantity.bigDecimal
  }

  private def validatePackingInformation(item: GovernmentAgencyGoodsItem) = {

    val items = item.getPackaging
    items.size() should be(1)
    items.get(0).getQuantityQuantity.getValue shouldBe BigDecimal(packageInformation.numberOfPackages.get).bigDecimal
    items.get(0).getMarksNumbersID.getValue shouldBe packageInformation.shippingMarks.get
    items.get(0).getTypeCode.getValue shouldBe packageInformation.typesOfPackages.get
  }

  private def validateStatisticalValueAmount(item: GovernmentAgencyGoodsItem) = {

    item.getStatisticalValueAmount.getValue.intValue shouldBe 123
    item.getStatisticalValueAmount.getCurrencyID shouldBe "GBP"
  }
}
