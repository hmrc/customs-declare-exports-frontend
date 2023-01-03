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

package forms.declaration.consignor

import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Eori, YesNoAnswer}
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class ConsignorEoriNumber(eori: Option[Eori], hasEori: String)

object ConsignorEoriNumber extends DeclarationPage {
  implicit val format: OFormat[ConsignorEoriNumber] = Json.format[ConsignorEoriNumber]

  private val hasEori = "hasEori"
  private val eori = "eori"

  val formId = "ConsignorEoriDetails"

  val mapping: Mapping[ConsignorEoriNumber] = Forms.mapping(
    eori -> mandatoryIfEqual(hasEori, YesNoAnswers.yes, Eori.mapping("declaration.consignorEori.eori.empty")),
    hasEori -> requiredRadio("declaration.consignorEori.hasEori.empty", YesNoAnswer.allowedValues)
  )(ConsignorEoriNumber.apply)(ConsignorEoriNumber.unapply)

  def form: Form[ConsignorEoriNumber] = Form(ConsignorEoriNumber.mapping)

  def apply(consignorDetails: ConsignorDetails): ConsignorEoriNumber =
    consignorDetails.details.eori match {
      case Some(eori) => ConsignorEoriNumber(Some(eori), YesNoAnswers.yes)
      case _          => ConsignorEoriNumber(None, YesNoAnswers.no)
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.consignorEoriNumber.clearance"))
}
