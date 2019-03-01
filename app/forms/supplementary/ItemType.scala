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

package forms.supplementary

import forms.MetadataPropertiesConvertable
import forms.supplementary.ItemType._
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
  statisticalValue: String
) extends MetadataPropertiesConvertable {

  def updateWith(other: ItemType): ItemType = ItemType(
    combinedNomenclatureCode = other.combinedNomenclatureCode,
    taricAdditionalCodes = this.taricAdditionalCodes ++ other.taricAdditionalCodes,
    nationalAdditionalCodes = this.nationalAdditionalCodes ++ other.nationalAdditionalCodes,
    descriptionOfGoods = other.descriptionOfGoods,
    cusCode = other.cusCode,
    statisticalValue = other.statisticalValue
  )

  override def toMetadataProperties(): Map[String, String] = {
    val codeFieldsProperties = buildListOfProvidedCodesWithIdentifiers().zipWithIndex
      .foldLeft(Map.empty[String, String]) { (properties, newElem) =>
        newElem match {
          case ((code: String, idTypeCode: String), index: Int) =>
            properties ++
              Map(
                "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[" + index + "].id" ->
                  code,
                "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[" + index + "].identificationTypeCode" ->
                  idTypeCode
              )
        }
      }

    codeFieldsProperties ++ Map(
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.description" -> descriptionOfGoods,
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].statisticalValueAmount" -> statisticalValue
    )
  }

  private def buildListOfProvidedCodesWithIdentifiers(): List[(String, String)] = {
    val taricAdditionalCodesTuples = taricAdditionalCodes.map { code =>
      (Some(code), IdentificationTypeCodes.TARICAdditionalCode)
    }
    val nationalAdditionalCodesTuples = nationalAdditionalCodes.map { code =>
      (Some(code), IdentificationTypeCodes.NationalAdditionalCode)
    }
    val allCodesWithIdentifiers =
      Seq((Some(combinedNomenclatureCode), IdentificationTypeCodes.CombinedNomenclatureCode)) ++
        taricAdditionalCodesTuples ++
        nationalAdditionalCodesTuples ++
        Seq((cusCode, IdentificationTypeCodes.CUSCode))

    allCodesWithIdentifiers
      .filter(_._1.isDefined)
      .map { elem =>
        (elem._1.get, elem._2)
      }
      .toList
  }

}

object ItemType {

  implicit val reads: Reads[ItemType] = (
    (JsPath \ "combinedNomenclatureCode").read[String] and
      (JsPath \ "taricAdditionalCode").read[Seq[String]] and
      (JsPath \ "nationalAdditionalCode").read[Seq[String]] and
      (JsPath \ "descriptionOfGoods").read[String] and
      (JsPath \ "cusCode").readNullable[String] and
      (JsPath \ "statisticalValue").read[String]
  )(ItemType.apply _)

  implicit val writes: Writes[ItemType] = (
    (JsPath \ "combinedNomenclatureCode").write[String] and
      (JsPath \ "taricAdditionalCode").write[Seq[String]] and
      (JsPath \ "nationalAdditionalCode").write[Seq[String]] and
      (JsPath \ "descriptionOfGoods").write[String] and
      (JsPath \ "cusCode").writeNullable[String] and
      (JsPath \ "statisticalValue").write[String]
  )(unlift(ItemType.unapply))

  val mapping = Forms.mapping(
    "combinedNomenclatureCode" -> text(),
    "taricAdditionalCode" -> default(seq(text()), Seq.empty),
    "nationalAdditionalCode" -> default(seq(text()), Seq.empty),
    "descriptionOfGoods" -> text(),
    "cusCode" -> optional(text()),
    "statisticalValue" -> text()
  )(ItemType.apply)(ItemType.unapply)

  val id = "ItemType"

  def form(): Form[ItemType] = Form(mapping)

  object IdentificationTypeCodes {
    val CombinedNomenclatureCode = "TSP"
    val TARICAdditionalCode = "TRA"
    val NationalAdditionalCode = "GN"
    val CUSCode = "CV"
  }

  val empty: ItemType = ItemType("", Nil, Nil, "", None, "")
}
