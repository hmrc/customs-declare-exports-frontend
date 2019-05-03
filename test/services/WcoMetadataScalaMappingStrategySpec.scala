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
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.SupplementaryDeclarationDataSpec.cacheMapAllRecords
import models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem
import org.scalatest.OptionValues
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.{BorderTransportMeans, Consignment, GoodsShipment}

class WcoMetadataScalaMappingStrategySpec extends CustomExportsBaseSpec with GoodsItemCachingData with OptionValues {

  val expectedItems: Seq[GovernmentAgencyGoodsItem] = createGovernmentAgencyGoodsItemSeq(10)

  lazy val expectedWcoItems: Seq[uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItem] = createWcoGovernmentAgencyGoodsItems(
    expectedItems
  )

  val expectedPreviousDocs = createPreviousDocumentsData(6)
  val previousDocsCache = getCacheMap(expectedPreviousDocs, "PreviousDocuments")
  val expectedSeals = createSeals(10).sample.getOrElse(Seq.empty)
  val sealsCache = getCacheMap(expectedSeals, "Seal")
  val borderTransport = getBorderTransport
  val transportDetails = getTransportDetails
  val borderTransportCache = getCacheMap(borderTransport, "BorderTransport")
  val transportDetailsCache = getCacheMap(transportDetails, "TransportDetails")

  "WcoMetadataScalaMappingSpec" should {
    "produce metadata" in {
      val goodsItemCache = getCacheMap(expectedItems, "exportItems")
      val cacheMap =
        CacheMap(
          id = "eoriForCache",
          data = cacheMapAllRecords.data ++ goodsItemCache.data ++ previousDocsCache.data ++ sealsCache.data
            ++ borderTransportCache.data ++ transportDetailsCache.data
        )

      val mapper = new WcoMetadataMapper with WcoMetadataScalaMappingStrategy
      val result = mapper.produceMetaData(cacheMap)

      result.declaration must be(defined)

      result.declaration.flatMap(_.typeCode) mustBe Some("EXY")
      result.declaration.flatMap(_.functionalReferenceId) mustBe Some("123ABC")
      result.declaration.flatMap(_.goodsShipment).flatMap(_.ucr).flatMap(_.traderAssignedReferenceId) mustBe
        Some("8GB123456789012-1234567890QWERTYUIO")

      result.declaration.flatMap(_.exporter) must be(defined)
      result.declaration.flatMap(_.declarant) must be(defined)
      result.declaration.flatMap(_.agent) must be(defined)
      result.declaration.flatMap(_.invoiceAmount).flatMap(_.value) must be(Some(BigDecimal("1212312.12")))
      result.declaration.flatMap(_.invoiceAmount).flatMap(_.currencyId) must be(Some("GBP"))
      result.declaration.value.authorisationHolders.size mustBe 1
      result.declaration.flatMap(_.exitOffice).flatMap(_.id) mustBe Some("123qwe12")
      result.declaration.flatMap(_.goodsShipment) must be(defined)
      val goodsShipment = result.declaration.flatMap(_.goodsShipment).value
      goodsShipment.aeoMutualRecognitionParties.headOption mustBe defined

      goodsShipment.destination.flatMap(_.countryCode) mustBe Some("PL")
      goodsShipment.exportCountry.map(_.id) mustBe Some("PL")
      goodsShipment.warehouse.flatMap(_.id) mustBe Some("1234567GB")
      goodsShipment.warehouse.map(_.typeCode) mustBe Some("R")
      goodsShipment.consignment must be(defined)
      assertGoodsItem(goodsShipment)
      assertGoodsLocation(goodsShipment.consignment)
      assertPreviousDocuments(goodsShipment)
      assertTransportEquipment(goodsShipment.consignment.value)
      assertTransportMeans(goodsShipment.consignment)
      assertBorderTransportMeans(result.declaration.flatMap(_.borderTransportMeans))

      mapper.declarationUcr(result) mustBe Some("8GB123456789012-1234567890QWERTYUIO")
      mapper.declarationLrn(result) mustBe Some("123ABC")
    }
  }

  private def assertBorderTransportMeans(borderTransportMeans: Option[BorderTransportMeans]) = {
    borderTransportMeans mustBe defined
    borderTransportMeans.value.modeCode.value.toString mustBe borderTransport.borderModeOfTransportCode
    borderTransportMeans.value.identificationTypeCode.value mustBe transportDetails.meansOfTransportCrossingTheBorderType
    borderTransportMeans.value.id mustBe transportDetails.meansOfTransportCrossingTheBorderIDNumber
    borderTransportMeans.value.registrationNationalityCode mustBe
      allCountries
        .find(_.countryName == transportDetails.meansOfTransportCrossingTheBorderNationality.value)
        .map(_.countryCode)
  }

  private def assertTransportMeans(consignment: Option[Consignment]) = {
    consignment.value.departureTransportMeans.map { actual =>
      actual.identificationTypeCode.value mustBe borderTransport.meansOfTransportOnDepartureType
      actual.id mustBe borderTransport.meansOfTransportOnDepartureIDNumber
    }
    consignment.value.containerCode.value mustBe "1"

  }
  private def assertGoodsLocation(consignment: Option[Consignment]) = {
    consignment.flatMap(_.goodsLocation) must be(defined)
    val goodsLocation = consignment.flatMap(_.goodsLocation).value
    goodsLocation.id mustBe Some("Additional identifier")
    goodsLocation.typeCode mustBe Some("T")
    goodsLocation.address must be(defined)
    goodsLocation.address.value.typeCode.value mustBe ("Q")
    goodsLocation.address.value.postcodeId.value mustBe ("Postcode")
    goodsLocation.address.value.countryCode.value mustBe ("GB")
    goodsLocation.address.value.cityName.value mustBe ("City")

  }

  private def assertGoodsItem(goodsShipment: GoodsShipment) = {
    goodsShipment.governmentAgencyGoodsItems.size mustBe 10
    goodsShipment.governmentAgencyGoodsItems.map(_.governmentProcedures.size mustBe 6)
    goodsShipment.governmentAgencyGoodsItems.map(_.additionalDocuments.size mustBe 8)
    goodsShipment.governmentAgencyGoodsItems.map(_.additionalInformations.size mustBe 7)

    (goodsShipment.governmentAgencyGoodsItems zip expectedWcoItems).map {
      case (actual, expected) =>
        actual.governmentProcedures mustBe expected.governmentProcedures
        actual.additionalDocuments mustBe expected.additionalDocuments
        actual.additionalInformations mustBe expected.additionalInformations
        actual.commodity mustBe expected.commodity
    }
  }
  private def assertPreviousDocuments(goodsShipment: GoodsShipment) = {
    goodsShipment.previousDocuments.size mustBe expectedPreviousDocs.documents.size

    (goodsShipment.previousDocuments zip expectedPreviousDocs.documents).map {
      case (actual, expected) =>
        actual.categoryCode.value mustBe expected.documentCategory
        actual.typeCode.value mustBe expected.documentType
        actual.id.value mustBe expected.documentReference
        actual.lineNumeric.value mustBe expected.goodsItemIdentifier.value.toInt
    }
  }

  private def assertTransportEquipment(consignment: Consignment) = {
    val actualTransportEquipment = consignment.transportEquipments.headOption.value
    actualTransportEquipment.sequenceNumeric mustBe expectedSeals.size
    (actualTransportEquipment.seals zip expectedSeals.zipWithIndex).map {
      case (actual, (expected, index)) =>
        actual.id.value mustBe expected.id
        actual.sequenceNumeric mustBe (index + 1)
    }
  }
}
