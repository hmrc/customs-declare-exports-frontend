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

import java.time.Instant

import forms.declaration._
import models.ExportsDeclaration
import models.declaration.dectype.DeclarationTypeSupplementary

case class SupplementaryDeclarationData(
  declarationType: Option[DeclarationTypeSupplementary] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  parties: Option[Parties] = None,
  locations: Option[Locations] = None,
  containers: Seq[Container] = Seq.empty,
  items: Option[Items] = None,
  createDate: Option[Instant] = None,
  updatedDate: Option[Instant] = None
)

object SupplementaryDeclarationData {

  def apply(cacheData: ExportsDeclaration): SupplementaryDeclarationData =
    SupplementaryDeclarationData(
      declarationType = flattenIfEmpty(DeclarationTypeSupplementary(cacheData)),
      consignmentReferences = cacheData.consignmentReferences,
      parties = flattenIfEmpty(Parties(cacheData)),
      locations = flattenIfEmpty(Locations(cacheData)),
      containers = cacheData.transportData.map(_.containers).getOrElse(Seq.empty),
      items = flattenIfEmpty(Items(cacheData)),
      createDate = Some(cacheData.createdDateTime),
      updatedDate = Some(cacheData.updatedDateTime)
    )

  private def flattenIfEmpty[A <: SummaryContainer](container: A): Option[A] =
    if (container.isEmpty) None else Some(container)

  object SchemaMandatoryValues {
    val functionCode = "9"
    val wcoDataModelVersionCode = "3.6"
    val wcoTypeName = "DEC"
    val responsibleCountryCode = "GB"
    val responsibleAgencyName = "HMRC"
    val agencyAssignedCustomizationVersionCode = "v2.1"
  }
}
