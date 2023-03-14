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

package forms.declaration

import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class StatisticalValue(statisticalValue: String) extends Ordered[StatisticalValue] {
  override def compare(y: StatisticalValue): Int = statisticalValue.compareTo(y.statisticalValue)
}

object StatisticalValue extends DeclarationPage with FieldMapping {
  implicit val format = Json.format[StatisticalValue]

  val pointer: ExportsFieldPointer = "statisticalValue.statisticalValue"

  val statisticalValueKey = "statisticalValue"

  private val statisticalValueMaxLength = 15
  private val statisticalValueDecimalPlaces = 2

  private val mappingStatisticalValue = text()
    .verifying("declaration.statisticalValue.error.empty", nonEmpty)
    .verifying(
      "declaration.statisticalValue.error.length",
      input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll("\\.", ""))
    )
    .verifying("declaration.statisticalValue.error.wrongFormat", isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces))

  private val mapping: Mapping[StatisticalValue] =
    Forms.mapping(statisticalValueKey -> mappingStatisticalValue)(StatisticalValue.apply)(StatisticalValue.unapply)

  def form: Form[StatisticalValue] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.statisticalValue.common"))
}
