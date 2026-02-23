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
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class StatisticalValue(statisticalValue: String) extends Ordered[StatisticalValue] with Amendment {

  override def compare(y: StatisticalValue): Int = statisticalValue.compareTo(y.statisticalValue)

  def value: String = statisticalValue
}

object StatisticalValue extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[StatisticalValue] = Json.format[StatisticalValue]

  val pointer: ExportsFieldPointer = "statisticalValue.statisticalValue"

  val statisticalValueKey = "statisticalValue"

  private val statisticalValueMaxLength = 15
  private val statisticalValueDecimalPlaces = 2

  private val mappingStatisticalValue = text()
    .verifying("declaration.statisticalValue.error.empty", _.trim.nonEmpty)
    .verifying(
      "declaration.statisticalValue.error.length",
      input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll("\\.", ""))
    )
    .verifying("declaration.statisticalValue.error.wrongFormat", isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces))

  private val mapping: Mapping[StatisticalValue] =
    Forms.mapping(statisticalValueKey -> mappingStatisticalValue)(StatisticalValue.apply)(StatisticalValue.unapply)

  def form: Form[StatisticalValue] = Form(mapping)

  private val mappingStatisticalValueOptional = text()
    .verifying(
      "declaration.statisticalValue.error.length",
      input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll("\\.", ""))
    )
    .verifying("declaration.statisticalValue.error.wrongFormat", isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces))

  private val mappingOptional: Mapping[StatisticalValue] =
    Forms.mapping(statisticalValueKey -> mappingStatisticalValueOptional)(StatisticalValue.apply)(StatisticalValue.unapply)

  def formOptional: Form[StatisticalValue] = Form(mappingOptional)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    List(TariffContentKey("tariff.declaration.item.statisticalValue.common"))
}
