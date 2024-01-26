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
import models.{ExportsDeclaration, FieldMapping}
import models.ExportsFieldPointer.ExportsFieldPointer
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, ExportsDeclarationDiff}

case class Parties(
  exporterDetails: Option[ExporterDetails] = None,
  isExs: Option[IsExs] = None,
  consigneeDetails: Option[ConsigneeDetails] = None,
  consignorDetails: Option[ConsignorDetails] = None,
  declarantDetails: Option[DeclarantDetails] = None,
  declarantIsExporter: Option[DeclarantIsExporter] = None,
  representativeDetails: Option[RepresentativeDetails] = None,
  declarationAdditionalActorsData: Option[AdditionalActors] = None,
  declarationHoldersData: Option[AuthorisationHolders] = None,
  authorisationProcedureCodeChoice: Option[AuthorisationProcedureCodeChoice] = None,
  carrierDetails: Option[CarrierDetails] = None,
  isEntryIntoDeclarantsRecords: Option[YesNoAnswer] = None,
  personPresentingGoodsDetails: Option[PersonPresentingGoodsDetails] = None
) extends DiffTools[Parties] {

  // isExs, declarantIsExporter & authorisationProcedureCodeChoice fields are not used to create WCO XML
  override def createDiff(original: Parties, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareDifference(
        original.isEntryIntoDeclarantsRecords,
        isEntryIntoDeclarantsRecords,
        combinePointers(pointerString, Parties.isEntryIntoDeclarantsRecordsPointer, sequenceId)
      )
    ).flatten ++
      createDiffOfOptions(original.exporterDetails, exporterDetails, combinePointers(pointerString, ExporterDetails.pointer, sequenceId)) ++
      createDiffOfOptions(original.consigneeDetails, consigneeDetails, combinePointers(pointerString, ConsigneeDetails.pointer, sequenceId)) ++
      createDiffOfOptions(original.consignorDetails, consignorDetails, combinePointers(pointerString, ConsignorDetails.pointer, sequenceId)) ++
      createDiffOfOptions(original.declarantDetails, declarantDetails, combinePointers(pointerString, DeclarantDetails.pointer, sequenceId)) ++
      createDiffOfOptions(
        original.representativeDetails,
        representativeDetails,
        combinePointers(pointerString, RepresentativeDetails.pointer, sequenceId)
      ) ++
      createDiffOfOptionIsos(
        original.declarationHoldersData,
        declarationHoldersData,
        combinePointers(pointerString, AuthorisationHolders.pointer, sequenceId)
      ) ++
      createDiffOfOptionIsos(
        original.declarationAdditionalActorsData,
        declarationAdditionalActorsData,
        combinePointers(pointerString, AdditionalActors.pointer, sequenceId)
      ) ++
      createDiffOfOptions(original.carrierDetails, carrierDetails, combinePointers(pointerString, CarrierDetails.pointer, sequenceId)) ++
      createDiffOfOptions(
        original.personPresentingGoodsDetails,
        personPresentingGoodsDetails,
        combinePointers(pointerString, PersonPresentingGoodsDetails.pointer, sequenceId)
      )
}

object Parties extends FieldMapping {
  implicit val format: OFormat[Parties] = Json.format[Parties]

  val pointer: ExportsFieldPointer = "parties"
  val isEntryIntoDeclarantsRecordsPointer: ExportsFieldPointer = "personPresentingGoodsDetails.eori"

  val id = "Parties"

  // prefix of the message keys used for the 'Amendment details' page
  val partiesPrefix = "declaration.summary.parties"

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
