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
import forms.common.YesNoAnswer
import forms.declaration._
import forms.declaration.additionaldocuments.AdditionalDocument
import models.DeclarationType.STANDARD
import models.declaration.CommodityMeasure
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.item_section

class ItemSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val commodityMeasure = CommodityMeasure(Some("12"), Some(false), Some("666"), Some("555"))

  val doc1 = AdditionalDocument(Some("C501"), Some("GBAEOC1341"), None, None, None, None, None)
  val doc2 = AdditionalDocument(Some("C502"), Some("GBAEOC1342"), None, None, None, None, None)

  private val itemWithAnswers = anItem(
    withItemId(itemId),
    withSequenceId(1),
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
    withAdditionalDocuments(YesNoAnswer.Yes, doc1, doc2)
  )

  private val itemWithoutAnswers = anItem(withItemId(itemId), withSequenceId(1))

  "Item section" when {

    val itemSection = instanceOf[item_section]

    "has item answers and" when {

      "actions are enabled" should {
        val view = itemSection(itemWithAnswers, 0, STANDARD)(messages)

        "have item header" in {
          view.getElementsByClass("govuk-heading-m").text mustBe messages("declaration.summary.items.item.presentationId", "1")
        }

        "have header action" in {
          val action = view.getElementById("item-header-action")
          action.text mustBe messages("declaration.summary.items.item.headerAction")
          action must haveHref(controllers.declaration.routes.RemoveItemsSummaryController.displayRemoveItemConfirmationPage(itemId, true))
        }

        "have procedure code with change button" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
          row must haveSummaryValue("1234")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.procedureCode.change", "1")

          row must haveSummaryActionWithPlaceholder(ProcedureCodesController.displayPage(itemWithAnswers.id))
        }

        "have additional procedure codes separated by space with change button" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
          row must haveSummaryValue("000 111")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalProcedureCodes.change", "1")

          row must haveSummaryActionWithPlaceholder(AdditionalProcedureCodesController.displayPage(itemWithAnswers.id))
        }

        "have onward supply answer with change button" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
          row must haveSummaryValue("Yes")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.onwardSupplyRelief.change", "1")

          row must haveSummaryActionWithPlaceholder(FiscalInformationController.displayPage(itemWithAnswers.id))
        }

        "have VAT answer with change button" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
          row must haveSummaryValue("GB1234")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.VATdetails.change", "1")

          row must haveSummaryActionWithPlaceholder(AdditionalFiscalReferencesController.displayPage(itemWithAnswers.id))
        }

        "have commodity code with change button" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
          row must haveSummaryValue("1234567890")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.commodityCode.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityDetailsController.displayPage(itemWithAnswers.id))
        }

        "have goods description with change button" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
          row must haveSummaryValue("description")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.goodsDescription.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityDetailsController.displayPage(itemWithAnswers.id))
        }

        "have un dangerous goods code with change button" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
          row must haveSummaryValue("345")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.unDangerousGoodsCode.change", "1")

          row must haveSummaryActionWithPlaceholder(UNDangerousGoodsCodeController.displayPage(itemWithAnswers.id))
        }

        "have cus code with change button" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
          row must haveSummaryValue("321")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.cusCode.change", "1")

          row must haveSummaryActionWithPlaceholder(CusCodeController.displayPage(itemWithAnswers.id))
        }

        "have taric codes separated by comma with change button" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
          row must haveSummaryValue("999, 888")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.taricAdditionalCodes.change", "1")

          row must haveSummaryActionWithPlaceholder(TaricCodeSummaryController.displayPage(itemWithAnswers.id))
        }

        "have nact codes separated by comma with change button" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
          row must haveSummaryValue("111, 222")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.nationalAdditionalCodes.change", "1")

          row must haveSummaryActionWithPlaceholder(NactCodeSummaryController.displayPage(itemWithAnswers.id))
        }

        "have zero rated for vat row with change button" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.zeroRatedForVat"))
          row must haveSummaryValue(messages("declaration.summary.items.item.zeroRatedForVat.VATE"))

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.zeroRatedForVat.change", "1")

          row must haveSummaryActionWithPlaceholder(ZeroRatedForVatController.displayPage(itemWithAnswers.id))
        }

        "have statistical item value with change button" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
          row must haveSummaryValue("123")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.itemValue.change", "1")

          row must haveSummaryActionWithPlaceholder(StatisticalValueController.displayPage(itemWithAnswers.id))
        }

        "have package information section" in {
          view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text mustBe messages(
            "declaration.summary.items.item.packageInformation"
          )
        }

        "have package information displayed on the page before 'Commodity Measures'" in {
          val body = view.child(0).children.get(1)
          val elements = body.children
          assert(elements.get(2).text.startsWith("Packing details"))
          assert(elements.get(3).text.startsWith("Gross weight"))
        }

        "have supplementary units with change button" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
          row must haveSummaryValue("12")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.supplementaryUnits.change", "1")

          row must haveSummaryActionWithPlaceholder(SupplementaryUnitsController.displayPage(itemWithAnswers.id))
        }

        // CEDS-3668
        "not have a 'Supplementary Units' row" when {
          "the declaration has a 'CommodityMeasure' instance with 'supplementaryUnits' undefined" in {
            val item = itemWithAnswers.copy(commodityMeasure = Some(commodityMeasure.copy(supplementaryUnits = None)))
            val view = itemSection(item, 0, STANDARD)(messages)
            view.getElementsByClass("item-1-supplementaryUnits-row") mustBe empty
          }
        }

        "have gross weight with change button" in {
          val row = view.getElementsByClass("item-1-grossWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
          row must haveSummaryValue("666")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.grossWeight.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have net weight with change button" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
          row must haveSummaryValue("555")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.netWeight.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have union and national codes section" in {
          view.getElementById("additional-information-1-table").getElementsByClass("govuk-table__caption").text mustBe messages(
            "declaration.summary.items.item.additionalInformation"
          )
        }

        "have additional documents section" which {

          val summaryList = view.getElementsByClass("additional-documents-summary-1").first
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")

          "has all rows present" in {
            summaryListRows.size mustBe 5
          }

          "has heading present" in {
            val heading = summaryListRows.first.getElementsByClass("additional-documents-heading")
            heading must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments"))
          }

          "has answers and actions present" when {
            "doc code" in {
              val doc1Code = summaryListRows.get(1).getElementsByClass("additional-documents-code-1")
              doc1Code must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.code"))
              doc1Code.first.getElementsByClass(summaryValueClassName).first must containText(doc1.documentTypeCode.value)
              doc1Code must haveSummaryActionsTexts(
                "site.change",
                "declaration.summary.items.item.additionalDocuments.change",
                doc1.documentTypeCode.value,
                doc1.documentIdentifier.value,
                itemWithAnswers.sequenceId.toString
              )
              doc1Code must haveSummaryActionWithPlaceholder(AdditionalDocumentsController.displayPage(itemWithAnswers.id))
            }
            "doc id" which {
              "does not have change link present" in {
                val doc1Id = summaryListRows.get(2).getElementsByClass("additional-documents-id-1")
                doc1Id must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.identifier"))
                doc1Id.first.getElementsByClass(summaryValueClassName).first must containText(doc1.documentIdentifier.value)
                doc1Id.first.getElementsByClass(summaryActionsClassName) mustBe empty
              }
            }
          }

          "has answers only present" when {
            "doc id" in {
              val doc1Id = summaryListRows.get(2).getElementsByClass("additional-documents-id-1")
              doc1Id must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.identifier"))
              doc1Id.first.getElementsByClass(summaryValueClassName).first must containText(doc1.documentIdentifier.value)
              doc1Id.first.getElementsByClass(summaryActionsClassName) mustBe empty
            }
          }
        }

      }

      "actions are disabled using actionsEnabled = false" should {

        val view = itemSection(itemWithAnswers, 0, STANDARD, actionsEnabled = false)(messages)

        "have item header" in {
          view.getElementsByClass("govuk-heading-m").text mustBe messages("declaration.summary.items.item.presentationId", "1")
        }

        "not have header action" in {
          assert(Option(view.getElementById("item-header-action")).isEmpty)
        }

        "have procedure code with change button" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
          row must haveSummaryValue("1234")

          row mustNot haveSummaryActionsTexts("site.change", "declaration.summary.items.item.procedureCode.change")
          row mustNot haveSummaryActionsHref(ProcedureCodesController.displayPage(itemWithAnswers.id))
        }

        "have additional procedure codes separated by space with change button" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
          row must haveSummaryValue("000 111")

          row mustNot haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalProcedureCodes.change")
          row mustNot haveSummaryActionsHref(AdditionalProcedureCodesController.displayPage(itemWithAnswers.id))
        }

        "have onward supply answer with change button" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
          row must haveSummaryValue("Yes")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.onwardSupplyRelief.change")
          row mustNot haveSummaryActionsHref(FiscalInformationController.displayPage(itemWithAnswers.id))
        }

        "have VAT answer with change button" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
          row must haveSummaryValue("GB1234")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.VATdetails.change")
          row mustNot haveSummaryActionsHref(AdditionalFiscalReferencesController.displayPage(itemWithAnswers.id))
        }

        "have commodity code with change button" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
          row must haveSummaryValue("1234567890")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.commodityCode.change")
          row mustNot haveSummaryActionsHref(CommodityDetailsController.displayPage(itemWithAnswers.id))
        }

        "have goods description with change button" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
          row must haveSummaryValue("description")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.goodsDescription.change")
          row mustNot haveSummaryActionsHref(CommodityDetailsController.displayPage(itemWithAnswers.id))
        }

        "have un dangerous goods code with change button" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
          row must haveSummaryValue("345")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.unDangerousGoodsCode.change")
          row mustNot haveSummaryActionsHref(UNDangerousGoodsCodeController.displayPage(itemWithAnswers.id))
        }

        "have cus code with change button" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
          row must haveSummaryValue("321")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.cusCode.change")
          row mustNot haveSummaryActionsHref(CusCodeController.displayPage(itemWithAnswers.id))
        }

        "have taric codes separated by comma with change button" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
          row must haveSummaryValue("999, 888")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.taricAdditionalCodes.change")
          row mustNot haveSummaryActionsHref(TaricCodeSummaryController.displayPage(itemWithAnswers.id))
        }

        "have nact codes separated by comma with change button" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
          row must haveSummaryValue("111, 222")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.nationalAdditionalCodes.change")
          row mustNot haveSummaryActionsHref(NactCodeSummaryController.displayPage(itemWithAnswers.id))
        }

        "have zero rated for vat row with change button" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.zeroRatedForVat"))
          row must haveSummaryValue(messages("declaration.summary.items.item.zeroRatedForVat.VATE"))

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.zeroRatedForVat.change")
          row mustNot haveSummaryActionsHref(ZeroRatedForVatController.displayPage(itemWithAnswers.id))
        }

        "have statistical item value with change button" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
          row must haveSummaryValue("123")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.itemValue.change")
          row mustNot haveSummaryActionsHref(StatisticalValueController.displayPage(itemWithAnswers.id))
        }

        "have supplementary units with change button" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
          row must haveSummaryValue("12")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.supplementaryUnits.change")
          row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have gross weight with change button" in {
          val row = view.getElementsByClass("item-1-grossWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
          row must haveSummaryValue("666")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.grossWeight.change")
          row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have net weight with change button" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
          row must haveSummaryValue("555")

          row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.netWeight.change")
          row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have package information section" in {
          view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text mustBe messages(
            "declaration.summary.items.item.packageInformation"
          )
        }

        "have union and national codes section" in {
          view.getElementById("additional-information-1-table").getElementsByClass("govuk-table__caption").text mustBe messages(
            "declaration.summary.items.item.additionalInformation"
          )
        }

      }
    }

    "has no answers" should {

      val view = itemSection(itemWithoutAnswers, 0, STANDARD)(messages)

      "not display procedure code" in {
        view.getElementsByClass("item-1-procedureCode-row") mustBe empty
      }

      "not display commodity code" in {
        view.getElementsByClass("item-1-commodityCode-row") mustBe empty
      }

      "not display goods description" in {
        view.getElementsByClass("item-1-goodsDescription-row") mustBe empty
      }

      "not display UN Dangerous Goods Code" in {
        view.getElementsByClass("item-1-unDangerousGoodsCode-row") mustBe empty
      }

      "not display UN CUS Code" in {
        view.getElementsByClass("item-1-cusCode-row") mustBe empty
      }

      "not display TARIC Codes" in {
        view.getElementsByClass("itetaricAdditionalCodes-row") mustBe empty
      }

      "not display National Additional Codes" in {
        view.getElementsByClass("item-1-nationalAdditionalCodes-row") mustBe empty
      }

      "not display Item Value" in {
        view.getElementsByClass("item-1-itemValue-row") mustBe empty
      }

      "not display Supplementary Units" in {
        view.getElementsByClass("item-1-supplementaryUnits-row") mustBe empty
      }

      "not display Gross Weight" in {
        view.getElementsByClass("item-1-grossWeight-row") mustBe empty
      }

      "not display Net Weight" in {
        view.getElementsByClass("item-1-netWeight-row") mustBe empty
      }

      "not display package information section" in {
        view.getElementsByClass("item-1-commodityCode-row") mustBe empty

        Option(view.getElementById("package-information-1")) mustBe None
      }

      "not display union and national codes section" in {
        Option(view.getElementById("additional-information-1")) mustBe None
      }

      "not display additional documents section" in {
        Option(view.getElementById("additional-documents-1")) mustBe None
      }
    }
  }
}
