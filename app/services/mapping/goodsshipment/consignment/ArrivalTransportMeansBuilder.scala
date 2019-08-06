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
import forms.declaration.WarehouseIdentification
import javax.inject.Inject
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment.ArrivalTransportMeans
import wco.datamodel.wco.declaration_ds.dms._2.ArrivalTransportMeansModeCodeType

class ArrivalTransportMeansBuilder @Inject()() extends ModifyingBuilder[WarehouseIdentification, Consignment] {
  override def buildThenAdd(model: WarehouseIdentification, consignment: Consignment): Unit =
    if (isDefined(model)) {
      consignment.setArrivalTransportMeans(createArrivalTransportMeans(model))
    }

  private def isDefined(model: WarehouseIdentification): Boolean = model.inlandModeOfTransportCode.isDefined

  private def createArrivalTransportMeans(transportInformation: WarehouseIdentification): ArrivalTransportMeans = {
    val arrivalTransportMeans = new ArrivalTransportMeans()

    transportInformation.inlandModeOfTransportCode.foreach { value =>
      val modeCodeType = new ArrivalTransportMeansModeCodeType()
      modeCodeType.setValue(value)
      arrivalTransportMeans.setModeCode(modeCodeType)
    }

    arrivalTransportMeans
  }
}
