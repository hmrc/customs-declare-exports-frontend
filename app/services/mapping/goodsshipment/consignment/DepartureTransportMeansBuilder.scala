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
import forms.declaration.{BorderTransport, TransportDetails}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment.DepartureTransportMeans
import wco.datamodel.wco.declaration_ds.dms._2.{
  DepartureTransportMeansIdentificationIDType,
  DepartureTransportMeansIdentificationTypeCodeType
}

object DepartureTransportMeansBuilder {

  def build()(implicit cacheMap: CacheMap): Consignment.DepartureTransportMeans = {
    val borderTransport = cacheMap
      .getEntry[BorderTransport](BorderTransport.formId)

    cacheMap
      .getEntry[TransportDetails](TransportDetails.formId)
      .filter(transportDetails => isDefined(transportDetails, borderTransport))
      .map(transportDetails => createDepartureTransportMeans(transportDetails, borderTransport))
      .orNull
  }
  private def isDefined(transportDetails: TransportDetails, borderTransport: Option[BorderTransport]): Boolean =
    borderTransport.forall(border => border.meansOfTransportOnDepartureIDNumber.nonEmpty) ||
      transportDetails.meansOfTransportCrossingTheBorderType.nonEmpty

  private def createDepartureTransportMeans(
    transportDetails: TransportDetails,
    borderTransport: Option[BorderTransport]
  ): Consignment.DepartureTransportMeans = {
    val departureTransportMeans = new DepartureTransportMeans()

    borderTransport.foreach { border =>
      border.meansOfTransportOnDepartureIDNumber.foreach { idValue =>
        val id = new DepartureTransportMeansIdentificationIDType()
        id.setValue(idValue)
        departureTransportMeans.setID(id)
      }
    }

    if (transportDetails.meansOfTransportCrossingTheBorderType.nonEmpty) {
      val identificationTypeCode = new DepartureTransportMeansIdentificationTypeCodeType()
      identificationTypeCode.setValue(transportDetails.meansOfTransportCrossingTheBorderType)
      departureTransportMeans.setIdentificationTypeCode(identificationTypeCode)
    }

    departureTransportMeans
  }
}
