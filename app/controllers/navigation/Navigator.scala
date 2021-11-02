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
import controllers.declaration.InlandTransportDetailsController.getSkipOtherTransportPagesValue
import controllers.helpers.SupervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified
import controllers.helpers._
import forms.Choice.AllowedChoiceValues
import forms.common.YesNoAnswer
import forms.declaration.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec
import forms.declaration.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.declaration.carrier.{CarrierDetails, CarrierEoriNumber}
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.countries.Countries.{DestinationCountryPage, OriginationCountryPage}
import forms.declaration.declarationHolder.{DeclarationHolder, DeclarationHolderRequired, DeclarationHolderSummary}
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.procedurecodes.{AdditionalProcedureCode, ProcedureCode}
import forms.declaration.removals.RemoveItem
import forms.{Choice, DeclarationPage}
import models.DeclarationType._
import models.Mode.ErrorFix
import models.declaration.ExportItem
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import models.{ExportsDeclaration, Mode}
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject

class Navigator @Inject()(appConfig: AppConfig, auditService: AuditService) {

  def continueTo(mode: Mode, factory: Mode => Call, isErrorFixInProgress: Boolean = false)(
    implicit req: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Result =
    (mode, FormAction.bindFromRequest) match {
      case (ErrorFix, formAction) => handleErrorFixMode(factory, formAction, isErrorFixInProgress)
      case (_, SaveAndReturn) =>
        auditService.auditAllPagesUserInput(AuditTypes.SaveAndReturnSubmission, req.cacheModel)
        goToDraftDeclaration
      case _ => Results.Redirect(factory(mode))
    }

  private def goToDraftDeclaration(implicit req: JourneyRequest[_]): Result = {
    val updatedDateTime = req.cacheModel.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds)
    Results
      .Redirect(routes.DraftDeclarationController.displayPage)
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

// scalastyle:off
object Navigator {

  val standard: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => routes.DepartureTransportController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case DestinationCountryPage      => routes.OriginationCountryController.displayPage
    case RoutingCountryQuestionPage  => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case DocumentSummary             => routes.NatureOfTransactionController.displayPage
    case GoodsLocationForm           => routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
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
    case ConsignmentReferences        => routes.AdditionalDeclarationTypeController.displayPage
    case ExporterDetails              => routes.ExporterEoriNumberController.displayPage
    case DeclarantDetails             => routes.EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => routes.EntryIntoDeclarantsRecordsController.displayPage
    case DeclarantIsExporter          => routes.DeclarantDetailsController.displayPage
    case ExpressConsignment           => routes.DepartureTransportController.displayPage
    case ContainerAdd                 => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage   => routes.DestinationCountryController.displayPage
    case RemoveCountryPage            => routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage            => routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm            => routes.DestinationCountryController.displayPage
    case ConsignorEoriNumber          => routes.IsExsController.displayPage
    case ConsignorDetails             => routes.ConsignorEoriNumberController.displayPage
    case OfficeOfExit                 => routes.LocationController.displayPage
    case TotalPackageQuantity         => routes.OfficeOfExitController.displayPage
    case DocumentSummary              => routes.OfficeOfExitController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case AdditionalInformationRequired => routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => routes.CommodityMeasureController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val supplementary: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case BorderTransport             => routes.DepartureTransportController.displayPage
    case ContainerFirst              => routes.BorderTransportController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case DestinationCountryPage      => routes.OriginationCountryController.displayPage
    case GoodsLocationForm           => routes.DestinationCountryController.displayPage
    case DocumentSummary             => routes.NatureOfTransactionController.displayPage
    case OfficeOfExit                => routes.LocationController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
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
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage  => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case TotalPackageQuantity        => routes.TotalNumberOfItemsController.displayPage
    case DocumentSummary             => routes.OfficeOfExitController.displayPage
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
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences       => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber          => routes.DeclarantExporterController.displayPage
    case ExporterDetails             => routes.ExporterEoriNumberController.displayPage
    case DeclarationAdditionalActors => routes.ConsigneeDetailsController.displayPage
    case ContainerAdd                => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage  => routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocationForm           => routes.RoutingCountriesSummaryController.displayPage
    case AdditionalActorsSummary     => routes.ConsigneeDetailsController.displayPage
    case ChangeCountryPage           => routes.RoutingCountriesSummaryController.displayPage
    case DepartureTransport          => routes.InlandTransportDetailsController.displayPage
    case TotalPackageQuantity        => routes.TotalNumberOfItemsController.displayPage
    case DocumentSummary             => routes.OfficeOfExitController.displayPage
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
    case LinkDucrToMucr                       => routes.ConsignmentReferencesController.displayPage
    case Mucr                                 => routes.LinkDucrToMucrController.displayPage
    case RepresentativeEntity                 => routes.RepresentativeAgentController.displayPage
    case RepresentativeStatus                 => routes.RepresentativeEntityController.displayPage
    case OfficeOfExit                         => routes.LocationController.displayPage
    case AdditionalDeclarationTypeStandardDec => routes.DeclarationChoiceController.displayPage
    case NatureOfTransaction                  => routes.TotalPackageQuantityController.displayPage
    case ProcedureCode                        => routes.ItemsSummaryController.displayItemsSummaryPage
    case ExportItem                           => routes.PreviousDocumentsSummaryController.displayPage
    case RemoveItem                           => routes.ItemsSummaryController.displayItemsSummaryPage
    case DocumentChangeOrRemove               => routes.PreviousDocumentsSummaryController.displayPage
    case TransportLeavingTheBorder            => routes.ItemsSummaryController.displayItemsSummaryPage
    case TransportPayment                     => routes.ExpressConsignmentController.displayPage
    case CarrierDetails                       => routes.CarrierEoriNumberController.displayPage
    case TotalNumberOfItems                   => routes.OfficeOfExitController.displayPage
  }

  val commonItem: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case AdditionalProcedureCode           => routes.ProcedureCodesController.displayPage
    case FiscalInformation                 => routes.AdditionalProcedureCodesController.displayPage
    case AdditionalFiscalReferencesSummary => routes.AdditionalProcedureCodesController.displayPage
    case CommodityDetails                  => routes.FiscalInformationController.displayPage(_, _, fastForward = true)
    case UNDangerousGoodsCode              => routes.CommodityDetailsController.displayPage
    case TaricCode                         => routes.TaricCodeSummaryController.displayPage
    case StatisticalValue                  => routes.NactCodeSummaryController.displayPage
  }

