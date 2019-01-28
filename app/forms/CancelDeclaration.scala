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

import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.hmrc.wco.dec._
import utils.validators.FormFieldValidator._

case class CancelDeclaration(functionalReferenceId: String, declarationId: String, statementDescription: String) {

  private val FunctionCode = 13
  private val TypeCode = "INV"
  private val StatementTypeCode = "AES"
  private val PointerSequenceNumeric = 1
  private val PointerDocumentSectionCode1 = "42A"
  private val PointerDocumentSectionCode2 = "06A"
  private val ChangeReasonCode = "1"

  def createCancellationMetadata(eori: String): MetaData =
    MetaData(
      declaration = Some(
        Declaration(
          functionCode = Some(FunctionCode),
          functionalReferenceId = Some(functionalReferenceId),
          id = Some(declarationId),
          typeCode = Some(TypeCode),
          submitter = Some(NamedEntityWithAddress(id = Some(eori))),
          additionalInformations = Seq(
            AdditionalInformation(
              statementDescription = Some(statementDescription),
              statementTypeCode = Some(StatementTypeCode),
              pointers = Seq(
                Pointer(
                  sequenceNumeric = Some(PointerSequenceNumeric),
                  documentSectionCode = Some(PointerDocumentSectionCode1)
                ),
                Pointer(documentSectionCode = Some(PointerDocumentSectionCode2))
              )
            )
          ),
          amendments = Seq(Amendment(changeReasonCode = Some(ChangeReasonCode)))
        )
      )
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
    "declarationId" -> text()
      .verifying("cancellation.declarationId.empty", nonEmpty)
      .verifying("cancellation.declarationId.tooLong", isEmpty or noLongerThan(70))
      .verifying("cancellation.declarationId.tooShort", isEmpty or noShorterThan(0)) // TODO what is the minimum value for declarationID?
      .verifying("cancellation.declarationId.wrongFormat", isEmpty or isAlphanumeric),
    "statementDescription" -> text()
      .verifying("cancellation.statementDescription.empty", nonEmpty)
      .verifying("cancellation.statementDescription.tooLong", isEmpty or noLongerThan(512))
      .verifying("cancellation.statementDescription.tooShort", isEmpty or noShorterThan(0))
      .verifying("cancellation.statementDescription.wrongFormat", isEmpty or isAlphanumericWithAllowedSpecialCharacters)
  )(CancelDeclaration.apply)(CancelDeclaration.unapply)

  def form: Form[CancelDeclaration] = Form(mapping)
}
