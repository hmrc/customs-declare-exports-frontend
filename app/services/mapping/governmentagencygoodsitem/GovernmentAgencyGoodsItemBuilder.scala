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
import java.util.Objects

import forms.declaration.ItemType
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{Amount, GovernmentAgencyGoodsItem}
import services.ExportsItemsCacheIds
import services.ExportsItemsCacheIds.defaultCurrencyCode
import services.mapping.CachingMappingHelper
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.{
  GovernmentAgencyGoodsItem => WCOGovernmentAgencyGoodsItem
}
import wco.datamodel.wco.declaration_ds.dms._2.GovernmentAgencyGoodsItemStatisticalValueAmountType

import scala.collection.JavaConverters._

object GovernmentAgencyGoodsItemBuilder {

  def build(implicit cacheMap: CacheMap): java.util.List[WCOGovernmentAgencyGoodsItem] =
    cacheMap
      .getEntry[Seq[GovernmentAgencyGoodsItem]](ExportsItemsCacheIds.itemsId)
      .getOrElse(Seq.empty)
      .map(goodsItem => createWCOGovernmentAgencyGoodsItem(goodsItem))
      .toList
      .asJava

  def createWCOGovernmentAgencyGoodsItem(
    governmentAgencyGoodsItem: GovernmentAgencyGoodsItem
  )(implicit cacheMap: CacheMap): WCOGovernmentAgencyGoodsItem = {

    val itemTypeData = goodsItemFromItemTypes(cacheMap)

    val maybeStatisticalValueAmount = itemTypeData.flatMap(_.statisticalValueAmount)

    val wcoGovernmentAgencyGoodsItem = new WCOGovernmentAgencyGoodsItem

    val statisticalAmount = maybeStatisticalValueAmount.flatMap(_.value.map(_.bigDecimal)).orNull
    if (Objects.nonNull(statisticalAmount)) {
      val statisticalValueAmountType = new GovernmentAgencyGoodsItemStatisticalValueAmountType
      statisticalValueAmountType.setCurrencyID(maybeStatisticalValueAmount.flatMap(_.currencyId).orNull)
      statisticalValueAmountType.setValue(statisticalAmount)
      wcoGovernmentAgencyGoodsItem.setStatisticalValueAmount(statisticalValueAmountType)
    }

    wcoGovernmentAgencyGoodsItem.setSequenceNumeric(BigDecimal(governmentAgencyGoodsItem.sequenceNumeric).bigDecimal)

    if (governmentAgencyGoodsItem.packagings.nonEmpty) {
      wcoGovernmentAgencyGoodsItem.getPackaging.addAll(PackagingBuilder.build(governmentAgencyGoodsItem.packagings))
    }

    if (governmentAgencyGoodsItem.governmentProcedures.nonEmpty) {
      wcoGovernmentAgencyGoodsItem.getGovernmentProcedure.addAll(
        GovernmentProcedureBuilder.build(governmentAgencyGoodsItem.governmentProcedures)
      )
    }

    if (governmentAgencyGoodsItem.additionalInformations.nonEmpty) {
      wcoGovernmentAgencyGoodsItem.getAdditionalInformation.addAll(
        AdditionalInformationBuilder.build(governmentAgencyGoodsItem.additionalInformations)
      )
    }

    if (governmentAgencyGoodsItem.additionalDocuments.nonEmpty) {
      wcoGovernmentAgencyGoodsItem.getAdditionalDocument.addAll(
        AdditionalDocumentsBuilder.build(governmentAgencyGoodsItem.additionalDocuments)
      )
    }

    wcoGovernmentAgencyGoodsItem.setCommodity(CommodityBuilder.build(governmentAgencyGoodsItem.commodity))
    wcoGovernmentAgencyGoodsItem
  }

  def goodsItemFromItemTypes(cachedData: CacheMap): Option[GovernmentAgencyGoodsItem] =
    cachedData
      .getEntry[ItemType](ItemType.id)
      .map(
        item =>
          GovernmentAgencyGoodsItem(
            sequenceNumeric = 1,
            statisticalValueAmount =
              Some(Amount(Some(defaultCurrencyCode), value = Some(BigDecimal(item.statisticalValue)))),
            commodity = Some(CachingMappingHelper.commodityFromItemTypes(item))
        )
      )

}
