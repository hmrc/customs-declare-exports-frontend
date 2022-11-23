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

package controllers.navigation

import controllers.declaration.routes
import controllers.helpers.DeclarationHolderHelper.userCanLandOnIsAuthRequiredPage
import controllers.helpers.ErrorFixModeHelper.{inErrorFixMode, setErrorFixMode}
import controllers.helpers.LocationOfGoodsHelper.skipLocationOfGoods
import controllers.helpers.TransportSectionHelper.{additionalDeclTypesAllowedOnInlandOrBorder, isPostalOrFTIModeOfTransport}
import controllers.helpers._
import controllers.routes.{ChoiceController, RejectedNotificationsController}
import forms.Choice.AllowedChoiceValues
import forms.declaration.InlandOrBorder.Border
import forms.declaration.NatureOfTransaction.{BusinessPurchase, Sale}
import forms.declaration.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{STANDARD_FRONTIER, STANDARD_PRE_LODGED, SUPPLEMENTARY_SIMPLIFIED}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypePage
import forms.declaration.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.declaration.carrier.{CarrierDetails, CarrierEoriNumber}
import forms.declaration.commodityMeasure.{CommodityMeasure, SupplementaryUnits}
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.countries.Countries.{DestinationCountryPage, RoutingCountryPage}
import forms.declaration.declarationHolder.{DeclarationHolder, DeclarationHolderRequired, DeclarationHolderSummary}
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.procedurecodes.{AdditionalProcedureCode, ProcedureCode}
import forms.declaration.removals.RemoveItem
import forms.{Choice, DeclarationPage}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.ExportItem
import models.requests.JourneyRequest
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.TariffApiService
import services.TariffApiService.SupplementaryUnitsNotRequired

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class ItemId(id: String)

