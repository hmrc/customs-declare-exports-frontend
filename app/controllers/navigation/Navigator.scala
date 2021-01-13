/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.declaration.routes
import controllers.util.{Add, FormAction, Remove, SaveAndReturn}
import forms.Choice.AllowedChoiceValues
import forms.declaration._
import forms.declaration.RoutingQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingQuestionPage}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec
import forms.declaration.additionaldocuments.{DocumentsProduced, DocumentsProducedSummary}
import forms.declaration.carrier.{CarrierDetails, CarrierEoriNumber}
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.countries.Countries.{DestinationCountryPage, OriginationCountryPage}
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.declaration.officeOfExit.{OfficeOfExitInsideUK, OfficeOfExitOutsideUK}
import forms.declaration.removals.RemoveItem
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
      .Redirect(routes.ConfirmationController.displayDraftConfirmation())
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
    case DeclarantDetails            => routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => routes.DepartureTransportController.displayPage
    case TransportPayment            => routes.BorderTransportController.displayPage
    case ContainerFirst              => routes.TransportPaymentController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case OriginationCountryPage      => routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => routes.OriginationCountryController.displayPage
    case RoutingQuestionPage         => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case DocumentSummary             => routes.NatureOfTransactionController.displayPage
    case GoodsLocationForm           => routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DeclarationHolderRequired   => routes.AdditionalActorsSummaryController.displayPage
    case DeclarationSummaryHolder    => routes.AdditionalActorsSummaryController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case InlandModeOfTransportCode   => routes.SupervisingCustomsOfficeController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case TotalPackageQuantity        => routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }
  val standardItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => routes.StatisticalValueController.displayPage
    case AdditionalInformationRequired => routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => routes.CommodityMeasureController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }

  val clearance: PartialFunction[DeclarationPage, Mode => Call] = {
    case EntryIntoDeclarantsRecords   => routes.ConsignmentReferencesController.displayPage
    case ExporterDetails              => routes.ExporterEoriNumberController.displayPage
    case DeclarantDetails             => routes.EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => routes.EntryIntoDeclarantsRecordsController.displayPage
    case TransportPayment             => routes.DepartureTransportController.displayPage
    case ContainerFirst               => routes.TransportPaymentController.displayPage
    case ContainerAdd                 => routes.TransportContainerController.displayContainerSummary
    case DestinationCountryPage       => routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage          => routes.DestinationCountryController.displayPage
    case RemoveCountryPage            => routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage            => routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm            => routes.DestinationCountryController.displayPage
    case DeclarationHolderRequired    => routes.ConsigneeDetailsController.displayPage
    case DeclarationSummaryHolder     => routes.ConsigneeDetailsController.displayPage
    case ConsignorEoriNumber          => routes.IsExsController.displayPage
    case ConsignorDetails             => routes.ConsignorEoriNumberController.displayPage
    case OfficeOfExitInsideUK         => routes.LocationController.displayPage
    case OfficeOfExitOutsideUK        => routes.OfficeOfExitController.displayPage
    case DepartureTransport           => routes.SupervisingCustomsOfficeController.displayPage
    case TotalPackageQuantity         => routes.OfficeOfExitController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case AdditionalInformationRequired => routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => routes.CommodityMeasureController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val supplementary: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => routes.DepartureTransportController.displayPage
    case ContainerFirst              => routes.BorderTransportController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case OriginationCountryPage      => routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => routes.OriginationCountryController.displayPage
    case GoodsLocationForm           => routes.DestinationCountryController.displayPage
    case DocumentSummary             => routes.NatureOfTransactionController.displayPage
    case OfficeOfExitInsideUK        => routes.LocationController.displayPage
    case OfficeOfExitOutsideUK       => routes.OfficeOfExitController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DeclarationHolderRequired   => routes.AdditionalActorsSummaryController.displayPage
    case DeclarationSummaryHolder    => routes.AdditionalActorsSummaryController.displayPage
    case InlandModeOfTransportCode   => routes.SupervisingCustomsOfficeController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case TotalPackageQuantity        => routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }
  val supplementaryItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => routes.StatisticalValueController.displayPage
    case AdditionalInformationRequired => routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => routes.CommodityMeasureController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val simplified: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case TransportPayment            => routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => routes.TransportPaymentController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case OfficeOfExitOutsideUK       => routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DeclarationHolderRequired   => routes.AdditionalActorsSummaryController.displayPage
    case DeclarationSummaryHolder    => routes.AdditionalActorsSummaryController.displayPage
    case InlandModeOfTransportCode   => routes.SupervisingCustomsOfficeController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case TotalPackageQuantity        => routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }
  val simplifiedItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => routes.NactCodeSummaryController.displayPage
    case AdditionalInformationRequired => routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => routes.PackageInformationSummaryController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val occasional: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case TransportPayment            => routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => routes.TransportPaymentController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case OfficeOfExitOutsideUK       => routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DeclarationHolderRequired   => routes.AdditionalActorsSummaryController.displayPage
    case DeclarationSummaryHolder    => routes.AdditionalActorsSummaryController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case InlandModeOfTransportCode   => routes.SupervisingCustomsOfficeController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case TotalPackageQuantity        => routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => routes.NactCodeSummaryController.displayPage
    case AdditionalInformationRequired => routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => routes.PackageInformationSummaryController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val common: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarationChoice =>
      _ =>
        controllers.routes.ChoiceController.displayPage(Some(Choice(AllowedChoiceValues.CreateDec)))
    case DispatchLocation                     => routes.DeclarationChoiceController.displayPage
    case ConsignmentReferences                => routes.AdditionalDeclarationTypeController.displayPage
    case DeclarantIsExporter                  => routes.DeclarantDetailsController.displayPage
    case RepresentativeEntity                 => routes.RepresentativeAgentController.displayPage
    case RepresentativeStatus                 => routes.RepresentativeEntityController.displayPage
    case OfficeOfExitInsideUK                 => routes.LocationController.displayPage
    case OfficeOfExitOutsideUK                => routes.OfficeOfExitController.displayPage
    case AdditionalDeclarationTypeStandardDec => routes.DispatchLocationController.displayPage
    case NatureOfTransaction                  => routes.TotalPackageQuantityController.displayPage
    case ProcedureCodes                       => routes.ItemsSummaryController.displayItemsSummaryPage
    case ExportItem                           => routes.PreviousDocumentsSummaryController.displayPage
    case RemoveItem                           => routes.ItemsSummaryController.displayItemsSummaryPage
    case DocumentChangeOrRemove               => routes.PreviousDocumentsSummaryController.displayPage
    case TransportLeavingTheBorder            => routes.ItemsSummaryController.displayItemsSummaryPage
    case CarrierDetails                       => routes.CarrierEoriNumberController.displayPage
  }

  val commonItem: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case FiscalInformation                 => routes.ProcedureCodesController.displayPage
    case AdditionalFiscalReferencesSummary => routes.ProcedureCodesController.displayPage
    case CommodityDetails                  => routes.FiscalInformationController.displayPage(_, _, fastForward = true)
    case UNDangerousGoodsCode              => routes.CommodityDetailsController.displayPage
    case TaricCode                         => routes.TaricCodeSummaryController.displayPage
    case TaricCodeFirst                    => routes.CusCodeController.displayPage
    case StatisticalValue                  => routes.NactCodeSummaryController.displayPage
  }

  val commonCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case TotalNumberOfItems       => totalNumberOfItemsPreviousPage
    case DeclarationHolder        => declarationHolderPreviousPage
    case SupervisingCustomsOffice => supervisingCustomsOfficePreviousPage
    case WarehouseIdentification  => warehouseIdentificationPreviousPage
  }

  val commonCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = {
    case DocumentsProducedSummary  => documentsProducedSummaryPreviousPage
    case DocumentsProduced         => documentsProducedPreviousPage
    case AdditionalInformation     => additionalInformationPreviousPage
    case AdditionalFiscalReference => additionalFiscalReferencesPreviousPage
  }

  val standardCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierEoriNumber   => carrierEoriNumberPreviousPage
    case Document            => previousDocumentsPreviousPageDefault
    case ConsigneeDetails    => consigneeDetailsPreviousPage
    case RepresentativeAgent => representativeAgentPreviousPage
  }

  val standardCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val supplementaryCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case ConsigneeDetails    => consigneeDetailsSupplementaryPreviousPage
    case Document            => previousDocumentsPreviousPageDefault
    case RepresentativeAgent => representativeAgentPreviousPage
  }

  val supplementaryCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val simplifiedCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierEoriNumber   => carrierEoriNumberPreviousPage
    case Document            => previousDocumentsPreviousPage
    case DocumentSummary     => previousDocumentsSummaryPreviousPage
    case ConsigneeDetails    => consigneeDetailsPreviousPage
    case RepresentativeAgent => representativeAgentPreviousPage
  }

  val simplifiedCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val occasionalCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierEoriNumber   => carrierEoriNumberPreviousPage
    case Document            => previousDocumentsPreviousPage
    case DocumentSummary     => previousDocumentsSummaryPreviousPage
    case ConsigneeDetails    => consigneeDetailsPreviousPage
    case RepresentativeAgent => representativeAgentPreviousPage
  }

  val occasionalCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val clearanceCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierEoriNumber   => carrierEoriNumberClearancePreviousPage
    case ExporterEoriNumber  => exporterEoriNumberClearancePreviousPage
    case ConsigneeDetails    => consigneeDetailsClearancePreviousPage
    case RepresentativeAgent => representativeAgentClearancePreviousPage
    case IsExs               => isExsClearancePreviousPage
    case Document            => previousDocumentsPreviousPage
    case DocumentSummary     => previousDocumentsSummaryPreviousPage
  }

  val clearanceCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = {
    case CommodityMeasure   => commodityMeasureClearancePreviousPage
    case PackageInformation => packageInformationClearancePreviousPage
  }

  private def previousDocumentsPreviousPageDefault(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.hasPreviousDocuments)
      routes.PreviousDocumentsSummaryController.displayPage(mode)
    else routes.NatureOfTransactionController.displayPage(mode)

  private def consigneeDetailsSupplementaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isDeclarantExporter)
      routes.DeclarantExporterController.displayPage(mode)
    else
      routes.RepresentativeStatusController.displayPage(mode)

  private def commodityMeasureClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
      if (cacheModel.isExs)
        routes.UNDangerousGoodsCodeController.displayPage(mode, itemId)
      else
        routes.CommodityDetailsController.displayPage(mode, itemId)
    else
      routes.PackageInformationSummaryController.displayPage(mode, itemId)

  private def packageInformationClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.isExs)
      routes.UNDangerousGoodsCodeController.displayPage(mode, itemId)
    else
      routes.CommodityDetailsController.displayPage(mode, itemId)

  private def documentsProducedSummaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      routes.AdditionalInformationController.displayPage(mode, itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(mode, itemId)

  private def documentsProducedPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).exists(_.documents.nonEmpty))
      routes.DocumentsProducedController.displayPage(mode, itemId)
    else
      documentsProducedSummaryPreviousPage(cacheModel, mode, itemId)

  private def additionalInformationPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      routes.AdditionalInformationController.displayPage(mode, itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(mode, itemId)

  private def additionalFiscalReferencesPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).exists(_.references.nonEmpty))
      routes.AdditionalFiscalReferencesController.displayPage(mode, itemId)
    else
      routes.FiscalInformationController.displayPage(mode, itemId)

  private def previousDocumentsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.hasPreviousDocuments)
      routes.PreviousDocumentsSummaryController.displayPage(mode)
    else if (cacheModel.locations.isOfficeOfExitInUk)
      routes.OfficeOfExitController.displayPage(mode)
    else routes.OfficeOfExitOutsideUkController.displayPage(mode)

  private def previousDocumentsSummaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.locations.isOfficeOfExitInUk)
      routes.OfficeOfExitController.displayPage(mode)
    else routes.OfficeOfExitOutsideUkController.displayPage(mode)

  private def exporterEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords)
      routes.PersonPresentingGoodsDetailsController.displayPage(mode)
    else
      routes.DeclarantExporterController.displayPage(mode)

  private def carrierEoriNumberPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      routes.DeclarantExporterController.displayPage(mode)
    else
      routes.RepresentativeStatusController.displayPage(mode)

  private def carrierEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (!cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      routes.RepresentativeStatusController.displayPage(mode)
    else {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        routes.ConsignorDetailsController.displayPage(mode)
      else
        routes.ConsignorEoriNumberController.displayPage(mode)
    }

  private def consigneeDetailsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.carrierDetails.flatMap(_.details.eori).isEmpty)
      routes.CarrierDetailsController.displayPage(mode)
    else
      routes.CarrierEoriNumberController.displayPage(mode)

  private def consigneeDetailsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isExs)
      consigneeDetailsPreviousPage(cacheModel, mode)
    else {
      if (cacheModel.isDeclarantExporter)
        routes.IsExsController.displayPage(mode)
      else
        routes.RepresentativeStatusController.displayPage(mode)
    }

  private def representativeAgentClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isExs) {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        routes.ConsignorDetailsController.displayPage(mode)
      else
        routes.ConsignorEoriNumberController.displayPage(mode)
    } else {
      routes.IsExsController.displayPage(mode)
    }

  private def isExsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isDeclarantExporter)
      exporterEoriNumberClearancePreviousPage(cacheModel, mode)
    else if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage(mode)
    else routes.ExporterDetailsController.displayPage(mode)

  private def totalNumberOfItemsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.locations.isOfficeOfExitInUk)
      routes.OfficeOfExitController.displayPage(mode)
    else
      routes.OfficeOfExitOutsideUkController.displayPage(mode)

  private def declarationHolderPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarationHoldersData.exists(_.holders.nonEmpty))
      routes.DeclarationHolderController.displayPage(mode)
    else
      routes.DeclarationHolderRequiredController.displayPage(mode)

  private def warehouseIdentificationPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    cacheModel.`type` match {
      case OCCASIONAL | SIMPLIFIED => routes.ItemsSummaryController.displayItemsSummaryPage(mode)
      case _                       => routes.TransportLeavingTheBorderController.displayPage(mode)
    }

  private def representativeAgentPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage(mode)
    else routes.ExporterDetailsController.displayPage(mode)

  private def supervisingCustomsOfficePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.requiresWarehouseId || cacheModel.`type` == CLEARANCE)
      routes.WarehouseIdentificationController.displayPage(mode)
    else
      warehouseIdentificationPreviousPage(cacheModel, mode)

  def backLink(page: DeclarationPage, mode: Mode)(implicit request: JourneyRequest[_]): Call =
    mode match {
      case Mode.ErrorFix if (request.sourceDecId.isDefined) => controllers.routes.RejectedNotificationsController.displayPage(request.sourceDecId.get)
      case Mode.ErrorFix                                    => controllers.routes.SubmissionsController.displayListOfSubmissions()
      case Mode.Change                                      => routes.SummaryController.displayPage(Mode.Normal)
      case Mode.ChangeAmend                                 => routes.SummaryController.displayPage(Mode.Amend)
      case Mode.Draft                                       => routes.SummaryController.displayPage(Mode.Draft)
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
      case Mode.Change                                      => routes.SummaryController.displayPage(Mode.Normal)
      case Mode.ChangeAmend                                 => routes.SummaryController.displayPage(Mode.Amend)
      case Mode.Draft                                       => routes.SummaryController.displayPage(Mode.Draft)
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
