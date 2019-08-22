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

import play.api.data.Forms.{default, optional, seq, text}
import play.api.data.{Form, Forms}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ItemType(
  combinedNomenclatureCode: String,
  taricAdditionalCodes: Seq[String],
  nationalAdditionalCodes: Seq[String],
  descriptionOfGoods: String,
  cusCode: Option[String],
  unDangerousGoodsCode: Option[String],
  statisticalValue: String
)

object ItemType {
  implicit val reads: Reads[ItemType] = (
    (JsPath \ "combinedNomenclatureCode").read[String] and
      (JsPath \ "taricAdditionalCode").read[Seq[String]] and
      (JsPath \ "nationalAdditionalCode").read[Seq[String]] and
      (JsPath \ "descriptionOfGoods").read[String] and
      (JsPath \ "cusCode").readNullable[String] and
      (JsPath \ "unDangerousGoodsCode").readNullable[String] and
      (JsPath \ "statisticalValue").read[String]
  )(ItemType.apply _)

  implicit val writes: Writes[ItemType] = (
    (JsPath \ "combinedNomenclatureCode").write[String] and
      (JsPath \ "taricAdditionalCode").write[Seq[String]] and
      (JsPath \ "nationalAdditionalCode").write[Seq[String]] and
      (JsPath \ "descriptionOfGoods").write[String] and
      (JsPath \ "cusCode").writeNullable[String] and
      (JsPath \ "unDangerousGoodsCode").writeNullable[String] and
      (JsPath \ "statisticalValue").write[String]
  )(unlift(ItemType.unapply))

  val combinedNomenclatureCodeKey = "combinedNomenclatureCode"
  val taricAdditionalCodesKey = "taricAdditionalCode"
  val nationalAdditionalCodesKey = "nationalAdditionalCode"
  val descriptionOfGoodsKey = "descriptionOfGoods"
  val cusCodeKey = "cusCode"
  val unDangerousGoodsCodeKey = "unDangerousGoodsCode"
  val statisticalValueKey = "statisticalValue"

  val mapping = Forms.mapping(
    combinedNomenclatureCodeKey -> text(),
    taricAdditionalCodesKey -> default(seq(text()), Seq.empty),
    nationalAdditionalCodesKey -> default(seq(text()), Seq.empty),
    descriptionOfGoodsKey -> text(),
    cusCodeKey -> optional(text()),
    unDangerousGoodsCodeKey -> optional(text()),
    statisticalValueKey -> text()
  )(ItemType.apply)(ItemType.unapply)

  val id = "ItemType"

  def form(): Form[ItemType] = Form(mapping)

  val empty: ItemType = ItemType("", Nil, Nil, "", None, None, "")
}
