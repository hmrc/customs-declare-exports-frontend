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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.declaration.{CommodityMeasure => CM}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class SupplementaryUnits(supplementaryUnits: Option[String], hasSupplementaryUnits: String)

object SupplementaryUnits extends DeclarationPage {

  def apply(commodityMeasure: CM): SupplementaryUnits =
    commodityMeasure.supplementaryUnits match {
      case Some(supplementaryUnits) => SupplementaryUnits(Some(supplementaryUnits), YesNoAnswers.yes)
      case _                        => SupplementaryUnits(None, YesNoAnswers.no)
    }

  def form: Form[SupplementaryUnits] = Form(mapping)

  val hasSupplementaryUnits = "hasSupplementaryUnits"
  val supplementaryUnits = "supplementaryUnits"

  private val mapping = Forms.mapping(
    supplementaryUnits -> mandatoryIfEqual(hasSupplementaryUnits, YesNoAnswers.yes, supplementaryUnitsMapping),
    hasSupplementaryUnits -> requiredRadio(
      "declaration.supplementaryUnits.empty",
      YesNoAnswer.allowedValues
    )
  )(SupplementaryUnits.apply)(SupplementaryUnits.unapply)

  private def supplementaryUnitsMapping: Mapping[String] =
    text()
      .verifying("declaration.supplementaryUnits.amount.empty", nonEmpty)
      .verifying("declaration.supplementaryUnits.amount.error", isEmpty or isNumeric)
}

