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

package forms.section5

import forms.DeclarationPage
import forms.common.YesNoAnswer.YesNoAnswers
import forms.mappings.MappingHelper.requiredRadio
import forms.section5.NactCode.{nactCodeKey, nactCodeLength}
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class NactCodeFirst(code: Option[String])

object NactCodeFirst extends DeclarationPage {
  implicit val format: OFormat[NactCodeFirst] = Json.format[NactCodeFirst]

  val hasNactCodeKey = "hasNact"

  private def form2Model: (String, Option[String]) => NactCodeFirst = { case (hasNactCode, code) =>
    hasNactCode match {
      case YesNoAnswers.yes => NactCodeFirst(code)
      case YesNoAnswers.no  => NactCodeFirst(None)
    }
  }

  private def model2Form: NactCodeFirst => Option[(String, Option[String])] =
    model =>
      model.code match {
        case Some(code) => Some((YesNoAnswers.yes, Some(code)))
        case None       => Some((YesNoAnswers.no, None))
      }

  private val mapping = Forms.mapping(
    hasNactCodeKey -> requiredRadio("declaration.nationalAdditionalCode.answer.empty"),
    nactCodeKey -> mandatoryIfEqual(
      hasNactCodeKey,
      YesNoAnswers.yes,
      text()
        .verifying("declaration.nationalAdditionalCode.error.empty", nonEmpty)
        .verifying("declaration.nationalAdditionalCode.error.invalid", isEmpty or (hasSpecificLength(nactCodeLength) and isAlphanumeric))
    )
  )(form2Model)(model2Form)

  def form: Form[NactCodeFirst] = Form(mapping)
}
