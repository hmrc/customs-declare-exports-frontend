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
import forms.Choice
import forms.Choice.AllowedChoiceValues
import javax.inject.Inject
import services.cache.ExportsCacheModel
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class ConsignmentBuilder @Inject()(
  goodsLocationBuilder: GoodsLocationBuilder,
  containerCodeBuilder: ContainerCodeBuilder,
  departureTransportMeansBuilder: DepartureTransportMeansBuilder,
  arrivalTransportMeansBuilder: ArrivalTransportMeansBuilder,
  transportEquipmentBuilder: TransportEquipmentBuilder
) {

  def buildThenAdd(exportsCacheModel: ExportsCacheModel, goodsShipment: GoodsShipment): Unit = {
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
      case AllowedChoiceValues.SupplementaryDec => transportEquipmentBuilder.buildThenAdd(Seq.empty, consignment)
      case AllowedChoiceValues.StandardDec =>
        transportEquipmentBuilder.buildThenAdd(exportsCacheModel.seals, consignment)
    }

    goodsShipment.setConsignment(consignment)
  }
}
object ConsignmentBuilder {

  def build(implicit cacheMap: CacheMap, choice: Choice): GoodsShipment.Consignment = {
    val consignment = new GoodsShipment.Consignment()

    consignment.setGoodsLocation(GoodsLocationBuilder.build)
    consignment.setContainerCode(ContainerCodeBuilder.build)
    consignment.setArrivalTransportMeans(ArrivalTransportMeansBuilder.build)
    consignment.setDepartureTransportMeans(DepartureTransportMeansBuilder.build)

    val transportEquipments = TransportEquipmentBuilder.build
    if (!transportEquipments.isEmpty) {
      consignment.getTransportEquipment.addAll(transportEquipments)
    }

    consignment
  }

}
