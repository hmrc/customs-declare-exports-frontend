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
import forms.declaration.PackageInformation
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.Packaging
import wco.datamodel.wco.declaration_ds.dms._2.{
  PackagingMarksNumbersIDType,
  PackagingQuantityQuantityType,
  PackagingTypeCodeType
}

object PackageBuilder {

  def build(implicit cacheMap: CacheMap): Option[Seq[Packaging]] =
    cacheMap
      .getEntry[Seq[PackageInformation]](PackageInformation.formId)
      .map(_.zipWithIndex.map {
        case (packageInfo, index) =>
          createWcoPackaging(packageInfo, index)
      })

  private def createWcoPackaging(packageInfo: PackageInformation, index: Int): Packaging = {
    val wcoPackaging = new Packaging
    val packagingTypeCodeType = new PackagingTypeCodeType
    packagingTypeCodeType.setValue(packageInfo.typesOfPackages.orNull)

    val packagingQuantityQuantityType = new PackagingQuantityQuantityType
    //TODO noticed here that quantity type in old scala wco is not captured.. no cannot set :-
    // packagingQuantityQuantityType.setUnitCode(????)
    packagingQuantityQuantityType.setValue(BigDecimal(packageInfo.numberOfPackages.getOrElse(0)).bigDecimal)

    val packagingMarksNumbersIDType = new PackagingMarksNumbersIDType
    packagingMarksNumbersIDType.setValue(packageInfo.shippingMarks.orNull)

    wcoPackaging.setMarksNumbersID(packagingMarksNumbersIDType)
    wcoPackaging.setQuantityQuantity(packagingQuantityQuantityType)
    wcoPackaging.setSequenceNumeric(BigDecimal(index).bigDecimal)
    wcoPackaging.setTypeCode(packagingTypeCodeType)
    wcoPackaging
  }
}
