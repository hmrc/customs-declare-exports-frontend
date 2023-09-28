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

package controllers.navigation

import controllers.declaration.routes
import controllers.helpers.DeclarationHolderHelper.userCanLandOnIsAuthRequiredPage
import controllers.helpers.TransportSectionHelper._
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper, TransportSectionHelper}
import forms.declaration.InlandOrBorder.Border
import forms.declaration.NatureOfTransaction.{BusinessPurchase, Sale}
import forms.declaration._
import models.DeclarationType._
import models.ExportsDeclaration
import play.api.mvc.Call
import services.TaggedAuthCodes

// scalastyle:off
trait CacheDependentNavigators {

  val taggedAuthCodes: TaggedAuthCodes
  val inlandOrBorderHelper: InlandOrBorderHelper
  val supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper

  protected def nactCodeFirstPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    cacheModel.natureOfTransaction match {
      case Some(NatureOfTransaction(`Sale`) | NatureOfTransaction(`BusinessPurchase`)) => routes.ZeroRatedForVatController.displayPage(itemId)
      case _                                                                           => routes.TaricCodeSummaryController.displayPage(itemId)
    }

  protected def nactCodePreviousPageForSimplified(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isLowValueDeclaration(itemId)) routes.ZeroRatedForVatController.displayPage(itemId)
    else routes.TaricCodeSummaryController.displayPage(itemId)

  protected def packageInformationPreviousPageForSimplified(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isLowValueDeclaration(itemId)) routes.StatisticalValueController.displayPage(itemId)
    else routes.NactCodeSummaryController.displayPage(itemId)

  protected def declarantIsExporterPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE =>
        if (cacheModel.isAmendmentDraft) routes.EntryIntoDeclarantsRecordsController.displayPage
        else routes.DeclarantDetailsController.displayPage

      case SUPPLEMENTARY => routes.ConsignmentReferencesController.displayPage

      case _ =>
        if (cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage
        else routes.MucrController.displayPage
    }

  protected def officeOfExitPreviousPage(declaration: ExportsDeclaration): Call =
    if (taggedAuthCodes.skipLocationOfGoods(declaration)) routes.DestinationCountryController.displayPage
    else routes.LocationOfGoodsController.displayPage

  protected def entryIntoDeclarantsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage
    else routes.MucrController.displayPage

  protected def previousDocumentsPreviousPageDefault(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.hasPreviousDocuments) routes.PreviousDocumentsSummaryController.displayPage
    else routes.NatureOfTransactionController.displayPage

  protected def consigneeDetailsSupplementaryPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isDeclarantExporter)
      routes.DeclarantExporterController.displayPage
    else
      routes.RepresentativeStatusController.displayPage

  protected def commodityMeasureClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
      if (cacheModel.isExs)
        routes.UNDangerousGoodsCodeController.displayPage(itemId)
      else
        routes.CommodityDetailsController.displayPage(itemId)
    else
      routes.PackageInformationSummaryController.displayPage(itemId)

  protected def packageInformationClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isExs)
      routes.UNDangerousGoodsCodeController.displayPage(itemId)
    else
      routes.CommodityDetailsController.displayPage(itemId)

  protected def additionalDocumentsPreviousPage(declaration: ExportsDeclaration, itemId: String): Call = {
    val isLicenseRequired = taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration) || declaration.isLicenseRequired(itemId)
    if (isLicenseRequired) routes.IsLicenceRequiredController.displayPage(itemId)
    else routes.AdditionalDocumentsRequiredController.displayPage(itemId)
  }

  protected def additionalDocumentsSummaryClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalInformationOfItem(itemId).nonEmpty)
      routes.AdditionalInformationController.displayPage(itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(itemId)

  protected def isLicenseRequiredPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalInformationOfItem(itemId).nonEmpty)
      routes.AdditionalInformationController.displayPage(itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(itemId)

  protected def additionalDocumentsClearancePreviousPage(declaration: ExportsDeclaration, itemId: String): Call =
    if (declaration.listOfAdditionalDocuments(itemId).nonEmpty)
      routes.AdditionalDocumentsController.displayPage(itemId)
    else if (taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration)) additionalDocumentsSummaryClearancePreviousPage(declaration, itemId)
    else routes.AdditionalDocumentsRequiredController.displayPage(itemId)

  protected def additionalInformationAddPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      routes.AdditionalInformationController.displayPage(itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(itemId)

  protected def additionalFiscalReferencesPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).exists(_.references.nonEmpty))
      routes.AdditionalFiscalReferencesController.displayPage(itemId)
    else
      routes.FiscalInformationController.displayPage(itemId)

  protected def previousDocumentsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.hasPreviousDocuments) routes.PreviousDocumentsSummaryController.displayPage
    else routes.OfficeOfExitController.displayPage

  protected def exporterEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords)
      routes.PersonPresentingGoodsDetailsController.displayPage
    else
      routes.DeclarantExporterController.displayPage

  protected def carrierEoriNumberPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      routes.DeclarantExporterController.displayPage
    else
      routes.RepresentativeStatusController.displayPage

  protected def carrierEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (!cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      routes.RepresentativeStatusController.displayPage
    else {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        routes.ConsignorDetailsController.displayPage
      else
        routes.ConsignorEoriNumberController.displayPage
    }

  protected def consigneeDetailsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.carrierDetails.flatMap(_.details.eori).isEmpty)
      routes.CarrierDetailsController.displayPage
    else
      routes.CarrierEoriNumberController.displayPage

  protected def consigneeDetailsClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isExs)
      consigneeDetailsPreviousPage(cacheModel)
    else {
      if (cacheModel.isDeclarantExporter)
        routes.IsExsController.displayPage
      else
        routes.RepresentativeStatusController.displayPage
    }

  protected def representativeAgentClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isExs) {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        routes.ConsignorDetailsController.displayPage
      else
        routes.ConsignorEoriNumberController.displayPage
    } else {
      routes.IsExsController.displayPage
    }

  protected def isExsClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isDeclarantExporter)
      exporterEoriNumberClearancePreviousPage(cacheModel)
    else if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage
    else routes.ExporterDetailsController.displayPage

  protected def declarationHolderRequiredPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE if !cacheModel.isEntryIntoDeclarantsRecords => routes.ConsigneeDetailsController.displayPage
      case OCCASIONAL                                            => routes.AdditionalActorsSummaryController.displayPage
      case _                                                     => routes.AuthorisationProcedureCodeChoiceController.displayPage
    }

  protected def declarationHolderAddPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.declarationHolders.nonEmpty) routes.DeclarationHolderSummaryController.displayPage
    else if (userCanLandOnIsAuthRequiredPage(cacheModel)) routes.DeclarationHolderRequiredController.displayPage
    else declarationHolderRequiredPreviousPage(cacheModel)

  protected def declarationHolderSummaryPreviousPage(cacheModel: ExportsDeclaration): Call =
    declarationHolderRequiredPreviousPage(cacheModel)

  protected def destinationCountryPreviousPage(cacheModel: ExportsDeclaration): Call =
    declarationHolderAddPreviousPage(cacheModel)

  protected def representativeAgentPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage
    else routes.ExporterDetailsController.displayPage

  protected def supervisingCustomsOfficePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.requiresWarehouseId || cacheModel.isType(CLEARANCE)) routes.WarehouseIdentificationController.displayPage
    else routes.TransportLeavingTheBorderController.displayPage

  protected def inlandOrBorderPreviousPage(cacheModel: ExportsDeclaration): Call = {
    val gotoTransportLeavingTheBorder = supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(cacheModel)
    if (gotoTransportLeavingTheBorder) routes.TransportLeavingTheBorderController.displayPage
    else routes.SupervisingCustomsOfficeController.displayPage
  }

  protected def inlandTransportDetailsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (inlandOrBorderHelper.skipInlandOrBorder(cacheModel)) inlandOrBorderPreviousPage(cacheModel)
    else routes.InlandOrBorderController.displayPage

  protected def departureTransportPreviousPage(cacheModel: ExportsDeclaration): Call = {
    val gotoInlandTransport = inlandOrBorderHelper.skipInlandOrBorder(cacheModel) || !cacheModel.isInlandOrBorder(Border)
    if (gotoInlandTransport) routes.InlandTransportDetailsController.displayPage else routes.InlandOrBorderController.displayPage
  }

  protected def departureTransportPreviousPageOnClearance(cacheModel: ExportsDeclaration): Call =
    // For Clearance the previous page should always be SupervisingCustomsOffice by any means, as
    // "1040" as PC is not applicable to this journey type.
    routes.SupervisingCustomsOfficeController.displayPage

  protected def transportCountryPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isInlandOrBorder(InlandOrBorder.Border)) routes.DepartureTransportController.displayPage
    else routes.BorderTransportController.displayPage

  protected def expressConsignmentPreviousPage(cacheModel: ExportsDeclaration): Call = {
    val guernseyOrJersey = isGuernseyOrJerseyDestination(cacheModel)
    if (cacheModel.isInlandOrBorder(Border)) {
      val postalOrFTI = isPostalOrFTIModeOfTransport(cacheModel.transportLeavingBorderCode)
      if (postalOrFTI || guernseyOrJersey) routes.InlandOrBorderController.displayPage
      else routes.TransportCountryController.displayPage
    } else {
      val postalOrFTI = isPostalOrFTIModeOfTransport(cacheModel.inlandModeOfTransportCode)
      if (postalOrFTI || guernseyOrJersey) routes.InlandTransportDetailsController.displayPage
      else routes.TransportCountryController.displayPage
    }
  }

  protected def expressConsignmentPreviousPageOnClearance(cacheModel: ExportsDeclaration): Call =
    if (!isPostalOrFTIModeOfTransport(cacheModel.transportLeavingBorderCode)) routes.DepartureTransportController.displayPage
    // For Clearance the previous page should always be SupervisingCustomsOffice by any means, as
    // "1040" as PC is not applicable to this journey type. This test is sort of superfluous then.
    else if (supervisingCustomsOfficeHelper.checkProcedureCodes(cacheModel)) routes.WarehouseIdentificationController.displayPage
    else routes.SupervisingCustomsOfficeController.displayPage

  protected def containerFirstPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.transport.transportPayment.nonEmpty) routes.TransportPaymentController.displayPage
    else routes.ExpressConsignmentController.displayPage

  protected def containerFirstPreviousPageOnSupplementary(cacheModel: ExportsDeclaration): Call =
    expressConsignmentPreviousPage(cacheModel)

  protected def additionalTaricCodesPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
      routes.CusCodeController.displayPage(itemId)
    else
      routes.UNDangerousGoodsCodeController.displayPage(itemId)

  protected def authorisationProcedureCodeChoicePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isType(CLEARANCE) && cacheModel.isEntryIntoDeclarantsRecords)
      routes.ConsigneeDetailsController.displayPage
    else
      routes.AdditionalActorsSummaryController.displayPage

  protected def totalPackageQuantityPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isInvoiceAmountGreaterThan100000) routes.InvoiceAndExchangeRateController.displayPage
    else routes.InvoiceAndExchangeRateChoiceController.displayPage

  protected def natureOfTransactionPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (TransportSectionHelper.isGuernseyOrJerseyDestination(cacheModel))
      totalPackageQuantityPreviousPage(cacheModel)
    else
      routes.TotalPackageQuantityController.displayPage

}
