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

package views.helpers

import controllers.declaration.routes._
import models.DeclarationType.CLEARANCE
import models.{ExportsDeclaration, Pointer}
import play.api.Logging
import play.api.mvc.Call

object PointerHelper extends Logging {

  val defaultItemsCall = ItemsSummaryController.displayItemsSummaryPage
  val clearanceDecDetailsCall = PersonPresentingGoodsDetailsController.displayPage

  private val containsItemsSeqRegEx = """.*items\.\$.*""".r
  private val containsDecDetailsEoriRegEx = """^declaration.declarantDetails.details.eori$""".r

  def getChangeLinkCall(maybePointer: Option[Pointer], declaration: ExportsDeclaration): Option[Call] =
    maybePointer.flatMap { pointer =>
      pointer.pattern match {
        case containsItemsSeqRegEx(_*)       => getItemId(pointer, declaration)
        case containsDecDetailsEoriRegEx(_*) => getDecDetailsEoriChangeLinkUrl(pointer, declaration)
        case _                               => getCallForPointer(pointer)
      }
    }

  private def getItemId(pointer: Pointer, declaration: ExportsDeclaration): Option[Call] = {
    val maybeItemId = for {
      seqNo <- pointer.sequenceArgs.headOption
      item <- declaration.itemBySequenceNo(seqNo)
    } yield item.id

    val call = maybeItemId
      .flatMap(itemId => urlMapping2Params.get(pointer.pattern).map(_(itemId)))
      .getOrElse(logResortingToDefault(defaultItemsCall, declaration))

    Some(call)
  }

  private def getDecDetailsEoriChangeLinkUrl(pointer: Pointer, declaration: ExportsDeclaration): Option[Call] =
    if (declaration.isType(CLEARANCE) && declaration.isExs && declaration.parties.personPresentingGoodsDetails.nonEmpty)
      Some(clearanceDecDetailsCall)
    else
      getCallForPointer(pointer)

  private def getCallForPointer(pointer: Pointer): Option[Call] = urlMapping1Param.get(pointer.pattern)

  private def logResortingToDefault(defaultCall: Call, declaration: ExportsDeclaration): Call = {
    logger.warn(s"Was not able to specialise the provided error change link url for declaration ${declaration.id}")
    defaultCall
  }

  /**
   * Export Error Pointers to their corresponding UI edit/summary page
   */
  // mappings to pages that require two parameters
  private val urlMapping2Params: Map[String, String => Call] = Map(
    "declaration.items.$.statisticalValue.statisticalValue" -> StatisticalValueController.displayPage,
    "declaration.items.$.additionalDocument" -> AdditionalDocumentsController.displayPage,
    "declaration.items.$.additionalDocument.$.documentIdentifier" -> AdditionalDocumentsController.displayPage,
    "declaration.items.$.additionalDocument.$.documentTypeCode" -> AdditionalDocumentsController.displayPage, // ?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.dateOfValidity" -> AdditionalDocumentsController.displayPage, // ?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.documentStatus" -> AdditionalDocumentsController.displayPage, // ?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.documentStatusReason" -> AdditionalDocumentsController.displayPage, // ?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.issuingAuthorityName" -> AdditionalDocumentsController.displayPage, // ?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity" -> AdditionalDocumentsController.displayPage, // ?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalInformation.$.code" -> AdditionalInformationController.displayPage, // ?? AdditionalInformationChangeController.displayPage
    "declaration.items.$.additionalInformation.$.description" -> AdditionalInformationController.displayPage, // ?? AdditionalInformationChangeController.displayPage
    "declaration.items.$.commodityDetails.descriptionOfGoods" -> CommodityDetailsController.displayPage,
    "declaration.items.$.cusCode.id" -> CusCodeController.displayPage,
    "declaration.items.$.cusCode.cusCode" -> CusCodeController.displayPage,
    "declaration.items.$.dangerousGoodsCode.dangerousGoodsCode" -> UNDangerousGoodsCodeController.displayPage,
    "declaration.items.$.commodityMeasure.grossMass" -> CommodityMeasureController.displayPage,
    "declaration.items.$.commodityMeasure.netMass" -> CommodityMeasureController.displayPage,
    "declaration.items.$.commodityMeasure.supplementaryUnits" -> SupplementaryUnitsController.displayPage,
    "declaration.items.$.additionalFiscalReferences.$.id" -> AdditionalFiscalReferencesController.displayPage, // ?? AdditionalFiscalReferencesRemoveController.displayPage
    "declaration.items.$.additionalFiscalReferences.$.roleCode" -> AdditionalFiscalReferencesController.displayPage, // ?? AdditionalFiscalReferencesRemoveController.displayPage
    "declaration.items.$.procedureCodes.procedureCode.current" -> ProcedureCodesController.displayPage,
    "declaration.items.$.procedureCodes.procedureCode.previous" -> ProcedureCodesController.displayPage,
    "declaration.items.$.packageInformation.$.shippingMarks" -> PackageInformationSummaryController.displayPage,
    "declaration.items.$.packageInformation.$.numberOfPackages" -> PackageInformationSummaryController.displayPage,
    "declaration.items.$.packageInformation.$.typesOfPackages" -> PackageInformationSummaryController.displayPage
  )

