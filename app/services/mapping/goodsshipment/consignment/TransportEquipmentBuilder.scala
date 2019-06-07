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
import forms.declaration.Seal
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.declaration_ds.dms._2.SealIdentificationIDType

import scala.collection.JavaConverters._

object TransportEquipmentBuilder {

  def build()(
    implicit cacheMap: CacheMap,
    choice: Choice
  ): java.util.List[GoodsShipment.Consignment.TransportEquipment] =
    choice match {
      case Choice(AllowedChoiceValues.SupplementaryDec) => Seq.empty.toList.asJava
      case Choice(AllowedChoiceValues.StandardDec) =>
        cacheMap
          .getEntry[Seq[Seal]](Seal.formId)
          .map(createTransportEquipment)
          .getOrElse(Seq.empty)
          .toList
          .asJava
    }

  private def createTransportEquipment(seals: Seq[Seal]): Seq[GoodsShipment.Consignment.TransportEquipment] = {
    val transportEquipment = new GoodsShipment.Consignment.TransportEquipment()
    transportEquipment.setSequenceNumeric(new java.math.BigDecimal(seals.size))
    transportEquipment.getSeal.addAll(seals.zipWithIndex.map(data => createSeal(data)).toList.asJava)
    transportEquipment.getSeal
    Seq(transportEquipment)
  }

  private def createSeal(sealData: (Seal, Int)): Consignment.TransportEquipment.Seal = {
    val seal = new Consignment.TransportEquipment.Seal

    seal.setSequenceNumeric(new java.math.BigDecimal(sealData._2 + 1))

    val identificationIDType = new SealIdentificationIDType
    identificationIDType.setValue(sealData._1.id)
    seal.setID(identificationIDType)

    seal
  }
}
