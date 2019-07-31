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

import forms.declaration.DispatchLocationSpec
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.cache.mapping.declaration.DeclarationBuilder
import services.cache.ExportsCacheModelBuilder

class DeclarationBuilderIntegrationSpec
    extends WordSpec with Matchers with MockitoSugar with ExportsCacheModelBuilder with GuiceOneAppPerSuite {

  val declarationBuilder: DeclarationBuilder = app.injector.instanceOf[DeclarationBuilder]

  "DeclarationBuilder" should {
    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance" in {
      val exportsCacheModel =
        aCacheModel(
          withConsignmentReferences(Some(DUCR), LRN),
          withAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard),
          withDispatchLocation(DispatchLocationSpec.correctDispatchLocation.dispatchLocation),
          withItems(3)
        )
      val declaration = declarationBuilder.build(exportsCacheModel)

      declaration.getFunctionCode.getValue should be("9")
      declaration.getFunctionalReferenceID.getValue should be(LRN)
      declaration.getTypeCode.getValue should be(
        DispatchLocationSpec.correctDispatchLocation.dispatchLocation + AllowedAdditionalDeclarationTypes.Standard
      )
      declaration.getGoodsItemQuantity.getValue.intValue() should be(3)
    }

    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance when dispatchLocation is not present" in {
      val exportsCacheModel =
        aCacheModel(
          withConsignmentReferences(Some(DUCR), LRN),
          withAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard)
        )

      val declaration = declarationBuilder.build(exportsCacheModel)

      declaration.getFunctionCode.getValue should be("9")
      declaration.getFunctionalReferenceID.getValue should be(LRN)
      declaration.getTypeCode.getValue should be(null)

    }
  }

}
