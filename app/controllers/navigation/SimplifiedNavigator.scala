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

import controllers.section1.routes._
import controllers.section2.routes._
import controllers.section3.routes.{DestinationCountryController, RoutingCountriesController}
import controllers.section5.routes._
import controllers.section6.routes.ContainerController
import controllers.summary.routes.SectionSummaryController
import forms.DeclarationPage
import forms.section1.{ConsignmentReferences, DeclarantDetails, DucrChoice, LinkDucrToMucr}
import forms.section2._
import forms.section2.carrier.CarrierEoriNumber
import forms.section2.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.section2.representative.RepresentativeAgent
import forms.section3.LocationOfGoods
import forms.section3.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.section4.{Document, DocumentSummary}
import forms.section5.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.section5.commodityMeasure.CommodityMeasure
import forms.section5._
import forms.section6._
import models.ExportsDeclaration
import play.api.mvc.Call

trait SimplifiedNavigator extends CacheDependentNavigators {

  val simplified: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails           => AdditionalDeclarationTypeController.displayPage
    case DucrChoice                 => DeclarantDetailsController.displayPage
    case ConsignmentReferences      => DeclarantDetailsController.displayPage
    case LinkDucrToMucr             => LocalReferenceNumberController.displayPage
    case ExporterEoriNumber         => DeclarantExporterController.displayPage
    case ExporterDetails            => ExporterEoriNumberController.displayPage
    case AdditionalActor            => ConsigneeDetailsController.displayPage
    case ContainerAdd               => ContainerController.displayContainerSummary
    case RoutingCountryQuestionPage => DestinationCountryController.displayPage
    case RemoveCountryPage          => RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage          => RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods            => RoutingCountriesController.displayRoutingCountry
    case AdditionalActorsSummary    => ConsigneeDetailsController.displayPage
    case DocumentSummary            => SectionSummaryController.displayPage(3)
    case CarrierEoriNumber          => ThirdPartyGoodsTransportationController.displayPage
    case page                       => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  val simplifiedItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalInformationRequired => PackageInformationSummaryController.displayPage
    case AdditionalInformationSummary  => PackageInformationSummaryController.displayPage
    case AdditionalDocumentsRequired   => IsLicenceRequiredController.displayPage
    case AdditionalDocumentsSummary    => IsLicenceRequiredController.displayPage
    case CusCode                       => UNDangerousGoodsCodeController.displayPage
    case NactCode                      => NactCodeSummaryController.displayPage
    case CommodityMeasure              => PackageInformationSummaryController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on simplified")
  }

  def simplifiedCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case Document                  => previousDocumentsPreviousPage
    case ConsigneeDetails          => consigneeDetailsPreviousPage
    case RepresentativeAgent       => representativeAgentPreviousPage
    case InlandModeOfTransportCode => inlandTransportDetailsPreviousPage
    case InlandOrBorder            => inlandOrBorderPreviousPage
    case DepartureTransport        => departureTransportPreviousPage
    case BorderTransport           => borderTransportPreviousPage
    case TransportCountry          => transportCountryPreviousPage
    case ExpressConsignment        => expressConsignmentPreviousPage
    case ContainerFirst            => containerFirstPreviousPage
  }

  val simplifiedCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalDocument => additionalDocumentsPreviousPage
    case NactCodeFirst      => nactCodePreviousPageForSimplified
    case PackageInformation => packageInformationPreviousPageForSimplified
  }
}
