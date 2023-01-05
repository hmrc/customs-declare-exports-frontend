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

package forms.declaration.carrier

import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import forms.common.{Eori, YesNoAnswer}
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class CarrierEoriNumber(eori: Option[Eori], hasEori: String)

object CarrierEoriNumber extends DeclarationPage {

  implicit val format: OFormat[CarrierEoriNumber] = Json.format[CarrierEoriNumber]

  private val hasEori = "hasEori"
  private val eori = "eori"

  val formId = "CarrierEoriDetails"

  val mapping: Mapping[CarrierEoriNumber] = Forms.mapping(
    eori -> mandatoryIfEqual(hasEori, YesNoAnswers.yes, Eori.mapping("declaration.carrierEori.eori.empty")),
    hasEori -> requiredRadio("declaration.carrierEori.hasEori.empty", YesNoAnswer.allowedValues)
  )(CarrierEoriNumber.apply)(CarrierEoriNumber.unapply)

  def form: Form[CarrierEoriNumber] = Form(mapping)

  def apply(carrierDetails: CarrierDetails): CarrierEoriNumber =
    carrierDetails.details.eori match {
      case Some(eori) => CarrierEoriNumber(Some(eori), YesNoAnswers.yes)
      case _          => CarrierEoriNumber(None, YesNoAnswers.no)
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.carrierEoriNumber.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
