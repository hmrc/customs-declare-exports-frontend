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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class ItemType(
  combinedNomenclatureCode: String,
  taricAdditionalCode: Option[String],
  nationalAdditionalCode: Option[String],
  descriptionOfGoods: String,
  cusCode: Option[String],
  statisticalValue: String
) {
  import ItemType.IdentificationTypeCodes

  def toMetadataProperties(): Map[String, String] = {
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

  private def buildListOfProvidedCodesWithIdentifiers(): List[(String, String)] =
    List(
      (Some(combinedNomenclatureCode), IdentificationTypeCodes.CombinedNomenclatureCode),
      (taricAdditionalCode, IdentificationTypeCodes.TARICAdditionalCode),
      (nationalAdditionalCode, IdentificationTypeCodes.NationalAdditionalCode),
      (cusCode, IdentificationTypeCodes.CUSCode)
    ).filter(_._1.isDefined).map { elem =>
      (elem._1.get, elem._2)
    }

}

object ItemType {
  implicit val format = Json.format[ItemType]

  private val combinedNomenclatureCodeMaxLength = 8
  private val taricAdditionalCodeLength = 4
  private val nationalAdditionalCodeMaxLength = 4
  private val descriptionOfGoodsMaxLength = 280
  private val cusCodeLength = 8
  private val statisticalValueMaxLength = 15
  private val statisticalValueDecimalPlaces = 2

  val mapping = Forms.mapping(
    "combinedNomenclatureCode" -> text()
      .verifying("supplementary.itemType.combinedNomenclatureCode.error.empty", nonEmpty)
      .verifying(
        "supplementary.itemType.combinedNomenclatureCode.error.length",
        isEmpty or noLongerThan(combinedNomenclatureCodeMaxLength)
      )
      .verifying("supplementary.itemType.combinedNomenclatureCode.error.specialCharacters", isEmpty or isAlphanumeric),
    "taricAdditionalCode" -> optional(
      text()
        .verifying(
          "supplementary.itemType.taricAdditionalCodes.error.length",
          hasSpecificLength(taricAdditionalCodeLength)
        )
        .verifying("supplementary.itemType.taricAdditionalCodes.error.specialCharacters", isAlphanumeric)
    ),
    "nationalAdditionalCode" -> optional(
      text()
        .verifying(
          "supplementary.itemType.nationalAdditionalCode.error.length",
          noLongerThan(nationalAdditionalCodeMaxLength)
        )
        .verifying("supplementary.itemType.nationalAdditionalCode.error.specialCharacters", isAlphanumeric)
    ),
    "descriptionOfGoods" -> text()
      .verifying("supplementary.itemType.description.error.empty", nonEmpty)
      .verifying(
        "supplementary.itemType.description.error.length",
        isEmpty or noLongerThan(descriptionOfGoodsMaxLength)
      ),
    "cusCode" -> optional(
      text()
        .verifying("supplementary.itemType.cusCode.error.length", hasSpecificLength(cusCodeLength))
        .verifying("supplementary.itemType.cusCode.error.specialCharacters", isAlphanumeric)
    ),
    "statisticalValue" -> text()
      .verifying("supplementary.itemType.statisticalValue.error.empty", nonEmpty)
      .verifying(
        "supplementary.itemType.statisticalValue.error.length",
        input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll(".", ""))
      )
      .verifying(
        "supplementary.itemType.statisticalValue.error.wrongFormat",
        isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces)
      )
  )(ItemType.apply)(ItemType.unapply)

  val id = "ItemType"

  def form(): Form[ItemType] = Form(mapping)

  object IdentificationTypeCodes {
    val CombinedNomenclatureCode = "TSP"
    val TARICAdditionalCode = "TRA"
    val NationalAdditionalCode = "GN"
    val CUSCode = "CV"
  }
}
