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
import controllers.section3.routes.DestinationCountryController
import controllers.section4.routes.NatureOfTransactionController
import controllers.section5.routes._
import controllers.section6.routes.ContainerController
import forms.DeclarationPage
import forms.section1.{ConsignmentReferences, DeclarantDetails, LinkDucrToMucr}
import forms.section2._
import forms.section2.exporter.{ExporterDetails, ExporterEoriNumber}
import forms.section2.representative.RepresentativeAgent
import forms.section3.LocationOfGoods
import forms.section4.{Document, DocumentSummary, TotalPackageQuantity}
import forms.section5.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.section5.commodityMeasure.CommodityMeasure
import forms.section5.{CusCode, NactCode, NactCodeFirst, PackageInformation}
import forms.section6._
import models.ExportsDeclaration
import play.api.mvc.Call

trait SupplementaryNavigator extends CacheDependentNavigators {

  val supplementary: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails        => AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr          => ConsignmentReferencesController.displayPage
    case ConsignmentReferences   => DeclarantDetailsController.displayPage
    case ExporterEoriNumber      => DeclarantExporterController.displayPage
    case ExporterDetails         => ExporterEoriNumberController.displayPage
    case ContainerAdd            => ContainerController.displayContainerSummary
    case LocationOfGoods         => DestinationCountryController.displayPage
    case DocumentSummary         => NatureOfTransactionController.displayPage
    case AdditionalActorsSummary => ConsigneeDetailsController.displayPage
    case AdditionalActor         => ConsigneeDetailsController.displayPage
    case page                    => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val supplementaryItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalDocumentsRequired => IsLicenceRequiredController.displayPage
    case AdditionalDocumentsSummary  => IsLicenceRequiredController.displayPage
    case PackageInformation          => StatisticalValueController.displayPage
    case CusCode                     => UNDangerousGoodsCodeController.displayPage
    case NactCode                    => NactCodeSummaryController.displayPage
    case CommodityMeasure            => PackageInformationSummaryController.displayPage
    case page                        => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val supplementaryCacheDependent: PartialFunction[DeclarationPage, ExportsDeclaration => Call] = {
    case ConsigneeDetails          => consigneeDetailsSupplementaryPreviousPage
    case DeclarantIsExporter       => declarantIsExporterPreviousPage
    case TotalPackageQuantity      => totalPackageQuantityPreviousPage
    case Document                  => previousDocumentsPreviousPageDefault
    case InlandOrBorder            => inlandOrBorderPreviousPage
    case InlandModeOfTransportCode => inlandTransportDetailsPreviousPage
    case DepartureTransport        => departureTransportPreviousPage
    case BorderTransport           => borderTransportPreviousPage
    case TransportCountry          => transportCountryPreviousPage
    case ContainerFirst            => containerFirstPreviousPageOnSupplementary
    case RepresentativeAgent       => representativeAgentPreviousPage
  }

  val supplementaryCacheItemDependent: PartialFunction[DeclarationPage, (ExportsDeclaration, String) => Call] = {
    case NactCodeFirst      => cusCodeOrDangerousGoodsPage
    case AdditionalDocument => additionalDocumentsPreviousPage
  }
}
