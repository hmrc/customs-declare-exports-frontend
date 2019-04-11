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

import base.TestHelper._
import forms.declaration.Document.AllowedValues.TemporaryStorage
import forms.declaration._
import generators.Generators
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import org.scalacheck.Gen.listOfN
import uk.gov.hmrc.wco.dec._
import services.Countries.allCountries
import scala.util.Random
import forms.declaration.TransportCodes._

trait GoodsItemCachingData extends Generators {

  def intBetween(min: Int, max: Int): Int = min + Random.nextInt((max - min) + 1)

  def decimalString(): String = Random.nextDouble().toString

  def createPackageInformation(): PackageInformation = PackageInformation(
    Some(createRandomAlphanumericString(2)),
    Some(Random.nextInt(20)),
    shippingMarks = Some(createRandomAlphanumericString(150))
  )

  def createProcedureCodesData(): ProcedureCodesData =
    ProcedureCodesData(
      Some(intBetween(1000, 9999).toString),
      getDataSeq(10, createRandomAlphanumericString(Random.nextInt(5)))
    )

  def createCommodityMeasure(): CommodityMeasure =
    CommodityMeasure(Some(Random.nextDouble().toString), Random.nextDouble().toString, Random.nextDouble().toString)

  def createAdditionalInformation() =
    forms.declaration.AdditionalInformation(createRandomAlphanumericString(5), createRandomString(70))

  def createAdditionalInformationData() = AdditionalInformationData(getDataSeq(5, createAdditionalInformation()))

  def createDocsProduced(): DocumentsProduced = DocumentsProduced(
    Some(createRandomAlphanumericString(4)),
    Some(createRandomAlphanumericString(30)),
    Some(createRandomAlphanumericString(5)),
    Some(createRandomAlphanumericString(2)),
    Some(createRandomAlphanumericString(35)),
    Some(BigDecimal(123))
  )
  def createDocumentsProducedData() = DocumentsProducedData(getDataSeq(Random.nextInt(10), createDocsProduced()))

  def createItemType(): ItemType = ItemType(
    createRandomAlphanumericString(8),
    getDataSeq(Random.nextInt(10), createRandomAlphanumericString(4)),
    getDataSeq(Random.nextInt(10), createRandomAlphanumericString(4)),
    createRandomString(70),
    Some(createRandomAlphanumericString(8)),
    Some(createRandomAlphanumericString(4)),
    decimalString()
  )

  def createAmount(): Amount = Amount(value = Some(Random.nextDouble()))

  def createMeasure(): Measure = Measure(value = Some(Random.nextDouble()))

  def createClassification(): Classification =
    Classification(
      Some(createRandomAlphanumericString(4)),
      identificationTypeCode = Some(createRandomAlphanumericString(4))
    )

  def createGovernmentProcedure(): GovernmentProcedure =
    GovernmentProcedure(Some(createRandomAlphanumericString(8)), Some(createRandomAlphanumericString(4)))

  def createPackaging(): Packaging =
    Packaging(
      Some(Random.nextInt()),
      Some(createRandomAlphanumericString(2)),
      Some(Random.nextInt(20)),
      Some(createRandomAlphanumericString(150))
    )

  def createCommodity(): Commodity = Commodity(
    classifications = getDataSeq(10, createClassification),
    goodsMeasure = Some(GoodsMeasure(Some(createMeasure), Some(createMeasure), Some(createMeasure)))
  )

  def createWcoAdditionalInformation =
    uk.gov.hmrc.wco.dec.AdditionalInformation(Some(createRandomAlphanumericString(5)), Some(createRandomString(70)))

  def createAdditionalDocument(): GovernmentAgencyGoodsItemAdditionalDocument =
    GovernmentAgencyGoodsItemAdditionalDocument(
      Some(createRandomAlphanumericString(5)),
      typeCode = Some(createRandomAlphanumericString(3)),
      id = Some(createRandomAlphanumericString(5)),
      lpcoExemptionCode = Some(createRandomAlphanumericString(5)),
      name = Some(createRandomAlphanumericString(5)),
      writeOff = Some(WriteOff(Some(createMeasure)))
    )

  def createGovernmentAgencyGoodsItem(index: Int) = GovernmentAgencyGoodsItem(
    sequenceNumeric = index,
    statisticalValueAmount = Some(createAmount),
    additionalDocuments = getDataSeq(8, createAdditionalDocument),
    additionalInformations = getDataSeq(7, createWcoAdditionalInformation),
    commodity = Some(createCommodity),
    governmentProcedures = getDataSeq(6, createGovernmentProcedure)
  )

  def createGovernmentAgencyGoodsItemSeq(size: Int = 5): Seq[GovernmentAgencyGoodsItem] =
    for (i <- 1 to size) yield createGovernmentAgencyGoodsItem(i)

  def createDocument() = Document(
    documentCategory = TemporaryStorage,
    documentType = createRandomAlphanumericString(4),
    documentReference = createRandomAlphanumericString(30),
    goodsItemIdentifier = Some(Random.nextInt(100).toString)
  )

  def createPreviousDocumentsData(size: Int) = PreviousDocumentsData(getDataSeq(size, createDocument()))

  def createSeals(size: Int) = listOfN(size, sealArbitrary.arbitrary).suchThat(_.size == size)

  def getBorderTransport() =
    BorderTransport(
      allowedModeOfTransportCodes.toSeq(intBetween(1, 5)),
      allowedMeansOfTransportTypeCodes.toSeq(intBetween(1, 5)),
      Some(createRandomAlphanumericString(4))
    )

  def getTransportDetails() =
    TransportDetails(
      Some(allCountries(intBetween(1, 20)).countryName),
      true,
      allowedMeansOfTransportTypeCodes.toSeq(intBetween(1, 5)),
      Some(createRandomAlphanumericString(20))
    )

}
