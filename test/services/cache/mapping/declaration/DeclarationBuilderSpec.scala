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
import org.scalatest.{Matchers, WordSpec}
import services.cache.CacheTestData

class DeclarationBuilderSpec extends WordSpec with Matchers with CacheTestData {

  "DeclarationBuilder" should {
    "correctly map a Supplementary declaration to the WCO-DEC Declaration instance" in {
      val exportsCacheModel =
        createEmptyExportsModel.copy(
          consignmentReferences = Some(createConsignmentReferencesData(Some(ducr), LRN)),
          additionalDeclarationType = Some(createAdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Standard)),
          dispatchLocation = Some(createDispatchLocation(dispatchLocation)),
          items = Set(createExportItem, createExportItem, createExportItem)
        )

      val declaration = DeclarationBuilder.build(exportsCacheModel)

      declaration.getFunctionCode.getValue should be(DeclarationBuilder.defaultFunctionCode)
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

      val declaration = DeclarationBuilder.build(exportsCacheModel)

      declaration.getFunctionCode.getValue should be(DeclarationBuilder.defaultFunctionCode)
      declaration.getFunctionalReferenceID.getValue should be(LRN)
      declaration.getTypeCode.getValue should be(null)

    }
  }

}
