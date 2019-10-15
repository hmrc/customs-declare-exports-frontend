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
import play.api.data.{Form, Forms}

case class ItemTypeForm(
  combinedNomenclatureCode: String,
  taricAdditionalCode: Option[String],
  nationalAdditionalCode: Option[String],
  descriptionOfGoods: String,
  cusCode: Option[String],
  unDangerousGoodsCode: Option[String],
  statisticalValue: String
)

object ItemTypeForm {

  val combinedNomenclatureCodeKey = "combinedNomenclatureCode"
  val taricAdditionalCodeKey = "taricAdditionalCode"
  val nationalAdditionalCodeKey = "nationalAdditionalCode"
  val descriptionOfGoodsKey = "descriptionOfGoods"
  val cusCodeKey = "cusCode"
  val unDangerousGoodsCodeKey = "unDangerousGoodsCode"
  val statisticalValueKey = "statisticalValue"

  val mapping = Forms.mapping(
    combinedNomenclatureCodeKey -> text(),
    taricAdditionalCodeKey -> optional(text()),
    nationalAdditionalCodeKey -> optional(text()),
    descriptionOfGoodsKey -> text(),
    cusCodeKey -> optional(text()),
    unDangerousGoodsCodeKey -> optional(text()),
    statisticalValueKey -> text()
  )(ItemTypeForm.apply)(ItemTypeForm.unapply)

  val id = "ItemType"

  def form(): Form[ItemTypeForm] = Form(mapping)

  val empty: ItemTypeForm = ItemTypeForm("", None, None, "", None, None, "")

  def fromItemType(model: ItemType) =
    ItemTypeForm(
      model.combinedNomenclatureCode,
      None,
      None,
      model.descriptionOfGoods,
      model.cusCode,
      model.unDangerousGoodsCode,
      model.statisticalValue
    )
}
