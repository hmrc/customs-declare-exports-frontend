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
import services.cache.ExportItem
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.{
  GovernmentAgencyGoodsItem => WCOGovernmentAgencyGoodsItem
}
import wco.datamodel.wco.declaration_ds.dms._2.GovernmentAgencyGoodsItemStatisticalValueAmountType

class StatisticalValueAmountBuilder @Inject()() extends ModifyingBuilder[ExportItem, WCOGovernmentAgencyGoodsItem] {

  private val defaultCurrencyCode = "GBP"

  def buildThenAdd(exportItem: ExportItem, wcoGovernmentAgencyGoodsItem: WCOGovernmentAgencyGoodsItem): Unit =
    exportItem.itemType.foreach { itemType =>
      wcoGovernmentAgencyGoodsItem.setStatisticalValueAmount(
        createWCODecStatisticalValueAmount(Some(BigDecimal(itemType.statisticalValue)), Some(defaultCurrencyCode))
      )
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
