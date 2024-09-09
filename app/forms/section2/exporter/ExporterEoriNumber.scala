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

package forms.section2.exporter

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Eori, YesNoAnswer}
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class ExporterEoriNumber(eori: Option[Eori], hasEori: String)

object ExporterEoriNumber extends DeclarationPage {
  implicit val format: OFormat[ExporterEoriNumber] = Json.format[ExporterEoriNumber]

  private val hasEori = "hasEori"
  private val eori = "eori"

  val formId = "ExporterEoriNumber"

  val mapping: Mapping[ExporterEoriNumber] = Forms.mapping(
    eori -> mandatoryIfEqual(hasEori, YesNoAnswers.yes, Eori.mapping("declaration.exporterEori.eori.empty")),
    hasEori -> requiredRadio("declaration.exporterEori.hasEori.empty", YesNoAnswer.allowedValues)
  )(ExporterEoriNumber.apply)(ExporterEoriNumber.unapply)

  def form: Form[ExporterEoriNumber] = Form(ExporterEoriNumber.mapping)

  def apply(exporterDetails: ExporterDetails): ExporterEoriNumber =
    exporterDetails.details.eori match {
      case Some(eori) => ExporterEoriNumber(Some(eori), YesNoAnswers.yes)
      case _          => ExporterEoriNumber(None, YesNoAnswers.no)
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.exporterEoriNumber.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
