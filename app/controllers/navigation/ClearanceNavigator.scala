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
import controllers.section5.routes.CommodityMeasureController
import controllers.section6.routes.ContainerController
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

  val clearance: PartialFunction[DeclarationPage, Call] = {
    case EntryIntoDeclarantsRecords   => SectionSummaryController.displayPage(1)
    case DucrChoice                   => AdditionalDeclarationTypeController.displayPage
    case ConsignmentReferences        => AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr               => LocalReferenceNumberController.displayPage
    case ExporterDetails              => ExporterEoriNumberController.displayPage
    case DeclarantDetails             => EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => EntryIntoDeclarantsRecordsController.displayPage
    case ContainerAdd                 => ContainerController.displayContainerSummary
    case RoutingCountryQuestionPage   => DestinationCountryController.displayPage
    case RemoveCountryPage            => RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage            => RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods              => DestinationCountryController.displayPage
    case ConsignorEoriNumber          => IsExsController.displayPage
    case ConsignorDetails             => ConsignorEoriNumberController.displayPage
    case DocumentSummary              => SectionSummaryController.displayPage(3)
    case CarrierEoriNumber            => ThirdPartyGoodsTransportationController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalInformationRequired => CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => CommodityMeasureController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
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
