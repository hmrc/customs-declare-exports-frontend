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

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

object ItemTypeSpec {
  private val combinedNomenclatureCode = "ABCD1234"
  private val taricAdditionalCode = "AB12"
  private val nationalAdditionalCode = "CD34"
  private val descriptionOfGoods = "Description of goods."
  private val cusCode = "QWER0987"
  private val unDangerousGoodsCode = "12CD"
  private val statisticalValue = "1234567890123.45"

  val correctItemType = ItemType(
    combinedNomenclatureCode = combinedNomenclatureCode,
    taricAdditionalCodes = Seq(taricAdditionalCode),
    nationalAdditionalCodes = Seq(nationalAdditionalCode),
    descriptionOfGoods = descriptionOfGoods,
    cusCode = Some(cusCode),
    unDangerousGoodsCode = Some(unDangerousGoodsCode),
    statisticalValue = statisticalValue
  )
  val mandatoryFieldsOnlyItemType = ItemType(
    combinedNomenclatureCode = combinedNomenclatureCode,
    taricAdditionalCodes = Nil,
    nationalAdditionalCodes = Nil,
    descriptionOfGoods = descriptionOfGoods,
    cusCode = None,
    unDangerousGoodsCode = None,
    statisticalValue = statisticalValue
  )
  val emptyItemType = ItemType(
    combinedNomenclatureCode = "",
    taricAdditionalCodes = Nil,
    nationalAdditionalCodes = Nil,
    descriptionOfGoods = "",
    cusCode = None,
    unDangerousGoodsCode = None,
    statisticalValue = ""
  )

  val correctItemTypeJSON: JsValue = JsObject(
    Map(
      "combinedNomenclatureCode" -> JsString(combinedNomenclatureCode),
      "taricAdditionalCode" -> JsArray(Seq(JsString(taricAdditionalCode))),
      "nationalAdditionalCode" -> JsArray(Seq(JsString(nationalAdditionalCode))),
      "descriptionOfGoods" -> JsString(descriptionOfGoods),
      "cusCode" -> JsString(cusCode),
      "statisticalValue" -> JsString(statisticalValue)
    )
  )
  val mandatoryFieldsOnlyItemTypeJSON: JsValue = JsObject(
    Map(
      "combinedNomenclatureCode" -> JsString(combinedNomenclatureCode),
      "taricAdditionalCode" -> JsArray(Seq(JsString(""))),
      "nationalAdditionalCode" -> JsArray(Seq(JsString(""))),
      "descriptionOfGoods" -> JsString(descriptionOfGoods),
      "cusCode" -> JsString(""),
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

  val correctItemTypeMap: Map[String, String] =
    Map(
      "combinedNomenclatureCode" -> combinedNomenclatureCode,
      "taricAdditionalCode[0]" -> taricAdditionalCode,
      "nationalAdditionalCode[0]" -> nationalAdditionalCode,
      "descriptionOfGoods" -> descriptionOfGoods,
      "cusCode" -> cusCode,
      "statisticalValue" -> statisticalValue
    )
  val mandatoryFieldsOnlyItemTypeMap: Map[String, String] =
    Map(
      "combinedNomenclatureCode" -> combinedNomenclatureCode,
      "taricAdditionalCode[0]" -> taricAdditionalCode,
      "nationalAdditionalCode[0]" -> nationalAdditionalCode,
      "descriptionOfGoods" -> descriptionOfGoods,
      "cusCode" -> cusCode,
      "statisticalValue" -> statisticalValue
    )

}
