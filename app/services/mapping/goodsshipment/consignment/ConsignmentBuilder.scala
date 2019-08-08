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
import forms.Choice.AllowedChoiceValues
import javax.inject.Inject
import models.ExportsDeclaration
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class ConsignmentBuilder @Inject()(
  goodsLocationBuilder: GoodsLocationBuilder,
  containerCodeBuilder: ContainerCodeBuilder,
  departureTransportMeansBuilder: DepartureTransportMeansBuilder,
  arrivalTransportMeansBuilder: ArrivalTransportMeansBuilder,
  transportEquipmentBuilder: TransportEquipmentBuilder
) {

  def buildThenAdd(exportsCacheModel: ExportsDeclaration, goodsShipment: GoodsShipment): Unit = {
    val consignment = new GoodsShipment.Consignment()

    exportsCacheModel.locations.goodsLocation
      .foreach(goodsLocation => goodsLocationBuilder.buildThenAdd(goodsLocation, consignment))

    exportsCacheModel.transportDetails.foreach(
      transportDetails => containerCodeBuilder.buildThenAdd(transportDetails, consignment)
    )

    exportsCacheModel.locations.warehouseIdentification.foreach(
      warehouseIdentification => arrivalTransportMeansBuilder.buildThenAdd(warehouseIdentification, consignment)
    )

    exportsCacheModel.borderTransport.foreach(
      borderTransport => departureTransportMeansBuilder.buildThenAdd(borderTransport, consignment)
    )

    exportsCacheModel.choice match {
      case AllowedChoiceValues.StandardDec =>
        transportEquipmentBuilder.buildThenAdd(exportsCacheModel.seals, consignment)
      case _ =>
    }

    goodsShipment.setConsignment(consignment)
  }
}
