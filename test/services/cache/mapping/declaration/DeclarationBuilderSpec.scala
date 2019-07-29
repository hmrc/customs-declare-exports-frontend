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

package services.cache.mapping.declaration

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import services.mapping.AuthorisationHoldersBuilder
import services.mapping.declaration._
import services.mapping.declaration.consignment.DeclarationConsignmentBuilder
import services.mapping.goodsshipment.GoodsShipmentBuilder

class DeclarationBuilderSpec extends WordSpec with Matchers with MockitoSugar with ExportsCacheModelBuilder {

  private val functionCodeBuilder = mock[FunctionCodeBuilder]
  private val functionalReferenceIdBuilder = mock[FunctionalReferenceIdBuilder]
  private val typeCodeBuilder = mock[TypeCodeBuilder]
  private val goodsItemQuantityBuilder = mock[GoodsItemQuantityBuilder]
  private val agentBuilder = mock[AgentBuilder]
  private val presentationOfficeBuilder = mock[PresentationOfficeBuilder]
  private val specificCircumstancesCodeBuilder = mock[SpecificCircumstancesCodeBuilder]
  private val exitOfficeBuilder = mock[ExitOfficeBuilder]
  private val borderTransportMeansBuilder = mock[BorderTransportMeansBuilder]
  private val exporterBuilder = mock[ExporterBuilder]
  private val declarantBuilder = mock[DeclarantBuilder]
  private val invoiceAmountBuilder = mock[InvoiceAmountBuilder]
  private val supervisingOfficeBuilder = mock[SupervisingOfficeBuilder]
  private val totalPackageQuantityBuilder = mock[TotalPackageQuantityBuilder]
  private val declarationConsignmentBuilder = mock[DeclarationConsignmentBuilder]
  private val authorisationHoldersBuilder = mock[AuthorisationHoldersBuilder]
  private val currencyExchangeBuilder = mock[CurrencyExchangeBuilder]
  private val goodsShipmentBuilder = mock[GoodsShipmentBuilder]

  private def builder =
    new DeclarationBuilder(
      functionCodeBuilder,
      functionalReferenceIdBuilder,
      typeCodeBuilder,
      goodsItemQuantityBuilder,
      agentBuilder,
      presentationOfficeBuilder,
      specificCircumstancesCodeBuilder,
      exitOfficeBuilder,
      borderTransportMeansBuilder,
      exporterBuilder,
      declarantBuilder,
      invoiceAmountBuilder,
      supervisingOfficeBuilder,
      totalPackageQuantityBuilder,
      declarationConsignmentBuilder,
      authorisationHoldersBuilder,
      currencyExchangeBuilder,
      goodsShipmentBuilder
    )

  "DeclarationBuilder" should {
    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance" in {
      val model = aCacheModel()

      val declaration = builder.build(model)

      verify(functionCodeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(functionalReferenceIdBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(typeCodeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(goodsItemQuantityBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(agentBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(presentationOfficeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(specificCircumstancesCodeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(exitOfficeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(borderTransportMeansBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(exporterBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(declarantBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(invoiceAmountBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(supervisingOfficeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(totalPackageQuantityBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(currencyExchangeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(authorisationHoldersBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(currencyExchangeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(goodsShipmentBuilder).buildThenAdd(refEq(model), refEq(declaration))
    }
  }

}
