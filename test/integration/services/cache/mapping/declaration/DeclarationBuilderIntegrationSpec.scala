/*
 * Copyright 2019 HM Revenue & Customs
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

package integration.services.cache.mapping.declaration

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import services.cache.mapping.declaration.DeclarationBuilder
import services.cache.{CacheTestData, ExportsCacheModelBuilder}
import services.mapping.AuthorisationHoldersBuilder
import services.mapping.declaration.consignment.{DeclarationConsignmentBuilder, FreightBuilder, IteneraryBuilder}
import services.mapping.declaration._
import services.mapping.goodsshipment.{ConsigneeBuilder, GoodsShipmentBuilder, GoodsShipmentNatureOfTransactionBuilder}
import services.mapping.goodsshipment.consignment.{
  ConsignmentBuilder,
  ConsignmentCarrierBuilder,
  DepartureTransportMeansBuilder,
  GoodsLocationBuilder
}
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarationBuilderIntegrationSpec
    extends WordSpec with Matchers with MockitoSugar with ExportsCacheModelBuilder with CacheTestData
    with BeforeAndAfterEach {

  val declarationConsignmentBuilder =
    new DeclarationConsignmentBuilder(new FreightBuilder, new IteneraryBuilder, new ConsignmentCarrierBuilder)

  val consignmentBuilder = new ConsignmentBuilder(new GoodsLocationBuilder, new DepartureTransportMeansBuilder)

  val goodsShipmentBuilder =
    new GoodsShipmentBuilder(new GoodsShipmentNatureOfTransactionBuilder, new ConsigneeBuilder, consignmentBuilder)

  val declarationBuilder = new DeclarationBuilder(
    new FunctionCodeBuilder,
    new FunctionalReferenceIdBuilder,
    new TypeCodeBuilder,
    new GoodsItemQuantityBuilder,
    new AgentBuilder,
    new PresentationOfficeBuilder,
    new SpecificCircumstancesCodeBuilder,
    new ExitOfficeBuilder,
    new BorderTransportMeansBuilder,
    new ExporterBuilder,
    new DeclarantBuilder,
    new InvoiceAmountBuilder,
    new SupervisingOfficeBuilder,
    new TotalPackageQuantityBuilder,
    declarationConsignmentBuilder,
    new AuthorisationHoldersBuilder,
    new CurrencyExchangeBuilder,
    goodsShipmentBuilder
  )

  "DeclarationBuilder" should {
    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance" in {
      val exportsCacheModel =
        aCacheModel(
          withConsignmentReference(Some(ducr), LRN),
          withAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard),
          withDispatchLocation(dispatchLocation),
          withItems(3)
        )
      val declaration = declarationBuilder.build(exportsCacheModel)

      declaration.getFunctionCode.getValue should be("9")
      declaration.getFunctionalReferenceID.getValue should be(LRN)
      declaration.getTypeCode.getValue should be(dispatchLocation + AllowedAdditionalDeclarationTypes.Standard)
      declaration.getGoodsItemQuantity.getValue.intValue() should be(3)
    }

    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance when dispatchLocation is not present" in {
      val exportsCacheModel =
        createEmptyExportsModel.copy(
          consignmentReferences = Some(createConsignmentReferencesData(Some(ducr), LRN)),
          additionalDeclarationType = Some(createAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard))
        )

      val declaration = declarationBuilder.build(exportsCacheModel)

      declaration.getFunctionCode.getValue should be("9")
      declaration.getFunctionalReferenceID.getValue should be(LRN)
      declaration.getTypeCode.getValue should be(null)

    }
  }

}
