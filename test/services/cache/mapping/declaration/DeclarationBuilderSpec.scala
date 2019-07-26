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

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import services.mapping.AuthorisationHoldersBuilder
import services.mapping.declaration.consignment.DeclarationConsignmentBuilder
import services.mapping.declaration.{CurrencyExchangeBuilder, SupervisingOfficeBuilder, TotalPackageQuantityBuilder}

class DeclarationBuilderSpec extends WordSpec with Matchers with MockitoSugar with ExportsCacheModelBuilder {

  private val supervisingOfficeBuilder = mock[SupervisingOfficeBuilder]
  private val totalPackageQuantityBuilder = mock[TotalPackageQuantityBuilder]
  private val declarationConsignmentBuilder = mock[DeclarationConsignmentBuilder]
  private val authorisationHoldersBuilder = mock[AuthorisationHoldersBuilder]
  private val currencyExchangeBuilder = mock[CurrencyExchangeBuilder]

  private def builder =
    new DeclarationBuilder(
      supervisingOfficeBuilder,
      totalPackageQuantityBuilder,
      declarationConsignmentBuilder,
      authorisationHoldersBuilder,
      currencyExchangeBuilder
    )

  "DeclarationBuilder" should {
    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance" in {
      val model = aCacheModel(
        withConsignmentReference(Some(DUCR), LRN),
        withAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard),
        withDispatchLocation("GB"),
        withTotalNumberOfItems(exchangeRate = Some("123")),
        withDeclarationHolder(Some("auth code"), Some("eori")),
        withItems(3)
      )

      val declaration = builder.build(model)

      declaration.getFunctionCode.getValue should be("9")
      declaration.getFunctionalReferenceID.getValue should be(LRN)
      declaration.getTypeCode.getValue should be("GB" + AllowedAdditionalDeclarationTypes.Standard)
      declaration.getGoodsItemQuantity.getValue.intValue() should be(3)

      verify(supervisingOfficeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(totalPackageQuantityBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(currencyExchangeBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(authorisationHoldersBuilder).buildThenAdd(refEq(model), refEq(declaration))
      verify(currencyExchangeBuilder).buildThenAdd(refEq(model), refEq(declaration))
    }

    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance when dispatchLocation is not present" in {
      val exportsCacheModel = aCacheModel(withAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard))

      val declaration = builder.build(exportsCacheModel)

      declaration.getTypeCode.getValue should be(null)
    }
  }

}
