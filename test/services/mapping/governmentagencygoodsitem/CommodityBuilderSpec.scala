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

package services.mapping.governmentagencygoodsitem
import forms.declaration.{CommodityMeasure, ItemType}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.ExportsItemsCacheIds
import uk.gov.hmrc.http.cache.client.CacheMap

class CommodityBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "CommodityBuilder" should {

    "map commodity item successfully when dangerous goods present" in {

      implicit val cacheMap: CacheMap = mock[CacheMap]
      val descriptionOfGoods = "descriptionOfGoods"
      val unDangerousGoodsCode = "unDangerousGoodsCode"
      val netMassString = "15.00"
      val grossMassString = "25.00"
      val tariffQuantity = "31"

      val itemType = Some(
        ItemType(
          "combinedNomenclatureCode",
          Seq("taricAdditionalCodes"),
          Seq("nationalAdditionalCodes"),
          descriptionOfGoods,
          Some("cusCode"),
          Some(unDangerousGoodsCode),
          "statisticalValue"
        )
      )

      testBuilder(
        descriptionOfGoods,
        Some(unDangerousGoodsCode),
        netMassString,
        grossMassString,
        tariffQuantity,
        itemType
      )

    }
  }

  "map commodity item successfully when dangerous goods not present" in {

    implicit val cacheMap: CacheMap = mock[CacheMap]
    val descriptionOfGoods = "descriptionOfGoods"
    val unDangerousGoodsCode = "unDangerousGoodsCode"
    val netMassString = "15.00"
    val grossMassString = "25.00"
    val tariffQuantity = "31"

    val itemType = Some(
      ItemType(
        "combinedNomenclatureCode",
        Seq("taricAdditionalCodes"),
        Seq("nationalAdditionalCodes"),
        descriptionOfGoods,
        Some("cusCode"),
        None,
        "statisticalValue"
      )
    )

    testBuilder(
      descriptionOfGoods,
      None,
      netMassString,
      grossMassString,
      tariffQuantity,
      itemType
    )

  }

  def testBuilder(
    descriptionOfGoods: String,
    unDangerousGoodsCode: Option[String],
    netMassString: String,
    grossMassString: String,
    tariffQuantity: String,
    itemType: Some[ItemType]
  )(implicit  cacheMap: CacheMap) = {
    val commodityMeasure = CommodityMeasure(Some(tariffQuantity), netMass = netMassString, grossMass = grossMassString)
    when(
      cacheMap
        .getEntry[ItemType](ItemType.id)
    ).thenReturn(itemType)

    when(cacheMap.getEntry[CommodityMeasure](CommodityMeasure.commodityFormId)).thenReturn(Some(commodityMeasure))

    val mappedCommodity = CommodityBuilder.build.get
    mappedCommodity.getDescription.getValue shouldBe descriptionOfGoods
    unDangerousGoodsCode.fold(mappedCommodity.getDangerousGoods.isEmpty shouldBe true){
      code => mappedCommodity.getDangerousGoods.get(0).getUNDGID.getValue shouldBe code
    }

    val goodsMeasure = mappedCommodity.getGoodsMeasure

    goodsMeasure.getNetNetWeightMeasure.getValue shouldBe BigDecimal(netMassString).bigDecimal
    goodsMeasure.getNetNetWeightMeasure.getUnitCode shouldBe ExportsItemsCacheIds.defaultMeasureCode

    goodsMeasure.getGrossMassMeasure.getValue shouldBe BigDecimal(grossMassString).bigDecimal
    goodsMeasure.getGrossMassMeasure.getUnitCode shouldBe ExportsItemsCacheIds.defaultMeasureCode

    goodsMeasure.getTariffQuantity.getValue shouldBe BigDecimal(tariffQuantity).bigDecimal
    goodsMeasure.getTariffQuantity.getUnitCode shouldBe ExportsItemsCacheIds.defaultMeasureCode
  }

}
