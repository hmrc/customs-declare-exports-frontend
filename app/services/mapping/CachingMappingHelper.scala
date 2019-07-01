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

package services.mapping

import forms.declaration.{CommodityMeasure, ItemType}
import forms.declaration.ItemType.IdentificationTypeCodes.{
  CUSCode,
  CombinedNomenclatureCode,
  NationalAdditionalCode,
  TARICAdditionalCode
}
import models.declaration.governmentagencygoodsitem.{Classification, Commodity, DangerousGoods, GoodsMeasure, Measure}
import services.ExportsItemsCacheIds.defaultMeasureCode

object CachingMappingHelper {

  def commodityFromItemTypes(itemType: ItemType): Commodity =
    Commodity(
      description = Some(itemType.descriptionOfGoods),
      classifications = getClassificationsFromItemTypes(itemType),
      dangerousGoods = itemType.unDangerousGoodsCode.map(code => Seq(DangerousGoods(Some(code)))).getOrElse(Seq.empty)
    )

  def getClassificationsFromItemTypes(itemType: ItemType): Seq[Classification] =
    Seq(
      Classification(Some(itemType.combinedNomenclatureCode), identificationTypeCode = Some(CombinedNomenclatureCode))
    ) ++ itemType.cusCode.map(id => Classification(Some(id), identificationTypeCode = Some(CUSCode))) ++
      itemType.nationalAdditionalCodes.map(
        code => Classification(Some(code), identificationTypeCode = Some(NationalAdditionalCode))
      ) ++ itemType.taricAdditionalCodes
      .map(code => Classification(Some(code), identificationTypeCode = Some(TARICAdditionalCode)))

  def mapGoodsMeasure(data: CommodityMeasure) =
    Commodity(
      goodsMeasure = Some(
        GoodsMeasure(
          Some(createMeasure(data.grossMass)),
          Some(createMeasure(data.netMass)),
          data.supplementaryUnits.map(createMeasure(_))
        )
      )
    )

  private def createMeasure(unitValue: String) =
    Measure(Some(defaultMeasureCode), value = Some(BigDecimal(unitValue)))

}
