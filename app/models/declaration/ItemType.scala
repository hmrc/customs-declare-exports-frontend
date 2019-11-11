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

package models.declaration
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, Reads, Writes}

case class ItemType(taricAdditionalCodes: Seq[String], nationalAdditionalCodes: Seq[String], cusCode: Option[String], statisticalValue: String)

object ItemType {

  implicit val reads: Reads[ItemType] = (
    (JsPath \ "taricAdditionalCode").read[Seq[String]] and
      (JsPath \ "nationalAdditionalCode").read[Seq[String]] and
      (JsPath \ "cusCode").readNullable[String] and
      (JsPath \ "statisticalValue").read[String]
  )(ItemType.apply _)

  implicit val writes: Writes[ItemType] = (
    (JsPath \ "taricAdditionalCode").write[Seq[String]] and
      (JsPath \ "nationalAdditionalCode").write[Seq[String]] and
      (JsPath \ "cusCode").writeNullable[String] and
      (JsPath \ "statisticalValue").write[String]
  )(unlift(ItemType.unapply))

  val empty: ItemType = ItemType(Nil, Nil, None, "")
}
