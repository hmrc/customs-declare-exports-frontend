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
import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.ModifyingBuilder
import services.mapping.goodsshipment.consignment.ConsignmentBuilder
import services.mapping.governmentagencygoodsitem.GovernmentAgencyGoodsItemBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class GoodsShipmentBuilder @Inject()(
  goodsShipmentNatureOfTransactionBuilder: GoodsShipmentNatureOfTransactionBuilder,
  consigneeBuilder: ConsigneeBuilder,
  consignmentBuilder: ConsignmentBuilder,
  destinationBuilder: DestinationBuilder,
  exportCountryBuilder: ExportCountryBuilder,
  governmentAgencyGoodsItemBuilder: GovernmentAgencyGoodsItemBuilder,
  ucrBuilder: UCRBuilder,
  warehouseBuilder: WarehouseBuilder,
  previousDocumentsBuilder: PreviousDocumentsBuilder,
  aeoMutualRecognitionPartiesBuilder: AEOMutualRecognitionPartiesBuilder
) extends ModifyingBuilder[ExportsCacheModel, Declaration] {

  override def buildThenAdd(exportsCacheModel: ExportsCacheModel, declaration: Declaration): Unit = {
    val goodsShipment = new GoodsShipment()

    exportsCacheModel.natureOfTransaction.foreach(
      goodsShipmentNatureOfTransactionBuilder.buildThenAdd(_, goodsShipment)
    )

    exportsCacheModel.parties.consigneeDetails
      .foreach(consigneeBuilder.buildThenAdd(_, goodsShipment))

    consignmentBuilder.buildThenAdd(exportsCacheModel, goodsShipment)

    exportsCacheModel.locations.destinationCountries.foreach { countries =>
      destinationBuilder.buildThenAdd(countries, goodsShipment)
      exportCountryBuilder.buildThenAdd(countries, goodsShipment)
    }

    exportsCacheModel.consignmentReferences.foreach(ucrBuilder.buildThenAdd(_, goodsShipment))

    exportsCacheModel.locations.warehouseIdentification
      .foreach(warehouseBuilder.buildThenAdd(_, goodsShipment))

    exportsCacheModel.previousDocuments.foreach(previousDocumentsBuilder.buildThenAdd(_, goodsShipment))

    exportsCacheModel.parties.declarationAdditionalActorsData.foreach {
      _.actors.foreach(
        declarationAdditionalActor =>
          aeoMutualRecognitionPartiesBuilder.buildThenAdd(declarationAdditionalActor, goodsShipment)
      )
    }

    exportsCacheModel.items.foreach(governmentAgencyGoodsItemBuilder.buildThenAdd(_, goodsShipment))

    declaration.setGoodsShipment(goodsShipment)
  }

}
