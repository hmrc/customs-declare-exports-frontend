/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.section5.routes.CommodityMeasureController
import controllers.section6.routes.ContainerController
import controllers.summary.routes.SectionSummaryController
import forms.DeclarationPage
import forms.section1.{ConsignmentReferences, DeclarantDetails, DucrChoice, LinkDucrToMucr}
import forms.section2._
import forms.section2.carrier.CarrierEoriNumber
import forms.section2.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.section2.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.section2.representative.RepresentativeAgent
import forms.section3.LocationOfGoods
import forms.section3.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.section4.{Document, DocumentSummary}
import forms.section5._
import forms.section5.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.section5.commodityMeasure.CommodityMeasure
import forms.section6.{ContainerAdd, ContainerFirst, DepartureTransport, ExpressConsignment}
import models.ExportsDeclaration
import play.api.mvc.Call

trait ClearanceNavigator extends CacheDependentNavigators {

  val clearance: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case EntryIntoDeclarantsRecords   => _ => SectionSummaryController.displayPage(1)
    case DucrChoice                   => _ => AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences        => _ => AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr               => _ => LocalReferenceNumberController.displayPage
    case ExporterDetails              => _ => ExporterEoriNumberController.displayPage
    case DeclarantDetails             => _ => EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => _ => EntryIntoDeclarantsRecordsController.displayPage
    case ContainerAdd                 => _ => ContainerController.displayContainerSummary()
    case RoutingCountryQuestionPage   => _ => DestinationCountryController.displayPage
    case RemoveCountryPage            => _ => RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage            => _ => RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods              => _ => DestinationCountryController.displayPage
    case ConsignorEoriNumber          => _ => IsExsController.displayPage
    case ConsignorDetails             => _ => ConsignorEoriNumberController.displayPage
    case DocumentSummary              => _ => SectionSummaryController.displayPage(3)
    case CarrierEoriNumber            => _ => ThirdPartyGoodsTransportationController.displayPage
    case page                         => _ => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case AdditionalInformationRequired => (_, itemId) => CommodityMeasureController.displayPage(itemId)
    case AdditionalInformationSummary  => (_, itemId) => CommodityMeasureController.displayPage(itemId)
    case page => (_, _) => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  def clearanceCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case DeclarantIsExporter => declarantIsExporterPreviousPage
    case ExporterEoriNumber  => exporterEoriNumberClearancePreviousPage
    case ConsigneeDetails    => consigneeDetailsClearancePreviousPage
    case RepresentativeAgent => representativeAgentClearancePreviousPage
    case IsExs               => isExsClearancePreviousPage
    case Document            => previousDocumentsPreviousPage
    case DepartureTransport  => departureTransportPreviousPageOnClearance
    case ContainerFirst      => containerFirstPreviousPage
    case ExpressConsignment  => expressConsignmentPreviousPageOnClearance
  }

  val clearanceCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case CommodityMeasure            => commodityMeasureClearancePreviousPage
    case PackageInformation          => packageInformationClearancePreviousPage
    case AdditionalDocumentsRequired => additionalDocumentsSummaryClearancePreviousPage
    case AdditionalDocumentsSummary  => additionalDocumentsSummaryClearancePreviousPage
    case AdditionalDocument          => additionalDocumentsClearancePreviousPage
  }

}
