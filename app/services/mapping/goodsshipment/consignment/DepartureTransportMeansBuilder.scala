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

package services.mapping.goodsshipment.consignment
import forms.declaration.BorderTransport
import javax.inject.Inject
import services.mapping.ModifyingBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment.DepartureTransportMeans
import wco.datamodel.wco.declaration_ds.dms._2.{
  DepartureTransportMeansIdentificationIDType,
  DepartureTransportMeansIdentificationTypeCodeType
}

class DepartureTransportMeansBuilder @Inject()() extends ModifyingBuilder[BorderTransport, GoodsShipment.Consignment] {
  override def buildThenAdd(model: BorderTransport, consignment: Consignment): Unit =
    consignment.setDepartureTransportMeans(DepartureTransportMeansBuilder.createDepartureTransportMeans(model))
}

object DepartureTransportMeansBuilder {

  def build()(implicit cacheMap: CacheMap): Consignment.DepartureTransportMeans =
    cacheMap
      .getEntry[BorderTransport](BorderTransport.formId)
      .filter(borderTransport => isDefined(borderTransport))
      .map(borderTransport => createDepartureTransportMeans(borderTransport))
      .orNull
  private def isDefined(borderTransport: BorderTransport): Boolean =
    borderTransport.meansOfTransportOnDepartureIDNumber.nonEmpty || borderTransport.meansOfTransportOnDepartureType.nonEmpty

  private def createDepartureTransportMeans(borderTransport: BorderTransport): Consignment.DepartureTransportMeans = {
    val departureTransportMeans = new DepartureTransportMeans()

    borderTransport.meansOfTransportOnDepartureIDNumber.foreach { idValue =>
      val id = new DepartureTransportMeansIdentificationIDType()
      id.setValue(idValue)
      departureTransportMeans.setID(id)
    }

    if (borderTransport.meansOfTransportOnDepartureType.nonEmpty) {
      val identificationTypeCode = new DepartureTransportMeansIdentificationTypeCodeType()
      identificationTypeCode.setValue(borderTransport.meansOfTransportOnDepartureType)
      departureTransportMeans.setIdentificationTypeCode(identificationTypeCode)
    }

    departureTransportMeans
  }
}
