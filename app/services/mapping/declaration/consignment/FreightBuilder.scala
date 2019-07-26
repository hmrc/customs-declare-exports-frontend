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

package services.mapping.declaration.consignment

import forms.Choice
import forms.declaration.TransportDetails
import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.ModifyingBuilder
import services.mapping.declaration.consignment.FreightBuilder.createFreight
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.Consignment.Freight
import wco.datamodel.wco.declaration_ds.dms._2.FreightPaymentMethodCodeType

class FreightBuilder @Inject()() extends ModifyingBuilder[Declaration.Consignment] {

  override def buildThenAdd(model: ExportsCacheModel, consignment: Declaration.Consignment): Unit = {
    model.transportDetails
      .flatMap(_.paymentMethod)
      .map(createFreight)
      .foreach(consignment.setFreight)
  }

}

object FreightBuilder {

  def build(implicit cacheMap: CacheMap, choice: Choice): Declaration.Consignment.Freight =
    cacheMap
      .getEntry[TransportDetails](TransportDetails.formId)
      .flatMap(_.paymentMethod)
      .map(createFreight)
      .orNull

  def createFreight(paymentMethod: String): Freight = {
    val freight = new Declaration.Consignment.Freight()

    val paymentMethodCodeType = new FreightPaymentMethodCodeType()
    paymentMethodCodeType.setValue(paymentMethod)
    freight.setPaymentMethodCode(paymentMethodCodeType)

    freight
  }
}
