/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.declaration.routes
import models.DeclarationType.CLEARANCE
import models.{ExportsDeclaration, Mode, Pointer}
import models.Mode.Normal
import play.api.mvc.Call
import play.api.Logging

object PointerHelper extends Logging {

  val defaultItemsCall = routes.ItemsSummaryController.displayItemsSummaryPage(Normal)
  val clearanceDecDetailsCall = routes.PersonPresentingGoodsDetailsController.displayPage(Normal)

  private val containsItemsSeqRegEx = """.*items\.\$.*""".r
  private val containsDecDetailsEoriRegEx = """^declaration.declarantDetails.details.eori$""".r

  def getChangeLinkCall(maybePointer: Option[Pointer], declaration: ExportsDeclaration): Option[Call] =
    maybePointer.flatMap { pointer =>
      pointer.pattern match {
        case containsItemsSeqRegEx(_*) =>
          getItemId(pointer, declaration)

        case containsDecDetailsEoriRegEx(_*) =>
          getDecDetailsEoriChangeLinkUrl(pointer, declaration)

        case _ =>
          getCallForPointer(pointer)
      }
    }

  private def getItemId(pointer: Pointer, declaration: ExportsDeclaration): Option[Call] = {
    val maybeItemId = for {
      seqNo <- pointer.sequenceArgs.headOption
      item <- declaration.itemBySequenceNo(seqNo)
    } yield item.id

    val call = maybeItemId
      .flatMap(itemId => urlMapping2Params.get(pointer.pattern).map(_(Normal, itemId)))
      .getOrElse(logResortingToDefault(defaultItemsCall, declaration))

    Some(call)
  }

  private def getDecDetailsEoriChangeLinkUrl(pointer: Pointer, declaration: ExportsDeclaration): Option[Call] =
    if (declaration.isType(CLEARANCE) && declaration.isExs && declaration.parties.personPresentingGoodsDetails.nonEmpty)
      Some(clearanceDecDetailsCall)
    else
      getCallForPointer(pointer)

  private def getCallForPointer(pointer: Pointer): Option[Call] =
    urlMapping1Param.get(pointer.pattern).map(_(Normal))

  private def logResortingToDefault(defaultCall: Call, declaration: ExportsDeclaration): Call = {
    logger.warn(s"Was not able to specialise the provided error change link url for declaration ${declaration.id}")
    defaultCall
  }

  /**
    * Export Error Pointers to their corresponding UI edit/summary page
    */
  //mappings to pages that require two parameters
  private val urlMapping2Params: Map[String, (Mode, String) => Call] = Map(
    "declaration.items.$.statisticalValue.statisticalValue" -> routes.StatisticalValueController.displayPage,
    "declaration.items.$.additionalDocument" -> routes.AdditionalDocumentsController.displayPage,
    "declaration.items.$.additionalDocument.$.documentIdentifier" -> routes.AdditionalDocumentsController.displayPage,
    "declaration.items.$.additionalDocument.$.documentTypeCode" -> routes.AdditionalDocumentsController.displayPage, //?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.dateOfValidity" -> routes.AdditionalDocumentsController.displayPage, //?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.documentStatus" -> routes.AdditionalDocumentsController.displayPage, //?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.documentStatusReason" -> routes.AdditionalDocumentsController.displayPage, //?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.issuingAuthorityName" -> routes.AdditionalDocumentsController.displayPage, //?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity" -> routes.AdditionalDocumentsController.displayPage, //?? AdditionalDocumentChangeController.displayPage
    "declaration.items.$.additionalInformation.$.code" -> routes.AdditionalInformationController.displayPage, //?? AdditionalInformationChangeController.displayPage
    "declaration.items.$.additionalInformation.$.description" -> routes.AdditionalInformationController.displayPage, //?? AdditionalInformationChangeController.displayPage
    "declaration.items.$.commodityDetails.descriptionOfGoods" -> routes.CommodityDetailsController.displayPage,
    "declaration.items.$.cusCode.id" -> routes.CusCodeController.displayPage,
    "declaration.items.$.cusCode.cusCode" -> routes.CusCodeController.displayPage,
    "declaration.items.$.dangerousGoodsCode.dangerousGoodsCode" -> routes.UNDangerousGoodsCodeController.displayPage,
    "declaration.items.$.commodityMeasure.grossMass" -> routes.CommodityMeasureController.displayPage,
    "declaration.items.$.commodityMeasure.netMass" -> routes.CommodityMeasureController.displayPage,
    "declaration.items.$.commodityMeasure.supplementaryUnits" -> routes.SupplementaryUnitsController.displayPage,
    "declaration.items.$.additionalFiscalReferences.$.id" -> routes.AdditionalFiscalReferencesController.displayPage, //?? AdditionalFiscalReferencesRemoveController.displayPage
    "declaration.items.$.additionalFiscalReferences.$.roleCode" -> routes.AdditionalFiscalReferencesController.displayPage, //?? AdditionalFiscalReferencesRemoveController.displayPage
    "declaration.items.$.procedureCodes.procedureCode.current" -> routes.ProcedureCodesController.displayPage,
    "declaration.items.$.procedureCodes.procedureCode.previous" -> routes.ProcedureCodesController.displayPage,
    "declaration.items.$.packageInformation.$.shippingMarks" -> routes.PackageInformationSummaryController.displayPage,
    "declaration.items.$.packageInformation.$.numberOfPackages" -> routes.PackageInformationSummaryController.displayPage,
    "declaration.items.$.packageInformation.$.typesOfPackages" -> routes.PackageInformationSummaryController.displayPage
  )

