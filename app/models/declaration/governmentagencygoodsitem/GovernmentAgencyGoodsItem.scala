/*
 * Copyright 2023 HM Revenue & Customs
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

package models.declaration.governmentagencygoodsitem

import forms.declaration.{AdditionalFiscalReference, AdditionalInformation}
import play.api.libs.json.Json

object Formats {

  implicit val amountFormat = Json.format[Amount]
  implicit val measureFormat = Json.format[Measure]
  implicit val dateTimeStringFormat = Json.format[DateTimeString]
  implicit val dateTimeElementFormat = Json.format[DateTimeElement]
  implicit val classificationFormat = Json.format[Classification]
  implicit val packagingFormat = Json.format[Packaging]
  implicit val governmentProcedureFormat = Json.format[GovernmentProcedure]
  implicit val writeOffFormat = Json.format[WriteOff]
  implicit val governmentAgencyGoodsItemAdditionalDocumentSubmitterformat =
    Json.format[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter]
  implicit val governmentAgencyGoodsItemAdditionalDocumentFormat =
    Json.format[GovernmentAgencyGoodsItemAdditionalDocument]
  implicit val goodsMeasureFormat = Json.format[GoodsMeasure]
  implicit val dangerousGoodsFormat = Json.format[DangerousGoods]
  implicit val commodityFormat = Json.format[Commodity]
  implicit val governmentAgencyGoodsItemFormat = Json.format[GovernmentAgencyGoodsItem]
}
case class GovernmentAgencyGoodsItem(
  sequenceNumeric: Int,
  statisticalValueAmount: Option[Amount] = None,
  commodity: Option[Commodity] = None,
  additionalInformations: Seq[AdditionalInformation] = Seq.empty,
  additionalDocuments: Seq[GovernmentAgencyGoodsItemAdditionalDocument] = Seq.empty,
  governmentProcedures: Seq[GovernmentProcedure] = Seq.empty,
  packagings: Seq[Packaging] = Seq.empty,
  fiscalReferences: Seq[AdditionalFiscalReference] = Seq.empty
)

case class Amount(currencyId: Option[String] = None, value: Option[BigDecimal] = None)

case class Classification(
  id: Option[String] = None,
  nameCode: Option[String] = None,
  identificationTypeCode: Option[String] = None,
  bindingTariffReferenceId: Option[String] = None
)

case class Packaging(
  sequenceNumeric: Option[Int] = None,
  marksNumbersId: Option[String] = None,
  quantity: Option[Int] = None,
  typeCode: Option[String] = None
)

case class GovernmentProcedure(currentCode: Option[String] = None, previousCode: Option[String] = None)

case class GovernmentAgencyGoodsItemAdditionalDocument(
  categoryCode: Option[String] = None,
  effectiveDateTime: Option[DateTimeElement] = None,
  id: Option[String] = None,
  name: Option[String] = None,
  typeCode: Option[String] = None,
  lpcoExemptionCode: Option[String] = None,
  submitter: Option[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter] = None,
  writeOff: Option[WriteOff] = None
)

case class WriteOff(quantity: Option[Measure] = None, amount: Option[Amount] = None)

case class GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name: Option[String] = None, roleCode: Option[String] = None)

case class DateTimeElement(dateTimeString: DateTimeString)

case class DateTimeString(formatCode: String, value: String)
