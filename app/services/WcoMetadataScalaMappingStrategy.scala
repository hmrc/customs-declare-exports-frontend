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
import forms.Choice
import forms.declaration._
import models.declaration.SupplementaryDeclarationData
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{GovernmentAgencyGoodsItem => InternalAgencyGoodsItem}
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.{
  BorderTransportMeans,
  MetaData,
  PreviousDocument,
  TransportEquipment,
  TransportMeans,
  Seal => WCOSeal
}

trait WcoMetadataScalaMappingStrategy extends WcoMetadataMappingStrategy {

  override def produceMetaData(cacheMap: CacheMap, choice: Choice): MetaData = {

    val metaData = createHeaderData(cacheMap)
    val goodsItems =
      cacheMap
        .getEntry[Seq[InternalAgencyGoodsItem]](ExportsItemsCacheIds.itemsId)
        .getOrElse(Seq.empty)
        .map(item => {
          WcoMetadataScalaMapper.mapGoodsItem(item)
        })

    val borderTransport = cacheMap.getEntry[BorderTransport](BorderTransport.formId)
    val transportDetails = cacheMap.getEntry[TransportDetails](TransportDetails.formId)

    val goodsShipmentWithGoodsItems =
      metaData.declaration.flatMap(
        _.goodsShipment.map(
          goodsShipment =>
            goodsShipment.copy(
              governmentAgencyGoodsItems = goodsItems,
              previousDocuments = mapPreviousDocumentsForHeaderFromCache(cacheMap),
              consignment = goodsShipment.consignment.map(
                _.copy(
                  transportEquipments = createTransportEquipment(cacheMap),
                  departureTransportMeans = createTransportMeans(borderTransport),
                  containerCode = getContainerCode(transportDetails)
                )
              )
          )
        )
      )
    metaData.copy(
      declaration = metaData.declaration.map(
        _.copy(
          goodsShipment = goodsShipmentWithGoodsItems,
          borderTransportMeans = getBorderTransportMeans(borderTransport, transportDetails)
        )
      )
    )
  }

  private def getContainerCode(transportDetails: Option[TransportDetails]) =
    transportDetails.map(data => if (data.container) "1" else "0")

  private def createTransportMeans(borderTransport: Option[BorderTransport]): Option[TransportMeans] =
    borderTransport.map(
      data =>
        TransportMeans(
          identificationTypeCode = Some(data.meansOfTransportOnDepartureType),
          id = data.meansOfTransportOnDepartureIDNumber
      )
    )

  private def getBorderTransportMeans(
    borderTransport: Option[BorderTransport],
    transportDetails: Option[TransportDetails]
  ): Option[BorderTransportMeans] =
    borderTransport.map(data => createBorderTransportMeans(data.borderModeOfTransportCode.toInt, transportDetails))

  private def createBorderTransportMeans(borderModeOfTransportCode: Int, transportDetails: Option[TransportDetails]) =
    BorderTransportMeans(
      modeCode = Some(borderModeOfTransportCode),
      registrationNationalityCode = getRegistrationNationalityCode(transportDetails),
      identificationTypeCode = transportDetails.map(_.meansOfTransportCrossingTheBorderType),
      id = transportDetails.flatMap(_.meansOfTransportCrossingTheBorderIDNumber)
    )

  private def getRegistrationNationalityCode(transportDetails: Option[TransportDetails]) =
    transportDetails.flatMap(_.meansOfTransportCrossingTheBorderNationality.flatMap(getCountryCode(_)))

  def getCountryCode(name: String) = allCountries.find(_.countryName == name).map(_.countryCode)

  private def createTransportEquipment(cacheMap: CacheMap): Seq[TransportEquipment] =
    cacheMap
      .getEntry[Seq[Seal]](Seal.formId)
      .map(seals => Seq(TransportEquipment(seals.size, seals = createWcoDecSeals(seals: Seq[Seal]))))
      .getOrElse(Seq.empty)

  private def createWcoDecSeals(seals: Seq[Seal]) = seals.zipWithIndex.map {
    case (seal, sequence) => WCOSeal(sequence + 1, Some(seal.id))
  }

  def mapPreviousDocumentsForHeaderFromCache(cacheMap: CacheMap): Seq[PreviousDocument] =
    cacheMap
      .getEntry[PreviousDocumentsData](Document.formId)
      .map(_.documents.map(createPreviousDocuments(_)))
      .getOrElse(Seq.empty)

  private def createPreviousDocuments(doc: Document) =
    PreviousDocument(
      Some(doc.documentCategory),
      Some(doc.documentReference),
      Some(doc.documentType),
      doc.goodsItemIdentifier.map(_.toInt)
    )

  private def createHeaderData(cacheMap: CacheMap): MetaData =
    MetaData.fromProperties(SupplementaryDeclarationData(cacheMap).toMetadataProperties())

  override def declarationUcr(metaData: Any): Option[String] =
    metaData
      .asInstanceOf[MetaData]
      .declaration
      .flatMap(_.goodsShipment.flatMap(_.ucr.flatMap(_.traderAssignedReferenceId)))

  override def declarationLrn(metaData: Any): Option[String] =
    metaData.asInstanceOf[MetaData].declaration.flatMap(_.functionalReferenceId)

  override def toXml(metaData: Any): String = metaData.asInstanceOf[MetaData].toXml
}
