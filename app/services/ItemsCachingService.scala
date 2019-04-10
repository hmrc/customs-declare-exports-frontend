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
import forms.declaration.ItemType.IdentificationTypeCodes._
import forms.declaration.{CommodityMeasure, DocumentsProduced, ItemType, PackageInformation}
import javax.inject.Singleton
import models.DeclarationFormats._
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import play.api.http.Status.NO_CONTENT
import services.ExportsItemsCacheIds._
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
          Some(createMeasure(data.grossMass)),
          Some(createMeasure(data.netMass)),
          data.supplementaryUnits.map(createMeasure(_))
        )
      )
    )

  private def createMeasure(unitValue: String) = Measure(Some(defaultMeasureCode), value = Some(BigDecimal(unitValue)))

  def additionalInfo(cachedData: CacheMap): Option[Seq[AdditionalInformation]] =
    cachedData
      .getEntry[AdditionalInformationData](AdditionalInformationData.formId)
      .map(_.items.map(info => AdditionalInformation(Some(info.code), Some(info.description))))

  def documents(cachedData: CacheMap): Option[Seq[GovernmentAgencyGoodsItemAdditionalDocument]] =
    cachedData
      .getEntry[DocumentsProducedData](DocumentsProducedData.formId)
      .map(_.documents.map(createGoodsItemAdditionalDocument(_)))

  private def createGoodsItemAdditionalDocument(doc: DocumentsProduced) =
    GovernmentAgencyGoodsItemAdditionalDocument(
      categoryCode = doc.documentTypeCode.map(_.substring(0, 1)),
      typeCode = doc.documentTypeCode.map(_.substring(1)),
      id = doc.documentIdentifier.map(_ + doc.documentPart.getOrElse("")),
      lpcoExemptionCode = doc.documentStatus,
      name = doc.documentStatusReason,
      submitter = Some(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name = doc.issuingAuthorityName)),
      effectiveDateTime = doc.dateOfValidity.map(date => DateTimeElement(DateTimeString(formatCode = "102", value = date.toString))),
      writeOff = doc.documentQuantity.map(q => WriteOff(quantity = Some(
        Measure(unitCode = doc.measurementUnit, value = Some(q))
      )))
    )

  def goodsItemFromItemTypes(cachedData: CacheMap): Option[GovernmentAgencyGoodsItem] =
    cachedData
      .getEntry[ItemType](ItemType.id)
      .map(
        item => // get all codes create classification
          GovernmentAgencyGoodsItem(
            sequenceNumeric = 1,
            statisticalValueAmount =
              Some(Amount(Some(defaultCurrencyCode), value = Some(BigDecimal(item.statisticalValue)))),
            commodity = Some(commodityFromItemTypes(item))
        )
      )

  private def commodityFromItemTypes(itemType: ItemType): Commodity =
    Commodity(
      description = Some(itemType.descriptionOfGoods),
      classifications = getClassificationsFromItemTypes(itemType),
      dangerousGoods = itemType.unDangerousGoodsCode.map(code => Seq(DangerousGoods(Some(code)))).getOrElse(Seq.empty)
    )

  private def getClassificationsFromItemTypes(itemType: ItemType): Seq[Classification] =
    Seq(
      Classification(Some(itemType.combinedNomenclatureCode), identificationTypeCode = Some(CombinedNomenclatureCode))
    ) ++ itemType.cusCode.map(id => Classification(Some(id), identificationTypeCode = Some(CUSCode))) ++
      itemType.nationalAdditionalCodes.map(
        code => Classification(Some(code), identificationTypeCode = Some(NationalAdditionalCode))
      ) ++ itemType.taricAdditionalCodes
      .map(code => Classification(Some(code), identificationTypeCode = Some(TARICAdditionalCode)))

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
    cacheId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    cacheService.fetch(goodsItemCacheId).flatMap {
      case Some(cachedData) =>
        cacheService
          .fetchAndGetEntry[Seq[GovernmentAgencyGoodsItem]](cacheId, itemsId)
          .flatMap(
            items =>
              cacheService
                .cache[Seq[GovernmentAgencyGoodsItem]](
                  cacheId,
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
  val defaultCurrencyCode = "GBP"
  val defaultMeasureCode = "KGM"
  val itemsCachePages = Map(
    PackageInformation.formId -> "package",
    ProcedureCodesData.formId -> "procedure codes",
    CommodityMeasure.commodityFormId -> "goods measure",
    DocumentsProducedData.formId -> "additional documents",
    ItemType.id -> "Item type",
    AdditionalInformationData.formId -> "additional information"
  )

}
