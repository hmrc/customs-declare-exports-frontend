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

package services.cache

import base.UnitWithMocksSpec
import forms.common.Date
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import forms.declaration.{
  AdditionalFiscalReference,
  AdditionalFiscalReferencesData,
  AdditionalInformation,
  CommodityDetails,
  CusCode,
  FiscalInformation,
  NactCode,
  PackageInformation,
  StatisticalValue,
  TaricCode,
  UNDangerousGoodsCode
}
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import models.declaration.{AdditionalDocuments, AdditionalInformationData, CommodityMeasure, ExportItem, ProcedureCodesData}
import models.DeclarationType
import services.AlteredField.constructAlteredField

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

  "ExportItem.createDiff" should {
    val baseFieldPointer = ExportItem.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val item = ExportItem("latestId")
        item.createDiff(item, baseFieldPointer) mustBe Seq.empty
      }

      "the original version's sequenceId field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${ExportItem.sequenceIdPointer}"
        val item = ExportItem("latestId", sequenceId = 1)
        val originalValue = 2
        item.createDiff(item.copy(sequenceId = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.sequenceId)
        )
      }

      "the original version's procedureCodes field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${ProcedureCodesData.pointer}"
        val item = ExportItem("latestId", procedureCodes = Some(ProcedureCodesData(None, Seq.empty)))
        val originalValue = None
        item.createDiff(item.copy(procedureCodes = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.procedureCodes)
        )
      }

      "the original version's additionalFiscalReferencesData field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AdditionalFiscalReferencesData.pointer}.${AdditionalFiscalReference.pointer}.#1"
        val item =
          ExportItem("latestId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("", "")))))
        val originalValue = None
        item.createDiff(item.copy(additionalFiscalReferencesData = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, Some(item.additionalFiscalReferencesData.get.references.head))
        )
      }

      "the original version's statisticalValue field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${StatisticalValue.pointer}"
        val item = ExportItem("latestId", statisticalValue = Some(StatisticalValue("latestStatisticalValue")))
        val originalValue = StatisticalValue("originalValue")
        item.createDiff(item.copy(statisticalValue = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.statisticalValue.get)
        )
      }

      "the original version's commodityDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CommodityDetails.pointer}"
        val item =
          ExportItem("latestId", commodityDetails = Some(CommodityDetails(Some("latestCombinedNomenclatureCode"), Some("latestDescriptionOfGoods"))))
        val originalValue = None
        item.createDiff(item.copy(commodityDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.commodityDetails)
        )
      }

      "the original version's dangerousGoodsCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${UNDangerousGoodsCode.pointer}"
        val item = ExportItem("latestId", dangerousGoodsCode = Some(UNDangerousGoodsCode(Some("latestStatisticalValue"))))
        val originalValue = UNDangerousGoodsCode(Some("originalValue"))
        item.createDiff(item.copy(dangerousGoodsCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.dangerousGoodsCode.get)
        )
      }

      "the original version's cusCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CusCode.pointer}"
        val item = ExportItem("latestId", cusCode = Some(CusCode(Some("latestStatisticalValue"))))
        val originalValue = CusCode(Some("originalValue"))
        item.createDiff(item.copy(cusCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.cusCode.get)
        )
      }

      val taricCodes = List(TaricCode("taricCodeOne"), TaricCode("taricCodeTwo"), TaricCode("taricCodeThree"))

      "when taricCodes are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${TaricCode.pointer}"
        withClue("original taricCodes are not present") {
          val item = ExportItem("latestId", taricCodes = Some(taricCodes))
          item.createDiff(item.copy(taricCodes = None), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}", None, Some(Seq(taricCodes(0), taricCodes(1), taricCodes(2))))
          )
        }

        withClue("this taricCodes are not present") {
          val item = ExportItem("latestId", taricCodes = None)
          item.createDiff(item.copy(taricCodes = Some(taricCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}", Some(Seq(taricCodes(0), taricCodes(1), taricCodes(2))), None)
          )
        }

        withClue("original taricCodes are present but empty") {
          val item = ExportItem("latestId", taricCodes = Some(taricCodes))
          item.createDiff(item.copy(taricCodes = Some(List.empty)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(taricCodes(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(taricCodes(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(taricCodes(2)))
          )
        }

        withClue("this taricCodes are present but empty") {
          val item = ExportItem("latestId", taricCodes = Some(List.empty))
          item.createDiff(item.copy(taricCodes = Some(taricCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(taricCodes(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(taricCodes(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(taricCodes(2)), None)
          )
        }

        withClue("both taricCodes contain different number of elements") {
          val item = ExportItem("latestId", taricCodes = Some(taricCodes.drop(1)))
          item.createDiff(item.copy(taricCodes = Some(taricCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(taricCodes(0)), Some(taricCodes(1))),
            constructAlteredField(s"${fieldPointer}.#2", Some(taricCodes(1)), Some(taricCodes(2))),
            constructAlteredField(s"${fieldPointer}.#3", Some(taricCodes(2)), None)
          )
        }

        withClue("both taricCodes contain same elements but in different order") {
          val item = ExportItem("latestId", taricCodes = Some(taricCodes))
          item.createDiff(item.copy(taricCodes = Some(taricCodes.reverse)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(taricCodes(2)), Some(taricCodes(0))),
            constructAlteredField(s"${fieldPointer}.#3", Some(taricCodes(0)), Some(taricCodes(2)))
          )
        }

        withClue("taricCodes contain elements with different values") {
          val newValue = TaricCode("taricCodeFour")
          val item = ExportItem("latestId", taricCodes = Some(List(newValue) ++ taricCodes.drop(1)))
          item.createDiff(item.copy(taricCodes = Some(taricCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(taricCodes(0)), Some(newValue))
          )
        }
      }

      val nactCodes = List(NactCode("nactCodeOne"), NactCode("nactCodeTwo"), NactCode("nactCodeThree"))

      "when nactCodes are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${NactCode.pointer}"
        withClue("original nactCodes are not present") {
          val item = ExportItem("latestId", nactCodes = Some(nactCodes))
          item.createDiff(item.copy(nactCodes = None), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}", None, Some(Seq(nactCodes(0), nactCodes(1), nactCodes(2))))
          )
        }

        withClue("this nactCodes are not present") {
          val item = ExportItem("latestId", nactCodes = None)
          item.createDiff(item.copy(nactCodes = Some(nactCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}", Some(Seq(nactCodes(0), nactCodes(1), nactCodes(2))), None)
          )
        }

        withClue("original nactCodes are present but empty") {
          val item = ExportItem("latestId", nactCodes = Some(nactCodes))
          item.createDiff(item.copy(nactCodes = Some(List.empty)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(nactCodes(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(nactCodes(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(nactCodes(2)))
          )
        }

        withClue("this nactCodes are present but empty") {
          val item = ExportItem("latestId", nactCodes = Some(List.empty))
          item.createDiff(item.copy(nactCodes = Some(nactCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(nactCodes(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(nactCodes(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(nactCodes(2)), None)
          )
        }

        withClue("both nactCodes contain different number of elements") {
          val item = ExportItem("latestId", nactCodes = Some(nactCodes.drop(1)))
          item.createDiff(item.copy(nactCodes = Some(nactCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(nactCodes(0)), Some(nactCodes(1))),
            constructAlteredField(s"${fieldPointer}.#2", Some(nactCodes(1)), Some(nactCodes(2))),
            constructAlteredField(s"${fieldPointer}.#3", Some(nactCodes(2)), None)
          )
        }

        withClue("both nactCodes contain same elements but in different order") {
          val item = ExportItem("latestId", nactCodes = Some(nactCodes))
          item.createDiff(item.copy(nactCodes = Some(nactCodes.reverse)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(nactCodes(2)), Some(nactCodes(0))),
            constructAlteredField(s"${fieldPointer}.#3", Some(nactCodes(0)), Some(nactCodes(2)))
          )
        }

        withClue("nactCodes contain elements with different values") {
          val newValue = NactCode("taricCodeFour")
          val item = ExportItem("latestId", nactCodes = Some(List(newValue) ++ nactCodes.drop(1)))
          item.createDiff(item.copy(nactCodes = Some(nactCodes)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(nactCodes(0)), Some(newValue))
          )
        }
      }

      "the original version's nactExemptionCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${NactCode.pointer}"
        val item = ExportItem("latestId", nactExemptionCode = Some(NactCode("taricCodeFour")))
        val originalValue = NactCode("originalValue")
        item.createDiff(item.copy(nactExemptionCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.nactExemptionCode.get)
        )
      }

      val packageInformations = List(
        PackageInformation(1, "1", Some("typesOfPackagesOne"), Some(1), Some("shippingMarksOne")),
        PackageInformation(2, "2", Some("typesOfPackagesTwo"), Some(2), Some("shippingMarksTwo")),
        PackageInformation(3, "3", Some("typesOfPackagesThree"), Some(3), Some("shippingMarksThree"))
      )

      "when packageInformation are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${PackageInformation.pointer}"
        withClue("original packageInformation are not present") {
          val item = ExportItem("latestId", packageInformation = Some(packageInformations))
          item.createDiff(item.copy(packageInformation = None), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(packageInformations(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(packageInformations(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(packageInformations(2)))
          )
        }

        withClue("this packageInformation are not present") {
          val item = ExportItem("latestId", packageInformation = None)
          item.createDiff(item.copy(packageInformation = Some(packageInformations)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(packageInformations(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(packageInformations(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(packageInformations(2)), None)
          )
        }

        withClue("original packageInformation are present but empty") {
          val item = ExportItem("latestId", packageInformation = Some(packageInformations))
          item.createDiff(item.copy(packageInformation = Some(List.empty)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(packageInformations(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(packageInformations(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(packageInformations(2)))
          )
        }

        withClue("this packageInformation are present but empty") {
          val item = ExportItem("latestId", packageInformation = Some(List.empty))
          item.createDiff(item.copy(packageInformation = Some(packageInformations)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(packageInformations(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(packageInformations(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(packageInformations(2)), None)
          )
        }

        withClue("both packageInformation contain different number of elements") {
          val item = ExportItem("latestId", packageInformation = Some(packageInformations.drop(1)))
          item.createDiff(item.copy(packageInformation = Some(packageInformations)), baseFieldPointer) must contain theSameElementsAs Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(packageInformations(0)), None)
          )
        }

        withClue("packageInformation contain elements with different values") {
          val newValue = PackageInformation(4, "4", Some("typesOfPackagesFour"), Some(3), Some("shippingMarksFour"))
          val item = ExportItem("latestId", packageInformation = Some(List(newValue) ++ packageInformations.drop(1)))
          item.createDiff(item.copy(packageInformation = Some(packageInformations)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(packageInformations(0)), None),
            constructAlteredField(s"${fieldPointer}.#4", None, Some(newValue))
          )
        }
      }

      "the original version's commodityMeasure field has a different value to this one" in {
        val comMesure = CommodityMeasure(Some("latestSupplementaryUnits"), Some(true), Some("latestNetMass"), Some("latestGrossMass"))
        val fieldPointer = s"${baseFieldPointer}.${CommodityMeasure.pointer}.${CommodityMeasure.supplementaryUnitsPointer}"
        val item = ExportItem("latestId", commodityMeasure = Some(comMesure))
        val originalValue = "originalValue"
        item.createDiff(item.copy(commodityMeasure = Some(comMesure.copy(supplementaryUnits = Some(originalValue)))), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, item.commodityMeasure.get.supplementaryUnits.get)
        )
      }

      "the original version's additionalInformation field has a different value to this one" in {
        val additionalInformations = AdditionalInformationData(None, Seq(AdditionalInformation("latestCode", "latestDescription")))
        val fieldPointer = s"${baseFieldPointer}.${AdditionalInformationData.pointer}.${AdditionalInformationData.itemsPointer}.#1"
        val item = ExportItem("latestId", additionalInformation = Some(additionalInformations))
        val originalValue = Seq.empty[AdditionalInformation]
        item.createDiff(item.copy(additionalInformation = Some(additionalInformations.copy(items = originalValue))), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, None, Some(item.additionalInformation.get.items(0)))
        )
      }

      "the original version's additionalDocuments field has a different value to this one" in {
        val additionalDoc = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val additionalDocuments = AdditionalDocuments(None, Seq(additionalDoc))
        val fieldPointer = s"${baseFieldPointer}.${AdditionalDocuments.pointer}.${AdditionalDocuments.documentsPointer}.#1"
        val item = ExportItem("latestId", additionalDocuments = Some(additionalDocuments))
        val originalValue = Seq.empty[AdditionalDocument]
        item.createDiff(item.copy(additionalDocuments = Some(additionalDocuments.copy(documents = originalValue))), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, None, Some(item.additionalDocuments.get.documents(0)))
        )
      }
    }
  }

  "ProcedureCodes.createDiff" should {
    val baseFieldPointer = ProcedureCodesData.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("both values are empty") {
          val procedureCodes = ProcedureCodesData(None, Seq.empty)
          procedureCodes.createDiff(procedureCodes, baseFieldPointer) mustBe Seq.empty
        }

        withClue("procedureCode is None, additionalProcedureCodesData has some values") {
          val procedureCodes = ProcedureCodesData(None, Seq("apc1"))
          procedureCodes.createDiff(procedureCodes, baseFieldPointer) mustBe Seq.empty
        }

        withClue("procedureCode is Some, additionalProcedureCodesData is empty") {
          val procedureCodes = ProcedureCodesData(Some("pc1"), Seq.empty)
          procedureCodes.createDiff(procedureCodes, baseFieldPointer) mustBe Seq.empty
        }

        withClue("procedureCode is Some, additionalProcedureCodesData has some values") {
          val procedureCodes = ProcedureCodesData(Some("pc1"), Seq("apc1", "apc2"))
          procedureCodes.createDiff(procedureCodes, baseFieldPointer) mustBe Seq.empty
        }
      }

      "the original version's procedureCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${ProcedureCodesData.procedureCodesPointer}"
        val procedureCodes = ProcedureCodesData(Some("pc1"), Seq("apc1", "apc2"))
        val originalValue = "pc2"
        procedureCodes.createDiff(procedureCodes.copy(procedureCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, procedureCodes.procedureCode.get)
        )
      }

      val apcs = Seq("apc1", "apc2", "apc3")

      "when additionalProcedureCodesData are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${ProcedureCodesData.additionalProcedureCodesPointer}"
        withClue("original procedureCode's additionalProcedureCodes are not present") {
          val procedureCodes = ProcedureCodesData(None, apcs)
          procedureCodes.createDiff(procedureCodes.copy(additionalProcedureCodes = Seq.empty), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(apcs(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(apcs(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(apcs(2)))
          )
        }

        withClue("this procedureCode's additionalProcedureCodes are not present") {
          val procedureCodes = ProcedureCodesData(None, Seq.empty)
          procedureCodes.createDiff(procedureCodes.copy(additionalProcedureCodes = apcs), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(apcs(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(apcs(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(apcs(2)), None)
          )
        }

        withClue("both procedureCode additionalProcedureCodes contain different number of elements") {
          val procedureCodes = ProcedureCodesData(None, apcs.drop(1))
          procedureCodes.createDiff(procedureCodes.copy(additionalProcedureCodes = apcs), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(apcs(0)), Some(apcs(1))),
            constructAlteredField(s"${fieldPointer}.#2", Some(apcs(1)), Some(apcs(2))),
            constructAlteredField(s"${fieldPointer}.#3", Some(apcs(2)), None)
          )
        }

        withClue("both procedureCode additionalProcedureCodes contain same elements but in different order") {
          val procedureCodes = ProcedureCodesData(None, apcs)
          procedureCodes.createDiff(procedureCodes.copy(additionalProcedureCodes = apcs.reverse), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(apcs(2)), Some(apcs(0))),
            constructAlteredField(s"${fieldPointer}.#3", Some(apcs(0)), Some(apcs(2)))
          )
        }

        withClue("procedureCode's additionalProcedureCodes contain elements with different values") {
          val procedureCodes = ProcedureCodesData(None, Seq("apc4") ++ apcs.drop(1))
          procedureCodes.createDiff(procedureCodes.copy(additionalProcedureCodes = apcs), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(apcs(0)), Some("apc4"))
          )
        }
      }
    }
  }

  "AdditionalFiscalReference.createDiff" should {
    val baseFieldPointer = AdditionalFiscalReference.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val additionalFiscalReference = AdditionalFiscalReference("latestCountry", "latestReference")
        additionalFiscalReference.createDiff(additionalFiscalReference, baseFieldPointer) mustBe Seq.empty
      }

      "the original version's country field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalFiscalReference = AdditionalFiscalReference("latestCountry", "latestReference")
        val originalValue = "originalCountry"
        additionalFiscalReference.createDiff(additionalFiscalReference.copy(country = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalFiscalReference.country)
        )
      }

      "the original version's reference field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalFiscalReference = AdditionalFiscalReference("latestCountry", "latestReference")
        val originalValue = "originalReference"
        additionalFiscalReference.createDiff(additionalFiscalReference.copy(reference = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalFiscalReference.reference)
        )
      }
    }
  }

  "AdditionalFiscalReferences.createDiff" should {
    val baseFieldPointer = ExportItem.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val additionalFiscalReferences = AdditionalFiscalReferencesData(Seq.empty)
        additionalFiscalReferences.createDiff(additionalFiscalReferences, baseFieldPointer) mustBe Seq.empty
      }

      val references = Seq(
        AdditionalFiscalReference("countryOne", "referenceOne"),
        AdditionalFiscalReference("countryTwo", "referenceTwo"),
        AdditionalFiscalReference("countryThree", "referenceThree")
      )

      "when references are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.#1.${AdditionalFiscalReferencesData.pointer}"
        withClue("original AdditionalFiscalReferences references are not present") {
          val additionalFiscalReferences = AdditionalFiscalReferencesData(references)
          additionalFiscalReferences.createDiff(additionalFiscalReferences.copy(references = Seq.empty), fieldPointer, None) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.references.#1", None, Some(references(0))),
            constructAlteredField(s"${fieldPointer}.references.#2", None, Some(references(1))),
            constructAlteredField(s"${fieldPointer}.references.#3", None, Some(references(2)))
          )
        }

        withClue("this AdditionalFiscalReferences references are not present") {
          val additionalFiscalReferences = AdditionalFiscalReferencesData(Seq.empty)
          additionalFiscalReferences.createDiff(additionalFiscalReferences.copy(references = references), fieldPointer, None) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(0)), None),
            constructAlteredField(s"${fieldPointer}.references.#2", Some(references(1)), None),
            constructAlteredField(s"${fieldPointer}.references.#3", Some(references(2)), None)
          )
        }

        withClue("both AdditionalFiscalReferences references contain different number of elements") {
          val additionalFiscalReferences = AdditionalFiscalReferencesData(references.drop(1))
          additionalFiscalReferences.createDiff(additionalFiscalReferences.copy(references = references), fieldPointer, None) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(0).country), Some(references(1).country)),
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(0).reference), Some(references(1).reference)),
            constructAlteredField(s"${fieldPointer}.references.#2", Some(references(1).country), Some(references(2).country)),
            constructAlteredField(s"${fieldPointer}.references.#2", Some(references(1).reference), Some(references(2).reference)),
            constructAlteredField(s"${fieldPointer}.references.#3", Some(references(2)), None)
          )
        }

        withClue("both AdditionalFiscalReferences references contain same elements but in different order") {
          val additionalFiscalReferences = AdditionalFiscalReferencesData(references)
          additionalFiscalReferences.createDiff(additionalFiscalReferences.copy(references = references.reverse), fieldPointer, None) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(2).country), Some(references(0).country)),
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(2).reference), Some(references(0).reference)),
            constructAlteredField(s"${fieldPointer}.references.#3", Some(references(0).country), Some(references(2).country)),
            constructAlteredField(s"${fieldPointer}.references.#3", Some(references(0).reference), Some(references(2).reference))
          )
        }

        withClue("container seals contain elements with different values") {
          val newValue = AdditionalFiscalReference("countryFour", "referenceFour")
          val additionalFiscalReferences = AdditionalFiscalReferencesData(Seq(newValue) ++ references.drop(1))
          additionalFiscalReferences.createDiff(additionalFiscalReferences.copy(references = references), fieldPointer, None) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(0).country), Some(newValue.country)),
            constructAlteredField(s"${fieldPointer}.references.#1", Some(references(0).reference), Some(newValue.reference))
          )
        }
      }
    }
  }

  "CommodityDetails.createDiff" should {
    val baseFieldPointer = CommodityDetails.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("both values are empty") {
          val commodityDetails = CommodityDetails(None, None)
          commodityDetails.createDiff(commodityDetails, baseFieldPointer) mustBe Seq.empty
        }

        withClue("combinedNomenclatureCode is None, descriptionOfGoods has some values") {
          val commodityDetails = CommodityDetails(None, Some("latestDescriptionOfGoods"))
          commodityDetails.createDiff(commodityDetails, baseFieldPointer) mustBe Seq.empty
        }

        withClue("combinedNomenclatureCode is Some, descriptionOfGoods is empty") {
          val commodityDetails = CommodityDetails(Some("latestCombinedNomenclatureCode"), None)
          commodityDetails.createDiff(commodityDetails, baseFieldPointer) mustBe Seq.empty
        }

        withClue("combinedNomenclatureCode is Some, descriptionOfGoods has some values") {
          val commodityDetails = CommodityDetails(Some("latestCombinedNomenclatureCode"), Some("latestDescriptionOfGoods"))
          commodityDetails.createDiff(commodityDetails, baseFieldPointer) mustBe Seq.empty
        }
      }

      "the original version's combinedNomenclatureCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CommodityDetails.combinedNomenclatureCodePointer}"
        val commodityDetails = CommodityDetails(Some("latestCombinedNomenclatureCode"), Some("latestDescriptionOfGoods"))
        val originalValue = "originalCombinedNomenclatureCode"
        commodityDetails.createDiff(commodityDetails.copy(combinedNomenclatureCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, commodityDetails.combinedNomenclatureCode.get)
        )
      }

      "the original version's descriptionOfGoods field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CommodityDetails.descriptionOfGoodsPointer}"
        val commodityDetails = CommodityDetails(Some("latestCombinedNomenclatureCode"), Some("latestDescriptionOfGoods"))
        val originalValue = "originalDescriptionOfGoods"
        commodityDetails.createDiff(commodityDetails.copy(descriptionOfGoods = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, commodityDetails.descriptionOfGoods.get)
        )
      }
    }
  }

  "PackageInformation.createDiff" should {
    val baseFieldPointer = PackageInformation.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("all values are empty") {
          val packageInformation = PackageInformation(1, "1", None, None, None)
          packageInformation.createDiff(packageInformation, baseFieldPointer) mustBe Seq.empty
        }

        withClue("typesOfPackages is None, the rest have some values") {
          val packageInformation = PackageInformation(1, "1", None, Some(2), Some("latestShippingMarks"))
          packageInformation.createDiff(packageInformation, baseFieldPointer) mustBe Seq.empty
        }

        withClue("typesOfPackages is None, the rest have some values") {
          val packageInformation = PackageInformation(1, "1", Some("latestTypesOfPackages"), None, Some("latestShippingMarks"))
          packageInformation.createDiff(packageInformation, baseFieldPointer) mustBe Seq.empty
        }

        withClue("typesOfPackages is None, the rest have some values") {
          val packageInformation = PackageInformation(1, "1", Some("latestTypesOfPackages"), Some(2), None)
          packageInformation.createDiff(packageInformation, baseFieldPointer) mustBe Seq.empty
        }
      }

      "the original version's id field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${PackageInformation.idPointer}"
        val packageInformation = PackageInformation(1, "1", Some("latestTypesOfPackages"), Some(2), Some("latestShippingMarks"))
        val originalValue = "2"
        packageInformation.createDiff(packageInformation.copy(id = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, packageInformation.id)
        )
      }

      "the original version's id typesOfPackages has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${PackageInformation.typesOfPackagesPointer}"
        val packageInformation = PackageInformation(1, "1", Some("latestTypesOfPackages"), Some(2), Some("latestShippingMarks"))
        val originalValue = "originalValue"
        packageInformation.createDiff(packageInformation.copy(typesOfPackages = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, packageInformation.typesOfPackages.get)
        )
      }

      "the original version's id numberOfPackages has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${PackageInformation.numberOfPackagesPointer}"
        val packageInformation = PackageInformation(1, "1", Some("latestTypesOfPackages"), Some(2), Some("latestShippingMarks"))
        val originalValue = 1
        packageInformation.createDiff(packageInformation.copy(numberOfPackages = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, packageInformation.numberOfPackages.get)
        )
      }

      "the original version's id shippingMarksPointer has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${PackageInformation.shippingMarksPointer}"
        val packageInformation = PackageInformation(1, "1", Some("latestTypesOfPackages"), Some(2), Some("latestShippingMarks"))
        val originalValue = "originalValue"
        packageInformation.createDiff(packageInformation.copy(shippingMarks = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, packageInformation.shippingMarks.get)
        )
      }
    }
  }

  "CommodityMeasure.createDiff" should {
    val baseFieldPointer = CommodityMeasure.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val commodityMeasure = CommodityMeasure(Some("latestSupplementaryUnits"), Some(true), Some("latestNetMass"), Some("latestGrossMass"))
        commodityMeasure.createDiff(commodityMeasure, baseFieldPointer) mustBe Seq.empty
      }

      "the original version's supplementaryUnits field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CommodityMeasure.supplementaryUnitsPointer}"
        val commodityMeasure = CommodityMeasure(Some("latestSupplementaryUnits"), Some(true), Some("latestNetMass"), Some("latestGrossMass"))
        val originalValue = "originalCountry"
        commodityMeasure.createDiff(commodityMeasure.copy(supplementaryUnits = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, commodityMeasure.supplementaryUnits.get)
        )
      }

      "the original version's netMass field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CommodityMeasure.netMassPointer}"
        val commodityMeasure = CommodityMeasure(Some("latestSupplementaryUnits"), Some(true), Some("latestNetMass"), Some("latestGrossMass"))
        val originalValue = "originalCountry"
        commodityMeasure.createDiff(commodityMeasure.copy(netMass = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, commodityMeasure.netMass.get)
        )
      }

      "the original version's grossMass field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CommodityMeasure.grossMassPointer}"
        val commodityMeasure = CommodityMeasure(Some("latestSupplementaryUnits"), Some(true), Some("latestNetMass"), Some("latestGrossMass"))
        val originalValue = "originalCountry"
        commodityMeasure.createDiff(commodityMeasure.copy(grossMass = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, commodityMeasure.grossMass.get)
        )
      }
    }
  }

  "AdditionalInformation.createDiff" should {
    val baseFieldPointer = AdditionalInformation.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val additionalInformation = AdditionalInformation("latestCode", "latestDescription")
        additionalInformation.createDiff(additionalInformation, baseFieldPointer) mustBe Seq.empty
      }

      "the original version's code field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalInformation = AdditionalInformation("latestCode", "latestDescription")
        val originalValue = "originalCountry"
        additionalInformation.createDiff(additionalInformation.copy(code = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalInformation.code)
        )
      }

      "the original version's description field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalInformation = AdditionalInformation("latestCode", "latestDescription")
        val originalValue = "originalCountry"
        additionalInformation.createDiff(additionalInformation.copy(description = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalInformation.description)
        )
      }
    }
  }

  "AdditionalInformations.createDiff" should {
    val baseFieldPointer = AdditionalInformationData.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val additionalInformations = AdditionalInformationData(None, Seq.empty)
        additionalInformations.createDiff(additionalInformations, baseFieldPointer) mustBe Seq.empty
      }

      val items = Seq(
        AdditionalInformation("codeOne", "descriptionOne"),
        AdditionalInformation("codeTwo", "descriptionTwo"),
        AdditionalInformation("codeThree", "descriptionThree")
      )

      "when items are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${AdditionalInformationData.itemsPointer}"
        withClue("original AdditionalInformations items are not present") {
          val additionalInformations = AdditionalInformationData(None, items)
          additionalInformations.createDiff(additionalInformations.copy(items = Seq.empty), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(items(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(items(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(items(2)))
          )
        }

        withClue("this AdditionalInformations items are not present") {
          val additionalInformations = AdditionalInformationData(None, Seq.empty)
          additionalInformations.createDiff(additionalInformations.copy(items = items), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(items(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(items(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(items(2)), None)
          )
        }

        withClue("both AdditionalInformations items contain different number of elements") {
          val additionalInformations = AdditionalInformationData(None, items.drop(1))
          additionalInformations.createDiff(additionalInformations.copy(items = items), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(items(0).code), Some(items(1).code)),
            constructAlteredField(s"${fieldPointer}.#1", Some(items(0).description), Some(items(1).description)),
            constructAlteredField(s"${fieldPointer}.#2", Some(items(1).code), Some(items(2).code)),
            constructAlteredField(s"${fieldPointer}.#2", Some(items(1).description), Some(items(2).description)),
            constructAlteredField(s"${fieldPointer}.#3", Some(items(2)), None)
          )
        }

        withClue("both AdditionalInformations items contain same elements but in different order") {
          val additionalInformations = AdditionalInformationData(None, items)
          additionalInformations.createDiff(additionalInformations.copy(items = items.reverse), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(items(2).code), Some(items(0).code)),
            constructAlteredField(s"${fieldPointer}.#1", Some(items(2).description), Some(items(0).description)),
            constructAlteredField(s"${fieldPointer}.#3", Some(items(0).code), Some(items(2).code)),
            constructAlteredField(s"${fieldPointer}.#3", Some(items(0).description), Some(items(2).description))
          )
        }

        withClue("AdditionalInformations items contain elements with different values") {
          val newValue = AdditionalInformation("codeFour", "descriptionFour")
          val additionalInformations = AdditionalInformationData(None, Seq(newValue) ++ items.drop(1))
          additionalInformations.createDiff(additionalInformations.copy(items = items), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(items(0).code), Some(newValue.code)),
            constructAlteredField(s"${fieldPointer}.#1", Some(items(0).description), Some(newValue.description))
          )
        }
      }
    }
  }

  "DocumentWriteOff.createDiff" should {
    val baseFieldPointer = DocumentWriteOff.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val documentWriteOff = DocumentWriteOff(None, None)
        documentWriteOff.createDiff(documentWriteOff, baseFieldPointer) mustBe Seq.empty
      }

      "the original version's measurementUnit field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val documentWriteOff = DocumentWriteOff(Some("latestMeasurementUnitPointer"), Some(BigDecimal(1)))
        val originalValue = "originalCountry"
        documentWriteOff.createDiff(documentWriteOff.copy(measurementUnit = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, documentWriteOff.measurementUnit.get)
        )
      }

      "the original version's documentQuantity field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val documentWriteOff = DocumentWriteOff(Some("latestMeasurementUnitPointer"), Some(BigDecimal(1)))
        val originalValue = BigDecimal(2)
        documentWriteOff.createDiff(documentWriteOff.copy(documentQuantity = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, documentWriteOff.documentQuantity.get)
        )
      }
    }
  }

  "AdditionalDocument.createDiff" should {
    val baseFieldPointer = AdditionalDocument.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val additionalDocument = AdditionalDocument(None, None, None, None, None, None, None)
        additionalDocument.createDiff(additionalDocument, baseFieldPointer) mustBe Seq.empty
      }

      "the original version's documentTypeCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = "originalCountry"
        additionalDocument.createDiff(additionalDocument.copy(documentTypeCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalDocument.documentTypeCode.get)
        )
      }

      "the original version's documentIdentifier field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = "originalCountry"
        additionalDocument.createDiff(additionalDocument.copy(documentIdentifier = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalDocument.documentIdentifier.get)
        )
      }

      "the original version's documentStatus field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = "originalCountry"
        additionalDocument.createDiff(additionalDocument.copy(documentStatus = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalDocument.documentStatus.get)
        )
      }

      "the original version's documentStatusReason field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = "originalCountry"
        additionalDocument.createDiff(additionalDocument.copy(documentStatusReason = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalDocument.documentStatusReason.get)
        )
      }

      "the original version's issuingAuthorityName field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = "originalCountry"
        additionalDocument.createDiff(additionalDocument.copy(issuingAuthorityName = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, additionalDocument.issuingAuthorityName.get)
        )
      }

      "the original version's dateOfValidity field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = Date(Some(1), None, None)
        additionalDocument.createDiff(additionalDocument.copy(dateOfValidity = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.day, additionalDocument.dateOfValidity.get.day)
        )
      }

      "the original version's documentWriteOff field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val additionalDocument = AdditionalDocument(
          Some("latestDocumentTypeCode"),
          Some("latestDocumentIdentifier"),
          Some("latestDocumentStatus"),
          Some("latestDocumentStatusReason"),
          Some("latestIssuingAuthorityName"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
        val originalValue = DocumentWriteOff(Some("originalValue"), None)
        additionalDocument.createDiff(additionalDocument.copy(documentWriteOff = Some(originalValue)), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.measurementUnit, additionalDocument.documentWriteOff.get.measurementUnit)
        )
      }
    }
  }

  "AdditionalDocuments.createDiff" should {
    val baseFieldPointer = AdditionalDocuments.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val additionalDocuments = AdditionalDocuments(None, Seq.empty)
        additionalDocuments.createDiff(additionalDocuments, baseFieldPointer) mustBe Seq.empty
      }

      val documents = Seq(
        AdditionalDocument(
          Some("documentTypeCodeOne"),
          Some("documentIdentifierOne"),
          Some("DocumentStatusOne"),
          Some("documentStatusReasonOne"),
          Some("issuingAuthorityNameOne"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        ),
        AdditionalDocument(
          Some("documentTypeCodeTwo"),
          Some("documentIdentifierTwo"),
          Some("DocumentStatusTwo"),
          Some("documentStatusReasonTwo"),
          Some("issuingAuthorityNameTwo"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        ),
        AdditionalDocument(
          Some("documentTypeCodeThree"),
          Some("documentIdentifierThree"),
          Some("DocumentStatusThree"),
          Some("documentStatusReasonThree"),
          Some("issuingAuthorityNameThree"),
          Some(Date(None, None, None)),
          Some(DocumentWriteOff(None, None))
        )
      )

      "when items are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${AdditionalDocuments.documentsPointer}"
        withClue("original AdditionalDocuments items are not present") {
          val additionalDocuments = AdditionalDocuments(None, documents)
          additionalDocuments.createDiff(additionalDocuments.copy(documents = Seq.empty), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(documents(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(documents(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(documents(2)))
          )
        }

        withClue("this AdditionalDocuments documents are not present") {
          val additionalDocuments = AdditionalDocuments(None, Seq.empty)
          additionalDocuments.createDiff(additionalDocuments.copy(documents = documents), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(documents(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(2)), None)
          )
        }

        withClue("both AdditionalDocuments documents contain different number of elements") {
          val additionalDocuments = AdditionalDocuments(None, documents.drop(1))
          additionalDocuments.createDiff(additionalDocuments.copy(documents = documents), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentTypeCode.get), Some(documents(1).documentTypeCode.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentIdentifier.get), Some(documents(1).documentIdentifier.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentStatus.get), Some(documents(1).documentStatus.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentStatusReason.get), Some(documents(1).documentStatusReason.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).issuingAuthorityName.get), Some(documents(1).issuingAuthorityName.get)),
            constructAlteredField(s"${fieldPointer}.#2", Some(documents(1).documentTypeCode.get), Some(documents(2).documentTypeCode.get)),
            constructAlteredField(s"${fieldPointer}.#2", Some(documents(1).documentIdentifier.get), Some(documents(2).documentIdentifier.get)),
            constructAlteredField(s"${fieldPointer}.#2", Some(documents(1).documentStatus.get), Some(documents(2).documentStatus.get)),
            constructAlteredField(s"${fieldPointer}.#2", Some(documents(1).documentStatusReason.get), Some(documents(2).documentStatusReason.get)),
            constructAlteredField(s"${fieldPointer}.#2", Some(documents(1).issuingAuthorityName.get), Some(documents(2).issuingAuthorityName.get)),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(2)), None)
          )
        }

        withClue("both AdditionalDocuments documents contain same elements but in different order") {
          val additionalDocuments = AdditionalDocuments(None, documents)
          additionalDocuments.createDiff(additionalDocuments.copy(documents = documents.reverse), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(2).documentTypeCode.get), Some(documents(0).documentTypeCode.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(2).documentIdentifier.get), Some(documents(0).documentIdentifier.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(2).documentStatus.get), Some(documents(0).documentStatus.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(2).documentStatusReason.get), Some(documents(0).documentStatusReason.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(2).issuingAuthorityName.get), Some(documents(0).issuingAuthorityName.get)),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(0).documentTypeCode.get), Some(documents(2).documentTypeCode.get)),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(0).documentIdentifier.get), Some(documents(2).documentIdentifier.get)),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(0).documentStatus.get), Some(documents(2).documentStatus.get)),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(0).documentStatusReason.get), Some(documents(2).documentStatusReason.get)),
            constructAlteredField(s"${fieldPointer}.#3", Some(documents(0).issuingAuthorityName.get), Some(documents(2).issuingAuthorityName.get))
          )
        }

        withClue("AdditionalDocuments documents contain elements with different values") {
          val newValue = AdditionalDocument(
            Some("documentTypeCodeFour"),
            Some("documentIdentifierFour"),
            Some("DocumentStatusFour"),
            Some("documentStatusReasonFour"),
            Some("issuingAuthorityNameFour"),
            Some(Date(None, None, None)),
            Some(DocumentWriteOff(None, None))
          )
          val additionalDocuments = AdditionalDocuments(None, Seq(newValue) ++ documents.drop(1))
          additionalDocuments.createDiff(additionalDocuments.copy(documents = documents), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentTypeCode.get), Some(newValue.documentTypeCode.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentIdentifier.get), Some(newValue.documentIdentifier.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentStatus.get), Some(newValue.documentStatus.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).documentStatusReason.get), Some(newValue.documentStatusReason.get)),
            constructAlteredField(s"${fieldPointer}.#1", Some(documents(0).issuingAuthorityName.get), Some(newValue.issuingAuthorityName.get))
          )
        }
      }
    }
  }
}