  val commonCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case DeclarationHolderRequired        => declarationHolderRequiredPreviousPage
    case DeclarationHolder                => declarationHolderAddPreviousPage
    case DeclarationHolderSummary         => declarationHolderSummaryPreviousPage
    case SupervisingCustomsOffice         => supervisingCustomsOfficePreviousPage
    case WarehouseIdentification          => warehouseIdentificationPreviousPage
    case AuthorisationProcedureCodeChoice => authorisationProcedureCodeChoicePreviousPage
  }

  val commonCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = {
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryPreviousPage
    case AdditionalDocument          => additionalDocumentsPreviousPage
    case AdditionalInformation       => additionalInformationPreviousPage
    case AdditionalFiscalReference   => additionalFiscalReferencesPreviousPage
    case TaricCodeFirst              => additionalTaricCodesPreviousPage
    case AdditionalDocumentsRequired => additionalDocumentsSummaryPreviousPage
  }

  val standardCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case CarrierEoriNumber         => carrierEoriNumberPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case ContainerFirst            => ifExpressConsignmentPreviousPage
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case Document                  => previousDocumentsPreviousPageDefault
    case ExpressConsignment        => inlandTransportDetailsPageOnCondition
    case InlandModeOfTransportCode => supervisingCustomsOfficePageOnCondition
    case OriginationCountryPage    => originationCountryPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
  }

  val standardCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val supplementaryCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case ConsigneeDetails          => consigneeDetailsSupplementaryPreviousPage
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case Document                  => previousDocumentsPreviousPageDefault
    case ExpressConsignment        => inlandTransportDetailsPageOnCondition
    case InlandModeOfTransportCode => supervisingCustomsOfficePageOnCondition
    case OriginationCountryPage    => originationCountryPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
  }

  val supplementaryCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val simplifiedCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case CarrierEoriNumber         => carrierEoriNumberPreviousPage
    case Document                  => previousDocumentsPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case DestinationCountryPage    => originationCountryPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
    case InlandModeOfTransportCode => supervisingCustomsOfficePageOnCondition
    case ExpressConsignment        => supervisingCustomsOfficePageOnCondition
    case ContainerFirst            => ifExpressConsignmentPreviousPage
  }

  val simplifiedCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val occasionalCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case CarrierEoriNumber         => carrierEoriNumberPreviousPage
    case Document                  => previousDocumentsPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case DestinationCountryPage    => originationCountryPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
    case InlandModeOfTransportCode => supervisingCustomsOfficePageOnCondition
    case ExpressConsignment        => supervisingCustomsOfficePageOnCondition
    case ContainerFirst            => ifExpressConsignmentPreviousPage
  }

  val occasionalCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = Map.empty

  val clearanceCacheDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode) => Call] = {
    case EntryIntoDeclarantsRecords => entryIntoDeclarantsPreviousPage
    case CarrierEoriNumber          => carrierEoriNumberClearancePreviousPage
    case ExporterEoriNumber         => exporterEoriNumberClearancePreviousPage
    case ConsigneeDetails           => consigneeDetailsClearancePreviousPage
    case DestinationCountryPage     => originationCountryPreviousPage
    case RepresentativeAgent        => representativeAgentClearancePreviousPage
    case IsExs                      => isExsClearancePreviousPage
    case Document                   => previousDocumentsPreviousPage
    case DepartureTransport         => departureTransportClearancePreviousPage
    case ContainerFirst             => ifExpressConsignmentPreviousPage
  }

  val clearanceCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, Mode, String) => Call] = {
    case CommodityMeasure   => commodityMeasureClearancePreviousPage
    case PackageInformation => packageInformationClearancePreviousPage
  }

  private def declarantIsExporterPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    cacheModel.`type` match {
      case SUPPLEMENTARY => routes.ConsignmentReferencesController.displayPage(mode)
      case _ =>
        if (cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage(mode)
        else routes.MucrController.displayPage(mode)
    }

  private def entryIntoDeclarantsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage(mode)
    else routes.MucrController.displayPage(mode)

  private def previousDocumentsPreviousPageDefault(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.hasPreviousDocuments) routes.PreviousDocumentsSummaryController.displayPage(mode)
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

  private def additionalDocumentsSummaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.itemBy(itemId).flatMap(_.additionalInformation).exists(_.items.nonEmpty))
      routes.AdditionalInformationController.displayPage(mode, itemId)
    else
      routes.AdditionalInformationRequiredController.displayPage(mode, itemId)

  private def additionalDocumentsPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.listOfAdditionalDocuments(itemId).nonEmpty)
      routes.AdditionalDocumentsController.displayPage(mode, itemId)
    else {
      if (cacheModel.isAuthCodeRequiringAdditionalDocuments) additionalDocumentsSummaryPreviousPage(cacheModel, mode, itemId)
      else routes.AdditionalDocumentsRequiredController.displayPage(mode, itemId)
    }

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
    if (cacheModel.hasPreviousDocuments) routes.PreviousDocumentsSummaryController.displayPage(mode)
    else routes.OfficeOfExitController.displayPage(mode)

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

  private def declarationHolderRequiredPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    cacheModel.`type` match {
      case CLEARANCE if !cacheModel.isEntryIntoDeclarantsRecords => routes.ConsigneeDetailsController.displayPage(mode)
      case OCCASIONAL                                            => routes.AdditionalActorsSummaryController.displayPage(mode)
      case _                                                     => routes.AuthorisationProcedureCodeChoiceController.displayPage(mode)
    }

  private def declarationHolderAddPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarationHoldersData.exists(_.holders.nonEmpty))
      routes.DeclarationHolderSummaryController.displayPage(mode)
    else declarationHolderSummaryPreviousPage(cacheModel, mode)

  private def declarationHolderSummaryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call = {
    val holdersData = cacheModel.parties.declarationHoldersData
    cacheModel.`type` match {
      case CLEARANCE if !cacheModel.isEntryIntoDeclarantsRecords => routes.ConsigneeDetailsController.displayPage(mode)
      case OCCASIONAL                                            => routes.AdditionalActorsSummaryController.displayPage(mode)
      case _ if holdersData.exists(_.isRequired.isDefined) && holdersData.exists(_.holders.isEmpty) =>
        routes.DeclarationHolderRequiredController.displayPage(mode)
      case _ => routes.AuthorisationProcedureCodeChoiceController.displayPage(mode)
    }
  }

  private def originationCountryPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.declarationHoldersData.exists(_.isRequired == YesNoAnswer.No))
      routes.DeclarationHolderRequiredController.displayPage(mode)
    else
      routes.DeclarationHolderSummaryController.displayPage(mode)

  private def warehouseIdentificationPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    cacheModel.`type` match {
      case OCCASIONAL | SIMPLIFIED => routes.ItemsSummaryController.displayItemsSummaryPage(mode)
      case _                       => routes.TransportLeavingTheBorderController.displayPage(mode)
    }

  private def representativeAgentPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.parties.exporterDetails.flatMap(_.details.eori).isDefined)
      routes.ExporterEoriNumberController.displayPage(mode)
    else routes.ExporterDetailsController.displayPage(mode)

  private def departureTransportClearancePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.isEntryIntoDeclarantsRecords) supervisingCustomsOfficePageOnCondition(cacheModel, mode)
    else routes.SupervisingCustomsOfficeController.displayPage(mode)

  private def supervisingCustomsOfficePageOnCondition(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (isConditionForAllProcedureCodesVerified(cacheModel)) supervisingCustomsOfficePreviousPage(cacheModel, mode)
    else routes.SupervisingCustomsOfficeController.displayPage(mode)

  private def supervisingCustomsOfficePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.requiresWarehouseId || cacheModel.`type` == CLEARANCE)
      routes.WarehouseIdentificationController.displayPage(mode)
    else
      warehouseIdentificationPreviousPage(cacheModel, mode)

  private def inlandTransportDetailsPageOnCondition(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (getSkipOtherTransportPagesValue(Some(cacheModel)).isDefined)
      routes.InlandTransportDetailsController.displayPage(mode)
    else
      routes.BorderTransportController.displayPage(mode)

  private def ifExpressConsignmentPreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.transport.transportPayment.nonEmpty)
      routes.TransportPaymentController.displayPage(mode)
    else
      routes.ExpressConsignmentController.displayPage(mode)

  private def additionalTaricCodesPreviousPage(cacheModel: ExportsDeclaration, mode: Mode, itemId: String): Call =
    if (cacheModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
      routes.CusCodeController.displayPage(mode, itemId)
    else
      routes.UNDangerousGoodsCodeController.displayPage(mode, itemId)

  private def authorisationProcedureCodeChoicePreviousPage(cacheModel: ExportsDeclaration, mode: Mode): Call =
    if (cacheModel.`type` == CLEARANCE && cacheModel.isEntryIntoDeclarantsRecords)
      routes.ConsigneeDetailsController.displayPage(mode)
    else
      routes.AdditionalActorsSummaryController.displayPage(mode)

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
