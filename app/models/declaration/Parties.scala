/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.common.YesNoAnswer
import forms.declaration._
import forms.declaration.carrier.CarrierDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.exporter.ExporterDetails
import models.ExportsDeclaration
import play.api.libs.json.Json

case class Parties(
  exporterDetails: Option[ExporterDetails] = None,
  isExs: Option[IsExs] = None,
  consigneeDetails: Option[ConsigneeDetails] = None,
  consignorDetails: Option[ConsignorDetails] = None,
  declarantDetails: Option[DeclarantDetails] = None,
  declarantIsExporter: Option[DeclarantIsExporter] = None,
  representativeDetails: Option[RepresentativeDetails] = None,
  declarationAdditionalActorsData: Option[DeclarationAdditionalActorsData] = None,
  declarationHoldersData: Option[DeclarationHoldersData] = None,
  authorisationProcedureCodeChoice: Option[AuthorisationProcedureCodeChoice] = None,
  carrierDetails: Option[CarrierDetails] = None,
  isEntryIntoDeclarantsRecords: Option[YesNoAnswer] = None,
  personPresentingGoodsDetails: Option[PersonPresentingGoodsDetails] = None
)

object Parties {
  val id = "Parties"

  implicit val format = Json.format[Parties]

  def apply(cacheData: ExportsDeclaration): Parties = Parties(
    exporterDetails = cacheData.parties.exporterDetails,
    consigneeDetails = cacheData.parties.consigneeDetails,
    consignorDetails = cacheData.parties.consignorDetails,
    declarantDetails = cacheData.parties.declarantDetails,
    representativeDetails = cacheData.parties.representativeDetails,
    declarationAdditionalActorsData = cacheData.parties.declarationAdditionalActorsData,
    declarationHoldersData = cacheData.parties.declarationHoldersData,
    authorisationProcedureCodeChoice = cacheData.parties.authorisationProcedureCodeChoice,
    carrierDetails = cacheData.parties.carrierDetails,
    isEntryIntoDeclarantsRecords = cacheData.parties.isEntryIntoDeclarantsRecords,
    personPresentingGoodsDetails = cacheData.parties.personPresentingGoodsDetails
  )
}
