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

package services.mapping.declaration

import models.declaration.governmentagencygoodsitem.{Formats, GovernmentAgencyGoodsItem}
import play.api.libs.json.Reads
import services.ExportsItemsCacheIds
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.declaration_ds.dms._2._

object GoodsItemQuantityBuilder {

  implicit private val goodsItemSequenceReads: Reads[Seq[GovernmentAgencyGoodsItem]] =
    Reads.seq(Formats.governmentAgencyGoodsItemFormat)

  def build(implicit cacheMap: CacheMap): DeclarationGoodsItemQuantityType =
    cacheMap
      .getEntry[Seq[GovernmentAgencyGoodsItem]](ExportsItemsCacheIds.itemsId)
      .map(goodsItems => createGoodsItemQuantity(goodsItems))
      .orNull

  def createGoodsItemQuantity[A](items: Seq[A]): DeclarationGoodsItemQuantityType = {
    val goodsQuantity = new DeclarationGoodsItemQuantityType()

    goodsQuantity.setValue(new java.math.BigDecimal(items.size))
    goodsQuantity
  }
}
