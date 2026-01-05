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

package forms.section5

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class FiscalInformation(onwardSupplyRelief: String)

object FiscalInformation extends DeclarationPage {

  implicit val format: OFormat[FiscalInformation] = Json.format[FiscalInformation]

  object AllowedFiscalInformationAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import AllowedFiscalInformationAnswers._

  val allowedValues: Seq[String] = Seq(yes, no)

  val mapping: Mapping[FiscalInformation] = Forms.mapping(
    "onwardSupplyRelief" -> requiredRadio("declaration.fiscalInformation.onwardSupplyRelief.empty")
      .verifying("declaration.fiscalInformation.onwardSupplyRelief.error", isContainedIn(allowedValues))
  )(FiscalInformation.apply)(FiscalInformation => Some(FiscalInformation.onwardSupplyRelief))

  val formId = "FiscalInformation"

  def form: Form[FiscalInformation] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.fiscalInformation.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
