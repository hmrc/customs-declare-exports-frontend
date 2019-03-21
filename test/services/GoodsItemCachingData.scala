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
import forms.supplementary._
import models.declaration.supplementary.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}

import scala.util.Random

trait GoodsItemCachingData {

  val maxStringSize = 150

  def maxRandomString(max: Int = maxStringSize) = Random.nextString(max)

  def intBetween(min: Int, max: Int) = min + Random.nextInt((max - min) + 1)

  def decimalString() = Random.nextDouble().toString

  def createPackageInformation() = PackageInformation(
    Some(createRandomString(2)),
    Some(Random.nextInt(20)),
    shippingMarks = Some(createRandomString(150))
  )

  def createProcedureCodesData() =
    ProcedureCodesData(Some(intBetween(1000, 9999).toString), getDataSeq(10, createRandomString(Random.nextInt(5))))

  def createCommodityMeasure() =
    CommodityMeasure(Some(Random.nextDouble().toString), Random.nextDouble().toString, Random.nextDouble().toString)

  def createAdditionalInformation() = AdditionalInformation(createRandomString(5), maxRandomString(70))

  def additionalInformationData() = AdditionalInformationData(getDataSeq(5, createAdditionalInformation()))

  def createDocsProduced() = DocumentsProduced(
    Some(createRandomString(4)),
    Some(createRandomString(30)),
    Some(createRandomString(5)),
    Some(createRandomString(2)),
    Some(createRandomString(35)),
    Some(decimalString())
  )
  def documentsProducedData() = DocumentsProducedData(getDataSeq(Random.nextInt(10), createDocsProduced()))

  def getItemType() = ItemType(
    createRandomString(8),
    getDataSeq(Random.nextInt(10), createRandomString(4)),
    getDataSeq(Random.nextInt(10), createRandomString(4)),
    maxRandomString(70),
    Some(createRandomString(8)),
    decimalString()
  )

}
