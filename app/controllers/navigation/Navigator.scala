/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import controllers.util.{Add, FormAction, Remove, SaveAndReturn}
import forms.Choice.AllowedChoiceValues
import forms.declaration.RoutingQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingQuestionPage}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.countries.Countries.{DestinationCountryPage, OriginationCountryPage}
import forms.declaration.officeOfExit.{OfficeOfExitInsideUK, OfficeOfExitOutsideUK}
import forms.declaration.{BorderTransport, ConsigneeDetails, Document, PackageInformation, _}
import forms.{Choice, DeclarationPage}
import javax.inject.Inject
import models.DeclarationType._
import models.Mode.ErrorFix
import models.declaration.ExportItem
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import models.{ExportsDeclaration, Mode}
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

class Navigator @Inject()(appConfig: AppConfig, auditService: AuditService) {

  def continueTo(mode: Mode, factory: Mode => Call, isErrorFixInProgress: Boolean = false)(
    implicit req: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Result =
    (mode, FormAction.bindFromRequest) match {
      case (ErrorFix, formAction) => handleErrorFixMode(factory, formAction, isErrorFixInProgress)
      case (_, SaveAndReturn) =>
        auditService.auditAllPagesUserInput(AuditTypes.SaveAndReturnSubmission, req.cacheModel)
        goToDraftConfirmation()
      case _ => Results.Redirect(factory(mode))
    }

  private def goToDraftConfirmation()(implicit req: JourneyRequest[_]): Result = {
    val updatedDateTime = req.cacheModel.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds)
    Results
      .Redirect(controllers.declaration.routes.ConfirmationController.displayDraftConfirmation())
      .flashing(FlashKeys.expiryDate -> expiry.toEpochMilli.toString)
      .removingFromSession(ExportsSessionKeys.declarationId)
  }

  private def handleErrorFixMode(factory: Mode => Call, formAction: FormAction, isErrorFixInProgress: Boolean)(
    implicit req: JourneyRequest[AnyContent]
  ): Result =
    formAction match {
      case Add | Remove(_)                  => Results.Redirect(factory(ErrorFix))
      case _ if isErrorFixInProgress        => Results.Redirect(factory(ErrorFix))
      case _ if (req.sourceDecId.isDefined) => Results.Redirect(controllers.routes.RejectedNotificationsController.displayPage(req.sourceDecId.get))
      case _                                => Results.Redirect(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }
}

case class ItemId(id: String)

object Navigator {

