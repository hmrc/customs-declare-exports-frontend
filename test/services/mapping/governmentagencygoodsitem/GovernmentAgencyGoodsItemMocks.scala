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
import forms.declaration._
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Reads
import uk.gov.hmrc.http.cache.client.CacheMap

trait GovernmentAgencyGoodsItemMocks extends MockitoSugar with GovernmentAgencyGoodsItemData {

  def setUpAdditionalDocuments()(implicit cacheMap: CacheMap) {
    when(cacheMap.getEntry[DocumentsProducedData](eqTo(DocumentsProducedData.formId))(any()))
      .thenReturn(Some(documentsProducedData))
  }

  def setUpAdditionalInformation()(implicit cacheMap: CacheMap): Unit =
    when(
      cacheMap
        .getEntry[AdditionalInformationData](eqTo(AdditionalInformationData.formId))(any())
    ).thenReturn(Some(additionalInformationData))

  def setUpPackageInformation()(implicit cacheMap: CacheMap): Unit =
    when(
      cacheMap
        .getEntry[Seq[PackageInformation]](eqTo(PackageInformation.formId))(any[Reads[Seq[PackageInformation]]])
    ).thenReturn(Some(Seq(packageInformation)))

  def setUpItemType()(implicit cacheMap: CacheMap): Unit =
    when(cacheMap.getEntry[ItemType](eqTo(ItemType.id))(any[Reads[ItemType]])).thenReturn(itemType)

  def setUpCommodityMeasure()(implicit cacheMap: CacheMap): Unit =
    when(cacheMap.getEntry[CommodityMeasure](eqTo(CommodityMeasure.commodityFormId))(any()))
      .thenReturn(Some(commodityMeasure))

  def setUpProcedureCodes()(implicit cacheMap: CacheMap): Unit =
    when(
      cacheMap
        .getEntry[ProcedureCodesData](eqTo(ProcedureCodesData.formId))(any())
    ).thenReturn(Some(procedureCodesData))
}
