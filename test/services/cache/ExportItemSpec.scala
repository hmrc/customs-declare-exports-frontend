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

package services.cache

import forms.declaration.{
  AdditionalFiscalReference,
  AdditionalFiscalReferencesData,
  CommodityMeasure,
  FiscalInformation
}
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import unit.base.UnitSpec

class ExportItemSpec extends UnitSpec with ExportsItemBuilder {

  "Export Item" should {

    "return correct information about fiscal references" when {

      "item doesn't contain fiscal references" in {

        val itemWithoutFiscalReferences = anItem(withItemId("id"))

        itemWithoutFiscalReferences.hasFiscalReferences mustBe false
      }

      "item contains fiscal references" in {

        val itemWithFiscalReferences = anItem(withItemId("id"), withFiscalInformation(FiscalInformation("Yes")))

        itemWithFiscalReferences.hasFiscalReferences mustBe true
      }
    }

    "return correct information about item completeness" when {

      "item is not completed" in {

        val notCompletedItem = anItem(withItemId("id"))

        notCompletedItem.isCompleted mustBe false
      }

      "item contain Yes in fiscal information but without additional fiscal references" in {

        val completedItem = anItem(
          withItemId("id"),
          withProcedureCodes(),
          withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
          withItemType(),
          withPackageInformation(),
          withCommodityMeasure(CommodityMeasure(None, "100", "100")),
          withAdditionalInformation("code", "description")
        ).copy(additionalFiscalReferencesData = None)

        completedItem.isCompleted mustBe false
      }

      "item contain No in fiscal information and doesn't contain additional fiscal references" in {

        val completedItem = anItem(
          withItemId("id"),
          withProcedureCodes(),
          withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.no)),
          withItemType(),
          withPackageInformation(),
          withCommodityMeasure(CommodityMeasure(None, "100", "100")),
          withAdditionalInformation("code", "description")
        )

        completedItem.isCompleted mustBe true
      }

      "item is completed" in {

        val completedItem = anItem(
          withItemId("id"),
          withProcedureCodes(),
          withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
          withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))),
          withItemType(),
          withPackageInformation(),
          withCommodityMeasure(CommodityMeasure(None, "100", "100")),
          withAdditionalInformation("code", "description")
        )

        completedItem.isCompleted mustBe true
      }
    }
  }
}
