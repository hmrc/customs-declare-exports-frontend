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

import forms.MetadataPropertiesConvertable
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class Document(
  documentTypeCode: Option[String],
  documentIdentifier: Option[String],
  documentPart: Option[String],
  documentStatus: Option[String],
  documentStatusReason: Option[String]
) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].categoryCode" ->
        documentTypeCode.flatMap(_.headOption).fold("")(_.toString),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].typeCode" ->
        documentTypeCode.map(_.drop(1).toString).getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].id" ->
        (documentIdentifier.getOrElse("") + documentPart.getOrElse("")),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].lpcoExemptionCode" ->
        documentStatus.getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].name" ->
        documentStatusReason.getOrElse("")
    )
}

object Document {
  implicit val format = Json.format[Document]

  val formId = "Document"

  val mapping = Forms.mapping(
    "documentTypeCode" -> optional(
      text().verifying("supplementary.addDocument.documentTypeCode.error", hasSpecificLength(4) and isAlphanumeric)
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
}
