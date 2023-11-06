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

package views.declaration.summary.sections

import base.Injector
import controllers.declaration.routes._
import forms.common.YesNoAnswer.Yes
import forms.declaration._
import models.DeclarationType.STANDARD
import models.declaration.CommodityMeasure
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.item_section

class ItemSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val itemSection = instanceOf[item_section]

  val commodityMeasure = CommodityMeasure(Some("12"), Some(false), Some("666"), Some("555"))

  private val itemWithAnswers = anItem(
    withItemId(itemId),
    withSequenceId(sequenceId.toInt),
    withProcedureCodes(Some("1234"), Seq("000", "111")),
    withFiscalInformation(FiscalInformation("Yes")),
    withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "1234")))),
    withStatisticalValue("123"),
    withCommodityDetails(CommodityDetails(Some("1234567890"), Some("description"))),
    withUNDangerousGoodsCode(UNDangerousGoodsCode(Some("345"))),
    withCUSCode(CusCode(Some("321"))),
    withTaricCodes(TaricCode("999"), TaricCode("888")),
    withNactCodes(NactCode("111"), NactCode("222")),
    withNactExemptionCode(NactCode("VATE")),
    withPackageInformation("PB", 10, "marks"),
    withCommodityMeasure(commodityMeasure),
    withAdditionalInformation("1234", "additionalDescription"),
    withIsLicenseRequired(),
    withAdditionalDocuments(Yes, withAdditionalDocument("C501", "GBAEOC1342"))
  )

  private val keyAD = "item.additionalDocuments"
  private val keyAI = "item.additionalInformation"
  private val keyPI = "item.packageInformation"

  private val itemWithoutAnswers = anItem(withItemId(itemId), withSequenceId(1))

  "Item section" when {

    "the item has answers and" when {

      "actions are enabled" should {
        val view = itemSection(itemWithAnswers, 0, STANDARD)(messages)

        "have a 'Item' header" in {
          val header = view.getElementsByClass("govuk-heading-s").get(0)
          header.text mustBe messages(s"declaration.summary.item", sequenceId)
        }

        "have a 'change' link at header level" in {
          val action = view.getElementById("item-header-action")
          action.text mustBe messages(s"declaration.summary.item.remove")
          action must haveHref(RemoveItemsSummaryController.displayRemoveItemConfirmationPage(itemId, true))
        }

        "have a 'procedure code' row" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          val call = Some(ProcedureCodesController.displayPage(itemId))
          checkSummaryRow(row, "item.procedureCode", "1234", call, "item.procedureCode")
        }

        "have an 'additional procedure codes' row" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          val call = Some(AdditionalProcedureCodesController.displayPage(itemId))
          checkSummaryRow(row, "item.additionalProcedureCodes", "000 111", call, "item.additionalProcedureCodes")
        }

        "have an 'onward supply answer' row" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          val call = Some(FiscalInformationController.displayPage(itemId))
          checkSummaryRow(row, "item.onwardSupplyRelief", messages("site.yes"), call, "item.onwardSupplyRelief")
        }

        "have a 'VAT answer' row" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          val call = Some(AdditionalFiscalReferencesController.displayPage(itemId))
          checkSummaryRow(row, "item.VATdetails", "GB1234", call, "item.VATdetails")
        }

        "have a 'commodity code' row" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          val call = Some(CommodityDetailsController.displayPage(itemId))
          checkSummaryRow(row, "item.commodityCode", "1234567890", call, "item.commodityCode")
        }

        "have a 'goods description' row" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          val call = Some(CommodityDetailsController.displayPage(itemId))
          checkSummaryRow(row, "item.goodsDescription", "description", call, "item.goodsDescription")
        }

        "have a 'undangerous goods code' row" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          val call = Some(UNDangerousGoodsCodeController.displayPage(itemId))
          checkSummaryRow(row, "item.unDangerousGoodsCode", "345", call, "item.unDangerousGoodsCode")
        }

        "have a 'cus code' row" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          val call = Some(CusCodeController.displayPage(itemId))
          checkSummaryRow(row, "item.cusCode", "321", call, "item.cusCode")
        }

        "have a 'taric codes' row" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          val call = Some(TaricCodeSummaryController.displayPage(itemId))
          checkSummaryRow(row, "item.taricAdditionalCodes", "999, 888", call, "item.taricAdditionalCodes")
        }

        "have a 'nact codes' row" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          val call = Some(NactCodeSummaryController.displayPage(itemId))
          checkSummaryRow(row, "item.nationalAdditionalCodes", "111, 222", call, "item.nationalAdditionalCodes")
        }

        "have a 'zero rated for vat' row" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          val call = Some(ZeroRatedForVatController.displayPage(itemId))
          val value = messages(s"declaration.summary.item.zeroRatedForVat.VATE")
          checkSummaryRow(row, "item.zeroRatedForVat", value, call, "item.zeroRatedForVat")
        }

        "have a 'statistical item value' row" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          val call = Some(StatisticalValueController.displayPage(itemId))
          checkSummaryRow(row, "item.itemValue", "123", call, "item.itemValue")
        }

        "have a 'Package Information' row" when {
          "packageInformation is defined but has no data" in {
            val item = anItemAfter(itemWithAnswers, withPackageInformation(List.empty))
            val view = itemSection(item, 0, STANDARD)(messages)

            val noDataRow = view.getElementsByClass("item-1-package-information-heading")
            val call = Some(PackageInformationSummaryController.displayPage(itemId))
            checkSummaryRow(noDataRow, keyPI, messages("site.none"), call, keyPI)
          }
        }

        "have a 'Package Information' section" in {
          val pi1 = PackageInformation(1, "pi1", Some("PB"), Some(1), Some("markings1"))
          val pi2 = PackageInformation(2, "pi2", Some("type2"), Some(2), None)
          val pi3 = PackageInformation(3, "pi3", Some("type3"), None, Some("markings3"))
          val pi4 = PackageInformation(4, "pi4", None, Some(4), Some("markings4"))
          val pi5 = PackageInformation(5, "pi5", Some("type5"), None, None)
          val pi6 = PackageInformation(6, "pi6", None, Some(6), None)
          val pi7 = PackageInformation(7, "pi7", None, None, Some("markings7"))
          val item = anItemAfter(itemWithoutAnswers, withPackageInformation(pi1, pi2, pi3, pi4, pi5, pi6, pi7))
          val view = itemSection(item, 0, STANDARD)(messages)

          val call = Some(PackageInformationSummaryController.displayPage(itemId))

          val headingRow = view.getElementsByClass("item-1-package-information-heading")
          checkSummaryRow(headingRow, keyPI, "", None, "ign")

          val pi1Row1 = view.getElementsByClass("item-1-package-information-1-type")
          checkSummaryRow(pi1Row1, s"$keyPI.type", "Pallet, box Combined open-ended box and pallet (PB)", call, keyPI)

          val pi1Row2 = view.getElementsByClass("item-1-package-information-1-number")
          checkSummaryRow(pi1Row2, s"$keyPI.number", "1")

          val pi1Row3 = view.getElementsByClass("item-1-package-information-1-markings")
          checkSummaryRow(pi1Row3, s"$keyPI.markings", "markings1")

          val pi2Row4 = view.getElementsByClass("item-1-package-information-2-type")
          checkSummaryRow(pi2Row4, s"$keyPI.type", "Unknown package type (type2)", call, keyPI)

          val pi2Row5 = view.getElementsByClass("item-1-package-information-2-number")
          checkSummaryRow(pi2Row5, s"$keyPI.number", "2")

          val pi3Row6 = view.getElementsByClass("item-1-package-information-3-type")
          checkSummaryRow(pi3Row6, s"$keyPI.type", "Unknown package type (type3)", call, keyPI)

          val pi3Row7 = view.getElementsByClass("item-1-package-information-3-markings")
          checkSummaryRow(pi3Row7, s"$keyPI.markings", "markings3")

          val pi4Row8 = view.getElementsByClass("item-1-package-information-4-number")
          checkSummaryRow(pi4Row8, s"$keyPI.number", "4", call, keyPI)

          val pi4Row9 = view.getElementsByClass("item-1-package-information-4-markings")
          checkSummaryRow(pi4Row9, s"$keyPI.markings", "markings4")

          val pi5Row10 = view.getElementsByClass("item-1-package-information-5-type")
          checkSummaryRow(pi5Row10, s"$keyPI.type", "Unknown package type (type5)", call, keyPI)

          val pi6Row11 = view.getElementsByClass("item-1-package-information-6-number")
          checkSummaryRow(pi6Row11, s"$keyPI.number", "6", call, keyPI)

          val pi7Row12 = view.getElementsByClass("item-1-package-information-7-markings")
          checkSummaryRow(pi7Row12, s"$keyPI.markings", "markings7", call, keyPI)
        }

        "have a 'supplementary units' row" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          val call = Some(SupplementaryUnitsController.displayPage(itemId))
          checkSummaryRow(row, "item.supplementaryUnits", "12", call, "item.supplementaryUnits")
        }

        // CEDS-3668
        "NOT have a 'Supplementary Units' row" when {
          "the declaration has a 'CommodityMeasure' instance with 'supplementaryUnits' undefined" in {
            val item = itemWithAnswers.copy(commodityMeasure = Some(commodityMeasure.copy(supplementaryUnits = None)))
            val view = itemSection(item, 0, STANDARD)(messages)
            view.getElementsByClass("item-1-supplementaryUnits-row") mustBe empty
          }
        }

        "have a 'gross weight' row" in {
          val row = view.getElementsByClass("item-1-grossWeight-row")
          val call = Some(CommodityMeasureController.displayPage(itemId))
          checkSummaryRow(row, "item.grossWeight", "666", call, "item.grossWeight")
        }

        "have a 'net weight' row" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          val call = Some(CommodityMeasureController.displayPage(itemId))
          checkSummaryRow(row, "item.netWeight", "555", call, "item.netWeight")
        }

        "have an 'Additional Information' row" when {
          "additionalInformation is defined but has no data" in {
            val item = anItemAfter(itemWithAnswers, withoutAdditionalInformation(true))
            val view = itemSection(item, 0, STANDARD)(messages)

            val noDataRow = view.getElementsByClass("item-1-additional-information-heading")
            val call = Some(AdditionalInformationRequiredController.displayPage(itemId))
            checkSummaryRow(noDataRow, keyAI, messages("site.none"), call, keyAI)
          }
        }

        "have an 'Additional Information' section" in {
          val ai1 = AdditionalInformation("Code1", "Exporter1")
          val ai2 = AdditionalInformation("Code2", "Exporter2")
          val item = anItemAfter(itemWithoutAnswers, withAdditionalInformation(ai1, ai2))
          val view = itemSection(item, 0, STANDARD)(messages)

          val call = Some(AdditionalInformationController.displayPage(itemId))

          val headingRow = view.getElementsByClass("item-1-additional-information-heading")
          checkSummaryRow(headingRow, keyAI, "", None, "ign")

          val info1Row1 = view.getElementsByClass("item-1-additional-information-1-code")
          checkSummaryRow(info1Row1, s"$keyAI.code", "Code1", call, keyAI)

          val info1Row2 = view.getElementsByClass("item-1-additional-information-1-description")
          checkSummaryRow(info1Row2, s"$keyAI.description", "Exporter1")

          val info2Row3 = view.getElementsByClass("item-1-additional-information-2-code")
          checkSummaryRow(info2Row3, s"$keyAI.code", "Code2", call, keyAI)

          val info2Row4 = view.getElementsByClass("item-1-additional-information-2-description")
          checkSummaryRow(info2Row4, s"$keyAI.description", "Exporter2")
        }

        "have a 'Licences' row" when {
          "isLicenceRequired is 'yes' (or 'no', but not None) and" when {
            val call = Some(IsLicenceRequiredController.displayPage(itemId))

            "additionalDocuments is undefined" in {
              val item = anItemAfter(itemWithAnswers, withoutAdditionalDocuments())
              val view = itemSection(item, 0, STANDARD)(messages)

              val licensesRow = view.getElementsByClass("item-1-licences")
              checkSummaryRow(licensesRow, "item.licences", messages("site.yes"), call, "item.licences")
            }

            "additionalDocuments is defined (but has no documents)" should {
              "also have an 'Additional documents' row" in {
                val item = anItemAfter(itemWithAnswers, withoutAdditionalDocuments(true))
                val view = itemSection(item, 0, STANDARD)(messages)

                val licensesRow = view.getElementsByClass("item-1-licences")
                checkSummaryRow(licensesRow, "item.licences", messages("site.yes"), call, "item.licences")

                val noDocumentsRow = view.getElementsByClass("item-1-additional-documents-heading")
                val call1 = Some(AdditionalDocumentsController.displayPage(itemId))
                checkSummaryRow(noDocumentsRow, keyAD, messages("site.none"), call1, keyAD)
              }
            }
          }
        }

        "have an 'Additional documents' section that" should {
          "also contains a 'Licenses' row" in {
            val document1 = withAdditionalDocument("C501", "GBAEOC1342")
            val document2 = withAdditionalDocument(Some("A123"), None)
            val document3 = withAdditionalDocument(None, Some("GBAEOS9876"))
            val item = anItemAfter(itemWithoutAnswers, withIsLicenseRequired(), withAdditionalDocuments(Yes, document1, document2, document3))
            val view = itemSection(item, 0, STANDARD)(messages)

            val call = Some(AdditionalDocumentsController.displayPage(itemId))

            val headingRow = view.getElementsByClass("item-1-additional-documents-heading")
            checkSummaryRow(headingRow, keyAD, "", None, "ign")

            val licensesRow = view.getElementsByClass("item-1-licences")
            val call1 = Some(IsLicenceRequiredController.displayPage(itemId))
            checkSummaryRow(licensesRow, "item.licences", messages("site.yes"), call1, "item.licences")

            val document1Row1 = view.getElementsByClass("item-1-additional-document-1-code")
            checkSummaryRow(document1Row1, s"$keyAD.code", "C501", call, keyAD)

            val document1Row2 = view.getElementsByClass("item-1-additional-document-1-identifier")
            checkSummaryRow(document1Row2, s"$keyAD.identifier", "GBAEOC1342")

            val document2Row = view.getElementsByClass("item-1-additional-document-2-code")
            checkSummaryRow(document2Row, s"$keyAD.code", "A123", call, keyAD)

            val document3Row = view.getElementsByClass("item-1-additional-document-3-identifier")
            checkSummaryRow(document3Row, s"$keyAD.identifier", "GBAEOS9876", call, keyAD)
          }
        }
      }

      "actions are disabled using actionsEnabled = false" should {
        val view = itemSection(itemWithAnswers, 0, STANDARD, actionsEnabled = false)(messages)

        "NOT have change links (which are instead added when 'actionsEnabled' is true" in {
          view.getElementsByClass(summaryActionsClassName) mustBe empty
        }

        "still have an 'Item' header" in {
          val header = messages(s"declaration.summary.item", sequenceId)
          view.getElementsByClass("govuk-heading-s").get(0).text mustBe header
        }

        "still have a 'procedure code' row" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          checkSummaryRow(row, "item.procedureCode", "1234")
        }

        "still have an 'additional procedure codes' row" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          checkSummaryRow(row, "item.additionalProcedureCodes", "000 111")
        }

        "still have an 'onward supply answer' row" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          checkSummaryRow(row, "item.onwardSupplyRelief", messages("site.yes"))
        }

        "still have a 'VAT answer' row" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          checkSummaryRow(row, "item.VATdetails", "GB1234")
        }

        "still have a 'commodity code' row" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          checkSummaryRow(row, "item.commodityCode", "1234567890")
        }

        "still have a 'goods description' row" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          checkSummaryRow(row, "item.goodsDescription", "description")
        }

        "still have an 'undangerous goods code' row" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          checkSummaryRow(row, "item.unDangerousGoodsCode", "345")
        }

        "still have a 'cus code' row" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          checkSummaryRow(row, "item.cusCode", "321")
        }

        "still have a 'taric codes' row" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          checkSummaryRow(row, "item.taricAdditionalCodes", "999, 888")
        }

        "still have a 'nact codes' row" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          checkSummaryRow(row, "item.nationalAdditionalCodes", "111, 222")
        }

        "still have a 'zero rated for vat' row" in {
          val value = messages(s"declaration.summary.item.zeroRatedForVat.VATE")
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          checkSummaryRow(row, "item.zeroRatedForVat", value)
        }

        "still have a 'statistical item value' row" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          checkSummaryRow(row, "item.itemValue", "123")
        }

        "still have a 'package information' section" in {
          val headingRow = view.getElementsByClass("item-1-package-information-heading")
          checkSummaryRow(headingRow, keyPI, "", None, "ign")

          val pi1Row1 = view.getElementsByClass("item-1-package-information-1-type")
          checkSummaryRow(pi1Row1, s"$keyPI.type", "Pallet, box Combined open-ended box and pallet (PB)")

          val pi1Row2 = view.getElementsByClass("item-1-package-information-1-number")
          checkSummaryRow(pi1Row2, s"$keyPI.number", "10")

          val pi1Row3 = view.getElementsByClass("item-1-package-information-1-markings")
          checkSummaryRow(pi1Row3, s"$keyPI.markings", "marks")
        }

        "still have a 'supplementary units' row" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          checkSummaryRow(row, "item.supplementaryUnits", "12")
        }

        "still have a 'gross weight' row" in {
          val row = view.getElementsByClass("item-1-grossWeight-row")
          checkSummaryRow(row, "item.grossWeight", "666")
        }

        "still have a 'net weight' row" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          checkSummaryRow(row, "item.netWeight", "555")
        }

        "still have an 'Additional information' section" in {
          val headingRow = view.getElementsByClass("item-1-additional-information-heading")
          checkSummaryRow(headingRow, keyAI, "", None, "ign")

          val info1Row1 = view.getElementsByClass("item-1-additional-information-1-code")
          checkSummaryRow(info1Row1, s"$keyAI.code", "1234")

          val info1Row2 = view.getElementsByClass("item-1-additional-information-1-description")
          checkSummaryRow(info1Row2, s"$keyAI.description", "additionalDescription")
        }

        "still have an 'Additional documents' section" in {
          val headingRow = view.getElementsByClass("item-1-additional-documents-heading")
          checkSummaryRow(headingRow, keyAD, "", None, "ign")

          val licensesRow = view.getElementsByClass("item-1-licences")
          checkSummaryRow(licensesRow, "item.licences", messages("site.yes"))

          val document1Row1 = view.getElementsByClass("item-1-additional-document-1-code")
          checkSummaryRow(document1Row1, s"$keyAD.code", "C501")

          val document1Row2 = view.getElementsByClass("item-1-additional-document-1-identifier")
          checkSummaryRow(document1Row2, s"$keyAD.identifier", "GBAEOC1342")
        }
      }
    }

    "has no answers" should {
      val view = itemSection(itemWithoutAnswers, 0, STANDARD)(messages)

      "not display a 'procedure code' row" in {
        view.getElementsByClass("item-1-procedureCode-row") mustBe empty
      }

      "not display a 'commodity code' row" in {
        view.getElementsByClass("item-1-commodityCode-row") mustBe empty
      }

      "not display a 'goods description' row" in {
        view.getElementsByClass("item-1-goodsDescription-row") mustBe empty
      }

      "not display a 'UN Dangerous Goods Code' row" in {
        view.getElementsByClass("item-1-unDangerousGoodsCode-row") mustBe empty
      }

      "not display a 'UN CUS Code' row" in {
        view.getElementsByClass("item-1-cusCode-row") mustBe empty
      }

      "not display a 'TARIC Codes' row" in {
        view.getElementsByClass("item-1-taricAdditionalCodes-row") mustBe empty
      }

      "not display a 'National Additional Codes' row" in {
        view.getElementsByClass("item-1-nationalAdditionalCodes-row") mustBe empty
      }

      "not display a 'Item Value' row" in {
        view.getElementsByClass("item-1-itemValue-row") mustBe empty
      }

      "not display a 'Supplementary Units' row" in {
        view.getElementsByClass("item-1-supplementaryUnits-row") mustBe empty
      }

      "not display a 'Gross Weight' row" in {
        view.getElementsByClass("item-1-grossWeight-row") mustBe empty
      }

      "not display a 'Net Weight' row" in {
        view.getElementsByClass("item-1-netWeight-row") mustBe empty
      }
    }
  }
}
