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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers.yes
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class DeclarantIsExporter(answer: String) {
  def isExporter: Boolean = answer == yes
}

object DeclarantIsExporter extends DeclarationPage {

  implicit val format: OFormat[DeclarantIsExporter] = Json.format[DeclarantIsExporter]

  val answerKey = "answer"

  private val mapping =
    Forms.mapping(
      answerKey -> requiredRadio("declaration.declarant.exporter.error")
        .verifying("declaration.declarant.exporter.error", isContainedIn(YesNoAnswer.allowedValues))
    )(DeclarantIsExporter.apply)(DeclarantIsExporter.unapply)

  def form(): Form[DeclarantIsExporter] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.areYouTheExporter.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
