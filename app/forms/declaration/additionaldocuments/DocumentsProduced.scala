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
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class DocumentsProduced(
  documentTypeCode: Option[String],
  documentIdentifierAndPart: Option[DocumentIdentifierAndPart],
  documentStatus: Option[String],
  documentStatusReason: Option[String],
  issuingAuthorityName: Option[String],
  dateOfValidity: Option[Date],
  measurementUnit: Option[String],
  documentQuantity: Option[BigDecimal]
) {
  implicit val writes = Json.writes[DocumentsProduced]

  def isDefined: Boolean =
    List(
      documentTypeCode,
      documentIdentifierAndPart,
      documentStatus,
      documentStatusReason,
      issuingAuthorityName,
      dateOfValidity,
      measurementUnit,
      documentQuantity
    ).exists(_.isDefined)
}

object DocumentsProduced {

  implicit val format = Json.format[DocumentsProduced]

  private val issuingAuthorityNameMaxLength = 70
  private val measurementUnitMaxLength = 4
  private val documentQuantityMaxLength = 16
  private val documentQuantityMaxDecimalPlaces = 6

  val documentTypeCodeKey = "documentTypeCode"
  val documentIdentifierAndPartKey = "documentIdentifierAndPart"
  val documentStatusKey = "documentStatus"
  val documentStatusReasonKey = "documentStatusReason"
  val issuingAuthorityNameKey = "issuingAuthorityName"
  val dateOfValidityKey = "dateOfValidity"
  val measurementUnitKey = "measurementUnit"
  val documentQuantityKey = "documentQuantity"

  val mapping = Forms
    .mapping(
      documentTypeCodeKey -> optional(
        text().verifying("supplementary.addDocument.documentTypeCode.error", hasSpecificLength(4) and isAlphanumeric)
      ),
      documentIdentifierAndPartKey -> optional(DocumentIdentifierAndPart.mapping),
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
      measurementUnitKey -> optional(
        text()
          .verifying(
            "supplementary.addDocument.measurementUnit.error.length",
            hasSpecificLength(measurementUnitMaxLength)
          )
          .verifying("supplementary.addDocument.measurementUnit.error.specialCharacters", isAlphanumeric)
      ),
      documentQuantityKey ->
        optional(
          bigDecimal
            .verifying(
              "supplementary.addDocument.documentQuantity.error.precision",
              _.precision <= documentQuantityMaxLength
            )
            .verifying(
              "supplementary.addDocument.documentQuantity.error.scale",
              _.scale <= documentQuantityMaxDecimalPlaces
            )
            .verifying("supplementary.addDocument.documentQuantity.error", _ >= 0)
        )
    )(DocumentsProduced.apply)(DocumentsProduced.unapply)
    .verifying(
      "supplementary.addDocument.error.measurementUnitAndQuantity",
      validateMeasurementUnitAndDocumentQuantity(_)
    )

  private def validateMeasurementUnitAndDocumentQuantity(doc: DocumentsProduced): Boolean =
    (doc.measurementUnit.isEmpty && doc.documentQuantity.isEmpty) || (doc.measurementUnit.nonEmpty && doc.documentQuantity.nonEmpty)

  def form(): Form[DocumentsProduced] = Form(mapping)
}
