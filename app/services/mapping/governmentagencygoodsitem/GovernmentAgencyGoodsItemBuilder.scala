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
import forms.declaration.ItemType
import services.ExportsItemsCacheIds
import services.ExportsItemsCacheIds.defaultCurrencyCode
import models.DeclarationFormats._
import services.mapping.CachingMappingHelper
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.{Amount, GovernmentAgencyGoodsItem}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.{GovernmentAgencyGoodsItem => WCOGovernmentAgencyGoodsItem}
import wco.datamodel.wco.declaration_ds.dms._2.GovernmentAgencyGoodsItemStatisticalValueAmountType

object GovernmentAgencyGoodsItemBuilder {

  def build(implicit cacheMap: CacheMap): List[WCOGovernmentAgencyGoodsItem] =
    cacheMap
      .getEntry[Seq[GovernmentAgencyGoodsItem]](ExportsItemsCacheIds.itemsId)
      .getOrElse(Seq.empty)
      .map(goodsItem => createWCOGovernmentAgencyGoodsItem(goodsItem))
      .toList

  def createWCOGovernmentAgencyGoodsItem(
    governmentAgencyGoodsItem: GovernmentAgencyGoodsItem
  )(implicit cacheMap: CacheMap): WCOGovernmentAgencyGoodsItem = {

    val itemTypeData = goodsItemFromItemTypes(cacheMap)

    val maybeStatisticalValueAmount = itemTypeData.flatMap(_.statisticalValueAmount)

    val wcoGovernmentAgencyGoodsItem = new WCOGovernmentAgencyGoodsItem

    val statisticalValueAmountType = new GovernmentAgencyGoodsItemStatisticalValueAmountType
    statisticalValueAmountType.setCurrencyID(maybeStatisticalValueAmount.flatMap(_.currencyId).orNull)
    statisticalValueAmountType.setValue(maybeStatisticalValueAmount.flatMap(_.value).orNull.bigDecimal)

    wcoGovernmentAgencyGoodsItem.setSequenceNumeric(
      BigDecimal(governmentAgencyGoodsItem.sequenceNumeric).bigDecimal
    )

    PackageBuilder.build
      .getOrElse(Seq.empty)
      .foreach(packingItem => wcoGovernmentAgencyGoodsItem.getPackaging.add(packingItem))

    ProcedureCodesBuilder.build.getOrElse(Seq.empty)
      .foreach(procedureCode => wcoGovernmentAgencyGoodsItem.getGovernmentProcedure.add(procedureCode))

    AdditionalInformationBuilder.build.getOrElse(Seq.empty)
      .foreach(info => wcoGovernmentAgencyGoodsItem.getAdditionalInformation.add(info))

    AdditionalDocumentsBuilder.build().getOrElse(Seq.empty)
      .foreach(doc => wcoGovernmentAgencyGoodsItem.getAdditionalDocument.add(doc))

    wcoGovernmentAgencyGoodsItem.setStatisticalValueAmount(statisticalValueAmountType)
    wcoGovernmentAgencyGoodsItem.setCommodity(CommodityBuilder.build.orNull)
    wcoGovernmentAgencyGoodsItem
  }

  def goodsItemFromItemTypes(cachedData: CacheMap): Option[GovernmentAgencyGoodsItem] =
    cachedData
      .getEntry[ItemType](ItemType.id)
      .map(
        item => // get all codes create classification
          GovernmentAgencyGoodsItem(
            sequenceNumeric = 1,
            statisticalValueAmount =
              Some(Amount(Some(defaultCurrencyCode), value = Some(BigDecimal(item.statisticalValue)))),
            commodity = Some(CachingMappingHelper.commodityFromItemTypes(item))
        )
      )

}
