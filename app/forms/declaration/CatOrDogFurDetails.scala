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
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class CatOrDogFurDetails(yesNo: String, purpose: Option[String])

object CatOrDogFurDetails extends DeclarationPage {

  val yesNoKey = "yesNo"
  val purposeKey = "purpose"

  val EducationalOrTaxidermyPurposes = "educational-or-taxidermy-purpose"
  val InvalidPurpose = "invalid-purpose"

  val mapping: Mapping[CatOrDogFurDetails] =
    Forms
      .mapping(
        yesNoKey -> requiredRadio("declaration.catOrDogFur.radios.yesNo.empty"),
        purposeKey -> mandatoryIfEqual(yesNoKey, YesNoAnswers.yes, requiredRadio("declaration.catOrDogFur.radios.purpose.empty"))
      )(apply)(unapply)

  def form: Form[CatOrDogFurDetails] = Form(mapping)

  implicit val format: OFormat[CatOrDogFurDetails] = Json.format[CatOrDogFurDetails]

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(
      TariffContentKey("tariff.declaration.item.catOrDogFurDetails.1.common"),
      TariffContentKey("tariff.declaration.item.catOrDogFurDetails.2.common")
    )
}
