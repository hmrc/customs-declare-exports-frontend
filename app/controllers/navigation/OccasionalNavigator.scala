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
import controllers.section3.routes.{DestinationCountryController, RoutingCountriesController}
import controllers.section6.routes.ContainerController
import forms.DeclarationPage
import forms.declaration._
import forms.declaration.additionaldocuments._
import forms.declaration.commodityMeasure.CommodityMeasure
import forms.section1.{ConsignmentReferences, DeclarantDetails, DucrChoice, LinkDucrToMucr}
import forms.section2._
import forms.section2.carrier.CarrierEoriNumber
import forms.section2.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.section2.representative.RepresentativeAgent
import forms.section3.LocationOfGoods
import forms.section3.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.section4.{Document, DocumentSummary}
import forms.section6._
import models.ExportsDeclaration
import play.api.mvc.Call

trait OccasionalNavigator extends CacheDependentNavigators {

  val occasional: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails           => AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr             => LocalReferenceNumberController.displayPage
    case DucrChoice                 => DeclarantDetailsController.displayPage
    case ConsignmentReferences      => DeclarantDetailsController.displayPage
    case ExporterEoriNumber         => DeclarantExporterController.displayPage
    case ExporterDetails            => ExporterEoriNumberController.displayPage
    case AdditionalActor            => ConsigneeDetailsController.displayPage
    case RoutingCountryQuestionPage => DestinationCountryController.displayPage
    case RemoveCountryPage          => RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage          => RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods            => RoutingCountriesController.displayRoutingCountry
    case AdditionalActorsSummary    => ConsigneeDetailsController.displayPage
    case DocumentSummary            => SectionSummaryController.displayPage(3)
    case ContainerAdd               => ContainerController.displayContainerSummary
    case CarrierEoriNumber          => ThirdPartyGoodsTransportationController.displayPage
    case page                       => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  val occasionalItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalInformationRequired => PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => PackageInformationSummaryController.displayPage
    case AdditionalDocumentsRequired   => IsLicenceRequiredController.displayPage
    case AdditionalDocumentsSummary    => IsLicenceRequiredController.displayPage
    case CusCode                       => UNDangerousGoodsCodeController.displayPage
    case NactCode                      => NactCodeSummaryController.displayPage
    case CommodityMeasure              => PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on occasional")
  }

  def occasionalCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
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
