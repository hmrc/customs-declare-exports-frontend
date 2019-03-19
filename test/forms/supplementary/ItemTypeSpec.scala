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
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import uk.gov.hmrc.wco.dec.{Classification, MetaData}

class ItemTypeSpec extends WordSpec with MustMatchers {
  import ItemTypeSpec._

  "ItemType converted into MetaData" should {

    "contain proper values" when {
      "provided with mandatory data only" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Nil,
          nationalAdditionalCodes = Nil,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          )
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)
        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(1)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
      }

      "provided with mandatory data and single TARIC Additional Code" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Seq(taricAdditionalCode),
          nationalAdditionalCodes = Nil,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          ),
          Classification(
            id = Some(taricAdditionalCode),
            identificationTypeCode = Some(IdentificationTypeCodes.TARICAdditionalCode)
          )
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)

        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(2)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
      }

      "provided with mandatory data and multiple TARIC Additional Codes" in {
        val taricAdditionalCode_1 = "AB12"
        val taricAdditionalCode_2 = "CD34"
        val taricAdditionalCode_3 = "56EF"
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Seq(taricAdditionalCode_1, taricAdditionalCode_2, taricAdditionalCode_3),
          nationalAdditionalCodes = Nil,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          ),
          Classification(
            id = Some(taricAdditionalCode_1),
            identificationTypeCode = Some(IdentificationTypeCodes.TARICAdditionalCode)
          ),
          Classification(
            id = Some(taricAdditionalCode_2),
            identificationTypeCode = Some(IdentificationTypeCodes.TARICAdditionalCode)
          ),
          Classification(
            id = Some(taricAdditionalCode_3),
            identificationTypeCode = Some(IdentificationTypeCodes.TARICAdditionalCode)
          )
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)

        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(4)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
      }

      "provided with mandatory data and single National Additional Code" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Nil,
          nationalAdditionalCodes = Seq(nationalAdditionalCode),
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          ),
          Classification(
            id = Some(nationalAdditionalCode),
            identificationTypeCode = Some(IdentificationTypeCodes.NationalAdditionalCode)
          )
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)

        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(2)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
      }

      "provided with mandatory data and multiple National Additional Codes" in {
        val nationalAdditionalCode_1 = "AB12"
        val nationalAdditionalCode_2 = "CD34"
        val nationalAdditionalCode_3 = "56EF"
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Nil,
          nationalAdditionalCodes = Seq(nationalAdditionalCode_1, nationalAdditionalCode_2, nationalAdditionalCode_3),
          descriptionOfGoods = descriptionOfGoods,
          cusCode = None,
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          ),
          Classification(
            id = Some(nationalAdditionalCode_1),
            identificationTypeCode = Some(IdentificationTypeCodes.NationalAdditionalCode)
          ),
          Classification(
            id = Some(nationalAdditionalCode_2),
            identificationTypeCode = Some(IdentificationTypeCodes.NationalAdditionalCode)
          ),
          Classification(
            id = Some(nationalAdditionalCode_3),
            identificationTypeCode = Some(IdentificationTypeCodes.NationalAdditionalCode)
          )
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)

        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(4)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
      }

      "provided with mandatory data and CUS Code" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Nil,
          nationalAdditionalCodes = Nil,
          descriptionOfGoods = descriptionOfGoods,
          cusCode = Some(cusCode),
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          ),
          Classification(id = Some(cusCode), identificationTypeCode = Some(IdentificationTypeCodes.CUSCode))
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)

        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(2)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
      }

      "provided with all data" in {
        val itemType = ItemType(
          combinedNomenclatureCode = combinedNomenclatureCode,
          taricAdditionalCodes = Seq(taricAdditionalCode),
          nationalAdditionalCodes = Seq(nationalAdditionalCode),
          descriptionOfGoods = descriptionOfGoods,
          cusCode = Some(cusCode),
          statisticalValue = statisticalValue
        )
        val expectedClassifications = Seq(
          Classification(
            id = Some(combinedNomenclatureCode),
            identificationTypeCode = Some(IdentificationTypeCodes.CombinedNomenclatureCode)
          ),
          Classification(
            id = Some(taricAdditionalCode),
            identificationTypeCode = Some(IdentificationTypeCodes.TARICAdditionalCode)
          ),
          Classification(
            id = Some(nationalAdditionalCode),
            identificationTypeCode = Some(IdentificationTypeCodes.NationalAdditionalCode)
          ),
          Classification(id = Some(cusCode), identificationTypeCode = Some(IdentificationTypeCodes.CUSCode))
        )

        val metadata = MetaData.fromProperties(itemType.toMetadataProperties())

        metadata.declaration must be(defined)
        metadata.declaration.get.goodsShipment must be(defined)
        metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems mustNot be(empty)

        val governmentAgencyGoodsItem = metadata.declaration.get.goodsShipment.get.governmentAgencyGoodsItems.head
        governmentAgencyGoodsItem.commodity must be(defined)

        governmentAgencyGoodsItem.commodity.get.description must be(defined)
        governmentAgencyGoodsItem.commodity.get.description.get must equal(descriptionOfGoods)
        governmentAgencyGoodsItem.statisticalValueAmount must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value must be(defined)
        governmentAgencyGoodsItem.statisticalValueAmount.get.value.get.toString() must equal(statisticalValue)

        governmentAgencyGoodsItem.commodity.get.classifications.size must equal(4)
        expectedClassifications.foreach(
          expClassification => governmentAgencyGoodsItem.commodity.get.classifications must contain(expClassification)
        )
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
    taricAdditionalCodes = Seq(taricAdditionalCode),
    nationalAdditionalCodes = Seq(nationalAdditionalCode),
    descriptionOfGoods = descriptionOfGoods,
    cusCode = Some(cusCode),
    statisticalValue = statisticalValue
  )
  val mandatoryOnlyItemType = ItemType(
    combinedNomenclatureCode = combinedNomenclatureCode,
    taricAdditionalCodes = Nil,
    nationalAdditionalCodes = Nil,
    descriptionOfGoods = descriptionOfGoods,
    cusCode = None,
    statisticalValue = statisticalValue
  )
  val emptyItemType = ItemType(
    combinedNomenclatureCode = "",
    taricAdditionalCodes = Nil,
    nationalAdditionalCodes = Nil,
    descriptionOfGoods = "",
    cusCode = None,
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
  val mandatoryOnlyItemTypeJSON: JsValue = JsObject(
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

}
