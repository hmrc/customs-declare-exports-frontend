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
import controllers.util.{FormAction, SaveAndReturn}
import forms.Choice.AllowedChoiceValues
import forms.declaration.RoutingQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingQuestionPage}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec
import forms.declaration.destinationCountries.DestinationCountries.{DestinationCountryPage, OriginationCountryPage}
import forms.declaration.officeOfExit.{OfficeOfExitStandard, OfficeOfExitSupplementary}
import forms.declaration.{BorderTransport, Document, PackageInformation, _}
import forms.{Choice, DeclarationPage}
import javax.inject.Inject
import models.DeclarationType._
import models.Mode
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

class Navigator @Inject()(appConfig: AppConfig, auditService: AuditService) {

  def continueTo(call: Call)(implicit req: JourneyRequest[AnyContent], hc: HeaderCarrier): Result =
    FormAction.bindFromRequest match {
      case SaveAndReturn =>
        auditService.auditAllPagesUserInput(AuditTypes.SaveAndReturnSubmission, req.cacheModel)
        goToDraftConfirmation()
      case _ => Results.Redirect(call)
    }

  def continueTo(mode: Mode, factory: Mode => Call)(implicit req: JourneyRequest[AnyContent], hc: HeaderCarrier): Result =
    redirectTo(mode.next, factory)

  def redirectTo(mode: Mode, factory: Mode => Call)(implicit req: JourneyRequest[AnyContent], hc: HeaderCarrier): Result =
    FormAction.bindFromRequest match {
      case SaveAndReturn =>
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

}

case class ItemId(id: String)

object Navigator {

  val standard: PartialFunction[DeclarationPage, Mode => Call] = {
    case ModeOfTransportCodes        => controllers.declaration.routes.InlandTransportDetailsController.displayPage
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
    case GoodsLocation               => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.CarrierDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }
  val standardItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformation => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.TaricCodeController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on standard")
  }

