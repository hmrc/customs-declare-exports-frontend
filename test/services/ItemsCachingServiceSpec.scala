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

package services

import base.CustomExportsBaseSpec
import base.TestHelper._
import forms.declaration.{CommodityMeasure, ItemType, PackageInformation}
import models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ItemsCachingServiceSpec extends CustomExportsBaseSpec with GoodsItemCachingData with OptionValues {

  val itemsCachingService = new ItemsCachingService(mockCustomsCacheService)(appConfig)

  override def afterEach(): Unit =
    reset(mockCustomsCacheService)

  "ItemsCachingService" should {

    "create packaging from PackageInformation cache" in {
      val input = getDataSeq(5, createPackageInformation)
      val cacheMap = getCacheMap(input, PackageInformation.formId)

      val packages = itemsCachingService.generatePackages(cacheMap).value
      (packages zip input).map {
        case (actual, expected) =>
          actual.typeCode mustBe expected.typesOfPackages
          actual.quantity mustBe expected.numberOfPackages
          actual.marksNumbersId mustBe expected.shippingMarks
      }
    }

    "create GovernmentProcedure from ProcedureCodesData cache" in {
      val input = createProcedureCodesData()
      val cacheMap = getCacheMap(input, ProcedureCodesData.formId)
      val govProcedureCodes = itemsCachingService.procedureCodes(cacheMap).value
      val code = govProcedureCodes.headOption.getOrElse(fail())
      (code.currentCode.value + code.previousCode.value) mustBe input.procedureCode.value

      (input.additionalProcedureCodes zip govProcedureCodes.drop(1)).map {
        case (expected, actual) =>
          expected mustBe actual.currentCode.value
      }

    }

    "populate Commodity with GoodsMeasure from CommodityMeasure cache" in {
      val input = createCommodityMeasure()
      val cacheMap = getCacheMap(input, CommodityMeasure.commodityFormId)
      val commodity = itemsCachingService.commodityFromGoodsMeasure(cacheMap).value
      commodity.goodsMeasure.getOrElse(fail()).grossMassMeasure.value.unitCode.value.toString mustBe "KGM"
      commodity.goodsMeasure.getOrElse(fail()).grossMassMeasure.value.value.value mustBe BigDecimal(input.grossMass)
      commodity.goodsMeasure.getOrElse(fail()).netWeightMeasure.value.unitCode.value.toString mustBe "KGM"
      commodity.goodsMeasure.getOrElse(fail()).netWeightMeasure.value.value.value mustBe BigDecimal(input.netMass)
      commodity.goodsMeasure
        .getOrElse(fail())
        .tariffQuantity
        .value
        .value
        .value
        .toString mustBe input.supplementaryUnits.value
    }

    "create Seq of AdditionalInformation from AdditionalInformationData cache" in {
      val input = createAdditionalInformationData()
      val cacheMap = getCacheMap(input, AdditionalInformationData.formId)
      val additionals = itemsCachingService.additionalInfo(cacheMap).value
      (input.items zip additionals).map {
        case (expected, actual) =>
          expected.code mustBe actual.code
          expected.description mustBe actual.description
      }
    }

    "create Seq of GovernmentAgencyGoodsItemAdditionalDocument from DocumentsProducedData cache" in {
      val input = createDocumentsProducedData()
      val cacheMap = getCacheMap(input, DocumentsProducedData.formId)
      val goodsItemAdditionalDocs = itemsCachingService.documents(cacheMap).value

      (goodsItemAdditionalDocs zip input.documents).map {
        case (actual, expected) =>
          actual.categoryCode.value must equal(expected.documentTypeCode.value.head.toString)
          actual.typeCode.value must equal(expected.documentTypeCode.value.drop(1))
          actual.id.value must equal(
            expected.documentIdentifierAndPart.value.documentIdentifier.value +
              expected.documentIdentifierAndPart.value.documentPart.value
          )
          actual.lpcoExemptionCode must equal(expected.documentStatus)
          actual.name mustBe expected.documentStatusReason

          actual.submitter.value.name.value must equal(expected.issuingAuthorityName.value)
          actual.effectiveDateTime.value.dateTimeString.formatCode must equal("102")
          actual.effectiveDateTime.value.dateTimeString.value must equal(expected.dateOfValidity.value.to102Format)
          actual.writeOff.value.quantity.value.unitCode.value must equal(
            expected.documentWriteOff.value.measurementUnit.value
          )
          actual.writeOff.value.quantity.value.value.value must equal(
            expected.documentWriteOff.value.documentQuantity.value
          )
      }
    }

    "populate GoodsItem with Commodity Classifications, description and  statisticalValueAmount" in {
      val input = createItemType()
      val cacheMap = getCacheMap(input, ItemType.id)
      val goodsItem = itemsCachingService.goodsItemFromItemTypes(cacheMap).value

      goodsItem.statisticalValueAmount.value.currencyId.value.toString mustBe "GBP"
      goodsItem.statisticalValueAmount.value.value.value.toString mustBe input.statisticalValue

      val classifications = goodsItem.commodity.value.classifications
      classifications.size mustBe
        (input.nationalAdditionalCodes.size + input.taricAdditionalCodes.size + 2)
      classifications.head.id.value mustBe input.combinedNomenclatureCode
      (classifications.drop(2) zip (input.nationalAdditionalCodes ++ input.taricAdditionalCodes)).map {
        case (actual, expected) =>
          actual.id.value mustBe expected
      }

      val dangerousGoods = goodsItem.commodity.value.dangerousGoods
      dangerousGoods.size must equal(1)
      dangerousGoods.head.undgid.value must equal(input.unDangerousGoodsCode.get)
    }

    "add goodsItem to cache" in {

      val input = getDataSeq(5, createPackageInformation)
      val cacheMap = CacheMap(
        id = "eoriForCache",
        data = getCacheMap(input, PackageInformation.formId).data ++
          getCacheMap(createProcedureCodesData(), ProcedureCodesData.formId).data ++
          getCacheMap(createProcedureCodesData(), ProcedureCodesData.formId).data ++
          getCacheMap(createCommodityMeasure(), CommodityMeasure.commodityFormId).data ++
          getCacheMap(createAdditionalInformationData(), AdditionalInformationData.formId).data ++
          getCacheMap(createDocumentsProducedData(), DocumentsProducedData.formId).data
      )

      when(mockCustomsCacheService.fetch(anyString())(any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))
      when(mockCustomsCacheService.remove(anyString())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(204)))
      withCaching[Seq[GovernmentAgencyGoodsItem]](None)
      val result = itemsCachingService.addItemToCache("goodsItemCacheId", "cacheId")
      result.futureValue mustBe true
      //TODO : Check specific goodsItem is being added
      verify(mockCustomsCacheService)
        .cache[Seq[GovernmentAgencyGoodsItem]](
          ArgumentMatchers.eq("cacheId"),
          ArgumentMatchers.eq("exportItems"),
          any()
        )(any(), any(), any())
      verify(mockCustomsCacheService)
        .remove(ArgumentMatchers.eq("goodsItemCacheId"))(any(), any())
    }
  }

}
