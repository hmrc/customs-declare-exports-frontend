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

package views.declaration.summary.sections

import base.Injector
import com.typesafe.config.ConfigFactory
import controllers.declaration.routes._
import forms.common.YesNoAnswer
import forms.declaration._
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.CommodityMeasure
import models.{DeclarationType, Mode}
import play.api.Configuration
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.item_section

class ItemSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  override val configuration: Configuration = Configuration(ConfigFactory.parseString("microservice.services.features.waiver999L=enabled"))

  val commodityMeasure = CommodityMeasure(Some("12"), Some(false), Some("666"), Some("555"))

  private val itemWithAnswers = anItem(
    withItemId("itemId"),
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
    withPackageInformation("PB", 10, "marks"),
    withCommodityMeasure(commodityMeasure),
    withAdditionalInformation("1234", "additionalDescription"),
    withAdditionalDocuments(YesNoAnswer.Yes, AdditionalDocument(Some("C501"), Some("GBAEOC1342"), None, None, None, None, None))
  )

  private val itemWithoutAnswers = anItem(withItemId("itemId"), withSequenceId(1))

  private val itemSection = instanceOf[item_section]

  "Item section with item answers" when {

    "actions are enabled" should {

      val view = itemSection(Mode.Normal, itemWithAnswers, DeclarationType.STANDARD)(messages)

      "have item header" in {
        view.getElementsByClass("govuk-heading-m").text() mustBe messages("declaration.summary.items.item.sequenceId", "1")
      }

      "have procedure code with change button" in {
        val row = view.getElementsByClass("item-1-procedureCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
        row must haveSummaryValue("1234")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.procedureCode.change", "1")

        row must haveSummaryActionsHref(ProcedureCodesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have additional procedure codes separated by space with change button" in {
        val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
        row must haveSummaryValue("000 111")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalProcedureCodes.change", "1")

        row must haveSummaryActionsHref(AdditionalProcedureCodesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have onward supply answer with change button" in {
        val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
        row must haveSummaryValue("Yes")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.onwardSupplyRelief.change", "1")

        row must haveSummaryActionsHref(FiscalInformationController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have VAT answer with change button" in {
        val row = view.getElementsByClass("item-1-VATdetails-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
        row must haveSummaryValue("GB1234")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.VATdetails.change", "1")

        row must haveSummaryActionsHref(AdditionalFiscalReferencesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have commodity code with change button" in {
        val row = view.getElementsByClass("item-1-commodityCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
        row must haveSummaryValue("1234567890")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.commodityCode.change", "1")

        row must haveSummaryActionsHref(CommodityDetailsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have goods description with change button" in {
        val row = view.getElementsByClass("item-1-goodsDescription-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
        row must haveSummaryValue("description")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.goodsDescription.change", "1")

        row must haveSummaryActionsHref(CommodityDetailsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have un dangerous goods code with change button" in {
        val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
        row must haveSummaryValue("345")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.unDangerousGoodsCode.change", "1")

        row must haveSummaryActionsHref(UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have cus code with change button" in {
        val row = view.getElementsByClass("item-1-cusCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
        row must haveSummaryValue("321")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.cusCode.change", "1")

        row must haveSummaryActionsHref(CusCodeController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have taric codes separated by comma with change button" in {
        val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
        row must haveSummaryValue("999, 888")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.taricAdditionalCodes.change", "1")

        row must haveSummaryActionsHref(TaricCodeSummaryController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have nact codes separated by comma with change button" in {
        val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
        row must haveSummaryValue("111, 222")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.nationalAdditionalCodes.change", "1")

        row must haveSummaryActionsHref(NactCodeSummaryController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have statistical item value with change button" in {
        val row = view.getElementsByClass("item-1-itemValue-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
        row must haveSummaryValue("123")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.itemValue.change", "1")

        row must haveSummaryActionsHref(StatisticalValueController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have supplementary units with change button" in {
        val row = view.getElementsByClass("item-1-supplementaryUnits-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
        row must haveSummaryValue("12")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.supplementaryUnits.change", "1")

        row must haveSummaryActionsHref(SupplementaryUnitsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      // CEDS-3668
      "not have a 'Supplementary Units' row" when {
        "the declaration has a 'CommodityMeasure' instance with 'supplementaryUnits' undefined" in {
          val item = itemWithAnswers.copy(commodityMeasure = Some(commodityMeasure.copy(supplementaryUnits = None)))
          val view = itemSection(Mode.Normal, item, DeclarationType.STANDARD)(messages)
          view.getElementsByClass("item-1-supplementaryUnits-row") mustBe empty
        }
      }

      "have gross weight with change button" in {
        val row = view.getElementsByClass("item-1-grossWeight-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
        row must haveSummaryValue("666")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.grossWeight.change", "1")

        row must haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have net weight with change button" in {
        val row = view.getElementsByClass("item-1-netWeight-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
        row must haveSummaryValue("555")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.netWeight.change", "1")

        row must haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have package information section" in {
        view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.packageInformation"
        )
      }

      "have union and national codes section" in {
        view.getElementById("additional-information-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.additionalInformation"
        )
      }

      "have additional documents section" in {
        view.getElementById("additional-documents-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.additionalDocuments"
        )
      }
    }

    "actions are disabled using actionsEnabled = false" should {

      val view = itemSection(Mode.Normal, itemWithAnswers, DeclarationType.STANDARD, actionsEnabled = false)(messages)

      "have item header" in {
        view.getElementsByClass("govuk-heading-m").text() mustBe messages("declaration.summary.items.item.sequenceId", "1")
      }

      "have procedure code with change button" in {
        val row = view.getElementsByClass("item-1-procedureCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
        row must haveSummaryValue("1234")

        row mustNot haveSummaryActionsTexts("site.change", "declaration.summary.items.item.procedureCode.change")
        row mustNot haveSummaryActionsHref(ProcedureCodesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have additional procedure codes separated by space with change button" in {
        val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
        row must haveSummaryValue("000 111")

        row mustNot haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalProcedureCodes.change")
        row mustNot haveSummaryActionsHref(AdditionalProcedureCodesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have onward supply answer with change button" in {
        val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
        row must haveSummaryValue("Yes")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.onwardSupplyRelief.change")
        row mustNot haveSummaryActionsHref(FiscalInformationController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have VAT answer with change button" in {
        val row = view.getElementsByClass("item-1-VATdetails-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
        row must haveSummaryValue("GB1234")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.VATdetails.change")
        row mustNot haveSummaryActionsHref(AdditionalFiscalReferencesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have commodity code with change button" in {
        val row = view.getElementsByClass("item-1-commodityCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
        row must haveSummaryValue("1234567890")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.commodityCode.change")
        row mustNot haveSummaryActionsHref(CommodityDetailsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have goods description with change button" in {
        val row = view.getElementsByClass("item-1-goodsDescription-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
        row must haveSummaryValue("description")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.goodsDescription.change")
        row mustNot haveSummaryActionsHref(CommodityDetailsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have un dangerous goods code with change button" in {
        val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
        row must haveSummaryValue("345")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.unDangerousGoodsCode.change")
        row mustNot haveSummaryActionsHref(UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have cus code with change button" in {
        val row = view.getElementsByClass("item-1-cusCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
        row must haveSummaryValue("321")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.cusCode.change")
        row mustNot haveSummaryActionsHref(CusCodeController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have taric codes separated by comma with change button" in {
        val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
        row must haveSummaryValue("999, 888")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.taricAdditionalCodes.change")
        row mustNot haveSummaryActionsHref(TaricCodeSummaryController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have nact codes separated by comma with change button" in {
        val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
        row must haveSummaryValue("111, 222")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.nationalAdditionalCodes.change")
        row mustNot haveSummaryActionsHref(NactCodeSummaryController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have statistical item value with change button" in {
        val row = view.getElementsByClass("item-1-itemValue-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
        row must haveSummaryValue("123")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.itemValue.change")
        row mustNot haveSummaryActionsHref(StatisticalValueController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have supplementary units with change button" in {
        val row = view.getElementsByClass("item-1-supplementaryUnits-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
        row must haveSummaryValue("12")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.supplementaryUnits.change")
        row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have gross weight with change button" in {
        val row = view.getElementsByClass("item-1-grossWeight-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
        row must haveSummaryValue("666")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.grossWeight.change")
        row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have net weight with change button" in {
        val row = view.getElementsByClass("item-1-netWeight-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
        row must haveSummaryValue("555")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.netWeight.change")
        row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have package information section" in {
        view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.packageInformation"
        )
      }

      "have union and national codes section" in {
        view.getElementById("additional-information-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.additionalInformation"
        )
      }

      "have additional documents section" in {
        view.getElementById("additional-documents-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.additionalDocuments"
        )
      }
    }

    "actions are disabled using actionsEnabled flag" should {

      val view = itemSection(Mode.Normal, itemWithAnswers, DeclarationType.STANDARD, actionsEnabled = false)(messages)

      "have item header" in {
        view.getElementsByClass("govuk-heading-m").text() mustBe messages("declaration.summary.items.item.sequenceId", "1")
      }

      "have procedure code with change button" in {
        val row = view.getElementsByClass("item-1-procedureCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
        row must haveSummaryValue("1234")

        row mustNot haveSummaryActionsTexts("site.change", "declaration.summary.items.item.procedureCode.change")
        row mustNot haveSummaryActionsHref(ProcedureCodesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have additional procedure codes separated by space with change button" in {
        val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
        row must haveSummaryValue("000 111")

        row mustNot haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalProcedureCodes.change")
        row mustNot haveSummaryActionsHref(AdditionalProcedureCodesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have onward supply answer with change button" in {
        val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
        row must haveSummaryValue("Yes")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.onwardSupplyRelief.change")
        row mustNot haveSummaryActionsHref(FiscalInformationController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have VAT answer with change button" in {
        val row = view.getElementsByClass("item-1-VATdetails-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
        row must haveSummaryValue("GB1234")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.VATdetails.change")
        row mustNot haveSummaryActionsHref(AdditionalFiscalReferencesController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have commodity code with change button" in {
        val row = view.getElementsByClass("item-1-commodityCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
        row must haveSummaryValue("1234567890")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.commodityCode.change")
        row mustNot haveSummaryActionsHref(CommodityDetailsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have goods description with change button" in {
        val row = view.getElementsByClass("item-1-goodsDescription-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
        row must haveSummaryValue("description")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.goodsDescription.change")
        row mustNot haveSummaryActionsHref(CommodityDetailsController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have un dangerous goods code with change button" in {
        val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
        row must haveSummaryValue("345")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.unDangerousGoodsCode.change")
        row mustNot haveSummaryActionsHref(UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have cus code with change button" in {
        val row = view.getElementsByClass("item-1-cusCode-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
        row must haveSummaryValue("321")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.cusCode.change")
        row mustNot haveSummaryActionsHref(CusCodeController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have taric codes separated by comma with change button" in {
        val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
        row must haveSummaryValue("999, 888")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.taricAdditionalCodes.change")
        row mustNot haveSummaryActionsHref(TaricCodeSummaryController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have nact codes separated by comma with change button" in {
        val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
        row must haveSummaryValue("111, 222")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.nationalAdditionalCodes.change")
        row mustNot haveSummaryActionsHref(NactCodeSummaryController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have statistical item value with change button" in {
        val row = view.getElementsByClass("item-1-itemValue-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
        row must haveSummaryValue("123")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.itemValue.change")
        row mustNot haveSummaryActionsHref(StatisticalValueController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have supplementary units with change button" in {
        val row = view.getElementsByClass("item-1-supplementaryUnits-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
        row must haveSummaryValue("12")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.supplementaryUnits.change")
        row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have gross weight with change button" in {
        val row = view.getElementsByClass("item-1-grossWeight-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
        row must haveSummaryValue("666")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.grossWeight.change")
        row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have net weight with change button" in {
        val row = view.getElementsByClass("item-1-netWeight-row")
        row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
        row must haveSummaryValue("555")

        row mustNot haveSummaryActionsText("site.change declaration.summary.items.item.netWeight.change")
        row mustNot haveSummaryActionsHref(CommodityMeasureController.displayPage(Mode.Normal, itemWithAnswers.id))
      }

      "have package information section" in {
        view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.packageInformation"
        )
      }

      "have union and national codes section" in {
        view.getElementById("additional-information-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.additionalInformation"
        )
      }

      "have additional documents section" in {
        view.getElementById("additional-documents-1-table").getElementsByClass("govuk-table__caption").text() mustBe messages(
          "declaration.summary.items.item.additionalDocuments"
        )
      }
    }
  }

  "Item section with no answers" should {

    val view = itemSection(Mode.Normal, itemWithoutAnswers, DeclarationType.STANDARD)(messages)

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
