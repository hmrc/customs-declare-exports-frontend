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

package forms.section2

import forms.DeclarationPage
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.mappings.MappingHelper.requiredRadio
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class DeclarantIsExporter(answer: String) {
  def isYes: Boolean = answer == yes
}

object DeclarantIsExporter extends DeclarationPage {

  implicit val format: OFormat[DeclarantIsExporter] = Json.format[DeclarantIsExporter]

  val answerKey = "answer"

  private val mapping =
    Forms.mapping(
      answerKey -> requiredRadio("declaration.declarant.exporter.error")
        .verifying("declaration.declarant.exporter.error", isContainedIn(YesNoAnswer.allowedValues))
    )(DeclarantIsExporter.apply)(DeclarantIsExporter => Some(DeclarantIsExporter.answer))

  def form: Form[DeclarantIsExporter] = Form(mapping)
}
