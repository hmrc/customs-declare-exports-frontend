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
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration._
import javax.inject.Singleton
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem._
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}
import play.api.http.Status.NO_CONTENT
import services.ExportsItemsCacheIds._
import services.mapping.CachingMappingHelper
import services.mapping.governmentagencygoodsitem.PackagingBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItemsCachingService @Inject()(cacheService: CustomsCacheService)(appConfig: AppConfig) {

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

  private def createGoodsItem(seq: Int, cachedData: CacheMap): GovernmentAgencyGoodsItem = {
    val itemTypeData = goodsItemFromItemTypes(cachedData)
    val commodity = itemTypeData.fold(models.declaration.governmentagencygoodsitem.Commodity())(
      _.commodity.getOrElse(models.declaration.governmentagencygoodsitem.Commodity())
    )
    val updatedCommodity = commodity.copy(goodsMeasure = commodityFromGoodsMeasure(cachedData).flatMap(_.goodsMeasure))

    GovernmentAgencyGoodsItem(
      sequenceNumeric = seq + 1,
      statisticalValueAmount = itemTypeData.flatMap(_.statisticalValueAmount),
      packagings = generatePackages(cachedData).getOrElse(Seq.empty),
      governmentProcedures = procedureCodes(cachedData).getOrElse(Seq.empty),
      commodity = Some(updatedCommodity),
      additionalInformations = additionalInfo(cachedData).getOrElse(Seq.empty),
      additionalDocuments = documents(cachedData).getOrElse(Seq.empty),
      fiscalReferences = if (hasFiscalReferences(cachedData)) fiscalReferences(cachedData) else Seq.empty
    )
  }

  def generatePackages(cachedData: CacheMap): Option[Seq[models.declaration.governmentagencygoodsitem.Packaging]] =
    cachedData
      .getEntry[Seq[PackageInformation]](PackageInformation.formId)
      .map(_.zipWithIndex.map {
        case (packageInfo, index) =>
          PackagingBuilder.createPackaging(packageInfo, index)
      })

  def procedureCodes(
    cachedData: CacheMap
  ): Option[Seq[models.declaration.governmentagencygoodsitem.GovernmentProcedure]] =
    cachedData
      .getEntry[ProcedureCodesData](ProcedureCodesData.formId)
      .map(form => {
        val extractedCode = ProcedureCodes.extractProcedureCode(form.toProcedureCode())
        Seq(
          models.declaration.governmentagencygoodsitem
            .GovernmentProcedure(extractedCode._1, extractedCode._2)
        ) ++ form.additionalProcedureCodes
          .map(code => models.declaration.governmentagencygoodsitem.GovernmentProcedure(Some(code)))
      })

  def commodityFromGoodsMeasure(cachedData: CacheMap): Option[models.declaration.governmentagencygoodsitem.Commodity] =
    cachedData
      .getEntry[CommodityMeasure](CommodityMeasure.commodityFormId)
      .map(CachingMappingHelper.mapGoodsMeasure(_))

  def additionalInfo(cachedData: CacheMap): Option[Seq[forms.declaration.AdditionalInformation]] =
    cachedData
      .getEntry[AdditionalInformationData](AdditionalInformationData.formId)
      .map(_.items.map(info => forms.declaration.AdditionalInformation(info.code, info.description)))

  def documents(cachedData: CacheMap): Option[Seq[GovernmentAgencyGoodsItemAdditionalDocument]] =
    cachedData
      .getEntry[DocumentsProducedData](DocumentsProducedData.formId)
      .map(_.documents.map(createGoodsItemAdditionalDocument))

  def hasFiscalReferences(cachedData: CacheMap): Boolean =
    cachedData
      .getEntry[FiscalInformation](FiscalInformation.formId)
      .map(_.onwardSupplyRelief == FiscalInformation.AllowedFiscalInformationAnswers.yes)
      .getOrElse(false)

  def fiscalReferences(cachedData: CacheMap): Seq[AdditionalFiscalReference] =
    cachedData
      .getEntry[AdditionalFiscalReferencesData](AdditionalFiscalReferencesData.formId)
      .map(_.references)
      .getOrElse(Seq.empty)

  private def createGoodsItemAdditionalDocument(doc: DocumentsProduced) =
    GovernmentAgencyGoodsItemAdditionalDocument(
      categoryCode = doc.documentTypeCode.map(_.substring(0, 1)),
      typeCode = doc.documentTypeCode.map(_.substring(1)),
      id = createAdditionalDocumentId(doc),
      lpcoExemptionCode = doc.documentStatus,
      name = doc.documentStatusReason,
      submitter =
        doc.issuingAuthorityName.map(name => GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name = Some(name))),
      effectiveDateTime = doc.dateOfValidity
        .map(date => DateTimeElement(DateTimeString(formatCode = dateTimeCode, value = date.to102Format))),
      writeOff = createAdditionalDocumentWriteOff(doc)
    )

  private def createAdditionalDocumentId(doc: DocumentsProduced): Option[String] =
    for {
      documentIdentifierAndPart <- doc.documentIdentifierAndPart
      documentIdentifier <- documentIdentifierAndPart.documentIdentifier
      documentPart <- documentIdentifierAndPart.documentPart
    } yield documentIdentifier + documentPart

  private def createAdditionalDocumentWriteOff(doc: DocumentsProduced): Option[WriteOff] =
    for {
      documentWriteOff <- doc.documentWriteOff
      measurementUnit <- documentWriteOff.measurementUnit
      quantity <- documentWriteOff.documentQuantity
    } yield WriteOff(quantity = Some(Measure(unitCode = Some(measurementUnit), value = Some(quantity))))

  def goodsItemFromItemTypes(
    cachedData: CacheMap
  ): Option[models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem] =
    cachedData
      .getEntry[ItemType](ItemType.id)
      .map(
        item => // get all codes create classification
          models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem(
            sequenceNumeric = 1,
            statisticalValueAmount = Some(
              models.declaration.governmentagencygoodsitem
                .Amount(Some(defaultCurrencyCode), value = Some(BigDecimal(item.statisticalValue)))
            ),
            commodity = Some(CachingMappingHelper.commodityFromItemTypes(item))
        )
      )

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
  val dateTimeCode = "102"

}
