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
import javax.inject.Inject
import models.declaration.governmentagencygoodsitem.Commodity
import services.cache.ExportItem
import services.mapping.{CachingMappingHelper, ModifyingBuilder}
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.{GovernmentAgencyGoodsItem => WCOGovernmentAgencyGoodsItem}

class GovernmentAgencyGoodsItemBuilder @Inject()(
  statisticalValueAmountBuilder: StatisticalValueAmountBuilder,
  packagingBuilder: PackagingBuilder,
  governmentProcedureBuilder: GovernmentProcedureBuilder,
  additionalInformationBuilder: AdditionalInformationBuilder,
  additionalDocumentsBuilder: AdditionalDocumentsBuilder,
  domesticDutyTaxPartyBuilder: DomesticDutyTaxPartyBuilder,
  commodityBuilder: CommodityBuilder
) extends ModifyingBuilder[ExportItem, Declaration.GoodsShipment] {

  override def buildThenAdd(exportItem: ExportItem, goodsShipment: Declaration.GoodsShipment): Unit = {
    val wcoGovernmentAgencyGoodsItem = new WCOGovernmentAgencyGoodsItem
    wcoGovernmentAgencyGoodsItem.setSequenceNumeric(BigDecimal(exportItem.sequenceId).bigDecimal)

    statisticalValueAmountBuilder.buildThenAdd(exportItem, wcoGovernmentAgencyGoodsItem)
    packagingBuilder.buildThenAdd(exportItem, wcoGovernmentAgencyGoodsItem)
    governmentProcedureBuilder.buildThenAdd(exportItem, wcoGovernmentAgencyGoodsItem)
    additionalInformationBuilder.buildThenAdd(exportItem, wcoGovernmentAgencyGoodsItem)
    additionalDocumentsBuilder.buildThenAdd(exportItem, wcoGovernmentAgencyGoodsItem)

    val combinedCommodity = for {
      commodityWithoutGoodsMeasure <- mapItemTypeToCommodity(exportItem.itemType)
      commodityOnlyGoodsMeasure <- mapCommodityMeasureToCommodity(exportItem.commodityMeasure)
    } yield combineCommodities(commodityWithoutGoodsMeasure, commodityOnlyGoodsMeasure)

    combinedCommodity.foreach(commodityBuilder.buildThenAdd(_, wcoGovernmentAgencyGoodsItem))

    exportItem.additionalFiscalReferencesData.foreach(
      _.references.foreach(
        additionalFiscalReference =>
          domesticDutyTaxPartyBuilder.buildThenAdd(additionalFiscalReference, wcoGovernmentAgencyGoodsItem)
      )
    )

    goodsShipment.getGovernmentAgencyGoodsItem.add(wcoGovernmentAgencyGoodsItem)
  }

  private def mapItemTypeToCommodity(itemType: Option[ItemType]): Option[Commodity] =
    itemType.map({ item =>
      CachingMappingHelper.commodityFromItemTypes(item)
    })

  private def mapCommodityMeasureToCommodity(commodityMeasure: Option[CommodityMeasure]): Option[Commodity] =
    commodityMeasure.map(measure => CachingMappingHelper.mapGoodsMeasure(measure))

  private def combineCommodities(commodityPart1: Commodity, commodityPart2: Commodity): Commodity =
    commodityPart1.copy(goodsMeasure = commodityPart2.goodsMeasure)

}