  //mappings to pages that require only one parameter
  private val urlMapping1Param: Map[String, Mode => Call] = Map(
    "declaration.consignmentReferences.lrn" -> routes.ConsignmentReferencesController.displayPage,
    "declaration.totalNumberOfItems.totalAmountInvoiced" -> routes.InvoiceAndExchangeRateController.displayPage,
    "declaration.totalPackageQuantity" -> routes.TotalPackageQuantityController.displayPage,
    "declaration.parties.representativeDetails.details.eori" -> routes.RepresentativeEntityController.displayPage,
    "declaration.parties.representativeDetails.statusCode" -> routes.RepresentativeStatusController.displayPage,
    "declaration.parties.declarationHolders.$.eori" -> routes.DeclarationHolderSummaryController.displayPage, //?? DeclarationHolderChangeController with seq No
    "declaration.parties.declarationHolders.$.authorisationTypeCode" -> routes.DeclarationHolderSummaryController.displayPage, //?? DeclarationHolderChangeController with seq No
    "declaration.borderTransport.meansOfTransportCrossingTheBorderIDNumber" -> routes.BorderTransportController.displayPage,
    "declaration.borderTransport.meansOfTransportCrossingTheBorderType" -> routes.BorderTransportController.displayPage,
    "declaration.borderTransport.meansOfTransportCrossingTheBorderNationality" -> routes.BorderTransportController.displayPage,
    "declaration.borderTransport.modeCode" -> routes.TransportLeavingTheBorderController.displayPage,
    "declaration.parties.carrierDetails.details.eori" -> routes.CarrierEoriNumberController.displayPage,
    "declaration.parties.carrierDetails.details.address.fullName" -> routes.CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.townOrCity" -> routes.CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.country" -> routes.CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.addressLine" -> routes.CarrierDetailsController.displayPage,
    "declaration.parties.carrierDetails.details.address.postCode" -> routes.CarrierDetailsController.displayPage,
    "declaration.borderTransport.paymentMethod" -> routes.TransportPaymentController.displayPage,
    "declaration.totalNumberOfItems.exchangeRate" -> routes.InvoiceAndExchangeRateController.displayPage,
    "declaration.declarantDetails.details.eori" -> routes.DeclarantDetailsController.displayPage, //Alters if dec is CLEARANCE and isEXS and personPresentingGoodsDetails is nonEmpty
    "declaration.locations.officeOfExit.officeId" -> routes.OfficeOfExitController.displayPage,
    "declaration.parties.exporterDetails.details.eori" -> routes.ExporterEoriNumberController.displayPage,
    "declaration.parties.exporterDetails.details.address.fullName" -> routes.ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.townOrCity" -> routes.ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.country" -> routes.ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.addressLine" -> routes.ExporterDetailsController.displayPage,
    "declaration.parties.exporterDetails.details.address.postCode" -> routes.ExporterDetailsController.displayPage,
    "declaration.natureOfTransaction.natureType" -> routes.NatureOfTransactionController.displayPage,
    "declaration.parties.additionalActors.actor.eori" -> routes.AdditionalActorsSummaryController.displayPage,
    "declaration.parties.additionalActors.actor" -> routes.AdditionalActorsSummaryController.displayPage,
    "declaration.parties.consigneeDetails.details.address.fullName" -> routes.ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.townOrCity" -> routes.ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.country" -> routes.ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.addressLine" -> routes.ConsigneeDetailsController.displayPage,
    "declaration.parties.consigneeDetails.details.address.postCode" -> routes.ConsigneeDetailsController.displayPage,
    "declaration.departureTransport.meansOfTransportOnDepartureIDNumber" -> routes.DepartureTransportController.displayPage,
    "declaration.departureTransport.borderModeOfTransportCode" -> routes.DepartureTransportController.displayPage,
    "declaration.departureTransport.meansOfTransportOnDepartureType" -> routes.InlandTransportDetailsController.displayPage,
    "declaration.locations.goodsLocation.nameOfLocation" -> routes.LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.identificationOfLocation" -> routes.LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.qualifierOfIdentification" -> routes.LocationOfGoodsController.displayPage,
    "declaration.locations.goodsLocation.typeOfLocation" -> routes.LocationOfGoodsController.displayPage,
    "declaration.containers.container.$.id" -> routes.TransportContainerController.displayContainerSummary, //?? SealController.displaySealSummary
    "declaration.containers.container.$.seals.seal.$.id" -> routes.TransportContainerController.displayContainerSummary, //?? SealController.displaySealRemove
    "declaration.locations.destinationCountries.countryOfDestination" -> routes.DestinationCountryController.displayPage,
    "declaration.previousDocuments.$.documentCategory" -> routes.PreviousDocumentsSummaryController.displayPage, //?? PreviousDocumentsChangeController.displayPage
    "declaration.previousDocuments.$.documentReference" -> routes.PreviousDocumentsSummaryController.displayPage, //?? PreviousDocumentsChangeController.displayPage
    "declaration.previousDocuments.$.documentType" -> routes.PreviousDocumentsSummaryController.displayPage, //?? PreviousDocumentsChangeController.displayPage
    "declaration.previousDocuments.$.goodsItemIdentifier" -> routes.PreviousDocumentsSummaryController.displayPage, //?? PreviousDocumentsChangeController.displayPage
    "declaration.locations.warehouseIdentification.identificationNumber" -> routes.WarehouseIdentificationController.displayPage,
    "declaration.locations.warehouseIdentification.identificationType" -> routes.WarehouseIdentificationController.displayPage,
    "declaration.locations.warehouseIdentification.supervisingCustomsOffice" -> routes.SupervisingCustomsOfficeController.displayPage,
    "declaration.consignmentReferences.ucr" -> routes.ConsignmentReferencesController.displayPage
  )
}
