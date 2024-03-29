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
import forms.DeclarationPage
import forms.declaration._
import forms.declaration.additionaldocuments.{AdditionalDocument, AdditionalDocumentsRequired, AdditionalDocumentsSummary}
import forms.declaration.commodityMeasure.CommodityMeasure
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import models.ExportsDeclaration
import play.api.mvc.Call

trait SupplementaryNavigator extends CacheDependentNavigators {

  val supplementary: PartialFunction[DeclarationPage, Call] = {
    case DeclarantDetails        => routes.AdditionalDeclarationTypeController.displayPage
    case LinkDucrToMucr          => routes.ConsignmentReferencesController.displayPage
    case ConsignmentReferences   => routes.DeclarantDetailsController.displayPage
    case ExporterEoriNumber      => routes.DeclarantExporterController.displayPage
    case ExporterDetails         => routes.ExporterEoriNumberController.displayPage
    case ContainerAdd            => routes.TransportContainerController.displayContainerSummary
    case LocationOfGoods         => routes.DestinationCountryController.displayPage
    case DocumentSummary         => routes.NatureOfTransactionController.displayPage
    case AdditionalActorsSummary => routes.ConsigneeDetailsController.displayPage
    case AdditionalActor         => routes.ConsigneeDetailsController.displayPage
    case page                    => throw new IllegalArgumentException(s"Navigator back-link route not implemented for $page on supplementary")
  }

  val supplementaryItemPage: PartialFunction[DeclarationPage, String => Call] = {
    case AdditionalDocumentsRequired => routes.IsLicenceRequiredController.displayPage
    case AdditionalDocumentsSummary  => routes.IsLicenceRequiredController.displayPage
    case PackageInformation          => routes.StatisticalValueController.displayPage
    case CusCode                     => routes.UNDangerousGoodsCodeController.displayPage
    case NactCode                    => routes.NactCodeSummaryController.displayPage
    case CommodityMeasure            => routes.PackageInformationSummaryController.displayPage
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
