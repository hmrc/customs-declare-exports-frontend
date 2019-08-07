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
import forms.declaration._
import forms.declaration.additionaldocuments.{DocumentIdentifierAndPart, DocumentWriteOff, DocumentsProduced}
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}

import scala.util.Random

trait GoodsItemCachingData {

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

  def createAdditionalInformation(): forms.declaration.AdditionalInformation =
    forms.declaration.AdditionalInformation(createRandomAlphanumericString(5), createRandomString(70))

  def createFiscalReferences(): AdditionalFiscalReferencesData = AdditionalFiscalReferencesData(
    Seq(AdditionalFiscalReference("FR", createRandomAlphanumericString(8)))
  )
}
