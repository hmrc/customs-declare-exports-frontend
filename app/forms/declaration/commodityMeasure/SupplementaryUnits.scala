/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.declaration.commodityMeasure

import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.DeclarationType
import models.declaration.{CommodityMeasure => CommodityMeasureModel}
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.text
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class SupplementaryUnits(supplementaryUnits: Option[String])

object SupplementaryUnits extends DeclarationPage {

  def apply(commodityMeasure: CommodityMeasureModel): SupplementaryUnits =
    commodityMeasure.supplementaryUnits match {
      case Some(supplementaryUnits) => SupplementaryUnits(Some(supplementaryUnits))
      case _                        => SupplementaryUnits(None)
    }

  def form(yesNoPage: Boolean): Form[SupplementaryUnits] =
    Form(if (yesNoPage) mappingForYesNoPage else mappingForMandatorySupplementaryUnits)

  val hasSupplementaryUnits = "hasSupplementaryUnits"
  val supplementaryUnits = "supplementaryUnits"

  private val mappingForYesNoPage =
    Forms.mapping(
      hasSupplementaryUnits -> requiredRadio("declaration.supplementaryUnits.yesNo.empty"),
      supplementaryUnits -> mandatoryIfEqual(hasSupplementaryUnits, YesNoAnswers.yes, supplementaryUnitsMapping)
    )(form2Model)(model2Form)

  private val mappingForMandatorySupplementaryUnits =
    Forms.mapping(supplementaryUnits -> supplementaryUnitsMapping)(inputValue => SupplementaryUnits(Some(inputValue)))(_.supplementaryUnits)

  private def form2Model: (String, Option[String]) => SupplementaryUnits = {
    case (hasSupplementaryUnits, value) =>
      hasSupplementaryUnits match {
        case YesNoAnswers.yes => SupplementaryUnits(value)
        case YesNoAnswers.no  => SupplementaryUnits(None)
      }
  }

  private def model2Form: SupplementaryUnits => Option[(String, Option[String])] =
    _.supplementaryUnits match {
      case Some(value) => Some((YesNoAnswers.yes, Some(value)))
      case None        => Some((YesNoAnswers.no, None))
    }

  private def supplementaryUnitsMapping: Mapping[String] =
    text
      .verifying("declaration.supplementaryUnits.quantity.empty", nonEmpty)
      .verifying("declaration.supplementaryUnits.quantity.empty", (value: String) => isEmpty(value) or containsNotOnlyZeros(value))
      .verifying("declaration.supplementaryUnits.quantity.error", (value: String) => isEmpty(value) or isValidDecimal(value))
      .verifying("declaration.supplementaryUnits.quantity.length", isEmpty or noLongerThan(16))

  private val isValidDecimal: String => Boolean =
    value => validateDecimalGreaterThanZero(99)(6)(value) or containsOnlyZeros(value)

  override def defineTariffContentKeys(declarationType: DeclarationType): Seq[TariffContentKey] =
    List(TariffContentKey("tariff.declaration.item.supplementaryUnits.common"))
}
