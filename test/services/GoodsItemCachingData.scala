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
import forms.common.DateSpec.correctDate
import forms.declaration.Document.AllowedValues.TemporaryStorage
import forms.declaration.TransportCodes._
import forms.declaration.{Seal, _}
import forms.declaration.additionaldocuments.{DocumentIdentifierAndPart, DocumentWriteOff, DocumentsProduced}
import generators.Generators
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import org.scalacheck.Gen
import org.scalacheck.Gen.listOfN
import services.Countries.allCountries
import uk.gov.hmrc.wco.dec.{Amount, Classification, Commodity, GoodsMeasure, GovernmentAgencyGoodsItem, GovernmentAgencyGoodsItemAdditionalDocument, GovernmentProcedure, Measure, Packaging, WriteOff}

import scala.util.Random

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
      getDataSeq(10, createRandomAlphanumericString, Random.nextInt(5))
    )

  def createCommodityMeasure(): CommodityMeasure =
    CommodityMeasure(Some(Random.nextDouble().toString), Random.nextDouble().toString, Random.nextDouble().toString)

  def createAdditionalInformationData() = AdditionalInformationData(getDataSeq(5, createAdditionalInformation))

  def createDocumentsProducedData() = DocumentsProducedData(getDataSeq(10, createDocsProduced))

  def createDocsProduced(): DocumentsProduced = DocumentsProduced(
    documentTypeCode = Some(createRandomAlphanumericString(4)),
    documentIdentifierAndPart = Some(createDocumentIdentifierAndPart()),
    documentStatus = Some(createRandomAlphanumericString(2)),
    documentStatusReason = Some(createRandomAlphanumericString(35)),
    issuingAuthorityName = Some(createRandomAlphanumericString(70)),
    dateOfValidity = Some(correctDate),
    documentWriteOff = Some(createDocumentWriteOff())
  )

  private def createDocumentIdentifierAndPart(): DocumentIdentifierAndPart = DocumentIdentifierAndPart(
    documentIdentifier = Some(createRandomAlphanumericString(30)),
    documentPart = Some(createRandomAlphanumericString(5))
  )

  private def createDocumentWriteOff(): DocumentWriteOff = DocumentWriteOff(
    measurementUnit = Some(createRandomAlphanumericString(4)),
    documentQuantity = Some(BigDecimal(123))
  )

  def createItemType(): ItemType = ItemType(
    createRandomAlphanumericString(8),
    getDataSeq(Random.nextInt(10), createRandomAlphanumericString, 4),
    getDataSeq(Random.nextInt(10), createRandomAlphanumericString, 4),
    createRandomString(70),
    Some(createRandomAlphanumericString(8)),
    Some(createRandomAlphanumericString(4)),
    decimalString()
  )

  def createWcoAmount(): Amount = Amount(value = Some(Random.nextDouble()))

  def createAmount(): models.declaration.governmentagencygoodsitem.Amount = models.declaration.governmentagencygoodsitem.Amount(value = Some(Random.nextDouble()))

  def createWcoMeasure(): Measure = Measure(value = Some(Random.nextDouble()))

  def createMeasure(): models.declaration.governmentagencygoodsitem.Measure = models.declaration.governmentagencygoodsitem.Measure(value = Some(Random.nextDouble()))

  def createWcoClassification(): Classification =
   Classification(
      Some(createRandomAlphanumericString(4)),
      identificationTypeCode = Some(createRandomAlphanumericString(4))
    )

  def createClassification(): models.declaration.governmentagencygoodsitem.Classification =
    models.declaration.governmentagencygoodsitem.Classification(
      Some(createRandomAlphanumericString(4)),
      identificationTypeCode = Some(createRandomAlphanumericString(4))
    )

  def createWcoGovernmentProcedure(): GovernmentProcedure =
    GovernmentProcedure(Some(createRandomAlphanumericString(8)), Some(createRandomAlphanumericString(4)))

  def createGovernmentProcedure(): models.declaration.governmentagencygoodsitem.GovernmentProcedure =
    models.declaration.governmentagencygoodsitem.GovernmentProcedure(Some(createRandomAlphanumericString(8)), Some(createRandomAlphanumericString(4)))

  def createWcoPackaging(): Packaging =
   Packaging(
      Some(Random.nextInt()),
      Some(createRandomAlphanumericString(2)),
      Some(Random.nextInt(20)),
      Some(createRandomAlphanumericString(150))
    )

  def createPackaging(): models.declaration.governmentagencygoodsitem.Packaging =
    models.declaration.governmentagencygoodsitem.Packaging(
      Some(Random.nextInt()),
      Some(createRandomAlphanumericString(2)),
      Some(Random.nextInt(20)),
      Some(createRandomAlphanumericString(150))
    )

  def createWcoCommodity(): Commodity = Commodity(
    classifications = getDataSeq(10, createWcoClassification),
    goodsMeasure = Some(GoodsMeasure(Some(createWcoMeasure()), Some(createWcoMeasure()), Some(createWcoMeasure())))
  )

  def createCommodity(): models.declaration.governmentagencygoodsitem.Commodity = models.declaration.governmentagencygoodsitem.Commodity(
    classifications = getDataSeq(10, createClassification),
    goodsMeasure = Some(models.declaration.governmentagencygoodsitem.GoodsMeasure(Some(createMeasure()), Some(createMeasure()), Some(createMeasure())))
  )

  def createWcoAdditionalInformation(): uk.gov.hmrc.wco.dec.AdditionalInformation =
    uk.gov.hmrc.wco.dec.AdditionalInformation(Some(createRandomAlphanumericString(5)), Some(createRandomString(70)))


  def createAdditionalInformation(): forms.declaration.AdditionalInformation =
    forms.declaration.AdditionalInformation(createRandomAlphanumericString(5), createRandomString(70))


  def createWcoAdditionalDocument(): GovernmentAgencyGoodsItemAdditionalDocument =
    GovernmentAgencyGoodsItemAdditionalDocument(
      Some(createRandomAlphanumericString(5)),
      typeCode = Some(createRandomAlphanumericString(3)),
      id = Some(createRandomAlphanumericString(5)),
      lpcoExemptionCode = Some(createRandomAlphanumericString(5)),
      name = Some(createRandomAlphanumericString(5)),
      writeOff = Some(WriteOff(Some(createWcoMeasure())))
    )

  def createAdditionalDocument(): models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItemAdditionalDocument =
    models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItemAdditionalDocument(
      Some(createRandomAlphanumericString(5)),
      typeCode = Some(createRandomAlphanumericString(3)),
      id = Some(createRandomAlphanumericString(5)),
      lpcoExemptionCode = Some(createRandomAlphanumericString(5)),
      name = Some(createRandomAlphanumericString(5)),
      writeOff = Some(models.declaration.governmentagencygoodsitem.WriteOff(Some(createMeasure())))
    )

 def  createWcoGovernmentAgencyGoodsItem(index: Int) = GovernmentAgencyGoodsItem(
   sequenceNumeric = index,
   statisticalValueAmount = Some(createWcoAmount()),
   additionalDocuments = getDataSeq(8, createWcoAdditionalDocument),
   additionalInformations = getDataSeq(7, createWcoAdditionalInformation),
   commodity = Some(createWcoCommodity()),
   governmentProcedures = getDataSeq(6, createWcoGovernmentProcedure),
   packagings = getDataSeq(5, createWcoPackaging)
 )

  def createGovernmentAgencyGoodsItem(index: Int) = models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem(
    sequenceNumeric = index,
    statisticalValueAmount = Some(createAmount()),
    additionalDocuments = getDataSeq(8, createAdditionalDocument),
    additionalInformations = getDataSeq(7, createAdditionalInformation),
    commodity = Some(createCommodity()),
    governmentProcedures = getDataSeq(6, createGovernmentProcedure),
    packagings = getDataSeq(size = 5, createPackaging)
  )

  def createWcoGovernmentAgencyGoodsItems(expectedItems: Seq[models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem]): Seq[GovernmentAgencyGoodsItem] = {
    expectedItems.map(item => WcoMetadataScalaMapper.mapGoodsItem(item))
  }

  def createGovernmentAgencyGoodsItemSeq(size: Int = 5): Seq[models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem] =
    for (i <- 1 to size) yield createGovernmentAgencyGoodsItem(i)

  def createDocument() = Document(
    documentCategory = TemporaryStorage,
    documentType = createRandomAlphanumericString(4),
    documentReference = createRandomAlphanumericString(30),
    goodsItemIdentifier = Some(Random.nextInt(100).toString)
  )

  def createPreviousDocumentsData(size: Int) = PreviousDocumentsData(getDataSeq(size, createDocument))

  def createSeals(size: Int): Gen[List[Seal]] = listOfN(size, sealArbitrary.arbitrary).suchThat(_.size == size)

  def getBorderTransport: BorderTransport =
    BorderTransport(
      allowedModeOfTransportCodes.toSeq(intBetween(1, 5)),
      allowedMeansOfTransportTypeCodes.toSeq(intBetween(1, 5)),
      Some(createRandomAlphanumericString(4))
    )

  def getTransportDetails: TransportDetails =
    TransportDetails(
      Some(allCountries(intBetween(1, 20)).countryName),
      container = true,
      allowedMeansOfTransportTypeCodes.toSeq(intBetween(1, 5)),
      Some(createRandomAlphanumericString(20))
    )

}
