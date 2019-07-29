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

import models.declaration.governmentagencygoodsitem.{Amount, GovernmentAgencyGoodsItem}
import services.cache.ExportItem
import wco.datamodel.wco.declaration_ds.dms._2.GovernmentAgencyGoodsItemStatisticalValueAmountType
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.{
  GovernmentAgencyGoodsItem => WCOGovernmentAgencyGoodsItem
}

object StatisticalValueAmountBuilder {

  val defaultCurrencyCode = "GBP"

  def buildThenAdd(exportItem: ExportItem, wcoGovernmentAgencyGoodsItem: WCOGovernmentAgencyGoodsItem): Unit =
    exportItem.itemType.foreach { itemType =>
      wcoGovernmentAgencyGoodsItem.setStatisticalValueAmount(
        createWCODecStatisticalValueAmount(Some(BigDecimal(itemType.statisticalValue)), Some(defaultCurrencyCode))
      )
    }

  def buildThenAdd(
    governmentAgencyGoodsItem: GovernmentAgencyGoodsItem,
    wcoGovernmentAgencyGoodsItem: WCOGovernmentAgencyGoodsItem
  ) {
    createStatisticalValueAmountType(governmentAgencyGoodsItem).foreach { statisticalValueAmount =>
      wcoGovernmentAgencyGoodsItem.setStatisticalValueAmount(statisticalValueAmount)
    }
  }

  private def createStatisticalValueAmountType(
    governmentAgencyGoodsItem: GovernmentAgencyGoodsItem
  ): Option[GovernmentAgencyGoodsItemStatisticalValueAmountType] =
    governmentAgencyGoodsItem.statisticalValueAmount.map { statisticalAmount =>
      createWCODecStatisticalValueAmount(statisticalAmount.value, statisticalAmount.currencyId)
    }

  private def createWCODecStatisticalValueAmount(amountValue: Option[BigDecimal], currencyId: Option[String]) = {
    val statisticalValueAmountType = new GovernmentAgencyGoodsItemStatisticalValueAmountType
    currencyId.foreach { id =>
      statisticalValueAmountType.setCurrencyID(id)
    }
    amountValue.foreach { value =>
      statisticalValueAmountType.setValue(new java.math.BigDecimal(value.bigDecimal.doubleValue()))
    }
    statisticalValueAmountType
  }
}
