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

package forms

import forms.cancellation.CancellationChangeReason.{Duplication, NoLongerRequired, OtherReason}
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.mapping.MetaDataBuilder
import services.mapping.declaration.DeclarationBuilder
import utils.validators.forms.FieldValidator._
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

case class CancelDeclaration(
  functionalReferenceId: String,
  mrn: String,
  statementDescription: String,
  changeReason: String
) {

  private val FunctionCode = 13
  private val TypeCode = "INV"
  private val StatementTypeCode = "AES"
  private val PointerSequenceNumeric = 1
  private val PointerDocumentSectionCode1 = "42A"
  private val PointerDocumentSectionCode2 = "06A"

  def createCancellationMetadata(eori: String): MetaData =
    MetaDataBuilder.buildRequest(
      DeclarationBuilder
        .buildCancellationRequest(functionalReferenceId, mrn, statementDescription, changeReason, eori)
    )
}

object CancelDeclaration {
  implicit val format = Json.format[CancelDeclaration]

  val correctDucrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{1,19}$"

  val mapping = Forms.mapping(
    "functionalReferenceId" -> text()
      .verifying("cancellation.functionalReferenceId.empty", nonEmpty)
      .verifying("cancellation.functionalReferenceId.tooLong", isEmpty or noLongerThan(35))
      .verifying("cancellation.functionalReferenceId.tooShort", isEmpty or noShorterThan(23))
      .verifying(
        "cancellation.functionalReferenceId.wrongFormat",
        isEmpty or (input => input.matches(correctDucrFormat))
      ),
    "mrn" -> text()
      .verifying("cancellation.mrn.empty", nonEmpty)
      .verifying("cancellation.mrn.tooLong", isEmpty or noLongerThan(70))
      .verifying("cancellation.mrn.tooShort", isEmpty or noShorterThan(0)) // TODO what is the minimum value for declarationID?
      .verifying("cancellation.mrn.wrongFormat", isEmpty or isAlphanumeric),
    "statementDescription" -> text()
      .verifying("cancellation.statementDescription.empty", nonEmpty)
      .verifying("cancellation.statementDescription.tooLong", isEmpty or noLongerThan(512))
      .verifying("cancellation.statementDescription.tooShort", isEmpty or noShorterThan(0))
      .verifying(
        "cancellation.statementDescription.wrongFormat",
        isEmpty or isAlphanumericWithAllowedSpecialCharacters
      ),
    "changeReason" ->
      text()
        .verifying(
          "cancellation.changeReason.error.wrongValue",
          isContainedIn(Seq(NoLongerRequired.toString, Duplication.toString, OtherReason.toString))
        )
  )(CancelDeclaration.apply)(CancelDeclaration.unapply)

  def form: Form[CancelDeclaration] = Form(mapping)
}
