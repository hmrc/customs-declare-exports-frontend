/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import controllers.helpers.TransportSectionHelper.clearCacheOnSkippingTransportPages
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Country, YesNoAnswer}
import forms.section1.AdditionalDeclarationType.AdditionalDeclarationType
import forms.section1.{ConsignmentReferences, Ducr, Mucr}
import forms.section2.authorisationHolder.AuthorisationHolder
import forms.section4.{Document, NatureOfTransaction, PreviousDocumentsData}
import forms.section5.AdditionalInformation
import forms.section5.additionaldocuments.AdditionalDocument
import forms.section6._
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration.isCodePrefixedWith
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ProcedureCodesData.lowValueDeclaration
import models.declaration._
import play.api.libs.json._
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, compareIntDifference, ExportsDeclarationDiff}

// scalastyle:off
case class ExportsDeclaration(
  id: String,
  declarationMeta: DeclarationMeta,
  eori: String, // Included for parity with BE model only
  `type`: DeclarationType,
  additionalDeclarationType: Option[AdditionalDeclarationType] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  linkDucrToMucr: Option[YesNoAnswer] = None,
  mucr: Option[Mucr] = None,
  transport: Transport = Transport(),
  parties: Parties = Parties(),
  locations: Locations = Locations(),
  items: Seq[ExportItem] = Seq.empty,
  totalNumberOfItems: Option[InvoiceAndPackageTotals] = None,
  previousDocuments: Option[PreviousDocumentsData] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None,
  statementDescription: Option[String] = None
) extends DiffTools[ExportsDeclaration] {

  /*
   This diff only compares the fields that are used when generating a WCO XML declaration.
   It does not include those fields that are purely for our own UI's operation e.g. linkDucrToMucr & DeclarationMeta
   */
  def createDiff(
    original: ExportsDeclaration,
    pointerString: ExportsFieldPointer = ExportsDeclaration.pointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    Seq(
      compareDifference(original.natureOfTransaction, natureOfTransaction, combinePointers(pointerString, NatureOfTransaction.pointer, sequenceId)),
      createDiff(original.items, items, combinePointers(pointerString, ExportItem.pointer, sequenceId)),
      compareIntDifference(original.items.size, items.size, combinePointers(pointerString, ExportsDeclaration.goodsItemQuantityPointer))
    ).flatten ++
      transport.createDiff(original.transport, combinePointers(pointerString, Transport.pointer, sequenceId)) ++
      parties.createDiff(original.parties, combinePointers(pointerString, Parties.pointer, sequenceId)) ++
      locations.createDiff(original.locations, combinePointers(pointerString, Locations.pointer, sequenceId)) ++
      createDiffOfOptions(
        original.totalNumberOfItems,
        totalNumberOfItems,
        combinePointers(pointerString, InvoiceAndPackageTotals.pointer, sequenceId)
      ) ++
      createDiffOfOptionIsos(original.previousDocuments, previousDocuments, combinePointers(pointerString, PreviousDocumentsData.pointer, sequenceId))

  def lrn: Option[String] = consignmentReferences.flatMap(_.lrn.map(_.lrn))
  def ducr: Option[Ducr] = consignmentReferences.flatMap(_.ducr)

  def inlandModeOfTransportCode: Option[ModeOfTransportCode] = locations.inlandModeOfTransportCode.flatMap(_.inlandModeOfTransportCode)

  def transportLeavingBorderCode: Option[ModeOfTransportCode] = transport.borderModeOfTransportCode.flatMap(_.code)

  def additionalDocumentsInformation(itemId: String): AdditionalDocuments =
    additionalDocumentsInformationIfAny(itemId).getOrElse(AdditionalDocuments(None, Seq.empty))

  def listOfAdditionalDocuments(itemId: String): Seq[AdditionalDocument] =
    additionalDocumentsInformationIfAny(itemId).map(_.documents).getOrElse(Seq.empty)

  def additionalDocumentsRequired(itemId: String): Option[YesNoAnswer] =
    additionalDocumentsInformationIfAny(itemId).flatMap(_.isRequired)

  def addOrUpdateContainer(container: Container): ExportsDeclaration =
    copy(transport = transport.addOrUpdateContainer(container))

  def clearRoutingCountries(): ExportsDeclaration =
    copy(locations = locations.copy(hasRoutingCountries = Some(false), routingCountries = Seq.empty))

  def commodityCodeOfItem(itemId: String): Option[String] =
    itemBy(itemId).flatMap(_.commodityDetails.flatMap(_.combinedNomenclatureCode))

  def commodityMeasure(itemId: String): Option[CommodityMeasure] = itemBy(itemId).flatMap(_.commodityMeasure)

  def containerBy(containerId: String): Option[Container] = containers.find(_.id.equalsIgnoreCase(containerId))

  def containerBySeqId(seqId: String): Option[Container] = containers.find(_.sequenceId == seqId.toInt)

  def containers: Seq[Container] = transport.containers.getOrElse(Seq.empty)

  def seals: Seq[Seal] = containers.flatMap(_.seals)

  def containRoutingCountries: Boolean = locations.routingCountries.nonEmpty

  def authorisationHolders: Seq[AuthorisationHolder] = parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)

  def hasContainers: Boolean = containers.nonEmpty

  def hasInlandModeOfTransportCode(code: ModeOfTransportCode): Boolean =
    locations.inlandModeOfTransportCode.fold(false)(_.inlandModeOfTransportCode.contains(code))

  def hasPreviousDocuments: Boolean = previousDocuments.map(_.documents).exists(_.nonEmpty)

  def isAdditionalDeclarationType(adt: AdditionalDeclarationType): Boolean = additionalDeclarationType.exists(_ == adt)

  // Note that the methods' names (if the value is or not actually >= 100,000) do not reflect their actual behaviours,
  // (defined or not defined) but rather the business rule behind them, which is used to drive the navigation.
  // The business rule requires that when non-empty, the value in totalAmountInvoiced "should" (because potentially a
  // user could still enter an amount < 100,000) always be >= 100,000, whereas if empty, or not defined, it is because
  // the amount was < 100,000.
  def isInvoiceAmountGreaterThan100000: Boolean =
    totalNumberOfItems.exists(_.totalAmountInvoiced.fold(false)(_.trim.nonEmpty))

  def isInvoiceAmountLessThan100000: Boolean = !isInvoiceAmountGreaterThan100000

  def isUsingOwnTransport: Boolean =
    parties.carrierDetails.isEmpty || parties.carrierDetails.exists(carrier => carrier.details.isEmpty)

  def hasOsrProcedureCode(itemId: String): Boolean = itemBy(itemId).exists(_.procedureCodes.exists(_.hasOsrProcedureCode))

  def hasFiscalInformation(itemId: String): Boolean = itemBy(itemId).exists(_.hasFiscalInformation)

  def hasFiscalReferences(itemId: String): Boolean =
    itemBy(itemId).exists(_.additionalFiscalReferencesData.exists(_.references.nonEmpty))

  def isCommodityCodeOfItemPrefixedWith(itemId: String, prefixes: Seq[Int]): Boolean =
    isCodePrefixedWith(commodityCodeOfItem(itemId), prefixes)

  def isComplete: Boolean = declarationMeta.status == DeclarationStatus.COMPLETE

  def isAmendmentDraft: Boolean = declarationMeta.status == DeclarationStatus.AMENDMENT_DRAFT

  def isDeclarantExporter: Boolean = parties.declarantIsExporter.exists(_.isYes)

  def isExs: Boolean = parties.isExs.exists(_.isExs == YesNoAnswers.yes)
  def isNotExs: Boolean = !isExs

  def isEntryIntoDeclarantsRecords: Boolean = parties.isEntryIntoDeclarantsRecords.exists(_.answer == YesNoAnswers.yes)
  def isNotEntryIntoDeclarantsRecords: Boolean = !isEntryIntoDeclarantsRecords

  def isInlandOrBorder(inlandOrBorder: InlandOrBorder): Boolean = locations.inlandOrBorder.contains(inlandOrBorder)

  def isLicenseRequired(itemId: String): Boolean = itemBy(itemId).exists(_.isLicenceRequired.contains(true))

  def isLowValueDeclaration(itemId: String): Boolean =
    procedureCodeOfItem(itemId).exists(_.containsAPC(lowValueDeclaration))

  def isType(declarationType: DeclarationType): Boolean = `type` == declarationType

  lazy val hasItems: Boolean = items.nonEmpty && items.exists(_.isDefined)

  def itemBy(itemId: String): Option[ExportItem] = items.find(_.id.equalsIgnoreCase(itemId))

  def itemWithIndexBy(itemId: String): Option[(ExportItem, Int)] = items.zipWithIndex.find(_._1.id.equalsIgnoreCase(itemId))

  def itemBySequenceNo(seqNo: String): Option[ExportItem] = items.find(_.sequenceId.toString == seqNo)

  def listOfAdditionalInformationOfItem(itemId: String): Seq[AdditionalInformation] =
    itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default).items

  def listOfAdditionalInformationOfItem(item: ExportItem): Seq[AdditionalInformation] =
    item.additionalInformation.getOrElse(AdditionalInformationData.default).items

  def procedureCodeOfItem(itemId: String): Option[ProcedureCodesData] =
    itemBy(itemId).flatMap(_.procedureCodes)

  def requiresWarehouseId: Boolean = items.exists(_.requiresWarehouseId)

  def removeCountryOfRouting(country: Country): ExportsDeclaration =
    copy(locations = locations.copy(routingCountries = locations.routingCountries.filterNot(_.country == country)))

  def removeSupervisingCustomsOffice: ExportsDeclaration =
    copy(locations = locations.copy(supervisingCustomsOffice = None))

  def updateDepartureTransport(departure: DepartureTransport): ExportsDeclaration =
    copy(transport =
      transport.copy(
        meansOfTransportOnDepartureType = departure.meansOfTransportOnDepartureType,
        meansOfTransportOnDepartureIDNumber = departure.meansOfTransportOnDepartureIDNumber,
        meansOfTransportCrossingTheBorderType = None,
        meansOfTransportCrossingTheBorderIDNumber = None
      )
    )

  def updateBorderTransport(borderTransport: BorderTransport): ExportsDeclaration = {
    val transportType = borderTransport.meansOfTransportCrossingTheBorderType.trim
    val transportRef = borderTransport.meansOfTransportCrossingTheBorderIDNumber.trim
    copy(transport =
      transport.copy(
        meansOfTransportCrossingTheBorderType = if (transportType.isEmpty) None else Some(transportType),
        meansOfTransportCrossingTheBorderIDNumber = if (transportRef.isEmpty) None else Some(transportRef)
      )
    )
  }

  def updateTransportCountry(transportCountry: TransportCountry): ExportsDeclaration =
    copy(transport = transport.copy(transportCrossingTheBorderNationality = transportCountry.countryCode.map(_ => transportCountry)))

  def removeAuthorisationProcedureCodeChoice: ExportsDeclaration =
    copy(parties = parties.copy(authorisationProcedureCodeChoice = None))

  def updatedItem(itemId: String, update: ExportItem => ExportItem): ExportsDeclaration =
    copy(items = items.map(item => if (item.id == itemId) update(item) else item))

  def updateType(`type`: DeclarationType): ExportsDeclaration = copy(`type` = `type`)

  def updateCountriesOfRouting(routingCountries: Seq[RoutingCountry]): ExportsDeclaration =
    copy(locations = locations.copy(routingCountries = routingCountries))

  def updateDestinationCountry(destinationCountry: Country): ExportsDeclaration =
    clearCacheOnSkippingTransportPages(copy(locations = locations.copy(destinationCountry = Some(destinationCountry))))

  def updateRoutingQuestion(answer: Boolean): ExportsDeclaration =
    copy(locations = locations.copy(hasRoutingCountries = Some(answer)))

  def updateContainers(containers: Seq[Container]): ExportsDeclaration =
    copy(transport = transport.copy(containers = Some(containers)))

  def updateTransportPayment(payment: TransportPayment): ExportsDeclaration =
    copy(transport = transport.copy(expressConsignment = YesNoAnswer.Yes, transportPayment = Some(payment)))

  def updatePreviousDocuments(previousDocuments: Seq[Document]): ExportsDeclaration =
    copy(previousDocuments = Some(PreviousDocumentsData(previousDocuments)))

  def updateReadyForSubmission(ready: Boolean): ExportsDeclaration =
    copy(declarationMeta = declarationMeta.copy(readyForSubmission = Some(ready)))

  def transform(function: ExportsDeclaration => ExportsDeclaration): ExportsDeclaration = function(this)

  def summaryPageWasVisited: Boolean = declarationMeta.summaryWasVisited.contains(true)

  private def additionalDocumentsInformationIfAny(itemId: String): Option[AdditionalDocuments] =
    itemBy(itemId).flatMap(_.additionalDocuments)
}

object ExportsDeclaration extends FieldMapping {
  implicit val format: OFormat[ExportsDeclaration] = Json.format[ExportsDeclaration]

  val pointer: ExportsFieldPointer = "declaration"
  val goodsItemQuantityPointer: ExportsFieldPointer = "goodsItemQuantity"

  def isCodePrefixedWith(maybeCode: Option[String], prefixes: Seq[Int]): Boolean =
    maybeCode match {
      case Some(code) if code.trim.nonEmpty => prefixes.exists(prefix => code.startsWith(prefix.toString))
      case _                                => false
    }
}
