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
import forms.declaration.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.{DeclarationPage, Ducr}
import forms.declaration._
import forms.declaration.additionaldocuments._
import forms.declaration.carrier.CarrierEoriNumber
import forms.declaration.commodityMeasure.CommodityMeasure
import forms.declaration.countries.Countries.DestinationCountryPage
import forms.declaration.exporter._
import models.ExportsDeclaration
import play.api.mvc.Call

trait OccasionalNavigator extends CacheDependentNavigators {

  val occasional: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails            => routes.AdditionalDeclarationTypeController.displayPage
    case DucrChoice                  => routes.DeclarantDetailsController.displayPage
    case Ducr                        => routes.DucrChoiceController.displayPage
    case LinkDucrToMucr              => routes.LocalReferenceNumberController.displayPage
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

}
