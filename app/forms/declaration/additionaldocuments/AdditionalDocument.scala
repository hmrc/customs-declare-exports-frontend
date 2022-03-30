/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.{AdditionalConstraintsMapping, ConditionalConstraint, DeclarationPage}
import forms.common.Date
import forms.declaration.additionaldocuments.DocumentWriteOff._
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsDeclaration
import models.viewmodels.TariffContentKey
import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
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

  // scalastyle:off
  private def mapping(cacheModel: ExportsDeclaration): Mapping[AdditionalDocument] = {
    val keyWhenDocumentTypeCodeEmpty =
      if (cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.documentTypeCode.empty.fromAuthCode"
      else "declaration.additionalDocument.documentTypeCode.empty"

    val documentTypeCodeRequired = optional(
      text()
        .verifying(keyWhenDocumentTypeCodeEmpty, nonEmpty)
        .verifying("declaration.additionalDocument.documentTypeCode.error", isEmpty or (hasSpecificLength(4) and isAlphanumeric))
    ).verifying(keyWhenDocumentTypeCodeEmpty, isPresent)

    val nonEmptyOptionString = (input: Option[String]) => nonEmpty(input.getOrElse(""))

    Forms
      .mapping(
        documentTypeCodeKey -> documentTypeCodeRequired,
        documentIdentifierKey -> optional(
          text()
            .transform(_.trim, (s: String) => s)
            .verifying(
              "declaration.additionalDocument.documentIdentifier.error",
              nonEmpty and isAlphanumericWithAllowedSpecialCharacters and noLongerThan(35)
            )
        ),
        documentStatusKey -> optional(text().verifying("declaration.additionalDocument.documentStatus.error", noLongerThan(2) and isAlphabetic)),
        documentStatusReasonKey -> AdditionalConstraintsMapping(
          optional(
            text()
              .verifying("declaration.additionalDocument.documentStatusReason.error", noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters)
          ),
          Seq(
            ConditionalConstraint(
              isAnyOf(documentTypeCodeKey, documentCodesRequiringAReason),
              "declaration.additionalDocument.documentStatusReason.required.forDocumentCode",
              nonEmptyOptionString
            ),
            ConditionalConstraint(
              isAnyOf(documentStatusKey, statusCodesRequiringAReason),
              "declaration.additionalDocument.documentStatusReason.required.forStatusCode",
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

  val statusCodesRequiringAReason = Seq("UA", "UE", "UP", "US", "XX", "XW")

  val documentCodesRequiringAReason = Seq(
    "Y036",
    "Y037",
    "Y082",
    "Y083",
    "Y105",
    "Y107",
    "Y108",
    "Y109",
    "Y115",
    "Y200",
    "Y201",
    "Y202",
    "Y203",
    "Y204",
    "Y205",
    "Y206",
    "Y207",
    "Y208",
    "Y209",
    "Y210",
    "Y211",
    "Y212",
    "Y213",
    "Y214",
    "Y215",
    "Y216",
    "Y217",
    "Y218",
    "Y219",
    "Y220",
    "Y221",
    "Y222",
    "Y300",
    "Y301",
    "Y900",
    "Y901",
    "Y902",
    "Y903",
    "Y904",
    "Y906",
    "Y907",
    "Y909",
    "Y916",
    "Y917",
    "Y918",
    "Y920",
    "Y921",
    "Y922",
    "Y923",
    "Y924",
    "Y927",
    "Y932",
    "Y934",
    "Y935",
    "Y939",
    "Y945",
    "Y946",
    "Y947",
    "Y948",
    "Y949",
    "Y952",
    "Y953",
    "Y957",
    "Y961",
    "Y966",
    "Y967",
    "Y968",
    "Y969",
    "Y970",
    "Y971"
  )
}
