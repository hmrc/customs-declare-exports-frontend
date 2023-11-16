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
import forms.{DeclarationPage, Ducr}
import forms.declaration._
import forms.declaration.RoutingCountryQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingCountryQuestionPage}
import forms.declaration.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.declaration.carrier.CarrierEoriNumber
import forms.declaration.commodityMeasure.CommodityMeasure
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import models.ExportsDeclaration
import play.api.mvc.Call

trait ClearanceNavigator extends CacheDependentNavigators {

  val clearance: PartialFunction[DeclarationPage, Call] = {
    case EntryIntoDeclarantsRecords   => routes.SectionSummaryController.displayPage(1)
    case DucrChoice                   => routes.AdditionalDeclarationTypeController.displayPage
    case Ducr                         => routes.DucrChoiceController.displayPage
    case ConsignmentReferences        => routes.AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr               => routes.LocalReferenceNumberController.displayPage
    case ExporterDetails              => routes.ExporterEoriNumberController.displayPage
    case DeclarantDetails             => routes.EntryIntoDeclarantsRecordsController.displayPage
    case PersonPresentingGoodsDetails => routes.EntryIntoDeclarantsRecordsController.displayPage
    case ContainerAdd                 => routes.TransportContainerController.displayContainerSummary
    case RoutingCountryQuestionPage   => routes.DestinationCountryController.displayPage
    case RemoveCountryPage            => routes.RoutingCountriesController.displayRoutingCountry
    case ChangeCountryPage            => routes.RoutingCountriesController.displayRoutingCountry
    case LocationOfGoods              => routes.DestinationCountryController.displayPage
    case ConsignorEoriNumber          => routes.IsExsController.displayPage
    case ConsignorDetails             => routes.ConsignorEoriNumberController.displayPage
    case DocumentSummary              => routes.OfficeOfExitController.displayPage
    case page                         => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalInformationRequired => routes.CommodityMeasureController.displayPage
    case AdditionalInformationSummary  => routes.CommodityMeasureController.displayPage
    case page                          => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on clearance")
  }

  val clearanceCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case DeclarantIsExporter => declarantIsExporterPreviousPage
    case CarrierEoriNumber   => carrierEoriNumberClearancePreviousPage
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
