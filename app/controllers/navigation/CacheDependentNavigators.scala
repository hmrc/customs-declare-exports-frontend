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

import controllers.helpers.AuthorisationHolderHelper.userCanLandOnIsAuthRequiredPage
import controllers.helpers.TransportSectionHelper._
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper, TransportSectionHelper}
import controllers.section1.routes._
import controllers.section2.routes._
import controllers.section3.routes._
import controllers.section4.routes._
import controllers.section5.routes._
import controllers.section6.routes._
import controllers.summary.routes.SectionSummaryController
import forms.section1.Ducr.generateDucrPrefix
import forms.section4.NatureOfTransaction
import forms.section4.NatureOfTransaction.{BusinessPurchase, Sale}
import forms.section5.CommodityDetails
import forms.section6.InlandOrBorder.Border
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.mvc.Call
import services.TaggedAuthCodes

// scalastyle:off
trait CacheDependentNavigators {

  val taggedAuthCodes: TaggedAuthCodes
  val inlandOrBorderHelper: InlandOrBorderHelper
  val supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper

  protected def ducrEntryPreviousPage(cacheModel: ExportsDeclaration)(implicit request: JourneyRequest[_]): Call =
    cacheModel.ducr.fold(DucrChoiceController.displayPage) { ducr =>
      if (ducr.ducr.startsWith(generateDucrPrefix)) ConfirmDucrController.displayPage
      else DucrChoiceController.displayPage
    }

  protected def lrnPreviousPage(cacheModel: ExportsDeclaration)(implicit request: JourneyRequest[_]): Call =
    cacheModel.ducr.fold(DucrChoiceController.displayPage) { ducr =>
      if (ducr.ducr.startsWith(generateDucrPrefix)) ConfirmDucrController.displayPage
      else DucrEntryController.displayPage
    }

  protected def nactCodeFirstPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    cacheModel.natureOfTransaction match {
      case Some(NatureOfTransaction(`Sale`) | NatureOfTransaction(`BusinessPurchase`)) => ZeroRatedForVatController.displayPage(itemId)
      case _                                                                           => cusCodeOrDangerousGoodsPage(cacheModel, itemId)
    }

  protected def nactCodePreviousPageForSimplified(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isLowValueDeclaration(itemId)) ZeroRatedForVatController.displayPage(itemId)
    else cusCodeOrDangerousGoodsPage(cacheModel, itemId)

  protected def packageInformationPreviousPageForSimplified(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isLowValueDeclaration(itemId)) StatisticalValueController.displayPage(itemId)
    else NactCodeSummaryController.displayPage(itemId)

  protected def declarantIsExporterPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE =>
        if (cacheModel.isAmendmentDraft) EntryIntoDeclarantsRecordsController.displayPage
        else DeclarantDetailsController.displayPage

