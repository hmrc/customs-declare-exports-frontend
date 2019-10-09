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

package forms.declaration.additionaldocuments

import forms.common.Date
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.{JsValue, Json}
import utils.validators.forms.FieldValidator._

case class DocumentsProduced(
  documentTypeCode: Option[String],
  documentIdentifier: Option[String],
  documentStatus: Option[String],
  documentStatusReason: Option[String],
  issuingAuthorityName: Option[String],
  dateOfValidity: Option[Date],
  documentWriteOff: Option[DocumentWriteOff]
) {
  def toJson: JsValue = Json.toJson(this)(DocumentsProduced.format)

  implicit val writes = Json.writes[DocumentsProduced]

  def isDefined: Boolean =
    List(
      documentTypeCode,
      documentIdentifier,
      documentStatus,
      documentStatusReason,
      issuingAuthorityName,
      dateOfValidity,
      documentWriteOff
    ).exists(_.isDefined)
}

object DocumentsProduced {

  def fromJsonString(value: String): Option[DocumentsProduced] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[DocumentsProduced]

  private val issuingAuthorityNameMaxLength = 70

  val documentTypeCodeKey = "documentTypeCode"
  val documentIdentifierKey = "documentIdentifier"
  val documentStatusKey = "documentStatus"
  val documentStatusReasonKey = "documentStatusReason"
  val issuingAuthorityNameKey = "issuingAuthorityName"
  val dateOfValidityKey = "dateOfValidity"
  val documentWriteOffKey = "documentWriteOff"

  val mapping = Forms
    .mapping(
      documentTypeCodeKey -> optional(
        text().verifying("supplementary.addDocument.documentTypeCode.error", hasSpecificLength(4) and isAlphanumeric)
      ),
      documentIdentifierKey -> optional(
        text().verifying(
          "supplementary.addDocument.documentIdentifier.error",
          nonEmpty and isAlphanumericWithAllowedSpecialCharacters and noLongerThan(35)
        )
      ),
      documentStatusKey -> optional(
        text().verifying("supplementary.addDocument.documentStatus.error", noLongerThan(2) and isAllCapitalLetter)
      ),
      documentStatusReasonKey -> optional(
        text().verifying("supplementary.addDocument.documentStatusReason.error", noLongerThan(35) and isAlphanumeric)
      ),
      issuingAuthorityNameKey -> optional(
        text()
          .verifying(
            "supplementary.addDocument.issuingAuthorityName.error.length",
            noLongerThan(issuingAuthorityNameMaxLength)
          )
      ),
      dateOfValidityKey -> optional(Date.mapping),
      documentWriteOffKey -> optional(DocumentWriteOff.mapping)
    )(DocumentsProduced.apply)(DocumentsProduced.unapply)

  def form(): Form[DocumentsProduced] = Form(mapping)
}
