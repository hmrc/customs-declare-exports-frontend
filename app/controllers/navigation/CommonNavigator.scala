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

import controllers.declaration.routes._
import controllers.section1.routes._
import controllers.section2.routes._
import controllers.section3.routes.RoutingCountriesController
import controllers.routes.ChoiceController
import forms.declaration._
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.countries.Countries.{DestinationCountryPage, RoutingCountryPage}
import forms.declaration.procedurecodes.{AdditionalProcedureCode, ProcedureCode}
import forms.section1.{ConfirmDucr, Mucr, TraderReference}
import forms.section2.authorisationHolder.{AuthorisationHolder, AuthorisationHolderRequired, AuthorisationHolderSummary}
import forms.section2.carrier.CarrierDetails
import forms.section2.representative.{RepresentativeEntity, RepresentativeStatus}
import forms.section2.{AuthorisationProcedureCodeChoice, ThirdPartyGoodsTransportationPage}
import forms.section3.OfficeOfExit
import forms.{DeclarationPage, Ducr, Lrn}
import models.ExportsDeclaration
import models.declaration.ExportItem
import models.requests.JourneyRequest
import play.api.mvc.Call
import views.helpers.summary.Card2ForParties

trait CommonNavigator extends CacheDependentNavigators {

  val common: PartialFunction[DeclarationPage, Call] = {
    case DeclarationChoice            => ChoiceController.displayPage
    case Mucr                         => LinkDucrToMucrController.displayPage
    case RepresentativeEntity         => RepresentativeAgentController.displayPage
    case RepresentativeStatus         => RepresentativeEntityController.displayPage
    case ProcedureCode                => ItemsSummaryController.displayItemsSummaryPage
    case ExportItem                   => SectionSummaryController.displayPage(4)
    case DocumentChangeOrRemove       => PreviousDocumentsSummaryController.displayPage
    case TransportLeavingTheBorder    => SectionSummaryController.displayPage(5)
    case WarehouseIdentification      => TransportLeavingTheBorderController.displayPage
    case TransportPayment             => ExpressConsignmentController.displayPage
    case CarrierDetails               => CarrierEoriNumberController.displayPage
    case InvoiceAndExchangeRateChoice => SectionSummaryController.displayPage(3)
    case InvoiceAndExchangeRate       => InvoiceAndExchangeRateChoiceController.displayPage
    case TraderReference              => DucrChoiceController.displayPage
    case ConfirmDucr                  => TraderReferenceController.displayPage
    case RoutingCountryPage           => RoutingCountriesController.displayRoutingQuestion
    case DestinationCountryPage       => SectionSummaryController.displayPage(2)
  }

  val commonItem: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalProcedureCode           => ProcedureCodesController.displayPage
    case FiscalInformation                 => AdditionalProcedureCodesController.displayPage
    case AdditionalFiscalReferencesSummary => FiscalInformationController.displayPage
    case UNDangerousGoodsCode              => CommodityDetailsController.displayPage
    case StatisticalValue                  => NactCodeSummaryController.displayPage
    case SupplementaryUnits                => CommodityMeasureController.displayPage
  }

  def commonCacheDependent(implicit request: JourneyRequest[_]): PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case Ducr                              => ducrEntryPreviousPage
    case Lrn                               => lrnPreviousPage
    case AuthorisationHolderRequired       => authorisationHolderRequiredPreviousPage
    case AuthorisationHolder               => authorisationHolderAddPreviousPage
    case AuthorisationHolderSummary        => authorisationHolderRequiredPreviousPage
    case Card2ForParties                   => card2ForPartiesPreviousPage
    case SupervisingCustomsOffice          => supervisingCustomsOfficePreviousPage
    case AuthorisationProcedureCodeChoice  => authorisationProcedureCodeChoicePreviousPage
    case OfficeOfExit                      => officeOfExitPreviousPage
    case NatureOfTransaction               => natureOfTransactionPreviousPage
    case ThirdPartyGoodsTransportationPage => thirdPartyGoodsTransportationPreviousPage
  }

  val commonCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case CommodityDetails          => commodityDetailsPreviousPage
    case AdditionalFiscalReference => additionalFiscalReferencesPreviousPage
    case IsLicenceRequired         => isLicenseRequiredPreviousPage
    case AdditionalInformation     => additionalInformationAddPreviousPage
    case ZeroRatedForVat           => cusCodeOrDangerousGoodsPage
  }
}
