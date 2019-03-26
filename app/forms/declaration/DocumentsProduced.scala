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

package forms.declaration

import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.{JsValue, Json}
import utils.validators.forms.FieldValidator._

case class DocumentsProduced(
  documentTypeCode: Option[String],
  documentIdentifier: Option[String],
  documentPart: Option[String],
  documentStatus: Option[String],
  documentStatusReason: Option[String],
  documentQuantity: Option[BigDecimal]
) {
  implicit val writes = Json.writes[DocumentsProduced]

  def isDefined: Boolean =
    List(documentTypeCode, documentIdentifier, documentPart, documentStatus, documentStatusReason, documentQuantity)
      .exists(_.isDefined)

  def toJson: JsValue = Json.toJson(this)
}

object DocumentsProduced {

  implicit val format = Json.format[DocumentsProduced]

  val formId = "Document"

  private val documentQuantityMaxLength = 16
  private val documentQuantityMaxDecimalPlaces = 6

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
    ),
    "documentQuantity" ->
      optional(bigDecimal
        .verifying("supplementary.addDocument.documentQuantity.precision.error", _.precision <= 16)
        .verifying("supplementary.addDocument.documentQuantity.scale.error", _.scale <= 6)
        .verifying("supplementary.addDocument.documentQuantity.error", _ >= 0))

  )(DocumentsProduced.apply)(DocumentsProduced.unapply)

  def form(): Form[DocumentsProduced] = Form(mapping)

  def fromJson(str: JsValue): DocumentsProduced = Json.fromJson(str).get
}
