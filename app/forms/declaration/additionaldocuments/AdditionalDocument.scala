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
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsDeclaration
import models.viewmodels.TariffContentKey
import play.api.data.Forms._
import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.libs.json.{JsValue, Json}
import utils.validators.forms.FieldValidator._

case class AdditionalDocument(
  documentTypeCode: Option[String],
  documentIdentifier: Option[String],
  documentStatus: Option[String],
  documentStatusReason: Option[String],
  issuingAuthorityName: Option[String],
  dateOfValidity: Option[Date],
  documentWriteOff: Option[DocumentWriteOff]
) {
  def toJson: JsValue = Json.toJson(this)(AdditionalDocument.format)

  implicit val writes = Json.writes[AdditionalDocument]

  def isDefined: Boolean =
    List(documentTypeCode, documentIdentifier, documentStatus, documentStatusReason, issuingAuthorityName, dateOfValidity, documentWriteOff).exists(
      _.isDefined
    )
}

object AdditionalDocument extends DeclarationPage {

  def fromJsonString(value: String): Option[AdditionalDocument] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[AdditionalDocument]

  val AdditionalDocumentFormGroupId: String = "additionalDocument"

  private val issuingAuthorityNameMaxLength = 70

  val documentTypeCodeKey = "documentTypeCode"
  val documentIdentifierKey = "documentIdentifier"
  val documentStatusKey = "documentStatus"
  val documentStatusReasonKey = "documentStatusReason"
  val issuingAuthorityNameKey = "issuingAuthorityName"
  val dateOfValidityKey = "dateOfValidity"

  private def mapping(cacheModel: ExportsDeclaration): Mapping[AdditionalDocument] = {
    val keyWhenDocumentTypeCodeEmpty =
      if (cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.documentTypeCode.empty.fromAuthCode"
      else "declaration.additionalDocument.documentTypeCode.empty"

    val documentTypeCodeRequired = optional(
      text()
        .verifying(keyWhenDocumentTypeCodeEmpty, nonEmpty)
        .verifying("declaration.additionalDocument.documentTypeCode.error", isEmpty or (hasSpecificLength(4) and isAlphanumeric))
    ).verifying(keyWhenDocumentTypeCodeEmpty, isPresent)

    Forms
      .mapping(
        documentTypeCodeKey -> documentTypeCodeRequired,
        documentIdentifierKey -> optional(
          text()
            .verifying(
              "declaration.additionalDocument.documentIdentifier.error",
              nonEmpty and isAlphanumericWithAllowedSpecialCharacters and noLongerThan(35)
            )
        ),
        documentStatusKey -> optional(text().verifying("declaration.additionalDocument.documentStatus.error", noLongerThan(2) and isAlphabetic)),
        documentStatusReasonKey -> optional(
          text()
            .verifying("declaration.additionalDocument.documentStatusReason.error", noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters)
        ),
        issuingAuthorityNameKey -> optional(
          text()
            .verifying("declaration.additionalDocument.issuingAuthorityName.error.length", noLongerThan(issuingAuthorityNameMaxLength))
        ),
        dateOfValidityKey -> optional(
          Date.mapping("declaration.additionalDocument.dateOfValidity.error.format", "declaration.additionalDocument.dateOfValidity.error.outOfRange")
        ),
        documentWriteOffKey -> optional(DocumentWriteOff.mapping)
      )(form2data)(AdditionalDocument.unapply)
  }

  private def form2data(
    documentTypeCode: Option[String],
    documentIdentifier: Option[String],
    documentStatus: Option[String],
    documentStatusReason: Option[String],
    issuingAuthorityName: Option[String],
    dateOfValidity: Option[Date],
    documentWriteOff: Option[DocumentWriteOff]
  ): AdditionalDocument =
    new AdditionalDocument(
      documentTypeCode.map(_.toUpperCase),
      documentIdentifier,
      documentStatus.map(_.toUpperCase),
      documentStatusReason,
      issuingAuthorityName,
      dateOfValidity,
      documentWriteOff
    )

  def form(cacheModel: ExportsDeclaration): Form[AdditionalDocument] =
    Form(mapping(cacheModel))

  def globalErrors(form: Form[AdditionalDocument]): Form[AdditionalDocument] = {

    def globalValidate(docs: AdditionalDocument): Seq[FormError] =
      docs.documentWriteOff.map(wo => DocumentWriteOff.globalErrors(wo)).getOrElse(Seq.empty)

    form.copy(errors = form.errors ++ form.value.map(globalValidate).getOrElse(Seq.empty))
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        Seq(
          TariffContentKey("tariff.declaration.item.additionalDocuments.1.clearance"),
          TariffContentKey("tariff.declaration.item.additionalDocuments.2.clearance"),
          TariffContentKey("tariff.declaration.item.additionalDocuments.3.clearance")
        )
      case _ => Seq(TariffContentKey("tariff.declaration.item.additionalDocuments.common"))
    }
}
