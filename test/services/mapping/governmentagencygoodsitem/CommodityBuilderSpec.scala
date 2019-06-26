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

import models.declaration.governmentagencygoodsitem._
import org.scalatest.{Matchers, WordSpec}

class CommodityBuilderSpec
    extends WordSpec with Matchers with GovernmentAgencyGoodsItemData {

  "CommodityBuilder" should {

    "map commodity item successfully when dangerous goods present" in {
      val commodity = Commodity(
        Some("commodityDescription"),
        Seq(CommodityBuilderSpec.classifications),
        Seq(CommodityBuilderSpec.dangerousGoods),
        Some(CommodityBuilderSpec.goodsMeasure)
      )
      val mappedCommodity = CommodityBuilder.build(Some(commodity))
      mappedCommodity.getDescription.getValue should be("commodityDescription")

      mappedCommodity.getClassification.get(0).getID.getValue should be("classificationsId")
      mappedCommodity.getClassification.get(0).getIdentificationTypeCode.getValue should be("identificationTypeCode")

      mappedCommodity.getDangerousGoods.get(0).getUNDGID.getValue should be("identificationTypeCode")

      mappedCommodity.getGoodsMeasure.getGrossMassMeasure.getUnitCode should be("KGM")
      mappedCommodity.getGoodsMeasure.getGrossMassMeasure.getValue.intValue() should be(100)

      mappedCommodity.getGoodsMeasure.getNetNetWeightMeasure.getUnitCode should be("KGM")
      mappedCommodity.getGoodsMeasure.getNetNetWeightMeasure.getValue.intValue() should be(90)

      mappedCommodity.getGoodsMeasure.getTariffQuantity.getUnitCode should be("KGM")
      mappedCommodity.getGoodsMeasure.getTariffQuantity.getValue.intValue() should be(2)
    }

    "map commodity item successfully when dangerous goods not present" in {
      val commodity = Commodity(
        Some("commodityDescription"),
        Seq(CommodityBuilderSpec.classifications),
        Seq(CommodityBuilderSpec.dangerousGoods),
        None
      )
      val mappedCommodity = CommodityBuilder.build(Some(commodity))
      mappedCommodity.getDescription.getValue should be("commodityDescription")

      mappedCommodity.getClassification.get(0).getID.getValue should be("classificationsId")
      mappedCommodity.getClassification.get(0).getIdentificationTypeCode.getValue should be("identificationTypeCode")

      mappedCommodity.getDangerousGoods.get(0).getUNDGID.getValue should be("identificationTypeCode")

      mappedCommodity.getGoodsMeasure should be(null)
    }
  }
}

object CommodityBuilderSpec {
  val grossMassMeasure = Measure(Some("kg"), Some(BigDecimal(100)))
  val netWeightMeasure = Measure(Some("kg"), Some(BigDecimal(90)))
  val tariffQuantity = Measure(Some("kg"), Some(BigDecimal(2)))

  val goodsMeasure = GoodsMeasure(Some(grossMassMeasure), Some(netWeightMeasure), Some(tariffQuantity))

  val dangerousGoods = DangerousGoods(Some("identificationTypeCode"))

  val classifications = Classification(
    Some("classificationsId"),
    Some("nameCodeId"),
    Some("identificationTypeCode"),
    Some("bindingTariffReferenceId")
  )
}
