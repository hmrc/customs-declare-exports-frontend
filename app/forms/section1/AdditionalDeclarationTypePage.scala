/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.section1.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}

object AdditionalDeclarationTypePage extends DeclarationPage {

  val radioButtonGroupId = "additionalDeclarationType"

  def form: Form[AdditionalDeclarationType] = Form(mapping)

  private val mapping: Mapping[AdditionalDeclarationType] =
    Forms.mapping(
      "additionalDeclarationType" -> requiredRadio("declaration.declarationType.radio.error.empty")
        .transform[AdditionalDeclarationType](AdditionalDeclarationType.from(_).get, _.toString)
    )(identity)(Some(_))

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.type.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
