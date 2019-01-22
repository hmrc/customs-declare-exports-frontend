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

package forms.supplementary

import play.api.data.{Form, Forms}
import play.api.data.Forms._
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class Document(
  documentTypeCode: Option[String],
  documentIdentifier: Option[String],
  documentPart: Option[String],
  documentStatus: Option[String],
  documentStatusReason: Option[String]
)

object Document {
  implicit val format = Json.format[Document]

  val formId = "Document"

  val mapping = Forms.mapping(
    "documentTypeCode" -> optional(
      text().verifying("supplementary.addDocument.documentTypeCode.error", hasSpecificLength(4) and isTailNumeric)
    ),
    "documentIdentifier" -> optional(
      text().verifying("supplementary.addDocument.documentIdentifier.error", isAlphanumeric and noLongerThan(30))
    ),
    "documentPart" -> optional(
      text().verifying("supplementary.addDocument.documentPart.error", isAlphanumeric and noLongerThan(5))
    ),
    "documentStatus" -> optional(
      text().verifying("supplementary.addDocument.documentStatus.error", noLongerThan(2) and isAllCapitalLetter)
    ),
    "documentStatusReason" -> optional(
      text().verifying("supplementary.addDocument.documentStatusReason.error", noLongerThan(35) and isAlphanumeric)
    )
  )(Document.apply)(Document.unapply)

  def form(): Form[Document] = Form(mapping)

  def toMetadataProperties(document: Document): Map[String, String] =
    Map(
      "declaration.goodsShipment.government.agencyGoodsItem.additionalDocument.categoryCode" ->
        document.documentTypeCode.flatMap(_.headOption).fold("")(_.toString),
      "declaration.goodsShipment.government.agencyGoodsItem.additionalDocument.typeCode" ->
        document.documentTypeCode.map(_.drop(1).toString).getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItem.additionalDocument.ID" ->
        (document.documentIdentifier.getOrElse("") + document.documentPart.getOrElse("")),
      "declaration.goodsShipment.government.AgencyGoodsItem.additionalDocument.lpcoExemptionCode" ->
        document.documentStatus.getOrElse(""),
      "declaration.goodsShipment.government.AgencyGoodsItem.additionalDocument.name" ->
        document.documentStatusReason.getOrElse("")
    )
}
