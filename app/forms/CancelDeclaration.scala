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

package forms

import forms.Mapping.requiredRadio
import forms.cancellation.CancellationChangeReason._
import play.api.data.Forms._
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class CancelDeclaration(functionalReferenceId: Lrn, mrn: String, statementDescription: String, changeReason: String)

object CancelDeclaration {
  implicit val format: OFormat[CancelDeclaration] = Json.format[CancelDeclaration]

  val functionalReferenceIdKey = "functionalReferenceId"
  val mrnKey = "mrn"
  val statementDescriptionKey = "statementDescription"
  val changeReasonKey = "changeReason"

  val mrnMaxLength = 70
  val statementDescriptionMaxLength = 512

  val mapping: Mapping[CancelDeclaration] = Forms.mapping(
    functionalReferenceIdKey -> Lrn.mapping("cancellation.functionalReferenceId"),
    mrnKey -> text()
      .verifying("cancellation.mrn.error.empty", nonEmpty)
      .verifying("cancellation.mrn.error.tooLong", isEmpty or noLongerThan(mrnMaxLength))
      .verifying("cancellation.mrn.error.wrongFormat", isEmpty or isAlphanumeric),
    statementDescriptionKey -> text()
      .verifying("cancellation.statementDescription.error.empty", nonEmpty)
      .verifying(
        "cancellation.statementDescription.error.invalid",
        isEmpty or (noLongerThan(statementDescriptionMaxLength) and isAlphanumericWithAllowedSpecialCharacters)
      ),
    changeReasonKey ->
      requiredRadio("cancellation.changeReason.error.wrongValue")
        .verifying(
          "cancellation.changeReason.error.wrongValue",
          isContainedIn(Seq(NoLongerRequired.toString, Duplication.toString, OtherReason.toString))
        )
  )(CancelDeclaration.apply)(CancelDeclaration.unapply)

  def form: Form[CancelDeclaration] = Form(mapping)
}
