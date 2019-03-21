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

package services
import com.google.inject.Inject
import config.AppConfig
import forms.supplementary.ItemType.IdentificationTypeCodes._
import forms.supplementary.{CommodityMeasure, DocumentsProduced, ItemType, PackageInformation}
import javax.inject.Singleton
import models.DeclarationFormats._
import models.declaration.supplementary.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import play.api.http.Status.NO_CONTENT
import services.ExportsItemsCacheIds.itemsId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItemsCachingService @Inject()(cacheService: CustomsCacheService)(appConfig: AppConfig) {

  def generatePackages(cachedData: CacheMap): Option[Seq[Packaging]] =
    cachedData
      .getEntry[Seq[PackageInformation]](PackageInformation.formId)
      .map(_.zipWithIndex.map {
        case (packageInfo, index) =>
          createPackaging(packageInfo, index)
      })

  private def createPackaging(packageInfo: PackageInformation, index: Int) = Packaging(
    sequenceNumeric = Some(index),
    typeCode = packageInfo.typesOfPackages,
    quantity = packageInfo.numberOfPackages,
    marksNumbersId = packageInfo.shippingMarks
  )

  def procedureCodes(cachedData: CacheMap): Option[Seq[GovernmentProcedure]] =
    cachedData
      .getEntry[ProcedureCodesData](ProcedureCodesData.formId)
      .map(
        form =>
          Seq(GovernmentProcedure(form.procedureCode.map(_.substring(0, 2)), form.procedureCode.map(_.substring(2, 4))))
            ++ form.additionalProcedureCodes.map(code => GovernmentProcedure(Some(code)))
      )

  def commodityFromGoodsMeasure(cachedData: CacheMap): Option[Commodity] =
    cachedData
      .getEntry[CommodityMeasure](CommodityMeasure.commodityFormId)
      .map(mapGoodsMeasure(_))

  private def mapGoodsMeasure(data: CommodityMeasure) =
    Commodity(
      goodsMeasure = Some(
        GoodsMeasure(
          Some(Measure(value = Some(BigDecimal(data.grossMass)))),
          Some(Measure(value = Some(BigDecimal(data.netMass)))),
          Some(Measure(value = data.supplementaryUnits.map((BigDecimal(_)))))
        )
      )
    )

  def additionalInfo(cachedData: CacheMap): Option[Seq[AdditionalInformation]] =
    cachedData
      .getEntry[AdditionalInformationData](AdditionalInformationData.formId)
      .map(_.items.map(info => AdditionalInformation(Some(info.code), Some(info.description))))

  def documents(cachedData: CacheMap) =
    cachedData
      .getEntry[DocumentsProducedData](DocumentsProducedData.formId)
      .map(_.documents.map(createGoodsItemAdditionalDocument(_)))

  private def createGoodsItemAdditionalDocument(doc: DocumentsProduced) =
    GovernmentAgencyGoodsItemAdditionalDocument(
      doc.documentTypeCode.map(_.substring(0, 1)),
      typeCode = doc.documentTypeCode.map(_.substring(1)),
      id = doc.documentIdentifier.map(_ + doc.documentPart.getOrElse("")),
      lpcoExemptionCode = doc.documentStatus,
      name = doc.documentStatusReason,
      writeOff = Some(WriteOff(Some(Measure(value = doc.documentQuantity.map(BigDecimal(_))))))
    )

  def goodsItemFromItemTypes(cachedData: CacheMap): Option[GovernmentAgencyGoodsItem] =
    cachedData
      .getEntry[ItemType](ItemType.id)
      .map(
        item => // get all codes create classification
          GovernmentAgencyGoodsItem(
            sequenceNumeric = 1,
            statisticalValueAmount = Some(Amount(value = Some(BigDecimal(item.statisticalValue)))),
            commodity = Some(
              Commodity(
                description = Some(item.descriptionOfGoods),
                classifications = Seq(
                  Classification(
                    Some(item.combinedNomenclatureCode),
                    identificationTypeCode = Some(CombinedNomenclatureCode)
                  ),
                  Classification(item.cusCode, identificationTypeCode = Some(CUSCode))
                ) ++
                  item.nationalAdditionalCodes.map(
                    code => Classification(Some(code), identificationTypeCode = Some(NationalAdditionalCode))
                  ) ++ item.taricAdditionalCodes
                  .map(code => Classification(Some(code), identificationTypeCode = Some(TARICAdditionalCode)))
              )
            )
        )
      )

  private def createGoodsItem(seq: Int, cachedData: CacheMap): GovernmentAgencyGoodsItem = {
    val itemTypeData = goodsItemFromItemTypes(cachedData)
    val commodity = itemTypeData.fold(Commodity())(_.commodity.getOrElse(Commodity()))
    val updatedCommodity = commodity.copy(goodsMeasure = commodityFromGoodsMeasure(cachedData).flatMap(_.goodsMeasure))

    GovernmentAgencyGoodsItem(
      sequenceNumeric = seq + 1,
      statisticalValueAmount = itemTypeData.flatMap(_.statisticalValueAmount),
      packagings = generatePackages(cachedData).getOrElse(Seq.empty),
      governmentProcedures = procedureCodes(cachedData).getOrElse(Seq.empty),
      commodity = Some(updatedCommodity),
      additionalInformations = additionalInfo(cachedData).getOrElse(Seq.empty),
      additionalDocuments = documents(cachedData).getOrElse(Seq.empty)
    )
  }
  /*
    Fetch cache elements of all the pages using goodsItemCacheId for pages procedure codes, itemType, packaging, goods measure,
    additional information , documents produced.
    create related wco-dec elements
    create goods item for the cache and append to items already added in the cache
    save updated items to cache and remove the elements in goodsItemCacheId
   */
  def addItemToCache(
    goodsItemCacheId: String,
    supplementaryCacheId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    cacheService.fetch(goodsItemCacheId).flatMap {
      case Some(cachedData) =>
        cacheService
          .fetchAndGetEntry[Seq[GovernmentAgencyGoodsItem]](supplementaryCacheId, itemsId)
          .flatMap(
            items =>
              cacheService
                .cache[Seq[GovernmentAgencyGoodsItem]](
                  supplementaryCacheId,
                  itemsId,
                  items.getOrElse(Seq.empty) :+ createGoodsItem(items.fold(0)(_.size), cachedData)
                )
                .flatMap(_ => cacheService.remove(goodsItemCacheId).map(_.status == NO_CONTENT))
          )
      case None => Future.successful(false)
    }

}

object ExportsItemsCacheIds {
  val itemsId = "exportItems"
  val itemsCachePages = Map(
    PackageInformation.formId -> "package",
    ProcedureCodesData.formId -> "procedure codes",
    CommodityMeasure.commodityFormId -> "goods measure",
    DocumentsProducedData.formId -> "additional documents",
    ItemType.id -> "Item type",
    AdditionalInformationData.formId -> "additional information"
  )

}