  val standard: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterDetails             => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ConsigneeDetails            => controllers.declaration.routes.CarrierDetailsController.displayPage
    case BorderTransport             => controllers.declaration.routes.DepartureTransportController.displayPage
    case TransportPayment            => controllers.declaration.routes.BorderTransportController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OriginationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.OriginationCountryController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case DeclarationHolder           => controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage
    case RepresentativeAgent         => controllers.declaration.routes.ExporterDetailsController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case TransportLeavingTheBorder   => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }
  val standardItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformation => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst         => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure      => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }

  val clearance: PartialFunction[DeclarationPage, Mode => Call] = {
    case EntryIntoDeclarantsRecords   => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case DeclarantDetails             => controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage
    case TransportPayment             => controllers.declaration.routes.DepartureTransportController.displayPage
    case ContainerFirst               => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                 => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                     => controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage
    case DestinationCountryPage       => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage          => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage            => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage            => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm            => controllers.declaration.routes.DestinationCountryController.displayPage
    case DeclarationHolder            => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case ConsignorEoriNumber          => controllers.declaration.routes.IsExsController.displayPage
    case ConsignorDetails             => controllers.declaration.routes.ConsignorEoriNumberController.displayPage
    case OfficeOfExitInsideUK         => controllers.declaration.routes.LocationController.displayPage
    case OfficeOfExitOutsideUK        => controllers.declaration.routes.OfficeOfExitController.displayPage
    case SupervisingCustomsOffice     => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case TransportLeavingTheBorder    => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification      => controllers.declaration.routes.ItemsSummaryController.displayPage
    case TotalPackageQuantity         => controllers.declaration.routes.OfficeOfExitController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case AdditionalInformation => controllers.declaration.routes.CommodityMeasureController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val supplementary: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterDetails             => controllers.declaration.routes.DeclarantExporterController.displayPage
    case BorderTransport             => controllers.declaration.routes.DepartureTransportController.displayPage
    case ContainerFirst              => controllers.declaration.routes.BorderTransportController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OriginationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.OriginationCountryController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.DestinationCountryController.displayPage
    case OfficeOfExitInsideUK        => controllers.declaration.routes.LocationController.displayPage
    case OfficeOfExitOutsideUK       => controllers.declaration.routes.OfficeOfExitController.displayPage
    case RepresentativeAgent         => controllers.declaration.routes.ExporterDetailsController.displayPage
    case DeclarationHolder           => controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case TransportLeavingTheBorder   => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }
  val supplementaryItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformation => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst         => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure      => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val simplified: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterDetails             => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ConsigneeDetails            => controllers.declaration.routes.CarrierDetailsController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TransportPayment            => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage
    case OfficeOfExitOutsideUK       => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case RepresentativeAgent         => controllers.declaration.routes.ExporterDetailsController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case DeclarationHolder           => controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case TransportLeavingTheBorder   => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }
  val simplifiedItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case AdditionalInformation => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst         => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure      => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val occasional: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterDetails             => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ConsigneeDetails            => controllers.declaration.routes.CarrierDetailsController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TransportPayment            => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage
    case OfficeOfExitOutsideUK       => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case RepresentativeAgent         => controllers.declaration.routes.ExporterDetailsController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case DeclarationHolder           => controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case TransportLeavingTheBorder   => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case AdditionalInformation => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst         => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure      => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val common: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarationChoice =>
      _ =>
        controllers.routes.ChoiceController.displayPage(Some(Choice(AllowedChoiceValues.CreateDec)))
    case DispatchLocation                     => controllers.declaration.routes.DeclarationChoiceController.displayPage
    case ConsignmentReferences                => controllers.declaration.routes.AdditionalDeclarationTypeController.displayPage
    case DeclarantIsExporter                  => controllers.declaration.routes.DeclarantDetailsController.displayPage
    case RepresentativeEntity                 => controllers.declaration.routes.RepresentativeAgentController.displayPage
    case RepresentativeStatus                 => controllers.declaration.routes.RepresentativeEntityController.displayPage
    case OfficeOfExitInsideUK                 => controllers.declaration.routes.LocationController.displayPage
    case OfficeOfExitOutsideUK                => controllers.declaration.routes.OfficeOfExitController.displayPage
    case AdditionalDeclarationTypeStandardDec => controllers.declaration.routes.DispatchLocationController.displayPage
    case TotalNumberOfItems                   => controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage
    case NatureOfTransaction                  => controllers.declaration.routes.TotalPackageQuantityController.displayPage
    case ProcedureCodes                       => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DepartureTransport                   => controllers.declaration.routes.TransportLeavingTheBorderController.displayPage
    case ExportItem                           => controllers.declaration.routes.PreviousDocumentsController.displayPage
  }

  val commonItem: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case FiscalInformation         => controllers.declaration.routes.ProcedureCodesController.displayPage
    case AdditionalFiscalReference => controllers.declaration.routes.FiscalInformationController.displayPage(_, _, fastForward = false)
    case CommodityDetails          => controllers.declaration.routes.FiscalInformationController.displayPage(_, _, fastForward = true)
    case UNDangerousGoodsCode      => controllers.declaration.routes.CommodityDetailsController.displayPage
    case TaricCode                 => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case TaricCodeFirst            => controllers.declaration.routes.CusCodeController.displayPage
    case StatisticalValue          => controllers.declaration.routes.NactCodeSummaryController.displayPage
  }

  val commonCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = Map.empty

  val commonCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = {
    case DocumentsProduced => documentsProducedPreviousPage
  }

  val standardCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierDetails => carrierDetailsPreviousPage
  }

  val standardCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val supplementaryCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case ConsigneeDetails => consigneeDetailsSupplementaryPreviousPage
  }

  val supplementaryCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val simplifiedCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierDetails => carrierDetailsPreviousPage
  }

  val simplifiedCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val occasionalCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierDetails => carrierDetailsPreviousPage
  }

  val occasionalCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val clearanceCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case ExporterDetails     => exporterDetailsClearancePreviousPage
    case CarrierDetails      => carrierDetailsClearancePreviousPage
    case ConsigneeDetails    => consigneeDetailsClearancePreviousPage
    case RepresentativeAgent => representativeAgentClearancePreviousPage
    case IsExs               => isExsClearancePreviousPage
  }

  val clearanceCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = {
    case CommodityMeasure   => commodityMeasureClearancePreviousPage
    case PackageInformation => packageInformationClearancePreviousPage
  }

  private def consigneeDetailsSupplementaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isDeclarantExporter)
      controllers.declaration.routes.DeclarantExporterController.displayPage(mode)
    else
      controllers.declaration.routes.RepresentativeStatusController.displayPage(mode)

  private def commodityMeasureClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
      if (cacheModel.isExs)
        controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(mode, itemId)
      else
        controllers.declaration.routes.CommodityDetailsController.displayPage(mode, itemId)
    else
      controllers.declaration.routes.PackageInformationSummaryController.displayPage(mode, itemId)

  private def packageInformationClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.isExs)
      controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(mode, itemId)
    else
      controllers.declaration.routes.CommodityDetailsController.displayPage(mode, itemId)

  private def documentsProducedPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId)
    else
      controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(mode, itemId)

  private def exporterDetailsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords)
      controllers.declaration.routes.PersonPresentingGoodsDetailsController.displayPage(mode)
    else
      controllers.declaration.routes.DeclarantExporterController.displayPage(mode)

  private def carrierDetailsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      controllers.declaration.routes.DeclarantExporterController.displayPage(mode)
    else
      controllers.declaration.routes.RepresentativeStatusController.displayPage(mode)

  private def carrierDetailsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (!cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      controllers.declaration.routes.RepresentativeStatusController.displayPage(mode)
    else {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        controllers.declaration.routes.ConsignorDetailsController.displayPage(mode)
      else
        controllers.declaration.routes.ConsignorEoriNumberController.displayPage(mode)
    }

  private def consigneeDetailsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isExs)
      controllers.declaration.routes.CarrierDetailsController.displayPage(mode)
    else {
      if (cacheModel.isDeclarantExporter)
        controllers.declaration.routes.IsExsController.displayPage(mode)
      else
        controllers.declaration.routes.RepresentativeStatusController.displayPage(mode)
    }

  private def representativeAgentClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isExs) {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        controllers.declaration.routes.ConsignorDetailsController.displayPage(mode)
      else
        controllers.declaration.routes.ConsignorEoriNumberController.displayPage(mode)
    } else {
      controllers.declaration.routes.IsExsController.displayPage(mode)
    }

  private def isExsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isDeclarantExporter) exporterDetailsClearancePreviousPage(cacheModel, mode)
    else controllers.declaration.routes.ExporterDetailsController.displayPage(mode)

  def backLink(page: DeclarationPage, mode: Mode)(implicit request: JourneyRequest[_]): Call =
    mode match {
      case Mode.ErrorFix if (request.sourceDecId.isDefined) => controllers.routes.RejectedNotificationsController.displayPage(request.sourceDecId.get)
      case Mode.ErrorFix                                    => controllers.routes.SubmissionsController.displayListOfSubmissions()
      case Mode.Change                                      => controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      case Mode.ChangeAmend                                 => controllers.declaration.routes.SummaryController.displayPage(Mode.Amend)
      case Mode.Draft                                       => controllers.declaration.routes.SummaryController.displayPage(Mode.Draft)
      case _ =>
        val specific = request.declarationType match {
          case STANDARD      => standardCacheDependent.orElse(standard)
          case SUPPLEMENTARY => supplementaryCacheDependent.orElse(supplementary)
          case SIMPLIFIED    => simplifiedCacheDependent.orElse(simplified)
          case OCCASIONAL    => occasionalCacheDependent.orElse(occasional)
          case CLEARANCE     => clearanceCacheDependent.orElse(clearance)
        }

        commonCacheDependent.orElse(common).orElse(specific)(page) match {
          case mapping: (Mode => Call)                       => mapping(mode)
          case mapping: ((ExportsDeclaration, Mode) => Call) => mapping(request.cacheModel, mode)
        }
    }

  def backLink(page: DeclarationPage, mode: Mode, itemId: ItemId)(implicit request: JourneyRequest[_]): Call =
    mode match {
      case Mode.ErrorFix if (request.sourceDecId.isDefined) => controllers.routes.RejectedNotificationsController.displayPage(request.sourceDecId.get)
      case Mode.ErrorFix                                    => controllers.routes.SubmissionsController.displayListOfSubmissions()
      case Mode.Change                                      => controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      case Mode.ChangeAmend                                 => controllers.declaration.routes.SummaryController.displayPage(Mode.Amend)
      case Mode.Draft                                       => controllers.declaration.routes.SummaryController.displayPage(Mode.Draft)
      case _ =>
        val specific = request.declarationType match {
          case STANDARD      => standardCacheItemDependent.orElse(standardItemPage)
          case SUPPLEMENTARY => supplementaryCacheItemDependent.orElse(supplementaryItemPage)
          case SIMPLIFIED    => simplifiedCacheItemDependent.orElse(simplifiedItemPage)
          case OCCASIONAL    => occasionalCacheItemDependent.orElse(occasionalItemPage)
          case CLEARANCE     => clearanceCacheItemDependent.orElse(clearanceItemPage)
        }
        commonCacheItemDependent.orElse(commonItem).orElse(specific)(page) match {
          case mapping: ((Mode, String) => Call)                     => mapping(mode, itemId.id)
          case mapping: ((ExportsDeclaration, Mode, String) => Call) => mapping(request.cacheModel, mode, itemId.id)
        }
    }
}
