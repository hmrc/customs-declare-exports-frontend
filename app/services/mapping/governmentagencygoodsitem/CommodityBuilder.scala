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
import services.ExportsItemsCacheIds
import services.mapping.CachingMappingHelper.getClassificationsFromItemTypes
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.Commodity.{
  Classification => WCOClassification,
  DangerousGoods => WCODangerousGoods,
  GoodsMeasure => WCOGoodsMeasure
}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.{Commodity => WCOCommodity}
import wco.datamodel.wco.declaration_ds.dms._2._

object CommodityBuilder {


  def build(implicit cacheMap: CacheMap): Option[WCOCommodity] = {
    cacheMap
      .getEntry[ItemType](ItemType.id)
      .map(
    item => // get all codes create classification
    mapCommodity(item)
    )
  }

  private def mapCommodity(item: ItemType)(implicit cacheMap: CacheMap): WCOCommodity = {
    val commodity = new WCOCommodity

    val classifications = getClassificationsFromItemTypes(item)
    val dangerousGoods: Option[Seq[WCODangerousGoods]] = item.unDangerousGoodsCode.map(code => Seq(mapDangerousGoods(code)))

    val commodityDescriptionTextType = new CommodityDescriptionTextType
    commodityDescriptionTextType.setValue(item.descriptionOfGoods)

    commodity.setDescription(commodityDescriptionTextType)

    dangerousGoods.getOrElse(Seq.empty).foreach(commodity.getDangerousGoods.add(_))

    classifications.foreach(classification => commodity.getClassification.add(mapClassification(classification)))

    commodity.setGoodsMeasure(buildMeasures.orNull)
    commodity
  }

  private def buildMeasures(implicit cacheMap: CacheMap): Option[WCOGoodsMeasure] = {
    cacheMap.getEntry[CommodityMeasure](CommodityMeasure.commodityFormId)
      .map(mapGoodsMeasure)
  }

  private def mapClassification(classification: Classification): WCOClassification ={
    val wcoClassification = new WCOClassification

    val typeCode = new ClassificationIdentificationTypeCodeType
    typeCode.setValue(classification.identificationTypeCode.orNull)

    val id = new ClassificationIdentificationIDType
    id.setValue(classification.id.orNull)

    wcoClassification.setIdentificationTypeCode(typeCode)
    wcoClassification
  }

  private def mapDangerousGoods(code: String) : WCODangerousGoods ={
    val dangerousGoods = new WCODangerousGoods
    val goodsUNDGIDType = new DangerousGoodsUNDGIDType
    goodsUNDGIDType.setValue(code)
    dangerousGoods.setUNDGID(goodsUNDGIDType)

    dangerousGoods
  }

  private def mapGoodsMeasure(data: CommodityMeasure) : WCOGoodsMeasure = {

    val goodsMeasure = new WCOGoodsMeasure()

    val grossMassMeasureType = new GoodsMeasureGrossMassMeasureType
    grossMassMeasureType.setValue(BigDecimal(data.grossMass).bigDecimal)
    grossMassMeasureType.setUnitCode(ExportsItemsCacheIds.defaultMeasureCode)

    val netWeightMeasureType = new GoodsMeasureNetNetWeightMeasureType
    netWeightMeasureType.setUnitCode(ExportsItemsCacheIds.defaultMeasureCode)
    netWeightMeasureType.setValue(BigDecimal(data.netMass).bigDecimal)

    val tarriffQuantity = data.supplementaryUnits.map(quantity => {
      val mappedQuantity = new GoodsMeasureTariffQuantityType
      mappedQuantity.setUnitCode(ExportsItemsCacheIds.defaultMeasureCode)
      mappedQuantity.setValue(BigDecimal(quantity).bigDecimal)
      mappedQuantity
    })

    goodsMeasure.setGrossMassMeasure(grossMassMeasureType)
    goodsMeasure.setNetNetWeightMeasure(netWeightMeasureType)
    goodsMeasure.setTariffQuantity(tarriffQuantity.orNull)

    goodsMeasure
  }


}
