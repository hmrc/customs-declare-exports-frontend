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
import javax.inject.Inject
import models.declaration.governmentagencygoodsitem.{Classification, Commodity, DangerousGoods, GoodsMeasure}
import services.ExportsItemsCacheIds
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.Commodity.{
  Classification => WCOClassification,
  DangerousGoods => WCODangerousGoods,
  GoodsMeasure => WCOGoodsMeasure
}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.{Commodity => WCOCommodity}
import wco.datamodel.wco.declaration_ds.dms._2._

import scala.collection.JavaConverters._

class CommodityBuilder @Inject()() extends ModifyingBuilder[Commodity, GovernmentAgencyGoodsItem] {
  override def buildThenAdd(model: Commodity, item: GovernmentAgencyGoodsItem): Unit =
    item.setCommodity(CommodityBuilder.mapCommodity(model))
}

object CommodityBuilder {

  def build(commodity: Option[Commodity]): WCOCommodity =
    commodity
      .map(item => mapCommodity(item))
      .orNull

  private def mapCommodity(commodity: Commodity): WCOCommodity = {
    val wcoCommodity = new WCOCommodity

    commodity.description.foreach { text =>
      val commodityDescriptionTextType = new CommodityDescriptionTextType
      commodityDescriptionTextType.setValue(text)
      wcoCommodity.setDescription(commodityDescriptionTextType)
    }

    if (commodity.classifications.nonEmpty) {
      wcoCommodity.getClassification.addAll(mapClassification(commodity.classifications))
    }

    if (commodity.dangerousGoods.nonEmpty) {
      wcoCommodity.getDangerousGoods.addAll(mapDangerousGoods(commodity.dangerousGoods))
    }

    if (commodity.goodsMeasure.isDefined) {
      wcoCommodity.setGoodsMeasure(mapGoodsMeasure(commodity.goodsMeasure.head))
    }

    wcoCommodity
  }

  private def mapClassification(classifications: Seq[Classification]): java.util.List[WCOClassification] =
    classifications
      .map(classification => {
        val wcoClassification = new WCOClassification
        classification.identificationTypeCode.foreach { value =>
          val typeCode = new ClassificationIdentificationTypeCodeType
          typeCode.setValue(value)
          wcoClassification.setIdentificationTypeCode(typeCode)
        }

        classification.id.foreach { value =>
          val id = new ClassificationIdentificationIDType
          id.setValue(value)
          wcoClassification.setID(id)
        }
        wcoClassification
      })
      .toList
      .asJava

  private def mapDangerousGoods(dangerousGoods: Seq[DangerousGoods]): java.util.List[WCODangerousGoods] =
    dangerousGoods
      .map(good => {
        val wcoDangerousGoods = new WCODangerousGoods
        val goodsUNDGIDType = new DangerousGoodsUNDGIDType
        goodsUNDGIDType.setValue(good.undgid.get)
        wcoDangerousGoods.setUNDGID(goodsUNDGIDType)

        wcoDangerousGoods
      })
      .toList
      .asJava

  private def mapGoodsMeasure(data: GoodsMeasure): WCOGoodsMeasure = {

    val goodsMeasure = new WCOGoodsMeasure()

    data.netWeightMeasure.foreach { measure =>
      val netWeightMeasureType = new GoodsMeasureNetNetWeightMeasureType
      netWeightMeasureType.setUnitCode(ExportsItemsCacheIds.defaultMeasureCode)
      netWeightMeasureType.setValue(measure.value.get.bigDecimal)
      goodsMeasure.setNetNetWeightMeasure(netWeightMeasureType)
    }

    data.grossMassMeasure.foreach { measure =>
      val grossMassMeasureType = new GoodsMeasureGrossMassMeasureType
      grossMassMeasureType.setValue(measure.value.get.bigDecimal)
      grossMassMeasureType.setUnitCode(ExportsItemsCacheIds.defaultMeasureCode)
      goodsMeasure.setGrossMassMeasure(grossMassMeasureType)
    }

    data.tariffQuantity.foreach { tariff =>
      val mappedQuantity = new GoodsMeasureTariffQuantityType
      mappedQuantity.setUnitCode(ExportsItemsCacheIds.defaultMeasureCode)
      mappedQuantity.setValue(tariff.value.get.bigDecimal)
      goodsMeasure.setTariffQuantity(mappedQuantity)
    }

    goodsMeasure
  }
}
