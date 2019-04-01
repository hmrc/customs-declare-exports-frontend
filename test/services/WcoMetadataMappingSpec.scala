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
import base.TestHelper.getCacheMap
import models.DeclarationFormats._
import models.declaration.SupplementaryDeclarationDataSpec.cacheMapAllRecords
import org.scalatest.OptionValues
import uk.gov.hmrc.http.cache.client.CacheMap

class WcoMetadataMappingSpec extends CustomExportsBaseSpec with GoodsItemCachingData with OptionValues {

  "WcoMetadataMappingSpec" should {
    "produce metadata" in {
      val expectedItems = goodsItemSeq(10)
      val goodsItemCache = getCacheMap(expectedItems, "exportItems")
      val cacheMap = CacheMap(id = "eoriForCache", data = cacheMapAllRecords.data ++ goodsItemCache.data)

      val result = WcoMetadataMapping.produceMetaData(cacheMap)

      result.declaration must be(defined)

      result.declaration.flatMap(_.typeCode) mustBe Some("EXY")
      result.declaration.flatMap(_.functionalReferenceId) mustBe Some("123ABC")
      result.declaration.flatMap(_.goodsShipment).flatMap(_.ucr).flatMap(_.traderAssignedReferenceId) mustBe
        Some("8GB123456789012-1234567890QWERTYUIO")

      result.declaration.flatMap(_.exporter) must be(defined)
      result.declaration.flatMap(_.declarant) must be(defined)
      result.declaration.flatMap(_.agent) must be(defined)
      result.declaration.flatMap(_.goodsShipment) must be(defined)
      result.declaration.flatMap(_.invoiceAmount).flatMap(_.value) must be(Some(BigDecimal("12312312312312.12")))
      result.declaration.flatMap(_.invoiceAmount).flatMap(_.currencyId) must be(Some("GBP"))
      val goodsShipment = result.declaration.flatMap(_.goodsShipment).value
      goodsShipment.aeoMutualRecognitionParties.headOption mustBe defined
      result.declaration.value.authorisationHolders.size mustBe 1
      result.declaration.flatMap(_.supervisingOffice).flatMap(_.id) mustBe Some("12345678")
      result.declaration.flatMap(_.exitOffice).flatMap(_.id) mustBe Some("123qwe12")
      result.declaration.flatMap(_.borderTransportMeans).flatMap(_.id) mustBe Some("QWERTY")
      result.declaration.flatMap(_.borderTransportMeans).flatMap(_.modeCode) mustBe Some(3)
      result.declaration.flatMap(_.borderTransportMeans).flatMap(_.identificationTypeCode) mustBe Some("11")
      result.declaration.flatMap(_.borderTransportMeans).flatMap(_.registrationNationalityCode) mustBe Some("GB")
      goodsShipment.destination.flatMap(_.countryCode) mustBe Some("PL")
      goodsShipment.exportCountry.map(_.id) mustBe Some("PL")
      goodsShipment.warehouse.flatMap(_.id) mustBe Some("1234567GB")
      goodsShipment.warehouse.map(_.typeCode) mustBe Some("R")
      goodsShipment.consignment must be(defined)
      goodsShipment.consignment.flatMap(_.goodsLocation) must be(defined)
      goodsShipment.consignment.flatMap(_.goodsLocation).flatMap(_.address) must be(defined)
      goodsShipment.consignment.flatMap(_.goodsLocation).flatMap(_.id) mustBe Some("Additional identifier")
      goodsShipment.consignment.flatMap(_.goodsLocation).flatMap(_.typeCode) mustBe Some("T")
      goodsShipment.consignment.flatMap(_.goodsLocation).flatMap(_.address) must be(defined)
      goodsShipment.consignment.flatMap(_.departureTransportMeans).flatMap(_.id) must be(defined)
      goodsShipment.consignment.flatMap(_.departureTransportMeans).flatMap(_.identificationTypeCode) must be(defined)
      goodsShipment.consignment.flatMap(_.containerCode) mustBe Some("1")

      goodsShipment.governmentAgencyGoodsItems.size mustBe 10
      goodsShipment.governmentAgencyGoodsItems.map(_.governmentProcedures.size mustBe 6)
      goodsShipment.governmentAgencyGoodsItems.map(_.additionalDocuments.size mustBe 8)
      goodsShipment.governmentAgencyGoodsItems.map(_.additionalInformations.size mustBe 7)

      (goodsShipment.governmentAgencyGoodsItems zip expectedItems).map {
        case (actual, expected) => actual.governmentProcedures mustBe expected.governmentProcedures
          actual.additionalDocuments mustBe expected.additionalDocuments
          actual.additionalInformations mustBe expected.additionalInformations
          actual.commodity mustBe expected.commodity
      }

      WcoMetadataMapping.declarationUcr(result.declaration) mustBe Some("8GB123456789012-1234567890QWERTYUIO")
    }
  }

}
