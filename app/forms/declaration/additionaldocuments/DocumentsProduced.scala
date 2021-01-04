/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.DeclarationPage
import forms.common.Date
import forms.declaration.additionaldocuments.DocumentWriteOff._
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
    List(documentTypeCode, documentIdentifier, documentStatus, documentStatusReason, issuingAuthorityName, dateOfValidity, documentWriteOff).exists(
      _.isDefined
    )
}

object DocumentsProduced extends DeclarationPage {

  def fromJsonString(value: String): Option[DocumentsProduced] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[DocumentsProduced]

  private val issuingAuthorityNameMaxLength = 70

  val documentTypeCodeKey = "documentTypeCode"
  val documentIdentifierKey = "documentIdentifier"
  val documentStatusKey = "documentStatus"
  val documentStatusReasonKey = "documentStatusReason"
  val issuingAuthorityNameKey = "issuingAuthorityName"
  val dateOfValidityKey = "dateOfValidity"

  val mapping = Forms
    .mapping(
      documentTypeCodeKey -> optional(text().verifying("declaration.addDocument.documentTypeCode.error", hasSpecificLength(4) and isAlphanumeric)),
      documentIdentifierKey -> optional(
        text()
          .verifying("declaration.addDocument.documentIdentifier.error", nonEmpty and isAlphanumericWithAllowedSpecialCharacters and noLongerThan(35))
      ),
      documentStatusKey -> optional(text().verifying("declaration.addDocument.documentStatus.error", noLongerThan(2) and isAlphabetic)),
      documentStatusReasonKey -> optional(
        text().verifying("declaration.addDocument.documentStatusReason.error", noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters)
      ),
      issuingAuthorityNameKey -> optional(
        text()
          .verifying("declaration.addDocument.issuingAuthorityName.error.length", noLongerThan(issuingAuthorityNameMaxLength))
      ),
      dateOfValidityKey -> optional(
        Date.mapping("declaration.addDocument.dateOfValidity.error.format", "declaration.addDocument.dateOfValidity.error.outOfRange")
      ),
      documentWriteOffKey -> optional(DocumentWriteOff.mapping)
    )(form2data)(DocumentsProduced.unapply)

  private def form2data(
    documentTypeCode: Option[String],
    documentIdentifier: Option[String],
    documentStatus: Option[String],
    documentStatusReason: Option[String],
    issuingAuthorityName: Option[String],
    dateOfValidity: Option[Date],
    documentWriteOff: Option[DocumentWriteOff]
  ): DocumentsProduced =
    new DocumentsProduced(
      documentTypeCode.map(_.toUpperCase),
      documentIdentifier,
      documentStatus.map(_.toUpperCase),
      documentStatusReason,
      issuingAuthorityName,
      dateOfValidity,
      documentWriteOff
    )

  def form(): Form[DocumentsProduced] = Form(mapping)

  def globalErrors(form: Form[DocumentsProduced]): Form[DocumentsProduced] = {

    def globalValidate(docs: DocumentsProduced) =
      docs.documentWriteOff.map(wo => DocumentWriteOff.globalErrors(wo)).getOrElse(Seq.empty)

    form.copy(errors = form.errors ++ form.value.map(globalValidate).getOrElse(Seq.empty))
  }

}

object DocumentsProducedSummary extends DeclarationPage
