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
import forms.declaration.exporter._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.mvc.Call

trait OccasionalNavigator extends CacheDependentNavigators {

  val occasional: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails           => routes.AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr             => routes.LocalReferenceNumberController.displayPage
    case DucrChoice                 => routes.DeclarantDetailsController.displayPage
    case Ducr                       => routes.DucrChoiceController.displayPage
    case ConsignmentReferences      => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber         => routes.DeclarantExporterController.displayPage
    case ExporterDetails            => routes.ExporterEoriNumberController.displayPage
    case AdditionalActor            => routes.ConsigneeDetailsController.displayPage
    case RoutingCountryQuestionPage => routes.DestinationCountryController.displayPage
    case RemoveCountryPage          => routes.RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage          => routes.RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods            => routes.RoutingCountriesController.displayRoutingCountry
    case AdditionalActorsSummary    => routes.ConsigneeDetailsController.displayPage
    case DocumentSummary            => routes.SectionSummaryController.displayPage(3)
    case ContainerAdd               => routes.TransportContainerController.displayContainerSummary
    case CarrierEoriNumber          => routes.ThirdPartyGoodsTransportationController.displayPage
    case page                       => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalInformationRequired => routes.PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => routes.PackageInformationSummaryController.displayPage
    case AdditionalDocumentsRequired   => routes.IsLicenceRequiredController.displayPage
    case AdditionalDocumentsSummary    => routes.IsLicenceRequiredController.displayPage
    case CusCode                       => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                      => routes.NactCodeSummaryController.displayPage
    case CommodityMeasure              => routes.PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  def occasionalCacheDependent(implicit request: JourneyRequest[_]): PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
    case Document                  => previousDocumentsPreviousPage
    case InlandOrBorder            => inlandOrBorderPreviousPage
    case InlandModeOfTransportCode => inlandTransportDetailsPreviousPage
    case DepartureTransport        => departureTransportPreviousPage
    case BorderTransport           => borderTransportPreviousPage
    case TransportCountry          => transportCountryPreviousPage
    case ExpressConsignment        => expressConsignmentPreviousPage
    case ContainerFirst            => containerFirstPreviousPage
  }

  val occasionalCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalDocument => additionalDocumentsPreviousPage
    case NactCodeFirst      => nactCodePreviousPageForSimplified
    case PackageInformation => packageInformationPreviousPageForSimplified
  }
}
