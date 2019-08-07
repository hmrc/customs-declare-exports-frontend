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

package services.mapping.governmentagencygoodsitem
import javax.inject.Inject
import services.cache.ExportItem
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.Packaging
import wco.datamodel.wco.declaration_ds.dms._2.{
  PackagingMarksNumbersIDType,
  PackagingQuantityQuantityType,
  PackagingTypeCodeType
}

class PackagingBuilder @Inject()() extends ModifyingBuilder[ExportItem, GoodsShipment.GovernmentAgencyGoodsItem] {

  def buildThenAdd(
    exportItem: ExportItem,
    wcoGovernmentAgencyGoodsItem: GoodsShipment.GovernmentAgencyGoodsItem
  ): Unit =
    exportItem.packageInformation.zipWithIndex.foreach {
      case (packing, index) => {
        wcoGovernmentAgencyGoodsItem.getPackaging.add(
          createWcoPackaging(Some(index), packing.typesOfPackages, packing.numberOfPackages, packing.shippingMarks)
        )
      }
    }

  private def createWcoPackaging(
    sequenceNumeric: Option[Int],
    typeCode: Option[String],
    quantity: Option[Int],
    marksNumbersId: Option[String]
  ): Packaging = {
    val wcoPackaging = new Packaging

    typeCode.foreach { typeCode =>
      val packagingTypeCodeType = new PackagingTypeCodeType
      packagingTypeCodeType.setValue(typeCode)
      wcoPackaging.setTypeCode(packagingTypeCodeType)
    }

    quantity.foreach { quantity =>
      val packagingQuantityQuantityType = new PackagingQuantityQuantityType
      //TODO noticed here that quantity type in old scala wco is not captured.. no cannot set :-
      // packagingQuantityQuantityType.setUnitCode(????)
      packagingQuantityQuantityType.setValue(new java.math.BigDecimal(quantity))
      wcoPackaging.setQuantityQuantity(packagingQuantityQuantityType)
    }

    marksNumbersId.foreach { markNumber =>
      val packagingMarksNumbersIDType = new PackagingMarksNumbersIDType
      packagingMarksNumbersIDType.setValue(markNumber)
      wcoPackaging.setMarksNumbersID(packagingMarksNumbersIDType)
    }

    sequenceNumeric.foreach(
      numericSequence => wcoPackaging.setSequenceNumeric(new java.math.BigDecimal(numericSequence))
    )

    wcoPackaging
  }
}
