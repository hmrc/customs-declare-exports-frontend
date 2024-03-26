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
import controllers.helpers.AuthorisationHolderHelper.userCanLandOnIsAuthRequiredPage
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
      case _                                                                           => cusCodeOrDangerousGoodsPage(cacheModel, itemId)
    }

  protected def nactCodePreviousPageForSimplified(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isLowValueDeclaration(itemId)) routes.ZeroRatedForVatController.displayPage(itemId)
    else cusCodeOrDangerousGoodsPage(cacheModel, itemId)

  protected def packageInformationPreviousPageForSimplified(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isLowValueDeclaration(itemId)) routes.StatisticalValueController.displayPage(itemId)
    else routes.NactCodeSummaryController.displayPage(itemId)

  protected def declarantIsExporterPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE =>
        if (cacheModel.isAmendmentDraft) routes.EntryIntoDeclarantsRecordsController.displayPage
        else routes.DeclarantDetailsController.displayPage

      case _ =>
        routes.SectionSummaryController.displayPage(1)
    }

  protected def officeOfExitPreviousPage(cacheModel: ExportsDeclaration): Call = {
    val skipLocationOfGoods = cacheModel.isAmendmentDraft || taggedAuthCodes.skipLocationOfGoods(cacheModel)
    if (!skipLocationOfGoods) routes.LocationOfGoodsController.displayPage
    else
      cacheModel.`type` match {
        case SUPPLEMENTARY | CLEARANCE => routes.DestinationCountryController.displayPage
        case _                         => routes.RoutingCountriesController.displayRoutingCountry
      }
  }

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

  protected def additionalDocumentsPreviousPage(declaration: ExportsDeclaration, itemId: String): Call =
    if (declaration.listOfAdditionalDocuments(itemId).nonEmpty) routes.AdditionalDocumentsController.displayPage(itemId)
    else {
      val isLicenseRequired = taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration) || declaration.isLicenseRequired(itemId)
      if (isLicenseRequired) routes.IsLicenceRequiredController.displayPage(itemId)
      else routes.AdditionalDocumentsRequiredController.displayPage(itemId)
    }

  protected def additionalDocumentsClearancePreviousPage(declaration: ExportsDeclaration, itemId: String): Call =
    if (declaration.listOfAdditionalDocuments(itemId).nonEmpty) routes.AdditionalDocumentsController.displayPage(itemId)
    else if (taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration)) additionalDocumentsSummaryClearancePreviousPage(declaration, itemId)
    else routes.AdditionalDocumentsRequiredController.displayPage(itemId)

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
    else routes.SectionSummaryController.displayPage(3)

  protected def exporterEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords)
      routes.PersonPresentingGoodsDetailsController.displayPage
    else
      routes.DeclarantExporterController.displayPage

  protected def carrierEoriNumberPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.declarantIsExporter.exists(_.isYes))
      routes.DeclarantExporterController.displayPage
    else
      routes.RepresentativeStatusController.displayPage

  protected def carrierEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (!cacheModel.parties.declarantIsExporter.exists(_.isYes))
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

  protected def authorisationHolderRequiredPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE if !cacheModel.isEntryIntoDeclarantsRecords => routes.ConsigneeDetailsController.displayPage
      case OCCASIONAL                                            => routes.AdditionalActorsSummaryController.displayPage
      case _                                                     => routes.AuthorisationProcedureCodeChoiceController.displayPage
    }

  protected def authorisationHolderAddPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.authorisationHolders.nonEmpty) routes.AuthorisationHolderSummaryController.displayPage
    else if (userCanLandOnIsAuthRequiredPage(cacheModel)) routes.AuthorisationHolderRequiredController.displayPage
    else authorisationHolderRequiredPreviousPage(cacheModel)

  protected def card2ForPartiesPreviousPage(cacheModel: ExportsDeclaration): Call =
    authorisationHolderAddPreviousPage(cacheModel)

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

  protected def departureTransportPreviousPageOnClearance(cacheModel: ExportsDeclaration): Call =
    routes.SupervisingCustomsOfficeController.displayPage

  // Must be a lazy val, as full-path Calls are generated at runtime from reverse routing.
  private lazy val condForTransportPages = List[(ExportsDeclaration => Boolean, Call)](
    (!isRailModeOfTransport(_), routes.TransportCountryController.displayPage),
    (!_.isInlandOrBorder(Border), routes.BorderTransportController.displayPage),
    (!isOccasionalOrSimplified(_), routes.DepartureTransportController.displayPage)
  )

  private val fromBeforeTransportPages = 0
  private val fromTransportCountry = 1
  private val fromBorderTransport = 2
  private val fromDepartureTransport = 3

  private def previousTransportPage(declaration: ExportsDeclaration, fromPage: Int): Call = {
    lazy val callWhenSkippingTransportPages = {
      val skipInlandOrBorder = inlandOrBorderHelper.skipInlandOrBorder(declaration)
      if (!skipInlandOrBorder && declaration.isInlandOrBorder(Border)) routes.InlandOrBorderController.displayPage
      else routes.InlandTransportDetailsController.displayPage
    }

    if (skipTransportPages(declaration)) callWhenSkippingTransportPages
    else condForTransportPages.drop(fromPage).find(t => t._1(declaration)).fold(callWhenSkippingTransportPages)(t => t._2)
  }

  protected def departureTransportPreviousPage(cacheModel: ExportsDeclaration): Call =
    previousTransportPage(cacheModel, fromDepartureTransport)

  protected def borderTransportPreviousPage(cacheModel: ExportsDeclaration): Call =
    previousTransportPage(cacheModel, fromBorderTransport)

  protected def transportCountryPreviousPage(cacheModel: ExportsDeclaration): Call =
    previousTransportPage(cacheModel, fromTransportCountry)

  protected def expressConsignmentPreviousPage(cacheModel: ExportsDeclaration): Call =
    previousTransportPage(cacheModel, fromBeforeTransportPages)

  protected def containerFirstPreviousPageOnSupplementary(cacheModel: ExportsDeclaration): Call =
    previousTransportPage(cacheModel, fromBeforeTransportPages)

  protected def expressConsignmentPreviousPageOnClearance(cacheModel: ExportsDeclaration): Call =
    if (!isPostalOrFTIModeOfTransport(cacheModel.transportLeavingBorderCode)) routes.DepartureTransportController.displayPage
    else routes.SupervisingCustomsOfficeController.displayPage

  protected def containerFirstPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.transport.transportPayment.nonEmpty) routes.TransportPaymentController.displayPage
    else routes.ExpressConsignmentController.displayPage

  protected def cusCodeOrDangerousGoodsPage(cacheModel: ExportsDeclaration, itemId: String): Call =
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
