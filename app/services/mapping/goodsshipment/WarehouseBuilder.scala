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
import forms.declaration.WarehouseIdentification
import javax.inject.Inject
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Warehouse
import wco.datamodel.wco.declaration_ds.dms._2.{WarehouseIdentificationIDType, WarehouseTypeCodeType}

class WarehouseBuilder @Inject()() extends ModifyingBuilder[WarehouseIdentification, GoodsShipment] {
  override def buildThenAdd(model: WarehouseIdentification, goodsShipment: GoodsShipment): Unit =
    if (isDefined(model)) {
      goodsShipment.setWarehouse(createWarehouse(model))
    }

  private def isDefined(warehouse: WarehouseIdentification): Boolean =
    warehouse.identificationNumber.isDefined && warehouse.identificationType.isDefined

  private def createWarehouse(data: WarehouseIdentification): Warehouse = {
    val warehouse = new Warehouse()

    val id = new WarehouseIdentificationIDType()
    id.setValue(data.identificationNumber.getOrElse(""))
    warehouse.setID(id)

    val typeCode = new WarehouseTypeCodeType()
    typeCode.setValue(data.identificationType.getOrElse(""))
    warehouse.setTypeCode(typeCode)

    warehouse
  }
}
