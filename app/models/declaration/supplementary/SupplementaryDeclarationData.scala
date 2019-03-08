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

package models.declaration.supplementary

import forms.MetadataPropertiesConvertable
import forms.supplementary._
import uk.gov.hmrc.http.cache.client.CacheMap

case class SupplementaryDeclarationData(
  declarationType: Option[DeclarationType] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  parties: Option[Parties] = None,
  locations: Option[Locations] = None,
  transportInformation: Option[TransportInformation] = None,
  items: Option[Items] = None,
  previousDocuments: Option[PreviousDocuments] = None,
  additionalInformationData: Option[AdditionalInformationData] = None,
  documentsProducedData: Option[DocumentsProducedData] = None
) extends SummaryContainer with MetadataPropertiesConvertable {
  import SupplementaryDeclarationData._

  override def toMetadataProperties(): Map[String, String] =
    this.toMap.values
      .foldLeft(Map("declaration.functionCode" -> suppDecFunctionCode)) { (map, convertable) =>
        map ++ convertable.toMetadataProperties()
      }
      .filter(_._2.nonEmpty)

  override def isEmpty: Boolean =
    declarationType.isEmpty &&
      consignmentReferences.isEmpty &&
      parties.isEmpty &&
      locations.isEmpty &&
      transportInformation.isEmpty &&
      items.isEmpty &&
      previousDocuments.isEmpty &&
      additionalInformationData.isEmpty &&
      documentsProducedData.isEmpty

  def toMap: Map[String, MetadataPropertiesConvertable] =
    Map(
      DeclarationType.id -> declarationType,
      ConsignmentReferences.id -> consignmentReferences,
      Parties.id -> parties,
      Locations.id -> locations,
      TransportInformation.id -> transportInformation,
      Items.id -> items,
      PreviousDocuments.formId -> previousDocuments,
      AdditionalInformationData.formId -> additionalInformationData,
      DocumentsProducedData.formId -> documentsProducedData
    ).collect { case (key, Some(data)) => (key, data) }
}

object SupplementaryDeclarationData {
  val suppDecFunctionCode = "9"

  def apply(cacheMap: CacheMap): SupplementaryDeclarationData =
    SupplementaryDeclarationData(
      declarationType = flattenIfEmpty(DeclarationType(cacheMap)),
      consignmentReferences = cacheMap.getEntry[ConsignmentReferences](ConsignmentReferences.id),
      parties = flattenIfEmpty(Parties(cacheMap)),
      locations = flattenIfEmpty(Locations(cacheMap)),
      transportInformation = cacheMap.getEntry[TransportInformation](TransportInformation.id),
      items = flattenIfEmpty(Items(cacheMap)),
      previousDocuments = cacheMap.getEntry[PreviousDocuments](PreviousDocuments.formId),
      additionalInformationData = cacheMap.getEntry[AdditionalInformationData](AdditionalInformationData.formId),
      documentsProducedData = cacheMap.getEntry[DocumentsProducedData](DocumentsProducedData.formId)
    )

  private def flattenIfEmpty[A <: SummaryContainer](container: A): Option[A] =
    if (container.isEmpty) None else Some(container)
}