  val clearance: PartialFunction[DeclarationPage, Mode => Call] = {
    case ModeOfTransportCodes        => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportLeavingTheBorderController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OriginationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.OriginationCountryController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocation               => controllers.declaration.routes.DestinationCountryController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.RepresentativeDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }
  val clearanceItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformation => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode               => controllers.declaration.routes.CommodityDetailsController.displayPage
    case NactCode              => controllers.declaration.routes.CusCodeController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val supplementary: PartialFunction[DeclarationPage, Mode => Call] = {
    case ModeOfTransportCodes        => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case BorderTransport             => controllers.declaration.routes.DepartureTransportController.displayPage
    case ContainerFirst              => controllers.declaration.routes.BorderTransportController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OriginationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.OriginationCountryController.displayPage
    case GoodsLocation               => controllers.declaration.routes.DestinationCountryController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.RepresentativeDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }
  val supplementaryItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.StatisticalValueController.displayPage
    case AdditionalInformation => controllers.declaration.routes.CommodityMeasureController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.TaricCodeController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val simplified: PartialFunction[DeclarationPage, Mode => Call] = {
    case ModeOfTransportCodes        => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case TransportPayment            => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocation               => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.CarrierDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }
  val simplifiedItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.NactCodeController.displayPage
    case AdditionalInformation => controllers.declaration.routes.PackageInformationController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.TaricCodeController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val occasional: PartialFunction[DeclarationPage, Mode => Call] = {
    case ModeOfTransportCodes        => controllers.declaration.routes.InlandTransportDetailsController.displayPage
    case TransportPayment            => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case ContainerFirst              => controllers.declaration.routes.TransportPaymentController.displayPage
    case ContainerAdd                => controllers.declaration.routes.TransportContainerController.displayContainerSummary
    case Document                    => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage      => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage         => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case GoodsLocation               => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage           => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case SupervisingCustomsOffice    => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode   => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification     => controllers.declaration.routes.ItemsSummaryController.displayPage
    case DeclarationAdditionalActors => controllers.declaration.routes.CarrierDetailsController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation    => controllers.declaration.routes.NactCodeController.displayPage
    case AdditionalInformation => controllers.declaration.routes.PackageInformationController.displayPage
    case CusCode               => controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage
    case NactCode              => controllers.declaration.routes.TaricCodeController.displayPage
    case page                  => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val common: PartialFunction[DeclarationPage, Mode => Call] = {
    case DeclarationChoice =>
      _ =>
        controllers.routes.ChoiceController.displayPage(Some(Choice(AllowedChoiceValues.CreateDec)))
    case DispatchLocation =>
      controllers.declaration.routes.DeclarationChoiceController.displayPage
    case ConsignmentReferences                            => controllers.declaration.routes.AdditionalDeclarationTypeController.displayPage
    case ExporterDetails                                  => controllers.declaration.routes.ConsignmentReferencesController.displayPage
    case ConsigneeDetails                                 => controllers.declaration.routes.ExporterDetailsController.displayPage
    case DeclarantDetails                                 => controllers.declaration.routes.ConsigneeDetailsController.displayPage
    case RepresentativeDetails                            => controllers.declaration.routes.DeclarantDetailsController.displayPage
    case CarrierDetails                                   => controllers.declaration.routes.RepresentativeDetailsController.displayPage
    case DeclarationHolder                                => controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage
    case OfficeOfExitStandard | OfficeOfExitSupplementary => controllers.declaration.routes.LocationController.displayPage
    case AdditionalDeclarationTypeStandardDec             => controllers.declaration.routes.DispatchLocationController.displayPage
    case TotalNumberOfItems                               => controllers.declaration.routes.OfficeOfExitController.displayPage
    case NatureOfTransaction                              => controllers.declaration.routes.TotalNumberOfItemsController.displayPage
    case ProcedureCodes                                   => controllers.declaration.routes.ItemsSummaryController.displayPage

  }

  val commonItem: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case FiscalInformation         => controllers.declaration.routes.ProcedureCodesController.displayPage
    case AdditionalFiscalReference => controllers.declaration.routes.FiscalInformationController.displayPage(_, _, fastForward = false)
    case CommodityDetails => controllers.declaration.routes.FiscalInformationController.displayPage(_, _, fastForward = true)
    case UNDangerousGoodsCode => controllers.declaration.routes.CommodityDetailsController.displayPage
    case TaricCode => controllers.declaration.routes.CusCodeController.displayPage
  }

  def backLink(page: DeclarationPage, mode: Mode)(implicit request: JourneyRequest[_]): Call =
    mode match {
      case Mode.Change      => controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      case Mode.ChangeAmend => controllers.declaration.routes.SummaryController.displayPage(Mode.Amend)
      case Mode.Draft       => controllers.declaration.routes.SummaryController.displayPage(Mode.Draft)
      case _ =>
        val specific = request.declarationType match {
          case STANDARD      => standard
          case SUPPLEMENTARY => supplementary
          case SIMPLIFIED    => simplified
          case OCCASIONAL    => occasional
          case CLEARANCE     => clearance
        }
        common.orElse(specific)(page)(mode)
    }

  def backLink(page: DeclarationPage, mode: Mode, itemId: ItemId)(implicit request: JourneyRequest[_]): Call =
    mode match {
      case Mode.Change      => controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      case Mode.ChangeAmend => controllers.declaration.routes.SummaryController.displayPage(Mode.Amend)
      case Mode.Draft       => controllers.declaration.routes.SummaryController.displayPage(Mode.Draft)
      case _ =>
        val specific = request.declarationType match {
          case STANDARD      => standardItemPage
          case SUPPLEMENTARY => supplementaryItemPage
          case SIMPLIFIED    => simplifiedItemPage
          case OCCASIONAL    => occasionalItemPage
          case CLEARANCE     => clearanceItemPage
        }
        commonItem.orElse(specific)(page)(mode, itemId.id)
    }

}
