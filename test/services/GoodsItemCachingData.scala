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
import forms.declaration._
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import uk.gov.hmrc.wco.dec._

import scala.util.Random

trait GoodsItemCachingData {

  val maxStringSize = 150

  def maxRandomString(max: Int = maxStringSize): String = Random.nextString(max)

  def intBetween(min: Int, max: Int): Int = min + Random.nextInt((max - min) + 1)

  def decimalString(): String = Random.nextDouble().toString

  def createPackageInformation(): PackageInformation = PackageInformation(
    Some(createRandomString(2)),
    Some(Random.nextInt(20)),
    shippingMarks = Some(createRandomString(150))
  )

  def createProcedureCodesData(): ProcedureCodesData =
    ProcedureCodesData(Some(intBetween(1000, 9999).toString), getDataSeq(10, createRandomString(Random.nextInt(5))))

  def createCommodityMeasure(): CommodityMeasure =
    CommodityMeasure(Some(Random.nextDouble().toString), Random.nextDouble().toString, Random.nextDouble().toString)

  def createAdditionalInformation() =
    forms.declaration.AdditionalInformation(createRandomString(5), maxRandomString(70))

  def additionalInformationData() = AdditionalInformationData(getDataSeq(5, createAdditionalInformation()))

  def createDocsProduced(): DocumentsProduced = DocumentsProduced(
    Some(createRandomString(4)),
    Some(createRandomString(30)),
    Some(createRandomString(5)),
    Some(createRandomString(2)),
    Some(createRandomString(35)),
    Some(BigDecimal(123))
  )
  def documentsProducedData() = DocumentsProducedData(getDataSeq(Random.nextInt(10), createDocsProduced()))

  def getItemType(): ItemType = ItemType(
    createRandomString(8),
    getDataSeq(Random.nextInt(10), createRandomString(4)),
    getDataSeq(Random.nextInt(10), createRandomString(4)),
    maxRandomString(70),
    Some(createRandomString(8)),
    decimalString()
  )

  def amount(): Amount = Amount(value = Some(Random.nextDouble()))
  def measure(): Measure = Measure(value = Some(Random.nextDouble()))
  def classification(): Classification =
    Classification(Some(createRandomString(4)), identificationTypeCode = Some(createRandomString(4)))
  def govProcedures(): GovernmentProcedure =
    GovernmentProcedure(Some(createRandomString(8)), Some(createRandomString(4)))
  def packaging(): Packaging =
    Packaging(
      Some(Random.nextInt()),
      Some(createRandomString(2)),
      Some(Random.nextInt(20)),
      Some(createRandomString(150))
    )

  def commodityData(): Commodity = Commodity(
    classifications = getDataSeq(10, classification),
    goodsMeasure = Some(GoodsMeasure(Some(measure), Some(measure), Some(measure)))
  )
  def addInfo = uk.gov.hmrc.wco.dec.AdditionalInformation(Some(createRandomString(5)), Some(maxRandomString(70)))

  def addDocs(): GovernmentAgencyGoodsItemAdditionalDocument =
    GovernmentAgencyGoodsItemAdditionalDocument(
      Some(createRandomString(5)),
      typeCode = Some(createRandomString(3)),
      id = Some(createRandomString(5)),
      lpcoExemptionCode = Some(createRandomString(5)),
      name = Some(createRandomString(5)),
      writeOff = Some(WriteOff(Some(measure)))
    )

  def goodsItem(index: Int) = GovernmentAgencyGoodsItem(
    sequenceNumeric = index,
    statisticalValueAmount = Some(amount),
    additionalDocuments = getDataSeq(8, addDocs),
    additionalInformations = getDataSeq(7, addInfo),
    commodity = Some(commodityData),
    governmentProcedures = getDataSeq(6, govProcedures)
  )
  def goodsItemSeq(size:Int = 5): Seq[GovernmentAgencyGoodsItem] = for (i <- 1 to size) yield goodsItem(i)

}
