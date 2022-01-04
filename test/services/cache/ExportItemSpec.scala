/*
 * Copyright 2022 HM Revenue & Customs
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

import base.UnitWithMocksSpec
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation}
import models.declaration.CommodityMeasure
import models.DeclarationType

class ExportItemSpec extends UnitWithMocksSpec with ExportsItemBuilder {

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
      "on Standard or Supplementary journey" when {

        "item is not completed" in {
          val notCompletedItem = anItem(withItemId("id"))
          notCompletedItem.isCompleted(DeclarationType.STANDARD) mustBe false
        }

        "item contain Yes in fiscal information but without additional fiscal references" in {
          val completedItem = anItem(
            withItemId("id"),
            withProcedureCodes(),
            withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
            withStatisticalValue(),
            withPackageInformation(),
            withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
          ).copy(additionalFiscalReferencesData = None)

          completedItem.isCompleted(DeclarationType.STANDARD) mustBe false
        }

        "item contain No in fiscal information and doesn't contain additional fiscal references" in {
          val completedItem = anItem(
            withItemId("id"),
            withProcedureCodes(),
            withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.no)),
            withStatisticalValue(),
            withPackageInformation(),
            withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
          )

          completedItem.isCompleted(DeclarationType.STANDARD) mustBe true
        }

        "item does not contain procedure code '1042' and doesn't contain fiscal references" in {
          val completedItem = anItem(
            withItemId("id"),
            withProcedureCodes(Some("1040"), Seq("000")),
            withStatisticalValue(),
            withPackageInformation(),
            withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
          )

          completedItem.isCompleted(DeclarationType.STANDARD) mustBe true
        }

        "item is completed" in {
          val completedItem = anItem(
            withItemId("id"),
            withProcedureCodes(),
            withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
            withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))),
            withStatisticalValue(),
            withPackageInformation(),
            withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
          )

          completedItem.isCompleted(DeclarationType.STANDARD) mustBe true
        }
      }
      "on Simplified journey" when {

        "item is not completed" in {
          val notCompletedItem = anItem(withItemId("id"))
          notCompletedItem.isCompleted(DeclarationType.SIMPLIFIED) mustBe false
        }

        "item contain Yes in fiscal information but without additional fiscal references" in {
          val completedItem = anItem(
            withItemId("id"),
            withProcedureCodes(),
            withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
            withPackageInformation(),
            withAdditionalInformation("code", "description")
          ).copy(additionalFiscalReferencesData = None)

          completedItem.isCompleted(DeclarationType.SIMPLIFIED) mustBe false
        }

        "item does not contain procedure code '1042' and doesn't contain fiscal references" in {
          val completedItem = anItem(withItemId("id"), withProcedureCodes(Some("1040"), Seq("000")), withPackageInformation())
          completedItem.isCompleted(DeclarationType.SIMPLIFIED) mustBe true
        }

        "item is completed" in {
          val completedItem = anItem(
            withItemId("id"),
            withProcedureCodes(),
            withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
            withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))),
            withPackageInformation()
          )

          completedItem.isCompleted(DeclarationType.SIMPLIFIED) mustBe true
        }
      }

      "on Clearance journey" when {

        "item is empty" in {
          val notCompletedItem = anItem(withItemId("id"))
          notCompletedItem.isCompleted(DeclarationType.CLEARANCE) mustBe false
        }

        "item contains procedure code but no package references" in {
          val notCompletedItem = anItem(withItemId("id"), withProcedureCodes(Some("1234")))
          notCompletedItem.isCompleted(DeclarationType.CLEARANCE) mustBe false
        }

        "item contains '0019' procedure code and package references" in {
          val completedItem = anItem(withItemId("id"), withProcedureCodes(Some("0019")), withPackageInformation())
          completedItem.isCompleted(DeclarationType.CLEARANCE) mustBe false
        }

        "item is completed without package information for 0019 procedure code" in {
          val completedItem = anItem(withItemId("id"), withProcedureCodes(Some("0019")))
          completedItem.isCompleted(DeclarationType.CLEARANCE) mustBe true
        }
      }
    }
  }
}
