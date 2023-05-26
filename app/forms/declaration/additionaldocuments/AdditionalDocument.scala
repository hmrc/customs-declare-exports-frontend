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

package forms.declaration.additionaldocuments

import forms.common.Date
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.{AdditionalConstraintsMapping, ConditionalConstraint, DeclarationPage}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ImplicitlySequencedObject
import models.viewmodels.TariffContentKey
import models.{ExportsDeclaration, FieldMapping}
import play.api.data.Forms._
import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.libs.json.{JsValue, Json}
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import services.{DiffTools, TaggedAdditionalDocumentCodes, TaggedAuthCodes}
import uk.gov.voa.play.form.ConditionalMappings.isAnyOf
import utils.validators.forms.FieldValidator.{nonEmpty, _}

case class AdditionalDocument(
  documentTypeCode: Option[String],
  documentIdentifier: Option[String],
  documentStatus: Option[String],
  documentStatusReason: Option[String],
  issuingAuthorityName: Option[String],
  dateOfValidity: Option[Date],
  documentWriteOff: Option[DocumentWriteOff]
) extends DiffTools[AdditionalDocument] with ImplicitlySequencedObject {
  def createDiff(original: AdditionalDocument, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.documentTypeCode, documentTypeCode, combinePointers(pointerString, sequenceId)),
      compareStringDifference(original.documentIdentifier, documentIdentifier, combinePointers(pointerString, sequenceId)),
      compareStringDifference(original.documentStatus, documentStatus, combinePointers(pointerString, sequenceId)),
      compareStringDifference(original.documentStatusReason, documentStatusReason, combinePointers(pointerString, sequenceId)),
      compareStringDifference(original.issuingAuthorityName, issuingAuthorityName, combinePointers(pointerString, sequenceId)),
      createDiffOfOptions(original.dateOfValidity, dateOfValidity, combinePointers(pointerString, sequenceId)),
      createDiffOfOptions(original.documentWriteOff, documentWriteOff, combinePointers(pointerString, sequenceId))
    ).flatten

  def toJson: JsValue = Json.toJson(this)(AdditionalDocument.format)

  implicit val writes = Json.writes[AdditionalDocument]

  def isDefined: Boolean =
    List(documentTypeCode, documentIdentifier, documentStatus, documentStatusReason, issuingAuthorityName, dateOfValidity, documentWriteOff).exists(
      _.isDefined
    )
}

object AdditionalDocument extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "documents"

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

  // scalastyle:off
  private def mapping(
    declaration: ExportsDeclaration
  )(implicit taggedAuthCodes: TaggedAuthCodes, taggedAdditionalDocumentCodes: TaggedAdditionalDocumentCodes): Mapping[AdditionalDocument] = {
    val keyWhenDocumentTypeCodeEmpty =
      if (taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration)) "declaration.additionalDocument.code.empty.fromAuthCode"
      else "declaration.additionalDocument.code.empty"

    val documentTypeCodeRequired = optional(
      text()
        .verifying(keyWhenDocumentTypeCodeEmpty, nonEmpty)
        .verifying("declaration.additionalDocument.code.error", isEmpty or (hasSpecificLength(4) and isAlphanumeric))
    ).verifying(keyWhenDocumentTypeCodeEmpty, isPresent)

    val nonEmptyOptionString = (input: Option[String]) => nonEmpty(input.getOrElse(""))

    Forms
      .mapping(
        documentTypeCodeKey -> documentTypeCodeRequired,
        documentIdentifierKey -> optional(
          text()
            .transform(_.trim, (s: String) => s)
            .verifying(
              "declaration.additionalDocument.identifier.error",
              nonEmpty and isAlphanumericWithAllowedSpecialCharacters and noLongerThan(35)
            )
        ),
        documentStatusKey -> optional(text().verifying("declaration.additionalDocument.status.error", noLongerThan(2) and isAlphabetic)),
        documentStatusReasonKey -> AdditionalConstraintsMapping(
          optional(
            text()
              .verifying("declaration.additionalDocument.statusReason.error", noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters)
          ),
          Seq(
            ConditionalConstraint(
              isAnyOf(documentTypeCodeKey, taggedAdditionalDocumentCodes.documentCodesRequiringAReason),
              "declaration.additionalDocument.statusReason.required.forDocumentCode",
              nonEmptyOptionString
            ),
            ConditionalConstraint(
              isAnyOf(documentStatusKey, taggedAdditionalDocumentCodes.statusCodesRequiringAReason),
              "declaration.additionalDocument.statusReason.required.forStatusCode",
              nonEmptyOptionString
            )
          )
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
  // scalastyle:on

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

  def form(
    declaration: ExportsDeclaration
  )(implicit taggedAuthCodes: TaggedAuthCodes, taggedAdditionalDocumentCodes: TaggedAdditionalDocumentCodes): Form[AdditionalDocument] =
    Form(mapping(declaration))

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
