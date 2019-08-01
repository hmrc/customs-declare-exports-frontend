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

package services.mapping.goodsshipment
import forms.declaration.ConsignmentReferences
import javax.inject.Inject
import services.mapping.ModifyingBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.UCR
import wco.datamodel.wco.declaration_ds.dms._2.UCRTraderAssignedReferenceIDType

class UCRBuilder @Inject()() extends ModifyingBuilder[ConsignmentReferences, GoodsShipment] {
  override def buildThenAdd(model: ConsignmentReferences, goodsShipment: GoodsShipment): Unit =
    if (UCRBuilder.isDefined(model)) goodsShipment.setUCR(UCRBuilder.createUCR(model))
}

object UCRBuilder {

  def build(implicit cacheMap: CacheMap): UCR =
    cacheMap
      .getEntry[ConsignmentReferences](ConsignmentReferences.id)
      .filter(isDefined)
      .map(createUCR)
      .orNull

  private def isDefined(reference: ConsignmentReferences): Boolean = reference.ducr.isDefined

  private def createUCR(data: ConsignmentReferences): UCR = {
    val ucr = new UCR()

    data.ducr.foreach { value =>
      val traderAssignedReferenceID = new UCRTraderAssignedReferenceIDType()
      traderAssignedReferenceID.setValue(value.ducr)
      ucr.setTraderAssignedReferenceID(traderAssignedReferenceID)
    }

    ucr
  }
}