  // mappings to pages that require only one parameter
  private val urlMapping1Param: Map[String, Call] = Map(
    "declaration.consignmentReferences.lrn" -> LocalReferenceNumberController.displayPage,
    "declaration.consignmentReferences.ucr" -> DucrEntryController.displayPage,
    "declaration.totalNumberOfItems.totalAmountInvoiced" -> InvoiceAndExchangeRateController.displayPage,
    "declaration.totalPackageQuantity" -> TotalPackageQuantityController.displayPage,
    "declaration.parties.representativeDetails.details.eori" -> RepresentativeEntityController.displayPage,
    "declaration.parties.representativeDetails.statusCode" -> RepresentativeStatusController.displayPage,
    "declaration.parties.declarationHolders.$.eori" -> DeclarationHolderSummaryController.displayPage, // ?? DeclarationHolderChangeController with seq No
    "declaration.parties.declarationHolders.$.authorisationTypeCode" -> DeclarationHolderSummaryController.displayPage, // ?? DeclarationHolderChangeController with seq No
    "declaration.transport.meansOfTransportCrossingTheBorderIDNumber" -> BorderTransportController.displayPage,
    "declaration.transport.meansOfTransportCrossingTheBorderType" -> BorderTransportController.displayPage,
    "declaration.transport.transportCrossingTheBorderNationality.countryName" -> TransportCountryController.displayPage,
    "declaration.borderTransport.modeCode" -> TransportLeavingTheBorderController.displayPage,
    "declaration.parties.carrierDetails.details.eori" -> CarrierEoriNumberController.displayPage,
    "declaration.parties.carrierDetails.details.address.fullName" -> CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.townOrCity" -> CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.country" -> CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.addressLine" -> CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.postCode" -> CarrierDetailsController.displayPage,
    "declaration.transport.transportPayment.paymentMethod" -> TransportPaymentController.displayPage,
    "declaration.locations.destinationCountries.countryOfRouting" -> RoutingCountriesController.displayRoutingQuestion,
    "declaration.locations.destinationCountries.countriesOfRouting.$" -> RoutingCountriesController.displayRoutingCountry, // ?? RoutingCountriesSummaryController.displayChangeCountryPage
    "declaration.locations.destinationCountries.countryOfDestination" -> DestinationCountryController.displayPage,
    "declaration.totalNumberOfItems.exchangeRate" -> InvoiceAndExchangeRateController.displayPage,
    "declaration.declarantDetails.details.eori" -> DeclarantDetailsController.displayPage, // Alters if dec is CLEARANCE and isEXS and personPresentingGoodsDetails is nonEmpty
    "declaration.locations.officeOfExit.circumstancesCode" -> OfficeOfExitController.displayPage,
    "declaration.locations.officeOfExit.officeId" -> OfficeOfExitController.displayPage,
    "declaration.parties.exporterDetails.details.eori" -> ExporterEoriNumberController.displayPage,
    "declaration.parties.exporterDetails.details.address.fullName" -> ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.townOrCity" -> ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.country" -> ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.addressLine" -> ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.postCode" -> ExporterDetailsController.displayPage,
    "declaration.natureOfTransaction.natureType" -> NatureOfTransactionController.displayPage,
    "declaration.parties.additionalActors.actor.eori" -> AdditionalActorsSummaryController.displayPage,
    "declaration.parties.additionalActors.actor" -> AdditionalActorsSummaryController.displayPage,
    "declaration.parties.consigneeDetails.details.address.fullName" -> ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.townOrCity" -> ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.country" -> ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.addressLine" -> ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.postCode" -> ConsigneeDetailsController.displayPage,
    "declaration.departureTransport.meansOfTransportOnDepartureIDNumber" -> DepartureTransportController.displayPage,
    "declaration.departureTransport.borderModeOfTransportCode" -> DepartureTransportController.displayPage,
    "declaration.departureTransport.meansOfTransportOnDepartureType" -> InlandTransportDetailsController.displayPage,
    "declaration.locations.goodsLocation.addressLine" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.city" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.country" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.identificationOfLocation" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.nameOfLocation" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.postCode" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.qualifierOfIdentification" -> LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.typeOfLocation" -> LocationOfGoodsController.displayPage,
    "declaration.containers.container.$.id" -> TransportContainerController.displayContainerSummary, // ?? SealController.displaySealSummary
    "declaration.containers.container.$.seals.seal.$.id" -> TransportContainerController.displayContainerSummary, // ?? SealController.displaySealRemove
    "declaration.previousDocuments.$.documentCategory" -> PreviousDocumentsSummaryController.displayPage, // ?? PreviousDocumentsChangeController.displayPage
    "declaration.previousDocuments.$.documentReference" -> PreviousDocumentsSummaryController.displayPage, // ?? PreviousDocumentsChangeController.displayPage
    "declaration.previousDocuments.$.documentType" -> PreviousDocumentsSummaryController.displayPage, // ?? PreviousDocumentsChangeController.displayPage
    "declaration.previousDocuments.$.goodsItemIdentifier" -> PreviousDocumentsSummaryController.displayPage, // ?? PreviousDocumentsChangeController.displayPage
    "declaration.locations.warehouseIdentification.identificationNumber" -> WarehouseIdentificationController.displayPage,
    "declaration.locations.warehouseIdentification.identificationType" -> WarehouseIdentificationController.displayPage,
    "declaration.locations.warehouseIdentification.supervisingCustomsOffice" -> SupervisingCustomsOfficeController.displayPage
  )
}
