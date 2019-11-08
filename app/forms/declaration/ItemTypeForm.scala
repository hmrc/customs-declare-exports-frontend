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

import models.declaration.ItemType
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}

case class ItemTypeForm(
  taricAdditionalCode: Option[String],
  nationalAdditionalCode: Option[String],
  cusCode: Option[String],
  statisticalValue: String
)

object ItemTypeForm {

  val taricAdditionalCodeKey = "taricAdditionalCode"
  val nationalAdditionalCodeKey = "nationalAdditionalCode"
  val cusCodeKey = "cusCode"
  val statisticalValueKey = "statisticalValue"

  private val mapping: Mapping[ItemTypeForm] = Forms.mapping(
    taricAdditionalCodeKey -> optional(text()),
    nationalAdditionalCodeKey -> optional(text()),
    cusCodeKey -> optional(text()),
    statisticalValueKey -> text()
  )(ItemTypeForm.apply)(ItemTypeForm.unapply)

  val id = "ItemType"

  def form(): Form[ItemTypeForm] = Form(mapping)

  val empty: ItemTypeForm = ItemTypeForm(None, None, None, "")

  def fromItemType(model: ItemType) =
    ItemTypeForm(None, None, model.cusCode, model.statisticalValue)
}
