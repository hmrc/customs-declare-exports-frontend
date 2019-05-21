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
import models.declaration.governmentagencygoodsitem
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.Packaging
import wco.datamodel.wco.declaration_ds.dms._2.{
  PackagingMarksNumbersIDType,
  PackagingQuantityQuantityType,
  PackagingTypeCodeType
}

import scala.collection.JavaConverters._
object PackagingBuilder {

  def build(packagings: Seq[models.declaration.governmentagencygoodsitem.Packaging]): java.util.List[Packaging] =
    packagings
      .map(createWcoPackaging)
      .toList
      .asJava

  def createWcoPackaging(packaging: governmentagencygoodsitem.Packaging): Packaging = {
    val wcoPackaging = new Packaging

    packaging.typeCode.foreach { typeCode =>
      val packagingTypeCodeType = new PackagingTypeCodeType
      packagingTypeCodeType.setValue(typeCode)
      wcoPackaging.setTypeCode(packagingTypeCodeType)
    }

    packaging.quantity.foreach { quantity =>
      val packagingQuantityQuantityType = new PackagingQuantityQuantityType
      //TODO noticed here that quantity type in old scala wco is not captured.. no cannot set :-
      // packagingQuantityQuantityType.setUnitCode(????)
      packagingQuantityQuantityType.setValue(new java.math.BigDecimal(quantity))
      wcoPackaging.setQuantityQuantity(packagingQuantityQuantityType)
    }

    packaging.marksNumbersId.foreach { markNumber =>
      val packagingMarksNumbersIDType = new PackagingMarksNumbersIDType
      packagingMarksNumbersIDType.setValue(markNumber)
      wcoPackaging.setMarksNumbersID(packagingMarksNumbersIDType)
    }

    packaging.sequenceNumeric.foreach(
      numericSequence => wcoPackaging.setSequenceNumeric(new java.math.BigDecimal(numericSequence))
    )

    wcoPackaging
  }
}
