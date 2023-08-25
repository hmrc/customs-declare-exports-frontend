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
import controllers.routes.ChoiceController
import forms.Choice.AllowedChoiceValues
import forms.{Choice, DeclarationPage, Lrn}
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypePage
import forms.declaration.carrier.CarrierDetails
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.countries.Countries.RoutingCountryPage
import forms.declaration.declarationHolder.{DeclarationHolder, DeclarationHolderRequired, DeclarationHolderSummary}
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.procedurecodes.{AdditionalProcedureCode, ProcedureCode}
import models.ExportsDeclaration
import models.declaration.ExportItem
import play.api.mvc.Call

trait CommonNavigator extends CacheDependentNavigators {

  val common: PartialFunction[DeclarationPage, Call] = {
    case DeclarationChoice             => ChoiceController.displayPage(Some(Choice(AllowedChoiceValues.CreateDec)))
    case Lrn                           => routes.DucrEntryController.displayPage
    case Mucr                          => routes.LinkDucrToMucrController.displayPage
    case RepresentativeEntity          => routes.RepresentativeAgentController.displayPage
    case RepresentativeStatus          => routes.RepresentativeEntityController.displayPage
    case AdditionalDeclarationTypePage => routes.DeclarationChoiceController.displayPage
    case NatureOfTransaction           => routes.TotalPackageQuantityController.displayPage
    case ProcedureCode                 => routes.ItemsSummaryController.displayItemsSummaryPage
    case ExportItem                    => routes.PreviousDocumentsSummaryController.displayPage
    case DocumentChangeOrRemove        => routes.PreviousDocumentsSummaryController.displayPage
    case TransportLeavingTheBorder     => routes.ItemsSummaryController.displayItemsSummaryPage
    case WarehouseIdentification       => routes.TransportLeavingTheBorderController.displayPage
    case TransportPayment              => routes.ExpressConsignmentController.displayPage
    case CarrierDetails                => routes.CarrierEoriNumberController.displayPage
    case InvoiceAndExchangeRateChoice  => routes.OfficeOfExitController.displayPage
    case InvoiceAndExchangeRate        => routes.InvoiceAndExchangeRateChoiceController.displayPage
    case TraderReference               => routes.DucrChoiceController.displayPage
    case ConfirmDucr                   => routes.TraderReferenceController.displayPage
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
}
