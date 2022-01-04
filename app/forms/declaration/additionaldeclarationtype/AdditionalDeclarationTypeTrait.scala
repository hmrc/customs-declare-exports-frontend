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

package forms.declaration.additionaldeclarationtype

import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import utils.validators.forms.FieldValidator.isContainedIn

trait AdditionalDeclarationTypeTrait extends DeclarationPage {
  def allowedValues: Set[AdditionalDeclarationType]

  val formMapping: Mapping[AdditionalDeclarationType] = Forms
    .mapping[AdditionalDeclarationType, AdditionalDeclarationType](
      "additionalDeclarationType" -> requiredRadio("declaration.declarationType.inputText.error.empty")
        .verifying("declaration.declarationType.inputText.error.incorrect", isContainedIn(AdditionalDeclarationType.values.map(_.toString)))
        .transform[AdditionalDeclarationType](AdditionalDeclarationType.from(_).get, _.toString)
        .verifying("declaration.declarationType.inputText.error.incorrect", isContainedIn(allowedValues))
    )(identity)(Some(_))

  val formId = "AdditionalDeclarationType"

  def form(): Form[AdditionalDeclarationType] = Form(formMapping)
}

object AdditionalDeclarationTypePage extends AdditionalDeclarationTypeTrait {
  override def allowedValues: Set[AdditionalDeclarationType] = Set.empty

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.type.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