      case _ =>
        SectionSummaryController.displayPage(1)
    }

  protected def thirdPartyGoodsTransportationPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE =>
        if (cacheModel.parties.consignorDetails.flatMap(_.details.eori).isDefined) ConsignorEoriNumberController.displayPage
        else ConsignorDetailsController.displayPage
      case _ if cacheModel.isDeclarantExporter => DeclarantExporterController.displayPage
      case _                                   => RepresentativeStatusController.displayPage
    }

  protected def officeOfExitPreviousPage(cacheModel: ExportsDeclaration): Call = {
    val skipLocationOfGoods = cacheModel.isAmendmentDraft || taggedAuthCodes.skipLocationOfGoods(cacheModel)
    if (!skipLocationOfGoods) LocationOfGoodsController.displayPage
    else
      cacheModel.`type` match {
        case SUPPLEMENTARY | CLEARANCE => DestinationCountryController.displayPage
        case _                         => RoutingCountriesController.displayRoutingCountry
      }
  }

  protected def previousDocumentsPreviousPageDefault(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.hasPreviousDocuments) PreviousDocumentsSummaryController.displayPage
    else NatureOfTransactionController.displayPage

  protected def commodityDetailsPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.hasOsrProcedureCode(itemId)) {
      if (cacheModel.hasFiscalReferences(itemId)) AdditionalFiscalReferencesController.displayPage(itemId)
      else FiscalInformationController.displayPage(itemId)
    } else AdditionalProcedureCodesController.displayPage(itemId)

  protected def commodityMeasureClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord)) {
      if (cacheModel.isExs) UNDangerousGoodsCodeController.displayPage(itemId)
      else CommodityDetailsController.displayPage(itemId)
    } else PackageInformationSummaryController.displayPage(itemId)

  protected def packageInformationClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isExs) UNDangerousGoodsCodeController.displayPage(itemId)
    else CommodityDetailsController.displayPage(itemId)

  protected def additionalDocumentsPreviousPage(declaration: ExportsDeclaration, itemId: String): Call =
    if (declaration.listOfAdditionalDocuments(itemId).nonEmpty) AdditionalDocumentsController.displayPage(itemId)
    else {
      val isLicenseRequired = taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration) || declaration.isLicenseRequired(itemId)
      if (isLicenseRequired) IsLicenceRequiredController.displayPage(itemId)
      else AdditionalDocumentsRequiredController.displayPage(itemId)
    }

  protected def additionalDocumentsClearancePreviousPage(declaration: ExportsDeclaration, itemId: String): Call =
    if (declaration.listOfAdditionalDocuments(itemId).nonEmpty) AdditionalDocumentsController.displayPage(itemId)
    else if (taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(declaration)) additionalDocumentsSummaryClearancePreviousPage(declaration, itemId)
    else AdditionalDocumentsRequiredController.displayPage(itemId)

  protected def additionalDocumentsSummaryClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalInformationOfItem(itemId).nonEmpty) AdditionalInformationController.displayPage(itemId)
    else AdditionalInformationRequiredController.displayPage(itemId)

  protected def isLicenseRequiredPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalInformationOfItem(itemId).nonEmpty) AdditionalInformationController.displayPage(itemId)
    else AdditionalInformationRequiredController.displayPage(itemId)

  protected def additionalInformationAddPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      AdditionalInformationController.displayPage(itemId)
    else
      AdditionalInformationRequiredController.displayPage(itemId)

  protected def additionalFiscalReferencesPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.hasFiscalReferences(itemId)) AdditionalFiscalReferencesController.displayPage(itemId)
    else FiscalInformationController.displayPage(itemId)

  protected def previousDocumentsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.hasPreviousDocuments) PreviousDocumentsSummaryController.displayPage
    else SectionSummaryController.displayPage(3)

  protected def exporterEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords) PersonPresentingGoodsDetailsController.displayPage
    else DeclarantExporterController.displayPage

  protected def consigneeDetailsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isUsingOwnTransport) ThirdPartyGoodsTransportationController.displayPage
    else if (cacheModel.parties.carrierDetails.exists(_.details.eori.nonEmpty)) CarrierEoriNumberController.displayPage
    else CarrierDetailsController.displayPage

  protected def consigneeDetailsClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isUsingOwnTransport) ThirdPartyGoodsTransportationController.displayPage
    else if (!cacheModel.isExs && cacheModel.isDeclarantExporter) IsExsController.displayPage
    else if (!cacheModel.isExs) RepresentativeStatusController.displayPage
    else if (cacheModel.parties.carrierDetails.exists(_.details.eori.nonEmpty)) CarrierEoriNumberController.displayPage
    else CarrierDetailsController.displayPage

  protected def consigneeDetailsSupplementaryPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isDeclarantExporter) DeclarantExporterController.displayPage
    else RepresentativeStatusController.displayPage

  protected def representativeAgentClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isExs) {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined) ConsignorDetailsController.displayPage
      else ConsignorEoriNumberController.displayPage
    } else IsExsController.displayPage

  protected def isExsClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isDeclarantExporter) exporterEoriNumberClearancePreviousPage(cacheModel)
    else if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined) ExporterEoriNumberController.displayPage
    else ExporterDetailsController.displayPage

  protected def authorisationHolderRequiredPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE if !cacheModel.isEntryIntoDeclarantsRecords => ConsigneeDetailsController.displayPage
      case OCCASIONAL                                            => AdditionalActorsSummaryController.displayPage
      case _                                                     => AuthorisationProcedureCodeChoiceController.displayPage
    }

  protected def authorisationHolderAddPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.authorisationHolders.nonEmpty) AuthorisationHolderSummaryController.displayPage
    else if (userCanLandOnIsAuthRequiredPage(cacheModel)) AuthorisationHolderRequiredController.displayPage
    else authorisationHolderRequiredPreviousPage(cacheModel)

  protected def card2ForPartiesPreviousPage(cacheModel: ExportsDeclaration): Call =
    authorisationHolderAddPreviousPage(cacheModel)

  protected def representativeAgentPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined) ExporterEoriNumberController.displayPage
    else ExporterDetailsController.displayPage

  protected def supervisingCustomsOfficePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.requiresWarehouseId || cacheModel.isType(CLEARANCE)) WarehouseIdentificationController.displayPage
    else TransportLeavingTheBorderController.displayPage

  protected def inlandOrBorderPreviousPage(cacheModel: ExportsDeclaration): Call = {
    val gotoTransportLeavingTheBorder = supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(cacheModel)
    if (gotoTransportLeavingTheBorder) TransportLeavingTheBorderController.displayPage
    else SupervisingCustomsOfficeController.displayPage
  }

  protected def inlandTransportDetailsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (inlandOrBorderHelper.skipInlandOrBorder(cacheModel)) inlandOrBorderPreviousPage(cacheModel)
    else InlandOrBorderController.displayPage

  protected def departureTransportPreviousPageOnClearance(cacheModel: ExportsDeclaration): Call =
    SupervisingCustomsOfficeController.displayPage

  // Must be a lazy val, as full-path Calls are generated at runtime from reverse routing.
  private lazy val condForTransportPages = List[(ExportsDeclaration => Boolean, Call)](
    (!isRailModeOfTransport(_), TransportCountryController.displayPage),
    (!_.isInlandOrBorder(Border), BorderTransportController.displayPage),
    (!isOccasionalOrSimplified(_), DepartureTransportController.displayPage)
  )

  private val fromBeforeTransportPages = 0
  private val fromTransportCountry = 1
  private val fromBorderTransport = 2
  private val fromDepartureTransport = 3

  private def previousTransportPage(declaration: ExportsDeclaration, fromPage: Int): Call = {
    lazy val callWhenSkippingTransportPages = {
      val skipInlandOrBorder = inlandOrBorderHelper.skipInlandOrBorder(declaration)
      if (!skipInlandOrBorder && declaration.isInlandOrBorder(Border)) InlandOrBorderController.displayPage
      else InlandTransportDetailsController.displayPage
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
    if (!isPostalOrFTIModeOfTransport(cacheModel.transportLeavingBorderCode)) DepartureTransportController.displayPage
    else SupervisingCustomsOfficeController.displayPage

  protected def containerFirstPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.transport.transportPayment.nonEmpty) TransportPaymentController.displayPage
    else ExpressConsignmentController.displayPage

  protected def cusCodeOrDangerousGoodsPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
      CusCodeController.displayPage(itemId)
    else
      UNDangerousGoodsCodeController.displayPage(itemId)

  protected def authorisationProcedureCodeChoicePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isType(CLEARANCE) && cacheModel.isEntryIntoDeclarantsRecords)
      ConsigneeDetailsController.displayPage
    else
      AdditionalActorsSummaryController.displayPage

  protected def totalPackageQuantityPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isInvoiceAmountGreaterThan100000) InvoiceAndExchangeRateController.displayPage
    else InvoiceAndExchangeRateChoiceController.displayPage

  protected def natureOfTransactionPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (TransportSectionHelper.isGuernseyOrJerseyDestination(cacheModel))
      totalPackageQuantityPreviousPage(cacheModel)
    else
      TotalPackageQuantityController.displayPage
}
