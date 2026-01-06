/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.timeline

import forms.mappings.MappingHelper.requiredRadio
import forms.timeline.CancellationChangeReason.*
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.*

case class CancelDeclarationDescription(changeReason: String, statementDescription: String)

object CancelDeclarationDescription {
  implicit val format: OFormat[CancelDeclarationDescription] = Json.format[CancelDeclarationDescription]

  val statementDescriptionKey = "statementDescription"
  val changeReasonKey = "changeReason"

  val statementDescriptionMaxLength = 512

  val mapping = Forms.mapping(
    changeReasonKey ->
      requiredRadio("cancellation.changeReason.error.wrongValue")
        .verifying(
          "cancellation.changeReason.error.wrongValue",
          isContainedIn(Seq(NoLongerRequired.toString, Duplication.toString, OtherReason.toString))
        ),
    statementDescriptionKey -> text()
      .verifying("cancellation.statementDescription.error.empty", nonEmpty)
      .verifying("cancellation.statementDescription.error.length", isEmpty or noLongerThan(statementDescriptionMaxLength))
      .verifying("cancellation.statementDescription.error.invalid", isEmpty or isAlphanumericWithAllowedSpecialCharacters)
  )(CancelDeclarationDescription.apply)(CancelDeclarationDescription => Some(Tuple.fromProductTyped(CancelDeclarationDescription)))

  def form: Form[CancelDeclarationDescription] = Form(mapping)
}