// scalastyle:off number.of.methods
@Singleton
class Navigator @Inject() (
  tariffApiService: TariffApiService,
  inlandOrBorderHelper: InlandOrBorderHelper,
  supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper
) {

  val common: PartialFunction[DeclarationPage, Call] = {
    case DeclarationChoice             => ChoiceController.displayPage(Some(Choice(AllowedChoiceValues.CreateDec)))
    case LinkDucrToMucr                => routes.DucrEntryController.displayPage
    case Mucr                          => routes.LinkDucrToMucrController.displayPage
    case RepresentativeEntity          => routes.RepresentativeAgentController.displayPage
    case RepresentativeStatus          => routes.RepresentativeEntityController.displayPage
    case AdditionalDeclarationTypePage => routes.DeclarationChoiceController.displayPage
    case NatureOfTransaction           => routes.TotalPackageQuantityController.displayPage
    case ProcedureCode                 => routes.ItemsSummaryController.displayItemsSummaryPage
    case ExportItem                    => routes.PreviousDocumentsSummaryController.displayPage
    case RemoveItem                    => routes.ItemsSummaryController.displayItemsSummaryPage
    case DocumentChangeOrRemove        => routes.PreviousDocumentsSummaryController.displayPage
    case TransportLeavingTheBorder     => routes.ItemsSummaryController.displayItemsSummaryPage
    case TransportPayment              => routes.ExpressConsignmentController.displayPage
    case CarrierDetails                => routes.CarrierEoriNumberController.displayPage
    case InvoiceAndExchangeRateChoice  => routes.OfficeOfExitController.displayPage
    case InvoiceAndExchangeRate        => routes.InvoiceAndExchangeRateChoiceController.displayPage
  }

  val commonItem: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalProcedureCode           => routes.ProcedureCodesController.displayPage
    case FiscalInformation                 => routes.AdditionalProcedureCodesController.displayPage
    case AdditionalFiscalReferencesSummary => routes.AdditionalProcedureCodesController.displayPage
    case CommodityDetails                  => routes.FiscalInformationController.displayPage(_, fastForward = true)
    case UNDangerousGoodsCode              => routes.CommodityDetailsController.displayPage
    case TaricCode                         => routes.TaricCodeSummaryController.displayPage
    case StatisticalValue                  => routes.NactCodeSummaryController.displayPage
    case SupplementaryUnits                => routes.CommodityMeasureController.displayPage
    case ZeroRatedForVat                   => routes.TaricCodeSummaryController.displayPage
  }

  val commonCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case DeclarationHolderRequired        => declarationHolderRequiredPreviousPage
    case DeclarationHolder                => declarationHolderAddPreviousPage
    case DeclarationHolderSummary         => declarationHolderSummaryPreviousPage
    case SupervisingCustomsOffice         => supervisingCustomsOfficePreviousPage
    case WarehouseIdentification          => warehouseIdentificationPreviousPage
    case AuthorisationProcedureCodeChoice => authorisationProcedureCodeChoicePreviousPage
    case OfficeOfExit                     => officeOfExitPreviousPage
    case RoutingCountryPage               => _ => routes.RoutingCountriesController.displayRoutingQuestion
  }

  val commonCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case IsLicenceRequired         => isLicenseRequiredPreviousPage
    case AdditionalInformation     => additionalInformationAddPreviousPage
    case AdditionalFiscalReference => additionalFiscalReferencesPreviousPage
    case TaricCodeFirst            => additionalTaricCodesPreviousPage
  }

  val standard: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => routes.DepartureTransportController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage  => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage           => routes.RoutingCountriesController.displayRoutingCountry
    case DocumentSummary             => routes.NatureOfTransactionController.displayPage
    case LocationOfGoods             => routes.RoutingCountriesController.displayRoutingCountry
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }

  val standardItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case PackageInformation => routes.StatisticalValueController.displayPage
    case CusCode            => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode           => routes.NactCodeSummaryController.displayPage
    case CommodityMeasure   => routes.PackageInformationSummaryController.displayPage
    case page               => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }

  val standardCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case CarrierEoriNumber         => carrierEoriNumberPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case ContainerFirst            => containerFirstPreviousPage
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case DestinationCountryPage    => destinationCountryPreviousPage
    case TotalPackageQuantity      => totalPackageQuantityPreviousPage
    case Document                  => previousDocumentsPreviousPageDefault
    case InlandOrBorder            => inlandOrBorderPreviousPage
    case InlandModeOfTransportCode => inlandTransportDetailsPreviousPage
    case DepartureTransport        => departureTransportPreviousPageOnStandardOrSuppl
    case TransportCountry          => transportCountryPreviousPage
    case ExpressConsignment        => expressConsignmentPreviousPageOnStandard
    case RepresentativeAgent       => representativeAgentPreviousPage
  }

  val standardCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalDocumentsRequired => additionalDocumentsSummaryPreviousPage
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryPreviousPage
    case AdditionalDocument          => additionalDocumentsPreviousPage
    case NactCodeFirst               => nactCodeFirstPreviousPage

  }

  val clearance: PartialFunction[DeclarationPage, Call] = {
    case ConsignmentReferences        => routes.AdditionalDeclarationTypeController.displayPage
    case ExporterDetails              => routes.ExporterEoriNumberController.displayPage
    case DeclarantDetails             => routes.EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => routes.EntryIntoDeclarantsRecordsController.displayPage
    case DeclarantIsExporter          => routes.DeclarantDetailsController.displayPage
    case ContainerAdd                 => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage   => routes.DestinationCountryController.displayPage
    case RemoveCountryPage            => routes.RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage            => routes.RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods              => routes.DestinationCountryController.displayPage
    case ConsignorEoriNumber          => routes.IsExsController.displayPage
    case ConsignorDetails             => routes.ConsignorEoriNumberController.displayPage
    case DocumentSummary              => routes.OfficeOfExitController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalInformationRequired => routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => routes.CommodityMeasureController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case EntryIntoDeclarantsRecords => entryIntoDeclarantsPreviousPage
    case CarrierEoriNumber          => carrierEoriNumberClearancePreviousPage
    case ExporterEoriNumber         => exporterEoriNumberClearancePreviousPage
    case ConsigneeDetails           => consigneeDetailsClearancePreviousPage
    case DestinationCountryPage     => destinationCountryPreviousPage
    case RepresentativeAgent        => representativeAgentClearancePreviousPage
    case IsExs                      => isExsClearancePreviousPage
    case Document                   => previousDocumentsPreviousPage
    case DepartureTransport         => departureTransportClearancePreviousPage
    case ContainerFirst             => containerFirstPreviousPage
    case ExpressConsignment         => expressConsignmentPreviousPageOnClearance
  }

  val clearanceCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case CommodityMeasure            => commodityMeasureClearancePreviousPage
    case PackageInformation          => packageInformationClearancePreviousPage
    case AdditionalDocumentsRequired => additionalDocumentsSummaryClearancePreviousPage
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryClearancePreviousPage
    case AdditionalDocument          => additionalDocumentsClearancePreviousPage
  }

  val supplementary: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => routes.DepartureTransportController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case LocationOfGoods             => routes.DestinationCountryController.displayPage
    case DocumentSummary             => routes.NatureOfTransactionController.displayPage
    case OfficeOfExit                => routes.LocationOfGoodsController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val supplementaryItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case PackageInformation => routes.StatisticalValueController.displayPage
    case CusCode            => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode           => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst      => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure   => routes.PackageInformationSummaryController.displayPage
    case page               => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val supplementaryCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case ConsigneeDetails          => consigneeDetailsSupplementaryPreviousPage
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case DestinationCountryPage    => destinationCountryPreviousPage
    case OfficeOfExit              => officeOfExitPreviousPage
    case TotalPackageQuantity      => totalPackageQuantityPreviousPage
    case Document                  => previousDocumentsPreviousPageDefault
    case InlandOrBorder            => inlandOrBorderPreviousPage
    case InlandModeOfTransportCode => inlandTransportDetailsPreviousPage
    case DepartureTransport        => departureTransportPreviousPageOnStandardOrSuppl
    case TransportCountry          => transportCountryPreviousPage
    case ContainerFirst            => containerFirstPreviousPageOnSupplementary
    case RepresentativeAgent       => representativeAgentPreviousPage
  }

  val supplementaryCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalDocumentsRequired => additionalDocumentsSummaryPreviousPage
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryPreviousPage
    case AdditionalDocument          => additionalDocumentsPreviousPage
  }

  val simplified: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage  => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage           => routes.RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods             => routes.RoutingCountriesController.displayRoutingCountry
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case DocumentSummary             => routes.OfficeOfExitController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val simplifiedItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case PackageInformation            => routes.NactCodeSummaryController.displayPage
    case AdditionalInformationRequired => routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => routes.PackageInformationSummaryController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val simplifiedCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case CarrierEoriNumber         => carrierEoriNumberPreviousPage
    case Document                  => previousDocumentsPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case DestinationCountryPage    => destinationCountryPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
    case InlandModeOfTransportCode => supervisingCustomsOfficePageOnCondition
    case ExpressConsignment        => supervisingCustomsOfficePageOnCondition
    case ContainerFirst            => containerFirstPreviousPage
  }

  val simplifiedCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalDocumentsRequired => additionalDocumentsSummaryPreviousPage
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryPreviousPage
    case AdditionalDocument          => additionalDocumentsPreviousPage
  }

  val occasional: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage  => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods             => routes.RoutingCountriesController.displayRoutingCountry
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesController.displayRoutingCountry
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case DocumentSummary             => routes.OfficeOfExitController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case PackageInformation            => routes.NactCodeSummaryController.displayPage
    case AdditionalInformationRequired => routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => routes.PackageInformationSummaryController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case CarrierEoriNumber         => carrierEoriNumberPreviousPage
    case Document                  => previousDocumentsPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case DestinationCountryPage    => destinationCountryPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
    case InlandModeOfTransportCode => supervisingCustomsOfficePageOnCondition
    case ExpressConsignment        => supervisingCustomsOfficePageOnCondition
    case ContainerFirst            => containerFirstPreviousPage
  }

  val occasionalCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalDocumentsRequired => additionalDocumentsSummaryPreviousPage
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryPreviousPage
    case AdditionalDocument          => additionalDocumentsPreviousPage
  }

  def continueTo(factory: Call)(implicit request: JourneyRequest[AnyContent]): Result = {
    val formAction = FormAction.bindFromRequest
    if (inErrorFixMode) handleErrorFixMode(factory, formAction)
    else if (formAction == SaveAndReturnToSummary) Results.Redirect(routes.SummaryController.displayPage)
    else Results.Redirect(factory)
  }

  private def handleErrorFixMode(factory: Call, formAction: FormAction)(implicit request: JourneyRequest[_]): Result =
    (formAction, request.cacheModel.parentDeclarationId) match {
      case (SaveAndReturnToErrors, Some(parentId)) => Results.Redirect(RejectedNotificationsController.displayPage(parentId))
      case (Add | Remove(_) | SaveAndContinue, _)  => setErrorFixMode(Results.Redirect(factory))
      case _                                       => setErrorFixMode(Results.Redirect(factory).flashing(request.flash))
    }

  private def nactCodeFirstPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    cacheModel.natureOfTransaction match {
      case Some(NatureOfTransaction(`Sale`) | NatureOfTransaction(`BusinessPurchase`)) => routes.ZeroRatedForVatController.displayPage(itemId)
      case _                                                                           => routes.TaricCodeSummaryController.displayPage(itemId)
    }

  private def declarantIsExporterPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case SUPPLEMENTARY => routes.DucrEntryController.displayPage
      case _ =>
        if (cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage()
        else routes.MucrController.displayPage()
    }

  private def officeOfExitPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (skipLocationOfGoods(cacheModel)) routes.DestinationCountryController.displayPage()
    else routes.LocationOfGoodsController.displayPage()

  private def entryIntoDeclarantsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage()
    else routes.MucrController.displayPage()

  private def previousDocumentsPreviousPageDefault(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.hasPreviousDocuments) routes.PreviousDocumentsSummaryController.displayPage()
    else routes.NatureOfTransactionController.displayPage()

  private def consigneeDetailsSupplementaryPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isDeclarantExporter)
      routes.DeclarantExporterController.displayPage()
    else
      routes.RepresentativeStatusController.displayPage()

  private def commodityMeasureClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
      if (cacheModel.isExs)
        routes.UNDangerousGoodsCodeController.displayPage(itemId)
      else
        routes.CommodityDetailsController.displayPage(itemId)
    else
      routes.PackageInformationSummaryController.displayPage(itemId)

  private def packageInformationClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isExs)
      routes.UNDangerousGoodsCodeController.displayPage(itemId)
    else
      routes.CommodityDetailsController.displayPage(itemId)

  private def additionalDocumentsPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call = {
    val isLicenseRequired = cacheModel.hasAuthCodeRequiringAdditionalDocs || cacheModel.isLicenseRequired(itemId)
    if (isLicenseRequired) routes.IsLicenceRequiredController.displayPage(itemId)
    else routes.AdditionalDocumentsRequiredController.displayPage(itemId)
  }

  private def additionalDocumentsSummaryPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    routes.IsLicenceRequiredController.displayPage(itemId)

  private def additionalDocumentsSummaryClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalInformationOfItem(itemId).nonEmpty)
      routes.AdditionalInformationController.displayPage(itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(itemId)

  private def isLicenseRequiredPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalInformationOfItem(itemId).nonEmpty)
      routes.AdditionalInformationController.displayPage(itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(itemId)

  private def additionalDocumentsClearancePreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.listOfAdditionalDocuments(itemId).nonEmpty)
      routes.AdditionalDocumentsController.displayPage(itemId)
    else if (cacheModel.hasAuthCodeRequiringAdditionalDocs) additionalDocumentsSummaryClearancePreviousPage(cacheModel, itemId)
    else routes.AdditionalDocumentsRequiredController.displayPage(itemId)

  private def additionalInformationAddPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      routes.AdditionalInformationController.displayPage(itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(itemId)

  private def additionalFiscalReferencesPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).exists(_.references.nonEmpty))
      routes.AdditionalFiscalReferencesController.displayPage(itemId)
    else
      routes.FiscalInformationController.displayPage(itemId)

  private def previousDocumentsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.hasPreviousDocuments) routes.PreviousDocumentsSummaryController.displayPage()
    else routes.OfficeOfExitController.displayPage()

  private def exporterEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords)
      routes.PersonPresentingGoodsDetailsController.displayPage()
    else
      routes.DeclarantExporterController.displayPage()

  private def carrierEoriNumberPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      routes.DeclarantExporterController.displayPage()
    else
      routes.RepresentativeStatusController.displayPage()

  private def carrierEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (!cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      routes.RepresentativeStatusController.displayPage()
    else {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        routes.ConsignorDetailsController.displayPage()
      else
        routes.ConsignorEoriNumberController.displayPage()
    }

  private def consigneeDetailsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.carrierDetails.flatMap(_.details.eori).isEmpty)
      routes.CarrierDetailsController.displayPage()
    else
      routes.CarrierEoriNumberController.displayPage()

  private def consigneeDetailsClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isExs)
      consigneeDetailsPreviousPage(cacheModel)
    else {
      if (cacheModel.isDeclarantExporter)
        routes.IsExsController.displayPage()
      else
        routes.RepresentativeStatusController.displayPage()
    }

  private def representativeAgentClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isExs) {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        routes.ConsignorDetailsController.displayPage()
      else
        routes.ConsignorEoriNumberController.displayPage()
    } else {
      routes.IsExsController.displayPage()
    }

  private def isExsClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isDeclarantExporter)
      exporterEoriNumberClearancePreviousPage(cacheModel)
    else if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage()
    else routes.ExporterDetailsController.displayPage()

  private def declarationHolderRequiredPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case CLEARANCE if !cacheModel.isEntryIntoDeclarantsRecords => routes.ConsigneeDetailsController.displayPage()
      case OCCASIONAL                                            => routes.AdditionalActorsSummaryController.displayPage()
      case _                                                     => routes.AuthorisationProcedureCodeChoiceController.displayPage()
    }

  private def declarationHolderAddPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.declarationHolders.nonEmpty) routes.DeclarationHolderSummaryController.displayPage()
    else if (userCanLandOnIsAuthRequiredPage(cacheModel)) routes.DeclarationHolderRequiredController.displayPage()
    else declarationHolderRequiredPreviousPage(cacheModel)

  private def declarationHolderSummaryPreviousPage(cacheModel: ExportsDeclaration): Call =
    declarationHolderRequiredPreviousPage(cacheModel)

  private def destinationCountryPreviousPage(cacheModel: ExportsDeclaration): Call =
    declarationHolderAddPreviousPage(cacheModel)

  private def warehouseIdentificationPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.`type` match {
      case OCCASIONAL | SIMPLIFIED => routes.ItemsSummaryController.displayItemsSummaryPage()
      case _                       => routes.TransportLeavingTheBorderController.displayPage()
    }

  private def representativeAgentPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage()
    else routes.ExporterDetailsController.displayPage()

  private def departureTransportClearancePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords) supervisingCustomsOfficePageOnCondition(cacheModel)
    else routes.SupervisingCustomsOfficeController.displayPage()

  private def supervisingCustomsOfficePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.requiresWarehouseId || cacheModel.isType(CLEARANCE)) routes.WarehouseIdentificationController.displayPage()
    else warehouseIdentificationPreviousPage(cacheModel)

  private def inlandOrBorderPreviousPage(cacheModel: ExportsDeclaration): Call =
    cacheModel.additionalDeclarationType match {
      case Some(STANDARD_FRONTIER) | Some(STANDARD_PRE_LODGED) | Some(SUPPLEMENTARY_SIMPLIFIED)
          if supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(cacheModel) =>
        routes.TransportLeavingTheBorderController.displayPage()

      case _ => routes.SupervisingCustomsOfficeController.displayPage()
    }

  private def inlandTransportDetailsPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (inlandOrBorderHelper.skipInlandOrBorder(cacheModel)) supervisingCustomsOfficePageOnCondition(cacheModel)
    else routes.InlandOrBorderController.displayPage()

  private def supervisingCustomsOfficePageOnCondition(cacheModel: ExportsDeclaration): Call =
    if (supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(cacheModel)) supervisingCustomsOfficePreviousPage(cacheModel)
    else routes.SupervisingCustomsOfficeController.displayPage()

  private def departureTransportPreviousPageOnStandardOrSuppl(cacheModel: ExportsDeclaration): Call = {
    val inAllowedFlow = cacheModel.additionalDeclarationType.exists(additionalDeclTypesAllowedOnInlandOrBorder.contains)
    if (inAllowedFlow && cacheModel.isInlandOrBorder(Border)) routes.InlandOrBorderController.displayPage()
    else routes.InlandTransportDetailsController.displayPage()
  }

  private def transportCountryPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isInlandOrBorder(Border)) routes.DepartureTransportController.displayPage()
    else routes.BorderTransportController.displayPage()

  private def expressConsignmentPreviousPageOnStandard(cacheModel: ExportsDeclaration): Call =
    if (isPostalOrFTIModeOfTransport(cacheModel.inlandModeOfTransportCode)) routes.InlandTransportDetailsController.displayPage()
    else {
      val postalOrFTI = isPostalOrFTIModeOfTransport(cacheModel.transportLeavingBorderCode)
      if (postalOrFTI && cacheModel.isInlandOrBorder(Border)) routes.InlandOrBorderController.displayPage()
      else routes.TransportCountryController.displayPage()
    }

  private def expressConsignmentPreviousPageOnClearance(cacheModel: ExportsDeclaration): Call = {
    val postalOrFTI = isPostalOrFTIModeOfTransport(cacheModel.transportLeavingBorderCode)
    if (postalOrFTI && supervisingCustomsOfficeHelper.checkProcedureCodes(cacheModel)) routes.WarehouseIdentificationController.displayPage()
    else if (postalOrFTI) routes.SupervisingCustomsOfficeController.displayPage()
    else routes.DepartureTransportController.displayPage()
  }

  private def containerFirstPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.transport.transportPayment.nonEmpty) routes.TransportPaymentController.displayPage()
    else routes.ExpressConsignmentController.displayPage()

  private def containerFirstPreviousPageOnSupplementary(cacheModel: ExportsDeclaration): Call =
    expressConsignmentPreviousPageOnStandard(cacheModel)

  private def additionalTaricCodesPreviousPage(cacheModel: ExportsDeclaration, itemId: String): Call =
    if (cacheModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
      routes.CusCodeController.displayPage(itemId)
    else
      routes.UNDangerousGoodsCodeController.displayPage(itemId)

  private def authorisationProcedureCodeChoicePreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isType(CLEARANCE) && cacheModel.isEntryIntoDeclarantsRecords)
      routes.ConsigneeDetailsController.displayPage()
    else
      routes.AdditionalActorsSummaryController.displayPage()

  private def totalPackageQuantityPreviousPage(cacheModel: ExportsDeclaration): Call =
    if (cacheModel.isInvoiceAmountGreaterThan100000) routes.InvoiceAndExchangeRateController.displayPage()
    else routes.InvoiceAndExchangeRateChoiceController.displayPage()

  def backLink(page: DeclarationPage)(implicit request: JourneyRequest[_]): Call = {
    val specific = request.declarationType match {
      case STANDARD      => standardCacheDependent.orElse(standard)
      case SUPPLEMENTARY => supplementaryCacheDependent.orElse(supplementary)
      case SIMPLIFIED    => simplifiedCacheDependent.orElse(simplified)
      case OCCASIONAL    => occasionalCacheDependent.orElse(occasional)
      case CLEARANCE     => clearanceCacheDependent.orElse(clearance)
    }

    commonCacheDependent.orElse(common).orElse(specific)(page) match {
      case mapping: Call                         => mapping
      case mapping: (ExportsDeclaration => Call) => mapping(request.cacheModel)
    }
  }

  def backLink(page: DeclarationPage, itemId: ItemId)(implicit request: JourneyRequest[_]): Call = {
    val specific = request.declarationType match {
      case STANDARD      => standardCacheItemDependent.orElse(standardItemPage)
      case SUPPLEMENTARY => supplementaryCacheItemDependent.orElse(supplementaryItemPage)
      case SIMPLIFIED    => simplifiedCacheItemDependent.orElse(simplifiedItemPage)
      case OCCASIONAL    => occasionalCacheItemDependent.orElse(occasionalItemPage)
      case CLEARANCE     => clearanceCacheItemDependent.orElse(clearanceItemPage)
    }
    commonCacheItemDependent.orElse(commonItem).orElse(specific)(page) match {
      case mapping: (String => Call)                       => mapping(itemId.id)
      case mapping: ((ExportsDeclaration, String) => Call) => mapping(request.cacheModel, itemId.id)
    }
  }

  def backLinkForAdditionalInformation(
    page: DeclarationPage,
    itemId: String
  )(implicit request: JourneyRequest[_], ec: ExecutionContext): Future[Call] = {
    def pageSelection: Future[Call] =
      tariffApiService.retrieveCommodityInfoIfAny(request.cacheModel, itemId) map {
        case Left(SupplementaryUnitsNotRequired) => routes.CommodityMeasureController.displayPage(itemId)
        case _                                   => routes.SupplementaryUnitsController.displayPage(itemId)
      }

    page match {
      case AdditionalInformationSummary | AdditionalInformationRequired =>
        request.declarationType match {
          case STANDARD | SUPPLEMENTARY => pageSelection
          case _                        => Future.successful(backLink(page, ItemId(itemId)))
        }
      case _ => Future.successful(backLink(page, ItemId(itemId)))
    }
  }
}
