/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class ItemType(statisticalValue: String)

object ItemType {

  implicit val format = Json.format[ItemType]

  val statisticalValueKey = "statisticalValue"

  private val statisticalValueMaxLength = 15
  private val statisticalValueDecimalPlaces = 2

  private val mappingStatisticalValue = text()
    .verifying("declaration.itemType.statisticalValue.error.empty", nonEmpty)
    .verifying(
      "declaration.itemType.statisticalValue.error.length",
      input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll("\\.", ""))
    )
    .verifying(
      "declaration.itemType.statisticalValue.error.wrongFormat",
      isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces)
    )

  private val mapping: Mapping[ItemType] =
    Forms.mapping(statisticalValueKey -> mappingStatisticalValue)(ItemType.apply)(ItemType.unapply)

  def form(): Form[ItemType] = Form(mapping)

}
