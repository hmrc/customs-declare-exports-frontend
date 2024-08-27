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

package forms.section1

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import forms.common.YesNoAnswer
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class DeclarantEoriConfirmation(answer: String)

object DeclarantEoriConfirmation extends DeclarationPage {

  implicit val format: OFormat[DeclarantEoriConfirmation] = Json.format[DeclarantEoriConfirmation]

  val isEoriKey = "isEori"

  private val mapping =
    Forms.mapping(
      isEoriKey -> requiredRadio("declaration.declarant.error")
        .verifying("declaration.declarant.error", isContainedIn(YesNoAnswer.allowedValues))
    )(DeclarantEoriConfirmation.apply)(DeclarantEoriConfirmation.unapply)

  def form: Form[DeclarantEoriConfirmation] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.declarantDetails.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
