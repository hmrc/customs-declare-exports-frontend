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
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec
import forms.declaration.additionaldocuments.{DocumentsProduced, DocumentsProducedSummary}
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.countries.Countries.{DestinationCountryPage, OriginationCountryPage}
import forms.declaration.DeclarationSummaryHolder
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.declaration.officeOfExit.{OfficeOfExitInsideUK, OfficeOfExitOutsideUK}
import forms.declaration.removals.RemoveItem
import forms.{Choice, DeclarationPage}
import forms.declaration.carrier.{CarrierDetails, CarrierEoriNumber}
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
    case ExporterEoriNumber          => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ExporterDetails             => controllers.declaration.routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => controllers.declaration.routes.DepartureTransportController.displayPage
    case TransportPayment            => controllers.declaration.routes.BorderTransportController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case OriginationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.OriginationCountryController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case DocumentSummary             => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case DeclarationSummaryHolder    => controllers.declaration.routes.AdditionalActorsSummaryController.displayPage
    case DepartureTransport          => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }
  val standardItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformationRequired => controllers.declaration.routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode                       => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }

  val clearance: PartialFunction[DeclarationPage, Mode => Call] = {
    case EntryIntoDeclarantsRecords   => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterDetails              => controllers.declaration.routes.ExporterEoriNumberController.displayPage
    case DeclarantDetails             => controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage
    case TransportPayment             => controllers.declaration.routes.DepartureTransportController.displayPage
    case ContainerFirst               => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                 => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case DestinationCountryPage       => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage          => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage            => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage            => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm            => controllers.declaration.routes.DestinationCountryController.displayPage
    case DeclarationSummaryHolder     => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case ConsignorEoriNumber          => controllers.declaration.routes.IsExsController.displayPage
    case ConsignorDetails             => controllers.declaration.routes.ConsignorEoriNumberController.displayPage
    case OfficeOfExitInsideUK         => controllers.declaration.routes.LocationController.displayPage
    case OfficeOfExitOutsideUK        => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DepartureTransport           => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case TotalPackageQuantity         => controllers.declaration.routes.OfficeOfExitController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case AdditionalInformationRequired => controllers.declaration.routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => controllers.declaration.routes.CommodityMeasureController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val supplementary: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ExporterDetails             => controllers.declaration.routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => controllers.declaration.routes.DepartureTransportController.displayPage
    case ContainerFirst              => controllers.declaration.routes.BorderTransportController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case OriginationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.OriginationCountryController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.DestinationCountryController.displayPage
    case DocumentSummary             => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OfficeOfExitInsideUK        => controllers.declaration.routes.LocationController.displayPage
    case OfficeOfExitOutsideUK       => controllers.declaration.routes.OfficeOfExitController.displayPage
    case AdditionalActorsSummary     => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case DeclarationSummaryHolder    => controllers.declaration.routes.AdditionalActorsSummaryController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case DepartureTransport          => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }
  val supplementaryItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformationRequired => controllers.declaration.routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode                       => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val simplified: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ExporterDetails             => controllers.declaration.routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TransportPayment            => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case OfficeOfExitOutsideUK       => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case DeclarationSummaryHolder    => controllers.declaration.routes.AdditionalActorsSummaryController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case DepartureTransport          => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }
  val simplifiedItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case AdditionalInformationRequired => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case CusCode                       => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val occasional: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ExporterEoriNumber          => controllers.declaration.routes.DeclarantExporterController.displayPage
    case ExporterDetails             => controllers.declaration.routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case TransportPayment            => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case OfficeOfExitOutsideUK       => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case DeclarationSummaryHolder    => controllers.declaration.routes.AdditionalActorsSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case DepartureTransport          => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case TotalPackageQuantity        => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation            => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case AdditionalInformationRequired => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case CusCode                       => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => controllers.declaration.routes.NactCodeSummaryController.displayPage
    case NactCodeFirst                 => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case CommodityMeasure              => controllers.declaration.routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
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
    case NatureOfTransaction                  => controllers.declaration.routes.TotalPackageQuantityController.displayPage
    case ProcedureCodes                       => controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage
    case ExportItem                           => controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage
    case RemoveItem                           => controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage
    case DocumentChangeOrRemove               => controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage
    case TransportLeavingTheBorder            => controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage
    case CarrierDetails                       => controllers.declaration.routes.CarrierEoriNumberController.displayPage
  }

  val commonItem: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case FiscalInformation                 => controllers.declaration.routes.ProcedureCodesController.displayPage
    case AdditionalFiscalReferencesSummary => controllers.declaration.routes.ProcedureCodesController.displayPage
    case CommodityDetails                  => controllers.declaration.routes.FiscalInformationController.displayPage(_, _, fastForward = true)
    case UNDangerousGoodsCode              => controllers.declaration.routes.CommodityDetailsController.displayPage
    case TaricCode                         => controllers.declaration.routes.TaricCodeSummaryController.displayPage
    case TaricCodeFirst                    => controllers.declaration.routes.CusCodeController.displayPage
    case StatisticalValue                  => controllers.declaration.routes.NactCodeSummaryController.displayPage
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
      controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(mode)
    else controllers.declaration.routes.NatureOfTransactionController.displayPage(mode)

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

  private def documentsProducedSummaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId)
    else
      controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(mode, itemId)

  private def documentsProducedPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).exists(_.documents.nonEmpty))
      controllers.declaration.routes.DocumentsProducedController.displayPage(mode, itemId)
    else
      documentsProducedSummaryPreviousPage(cacheModel, mode, itemId)

  private def additionalInformationPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId)
    else
      controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(mode, itemId)

  private def additionalFiscalReferencesPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).exists(_.references.nonEmpty))
      controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(mode, itemId)
    else
      controllers.declaration.routes.FiscalInformationController.displayPage(mode, itemId)

  private def previousDocumentsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.hasPreviousDocuments)
      controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage(mode)
    else if (cacheModel.locations.isOfficeOfExitInUk)
      controllers.declaration.routes.OfficeOfExitController.displayPage(mode)
    else controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage(mode)

  private def previousDocumentsSummaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.locations.isOfficeOfExitInUk)
      controllers.declaration.routes.OfficeOfExitController.displayPage(mode)
    else controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage(mode)

  private def exporterEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords)
      controllers.declaration.routes.PersonPresentingGoodsDetailsController.displayPage(mode)
    else
      controllers.declaration.routes.DeclarantExporterController.displayPage(mode)

  private def carrierEoriNumberPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      controllers.declaration.routes.DeclarantExporterController.displayPage(mode)
    else
      controllers.declaration.routes.RepresentativeStatusController.displayPage(mode)

  private def carrierEoriNumberClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (!cacheModel.parties.declarantIsExporter.exists(_.isExporter))
      controllers.declaration.routes.RepresentativeStatusController.displayPage(mode)
    else {
      if (cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined)
        controllers.declaration.routes.ConsignorDetailsController.displayPage(mode)
      else
        controllers.declaration.routes.ConsignorEoriNumberController.displayPage(mode)
    }

  private def consigneeDetailsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.carrierDetails.flatMap(_.details.eori).isEmpty)
      controllers.declaration.routes.CarrierDetailsController.displayPage(mode)
    else
      controllers.declaration.routes.CarrierEoriNumberController.displayPage(mode)

  private def consigneeDetailsClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isExs)
      consigneeDetailsPreviousPage(cacheModel, mode)
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
    if (cacheModel.isDeclarantExporter)
      exporterEoriNumberClearancePreviousPage(cacheModel, mode)
    else if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      controllers.declaration.routes.ExporterEoriNumberController.displayPage(mode)
    else controllers.declaration.routes.ExporterDetailsController.displayPage(mode)

  private def totalNumberOfItemsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.locations.isOfficeOfExitInUk)
      controllers.declaration.routes.OfficeOfExitController.displayPage(mode)
    else
      controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage(mode)

  private def declarationHolderPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarationHoldersData.exists(_.holders.nonEmpty))
      controllers.declaration.routes.DeclarationHolderController.displayPage(mode)
    else
      cacheModel.`type` match {
        case CLEARANCE => controllers.declaration.routes.ConsigneeDetailsController.displayPage(mode)
        case _         => controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(mode)
      }

  private def warehouseIdentificationPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    cacheModel.`type` match {
      case OCCASIONAL | SIMPLIFIED => controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage(mode)
      case _                       => controllers.declaration.routes.TransportLeavingTheBorderController.displayPage(mode)
    }

  private def representativeAgentPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      controllers.declaration.routes.ExporterEoriNumberController.displayPage(mode)
    else controllers.declaration.routes.ExporterDetailsController.displayPage(mode)

  private def supervisingCustomsOfficePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.requiresWarehouseId || cacheModel.`type` == CLEARANCE)
      controllers.declaration.routes.WarehouseIdentificationController.displayPage(mode)
    else
      warehouseIdentificationPreviousPage(cacheModel, mode)

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
