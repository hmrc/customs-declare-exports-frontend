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

package forms.declaration

import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class TaricCodeFirst(code: Option[String])

object TaricCodeFirst extends DeclarationPage {

  import TaricCode._
  implicit val format = Json.format[TaricCodeFirst]

  val hasTaricCodeKey = "hasTaric"
  val none = TaricCodeFirst(None)

  private def form2Model: (String, Option[String]) => TaricCodeFirst = { case (hasNactCode, code) =>
    hasNactCode match {
      case YesNoAnswers.yes => TaricCodeFirst(code)
      case YesNoAnswers.no  => none
    }
  }

  private def model2Form: TaricCodeFirst => Option[(String, Option[String])] =
    model =>
      model.code match {
        case Some(code) => Some((YesNoAnswers.yes, Some(code)))
        case None       => Some((YesNoAnswers.no, None))
      }

  val mapping = Forms.mapping(
    hasTaricCodeKey -> requiredRadio("declaration.taricAdditionalCodes.answer.empty"),
    taricCodeKey -> mandatoryIfEqual(
      hasTaricCodeKey,
      YesNoAnswers.yes,
      text()
        .verifying("declaration.taricAdditionalCodes.error.empty", nonEmpty)
        .verifying("declaration.taricAdditionalCodes.error.invalid", isEmpty or (hasSpecificLength(taricCodeLength) and isAlphanumeric))
    )
  )(form2Model)(model2Form)

  def form(): Form[TaricCodeFirst] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.additionalTaricCode.common"))
}
