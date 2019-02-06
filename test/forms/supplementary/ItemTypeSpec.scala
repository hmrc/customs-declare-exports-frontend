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

import forms.supplementary.ItemType._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class ItemTypeSpec extends WordSpec with MustMatchers {
  import ItemTypeSpec._

  "ItemType" should {

    "convert itself into Item Type properties" when {
      "provided with mandatory data only" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCode = None,
          nationalAdditionalCode = None,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedItemTypeProperties: Map[String, String] = Map(
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].id" -> combinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].identificationTypeCode" ->
            IdentificationTypeCodes.CombinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.description" -> descriptionOfGoods,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].statisticalValueAmount" -> statisticalValue
        )

        itemType.toMetadataProperties() must equal(expectedItemTypeProperties)
      }

      "provided with mandatory data and TARIC additional code" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCode = Some(taricAdditionalCode),
          nationalAdditionalCode = None,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedItemTypeProperties: Map[String, String] = Map(
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].id" -> combinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].identificationTypeCode" ->
            IdentificationTypeCodes.CombinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].id" -> taricAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].identificationTypeCode" ->
            IdentificationTypeCodes.TARICAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.description" -> descriptionOfGoods,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].statisticalValueAmount" -> statisticalValue
        )

        itemType.toMetadataProperties() must equal(expectedItemTypeProperties)
      }

      "provided with mandatory data and National Additional Code" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCode = None,
          nationalAdditionalCode = Some(nationalAdditionalCode),
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedItemTypeProperties: Map[String, String] = Map(
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].id" -> combinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].identificationTypeCode" ->
            IdentificationTypeCodes.CombinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].id" -> nationalAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].identificationTypeCode" ->
            IdentificationTypeCodes.NationalAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.description" -> descriptionOfGoods,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].statisticalValueAmount" -> statisticalValue
        )

        itemType.toMetadataProperties() must equal(expectedItemTypeProperties)
      }

      "provided with mandatory data and CUS Code" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCode = None,
          nationalAdditionalCode = None,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = Some(cusCode),
          statisticalValue = statisticalValue
        )
        val expectedItemTypeProperties: Map[String, String] = Map(
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].id" -> combinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].identificationTypeCode" ->
            IdentificationTypeCodes.CombinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].id" -> cusCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].identificationTypeCode" ->
            IdentificationTypeCodes.CUSCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.description" -> descriptionOfGoods,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].statisticalValueAmount" -> statisticalValue
        )

        itemType.toMetadataProperties() must equal(expectedItemTypeProperties)
      }

      "provided with all data" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCode = Some(taricAdditionalCode),
          nationalAdditionalCode = Some(nationalAdditionalCode),
          descriptionOfGoods = descriptionOfGoods,
          cusCode = Some(cusCode),
          statisticalValue = statisticalValue
        )
        val expectedItemTypeProperties: Map[String, String] = Map(
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].id" -> combinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[0].identificationTypeCode" ->
            IdentificationTypeCodes.CombinedNomenclatureCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].id" -> taricAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[1].identificationTypeCode" ->
            IdentificationTypeCodes.TARICAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[2].id" -> nationalAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[2].identificationTypeCode" ->
            IdentificationTypeCodes.NationalAdditionalCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[3].id" -> cusCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.classifications[3].identificationTypeCode" ->
            IdentificationTypeCodes.CUSCode,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.description" -> descriptionOfGoods,
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].statisticalValueAmount" -> statisticalValue
        )

        itemType.toMetadataProperties() must equal(expectedItemTypeProperties)
      }
    }
  }

}

object ItemTypeSpec {
  private val combinedNomenclatureCode = "ABCD1234"
  private val taricAdditionalCode = "AB12"
  private val nationalAdditionalCode = "CD34"
  private val descriptionOfGoods = "Description of goods."
  private val cusCode = "QWER0987"
  private val statisticalValue = "1234567890123.45"

  val correctItemType = ItemType(
    combinedNomenclatureCode = combinedNomenclatureCode,
    taricAdditionalCode = Some(taricAdditionalCode),
    nationalAdditionalCode = Some(nationalAdditionalCode),
    descriptionOfGoods = descriptionOfGoods,
    cusCode = Some(cusCode),
    statisticalValue = statisticalValue
  )
  val emptyItemType = ItemType(
    combinedNomenclatureCode = "",
    taricAdditionalCode = None,
    nationalAdditionalCode = None,
    descriptionOfGoods = "",
    cusCode = None,
    statisticalValue = ""
  )

  val correctItemTypeJSON: JsValue = JsObject(
    Map(
      "combinedNomenclatureCode" -> JsString(combinedNomenclatureCode),
      "taricAdditionalCode" -> JsString(taricAdditionalCode),
      "nationalAdditionalCode" -> JsString(nationalAdditionalCode),
      "descriptionOfGoods" -> JsString(descriptionOfGoods),
      "cusCode" -> JsString(cusCode),
      "statisticalValue" -> JsString(statisticalValue)
    )
  )
  val emptyItemTypeJSON: JsValue = JsObject(
    Map(
      "combinedNomenclatureCode" -> JsString(""),
      "taricAdditionalCode" -> JsString(""),
      "nationalAdditionalCode" -> JsString(""),
      "descriptionOfGoods" -> JsString(""),
      "cusCode" -> JsString(""),
      "statisticalValue" -> JsString("")
    )
  )

}
