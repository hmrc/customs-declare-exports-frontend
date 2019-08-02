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

import forms.declaration.NatureOfTransaction
import javax.inject.Inject
import services.mapping.ModifyingBuilder
import services.mapping.goodsshipment.GoodsShipmentNatureOfTransactionBuilder.isDefined
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.declaration_ds.dms._2.GoodsShipmentTransactionNatureCodeType

class GoodsShipmentNatureOfTransactionBuilder @Inject()() extends ModifyingBuilder[NatureOfTransaction, GoodsShipment] {
  override def buildThenAdd(natureOfTransaction: NatureOfTransaction, goodsShipment: GoodsShipment) {
    if (isDefined(natureOfTransaction)) {
      val natureOfTransactionWCO = new GoodsShipmentTransactionNatureCodeType()
      natureOfTransactionWCO.setValue(natureOfTransaction.natureType)
      goodsShipment.setTransactionNatureCode(natureOfTransactionWCO)
    }
  }
}

object GoodsShipmentNatureOfTransactionBuilder {

  private def isDefined(natureOfTransaction: NatureOfTransaction): Boolean = natureOfTransaction.natureType.nonEmpty

}
