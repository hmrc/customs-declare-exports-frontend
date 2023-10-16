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
import org.jsoup.select.Elements
import org.scalatest.Assertion
import play.api.mvc.Call
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.item_section

class ItemSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val itemSection = instanceOf[item_section]

  val commodityMeasure = CommodityMeasure(Some("12"), Some(false), Some("666"), Some("555"))

  private val seqId = "1"
  private val tx = "declaration.summary.items.item"

  private val itemWithAnswers = anItem(
    withItemId(itemId),
    withSequenceId(seqId.toInt),
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

  private val itemWithoutAnswers = anItem(withItemId(itemId), withSequenceId(1))

  "Item section" when {

    "the item has answers and" when {

      "actions are enabled" should {
        val view = itemSection(itemWithAnswers, 0, STANDARD)(messages)

        "have a 'Item' header" in {
          val subHeader = view.getElementsByClass("govuk-heading-s").get(0)
          subHeader.text mustBe messages("declaration.summary.items.item.presentationId", "1")
        }

        "have a 'change' link at header level" in {
          val action = view.getElementById("item-header-action")
          action.text mustBe messages(s"$tx.headerAction")
          action must haveHref(RemoveItemsSummaryController.displayRemoveItemConfirmationPage(itemId, true))
        }

        "have a 'procedure code' row" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          val call = Some(ProcedureCodesController.displayPage(itemId))
          checkRow(row, s"$tx.procedureCode", "1234", call, s"$tx.procedureCode.change")
        }

        "have an 'additional procedure codes' row" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          val call = Some(AdditionalProcedureCodesController.displayPage(itemId))
          checkRow(row, s"$tx.additionalProcedureCodes", "000 111", call, s"$tx.additionalProcedureCodes.change")
        }

        "have an 'onward supply answer' row" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          val call = Some(FiscalInformationController.displayPage(itemId))
          checkRow(row, s"$tx.onwardSupplyRelief", "Yes", call, s"$tx.onwardSupplyRelief.change")
        }

        "have a 'VAT answer' row" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          val call = Some(AdditionalFiscalReferencesController.displayPage(itemId))
          checkRow(row, s"$tx.VATdetails", "GB1234", call, s"$tx.VATdetails.change")
        }

        "have a 'commodity code' row" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          val call = Some(CommodityDetailsController.displayPage(itemId))
          checkRow(row, s"$tx.commodityCode", "1234567890", call, s"$tx.commodityCode.change")
        }

        "have a 'goods description' row" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          val call = Some(CommodityDetailsController.displayPage(itemId))
          checkRow(row, s"$tx.goodsDescription", "description", call, s"$tx.goodsDescription.change")
        }

        "have a 'undangerous goods code' row" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          val call = Some(UNDangerousGoodsCodeController.displayPage(itemId))
          checkRow(row, s"$tx.unDangerousGoodsCode", "345", call, s"$tx.unDangerousGoodsCode.change")
        }

        "have a 'cus code' row" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          val call = Some(CusCodeController.displayPage(itemId))
          checkRow(row, s"$tx.cusCode", "321", call, s"$tx.cusCode.change")
        }

        "have a 'taric codes' row" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          val call = Some(TaricCodeSummaryController.displayPage(itemId))
          checkRow(row, s"$tx.taricAdditionalCodes", "999, 888", call, s"$tx.taricAdditionalCodes.change")
        }

        "have a 'nact codes' row" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          val call = Some(NactCodeSummaryController.displayPage(itemId))
          checkRow(row, s"$tx.nationalAdditionalCodes", "111, 222", call, s"$tx.nationalAdditionalCodes.change")
        }

        "have a 'zero rated for vat' row" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          val call = Some(ZeroRatedForVatController.displayPage(itemId))
          checkRow(row, s"$tx.zeroRatedForVat", messages(s"$tx.zeroRatedForVat.VATE"), call, s"$tx.zeroRatedForVat.change")
        }

        "have a 'statistical item value' row" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          val call = Some(StatisticalValueController.displayPage(itemId))
          checkRow(row, s"$tx.itemValue", "123", call, s"$tx.itemValue.change")
        }

        "have a 'Package Information' row" when {
          "packageInformation is defined but has no data" in {
            val item = anItemAfter(itemWithAnswers, withPackageInformation(List.empty))
            val view = itemSection(item, 0, STANDARD)(messages)

            val summaryList = view.getElementsByClass("item-1-package-information-summary").get(0)
            val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
            summaryListRows.size mustBe 1

            val noDataRow = summaryListRows.get(0).getElementsByClass("item-1-package-information-heading")
            val call = Some(PackageInformationSummaryController.displayPage(itemId))
            checkRow(noDataRow, s"$tx.packageInformation", messages("site.none"), call, s"$tx.packageInformation.change")
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

          val hint = s"$tx.packageInformation.change"
          val call = Some(PackageInformationSummaryController.displayPage(itemId))

          val summaryList = view.getElementsByClass("item-1-package-information-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 13

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-package-information-heading")
          checkRow(headingRow, s"$tx.packageInformation", "", None, "ign")

          val pi1Row1 = summaryListRows.get(1).getElementsByClass("item-1-package-information-1-type")
          checkRow(pi1Row1, s"$tx.packageInformation.type", "Pallet, box Combined open-ended box and pallet (PB)", call, hint)

          val pi1Row2 = summaryListRows.get(2).getElementsByClass("item-1-package-information-1-number")
          checkRow(pi1Row2, s"$tx.packageInformation.number", "1")

          val pi1Row3 = summaryListRows.get(3).getElementsByClass("item-1-package-information-1-markings")
          checkRow(pi1Row3, s"$tx.packageInformation.markings", "markings1")

          val pi2Row4 = summaryListRows.get(4).getElementsByClass("item-1-package-information-2-type")
          checkRow(pi2Row4, s"$tx.packageInformation.type", "Unknown package type (type2)", call, hint)

          val pi2Row5 = summaryListRows.get(5).getElementsByClass("item-1-package-information-2-number")
          checkRow(pi2Row5, s"$tx.packageInformation.number", "2")

          val pi3Row6 = summaryListRows.get(6).getElementsByClass("item-1-package-information-3-type")
          checkRow(pi3Row6, s"$tx.packageInformation.type", "Unknown package type (type3)", call, hint)

          val pi3Row7 = summaryListRows.get(7).getElementsByClass("item-1-package-information-3-markings")
          checkRow(pi3Row7, s"$tx.packageInformation.markings", "markings3")

          val pi4Row8 = summaryListRows.get(8).getElementsByClass("item-1-package-information-4-number")
          checkRow(pi4Row8, s"$tx.packageInformation.number", "4", call, hint)

          val pi4Row9 = summaryListRows.get(9).getElementsByClass("item-1-package-information-4-markings")
          checkRow(pi4Row9, s"$tx.packageInformation.markings", "markings4")

          val pi5Row10 = summaryListRows.get(10).getElementsByClass("item-1-package-information-5-type")
          checkRow(pi5Row10, s"$tx.packageInformation.type", "Unknown package type (type5)", call, hint)

          val pi6Row11 = summaryListRows.get(11).getElementsByClass("item-1-package-information-6-number")
          checkRow(pi6Row11, s"$tx.packageInformation.number", "6", call, hint)

          val pi7Row12 = summaryListRows.get(12).getElementsByClass("item-1-package-information-7-markings")
          checkRow(pi7Row12, s"$tx.packageInformation.markings", "markings7", call, hint)
        }

        "have a 'supplementary units' row" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          val call = Some(SupplementaryUnitsController.displayPage(itemId))
          checkRow(row, s"$tx.supplementaryUnits", "12", call, s"$tx.supplementaryUnits.change")
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
          checkRow(row, s"$tx.grossWeight", "666", call, s"$tx.grossWeight.change")
        }

        "have a 'net weight' row" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          val call = Some(CommodityMeasureController.displayPage(itemId))
          checkRow(row, s"$tx.netWeight", "555", call, s"$tx.netWeight.change")
        }

        "have an 'Additional Information' row" when {
          "additionalInformation is defined but has no data" in {
            val item = anItemAfter(itemWithAnswers, withoutAdditionalInformation(true))
            val view = itemSection(item, 0, STANDARD)(messages)

            val summaryList = view.getElementsByClass("item-1-additional-information-summary").get(0)
            val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
            summaryListRows.size mustBe 1

            val noDataRow = summaryListRows.get(0).getElementsByClass("item-1-additional-information-heading")
            val call = Some(AdditionalInformationRequiredController.displayPage(itemId))
            checkRow(noDataRow, s"$tx.additionalInformation", messages("site.none"), call, s"$tx.additionalInformation.change")
          }
        }

        "have an 'Additional Information' section" in {
          val ai1 = AdditionalInformation("Code1", "Exporter1")
          val ai2 = AdditionalInformation("Code2", "Exporter2")
          val item = anItemAfter(itemWithoutAnswers, withAdditionalInformation(ai1, ai2))
          val view = itemSection(item, 0, STANDARD)(messages)

          val hint = s"$tx.additionalInformation.change"
          val call = Some(AdditionalInformationController.displayPage(itemId))

          val summaryList = view.getElementsByClass("item-1-additional-information-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 5

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-additional-information-heading")
          checkRow(headingRow, s"$tx.additionalInformation", "", None, "ign")

          val info1Row1 = summaryListRows.get(1).getElementsByClass("item-1-additional-information-1-code")
          checkRow(info1Row1, s"$tx.additionalInformation.code", "Code1", call, hint)

          val info1Row2 = summaryListRows.get(2).getElementsByClass("item-1-additional-information-1-description")
          checkRow(info1Row2, s"$tx.additionalInformation.description", "Exporter1")

          val info2Row3 = summaryListRows.get(3).getElementsByClass("item-1-additional-information-2-code")
          checkRow(info2Row3, s"$tx.additionalInformation.code", "Code2", call, hint)

          val info2Row4 = summaryListRows.get(4).getElementsByClass("item-1-additional-information-2-description")
          checkRow(info2Row4, s"$tx.additionalInformation.description", "Exporter2")
        }

        "have a 'Licenses' row" when {
          "isLicenceRequired is 'yes' (or 'no', but not None) and" when {
            val call = Some(IsLicenceRequiredController.displayPage(itemId))

            "additionalDocuments is undefined" in {
              val item = anItemAfter(itemWithAnswers, withoutAdditionalDocuments())
              val view = itemSection(item, 0, STANDARD)(messages)

              val summaryList = view.getElementsByClass("item-1-additional-documents-summary").get(0)
              val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
              summaryListRows.size mustBe 1

              val licensesRow = summaryListRows.get(0).getElementsByClass("item-1-licenses")
              checkRow(licensesRow, s"$tx.licences", messages("site.yes"), call, s"$tx.licences.change")
            }

            "additionalDocuments is defined (but has no documents)" should {
              "also have an 'Additional documents' row" in {
                val item = anItemAfter(itemWithAnswers, withoutAdditionalDocuments(true))
                val view = itemSection(item, 0, STANDARD)(messages)

                val summaryList = view.getElementsByClass("item-1-additional-documents-summary").get(0)
                val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
                summaryListRows.size mustBe 2

                val licensesRow = summaryListRows.get(0).getElementsByClass("item-1-licenses")
                checkRow(licensesRow, s"$tx.licences", messages("site.yes"), call, s"$tx.licences.change")

                val noDocumentsRow = summaryListRows.get(1).getElementsByClass("item-1-additional-documents-heading")
                val call1 = Some(AdditionalDocumentsController.displayPage(itemId))
                checkRow(noDocumentsRow, s"$tx.additionalDocuments", messages("site.none"), call1, s"$tx.additionalDocuments.change")
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

            val hint = s"$tx.additionalDocuments.change"
            val call = Some(AdditionalDocumentsController.displayPage(itemId))

            val summaryList = view.getElementsByClass("item-1-additional-documents-summary").get(0)
            val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
            summaryListRows.size mustBe 6

            val headingRow = summaryListRows.get(0).getElementsByClass("item-1-additional-documents-heading")
            checkRow(headingRow, s"$tx.additionalDocuments", "", None, "ign")

            val licensesRow = summaryListRows.get(1).getElementsByClass("item-1-licenses")
            val call1 = Some(IsLicenceRequiredController.displayPage(itemId))
            checkRow(licensesRow, s"$tx.licences", messages("site.yes"), call1, s"$tx.licences.change")

            val document1Row1 = summaryListRows.get(2).getElementsByClass("item-1-document-1-code")
            checkRow(document1Row1, s"$tx.additionalDocuments.code", "C501", call, hint)

            val document1Row2 = summaryListRows.get(3).getElementsByClass("item-1-document-1-identifier")

            document1Row2 must haveSummaryKey(messages(s"$tx.additionalDocuments.identifier"))
            document1Row2 must haveSummaryValue("GBAEOC1342")
            document1Row2.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0

            val document2Row = summaryListRows.get(4).getElementsByClass("item-1-document-2-code")
            checkRow(document2Row, s"$tx.additionalDocuments.code", "A123", call, hint)

            val document3Row = summaryListRows.get(5).getElementsByClass("item-1-document-3-identifier")
            checkRow(document3Row, s"$tx.additionalDocuments.identifier", "GBAEOS9876", call, hint)
          }
        }
      }

      "actions are disabled using actionsEnabled = false" should {
        val view = itemSection(itemWithAnswers, 0, STANDARD, actionsEnabled = false)(messages)

        "NOT have change links (which are instead added when 'actionsEnabled' is true" in {
          view.getElementsByClass("govuk-summary-list__actions") mustBe empty
        }

        "still have an 'Item' header" in {
          view.getElementsByClass("govuk-heading-s").get(0).text mustBe messages("declaration.summary.items.item.presentationId", "1")
        }

        "still have a 'procedure code' row" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          checkRow(row, s"$tx.procedureCode", "1234")
        }

        "still have an 'additional procedure codes' row" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          checkRow(row, s"$tx.additionalProcedureCodes", "000 111")
        }

        "still have an 'onward supply answer' row" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          checkRow(row, s"$tx.onwardSupplyRelief", "Yes")
        }

        "still have a 'VAT answer' row" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          checkRow(row, s"$tx.VATdetails", "GB1234")
        }

        "still have a 'commodity code' row" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          checkRow(row, s"$tx.commodityCode", "1234567890")
        }

        "still have a 'goods description' row" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          checkRow(row, s"$tx.goodsDescription", "description")
        }

        "still have an 'undangerous goods code' row" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          checkRow(row, s"$tx.unDangerousGoodsCode", "345")
        }

        "still have a 'cus code' row" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          checkRow(row, s"$tx.cusCode", "321")
        }

        "still have a 'taric codes' row" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          checkRow(row, s"$tx.taricAdditionalCodes", "999, 888")
        }

        "still have a 'nact codes' row" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          checkRow(row, s"$tx.nationalAdditionalCodes", "111, 222")
        }

        "still have a 'zero rated for vat' row" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          checkRow(row, s"$tx.zeroRatedForVat", messages(s"$tx.zeroRatedForVat.VATE"))
        }

        "still have a 'statistical item value' row" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          checkRow(row, s"$tx.itemValue", "123")
        }

        "still have a 'package information' section" in {
          val summaryList = view.getElementsByClass("item-1-package-information-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 4

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-package-information-heading")
          checkRow(headingRow, s"$tx.packageInformation", "", None, "ign")

          val pi1Row1 = summaryListRows.get(1).getElementsByClass("item-1-package-information-1-type")
          checkRow(pi1Row1, s"$tx.packageInformation.type", "Pallet, box Combined open-ended box and pallet (PB)")

          val pi1Row2 = summaryListRows.get(2).getElementsByClass("item-1-package-information-1-number")
          checkRow(pi1Row2, s"$tx.packageInformation.number", "10")

          val pi1Row3 = summaryListRows.get(3).getElementsByClass("item-1-package-information-1-markings")
          checkRow(pi1Row3, s"$tx.packageInformation.markings", "marks")
        }

        "still have a 'supplementary units' row" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          checkRow(row, s"$tx.supplementaryUnits", "12")
        }

        "still have a 'gross weight' row" in {
          val row = view.getElementsByClass("item-1-grossWeight-row")
          checkRow(row, s"$tx.grossWeight", "666")
        }

        "still have a 'net weight' row" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          checkRow(row, s"$tx.netWeight", "555")
        }

        "still have an 'Additional information' section" in {
          val summaryList = view.getElementsByClass("item-1-additional-information-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 3

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-additional-information-heading")
          checkRow(headingRow, s"$tx.additionalInformation", "", None, "ign")

          val info1Row1 = summaryListRows.get(1).getElementsByClass("item-1-additional-information-1-code")
          checkRow(info1Row1, s"$tx.additionalInformation.code", "1234")

          val info1Row2 = summaryListRows.get(2).getElementsByClass("item-1-additional-information-1-description")
          checkRow(info1Row2, s"$tx.additionalInformation.description", "additionalDescription")
        }

        "still have an 'Additional documents' section" in {
          val summaryList = view.getElementsByClass("item-1-additional-documents-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 4

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-additional-documents-heading")
          checkRow(headingRow, s"$tx.additionalDocuments", "", None, "ign")

          val licensesRow = summaryListRows.get(1).getElementsByClass("item-1-licenses")
          checkRow(licensesRow, s"$tx.licences", messages("site.yes"))

          val document1Row1 = summaryListRows.get(2).getElementsByClass("item-1-document-1-code")
          checkRow(document1Row1, s"$tx.additionalDocuments.code", "C501")

          val document1Row2 = summaryListRows.get(3).getElementsByClass("item-1-document-1-identifier")
          checkRow(document1Row2, s"$tx.additionalDocuments.identifier", "GBAEOC1342")
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

      "not display a 'package information section' row" in {
        view.getElementsByClass("item-1-additional-package-information-summary") mustBe empty
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

      "not display an 'Additional information' row" in {
        view.getElementsByClass("item-1-additional-information-summary") mustBe empty
      }

      "not display an 'Additional documents'' row" in {
        view.getElementsByClass("item-1-additional-documents-summary") mustBe empty
      }
    }
  }

  private def checkRow(row: Elements, labelKey: String, value: String, maybeUrl: Option[Call] = None, hint: String = ""): Assertion = {
    row must haveSummaryKey(messages(labelKey))
    row must haveSummaryValue(value)
    maybeUrl.fold {
      val action = row.get(0).getElementsByClass("govuk-summary-list__actions")
      if (hint.isEmpty) action.size mustBe 0 else action.text mustBe ""
    } { url =>
      row must haveSummaryActionsTexts("site.change", hint, seqId)
      row must haveSummaryActionWithPlaceholder(url)
    }
  }
}
