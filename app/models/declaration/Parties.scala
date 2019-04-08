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

package models.declaration

import forms.MetadataPropertiesConvertable
import forms.declaration._
import uk.gov.hmrc.http.cache.client.CacheMap

case class Parties(
  exporterDetails: Option[ExporterDetails] = None,
  consigneeDetails: Option[ConsigneeDetails] = None,
  declarantDetails: Option[DeclarantDetails] = None,
  representativeDetails: Option[RepresentativeDetails] = None,
  declarationAdditionalActorsData: Option[DeclarationAdditionalActorsData] = None,
  declarationHoldersData: Option[DeclarationHoldersData] = None
) extends SummaryContainer with MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Seq(
      exporterDetails.map(_.toMetadataProperties()),
      consigneeDetails.map(_.toMetadataProperties()),
      declarantDetails.map(_.toMetadataProperties()),
      representativeDetails.map(_.toMetadataProperties()),
      declarationAdditionalActorsData.map(_.toMetadataProperties()),
      declarationHoldersData.map(_.toMetadataProperties())
    ).flatten.fold(Map.empty)(_ ++ _)

  def isEmpty: Boolean =
      exporterDetails.isEmpty &&
      consigneeDetails.isEmpty &&
      declarantDetails.isEmpty &&
      representativeDetails.isEmpty &&
      declarationAdditionalActorsData.isEmpty &&
      declarationHoldersData.isEmpty
}

object Parties {
  val id = "Parties"

  def apply(cacheMap: CacheMap): Parties = Parties(
    exporterDetails = cacheMap.getEntry[ExporterDetails](ExporterDetails.id),
    consigneeDetails = cacheMap.getEntry[ConsigneeDetails](ConsigneeDetails.id),
    declarantDetails = cacheMap.getEntry[DeclarantDetails](DeclarantDetails.id),
    representativeDetails = cacheMap.getEntry[RepresentativeDetails](RepresentativeDetails.formId),
    declarationAdditionalActorsData =
      cacheMap.getEntry[DeclarationAdditionalActorsData](DeclarationAdditionalActorsData.formId),
    declarationHoldersData = cacheMap.getEntry[DeclarationHoldersData](DeclarationHoldersData.formId)
  )
}
