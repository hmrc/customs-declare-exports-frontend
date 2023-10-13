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
          action.text mustBe messages("declaration.summary.items.item.headerAction")
          action must haveHref(controllers.declaration.routes.RemoveItemsSummaryController.displayRemoveItemConfirmationPage(itemId, true))
        }

        "have a 'procedure code' row" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
          row must haveSummaryValue("1234")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.procedureCode.change", "1")

          row must haveSummaryActionWithPlaceholder(ProcedureCodesController.displayPage(itemWithAnswers.id))
        }

        "have an 'additional procedure codes' row" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
          row must haveSummaryValue("000 111")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalProcedureCodes.change", "1")

          row must haveSummaryActionWithPlaceholder(AdditionalProcedureCodesController.displayPage(itemWithAnswers.id))
        }

        "have an 'onward supply answer' row" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
          row must haveSummaryValue("Yes")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.onwardSupplyRelief.change", "1")

          row must haveSummaryActionWithPlaceholder(FiscalInformationController.displayPage(itemWithAnswers.id))
        }

        "have a 'VAT answer' row" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
          row must haveSummaryValue("GB1234")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.VATdetails.change", "1")

          row must haveSummaryActionWithPlaceholder(AdditionalFiscalReferencesController.displayPage(itemWithAnswers.id))
        }

        "have a 'commodity code' row" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
          row must haveSummaryValue("1234567890")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.commodityCode.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityDetailsController.displayPage(itemWithAnswers.id))
        }

        "have a 'goods description' row" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
          row must haveSummaryValue("description")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.goodsDescription.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityDetailsController.displayPage(itemWithAnswers.id))
        }

        "have a 'undangerous goods code' row" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
          row must haveSummaryValue("345")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.unDangerousGoodsCode.change", "1")

          row must haveSummaryActionWithPlaceholder(UNDangerousGoodsCodeController.displayPage(itemWithAnswers.id))
        }

        "have a 'cus code' row" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
          row must haveSummaryValue("321")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.cusCode.change", "1")

          row must haveSummaryActionWithPlaceholder(CusCodeController.displayPage(itemWithAnswers.id))
        }

        "have a 'taric codes' row" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
          row must haveSummaryValue("999, 888")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.taricAdditionalCodes.change", "1")

          row must haveSummaryActionWithPlaceholder(TaricCodeSummaryController.displayPage(itemWithAnswers.id))
        }

        "have a 'nact codes' row" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
          row must haveSummaryValue("111, 222")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.nationalAdditionalCodes.change", "1")

          row must haveSummaryActionWithPlaceholder(NactCodeSummaryController.displayPage(itemWithAnswers.id))
        }

        "have a 'zero rated for vat' row" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.zeroRatedForVat"))
          row must haveSummaryValue(messages("declaration.summary.items.item.zeroRatedForVat.VATE"))

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.zeroRatedForVat.change", "1")

          row must haveSummaryActionWithPlaceholder(ZeroRatedForVatController.displayPage(itemWithAnswers.id))
        }

        "have a 'statistical item value' row" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
          row must haveSummaryValue("123")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.itemValue.change", "1")

          row must haveSummaryActionWithPlaceholder(StatisticalValueController.displayPage(itemWithAnswers.id))
        }

        "have a 'package information' section" in {
          view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text mustBe messages(
            "declaration.summary.items.item.packageInformation"
          )
        }

        "have a 'package information displayed on the page before 'Commodity Measures'" in {
          val body = view.child(0).children.get(1)
          val elements = body.children
          assert(elements.get(2).text.startsWith("Packing details"))
          assert(elements.get(3).text.startsWith("Gross weight"))
        }

        "have a 'supplementary units' row" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
          row must haveSummaryValue("12")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.supplementaryUnits.change", "1")

          row must haveSummaryActionWithPlaceholder(SupplementaryUnitsController.displayPage(itemWithAnswers.id))
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
          row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
          row must haveSummaryValue("666")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.grossWeight.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have a 'net weight' row" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
          row must haveSummaryValue("555")

          row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.netWeight.change", "1")

          row must haveSummaryActionWithPlaceholder(CommodityMeasureController.displayPage(itemWithAnswers.id))
        }

        "have an 'Additional Information' row" when {
          "additionalInformation is defined but has no data" in {
            val item = anItemAfter(itemWithAnswers, withoutAdditionalInformation(true))
            val view = itemSection(item, 0, STANDARD)(messages)

            val sequenceId = item.sequenceId.toString
            val summaryList = view.getElementsByClass(s"item-$sequenceId-additional-information-summary").get(0)
            val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
            summaryListRows.size mustBe 1

            val noDataRow = summaryListRows.get(0).getElementsByClass(s"item-$sequenceId-additional-information-heading")
            noDataRow must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation"))
            noDataRow must haveSummaryValue(messages("site.none"))
            noDataRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalInformation.change", sequenceId)
            noDataRow must haveSummaryActionWithPlaceholder(AdditionalInformationRequiredController.displayPage(item.id))
          }
        }

        "have an 'Additional Information' section" in {
          val ai1 = AdditionalInformation("Code1", "Exporter1")
          val ai2 = AdditionalInformation("Code2", "Exporter2")
          val item = anItemAfter(itemWithoutAnswers, withAdditionalInformation(ai1, ai2))
          val view = itemSection(item, 0, STANDARD)(messages)

          val sequenceId = item.sequenceId.toString
          val summaryList = view.getElementsByClass(s"item-$sequenceId-additional-information-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 5

          val headingRow = summaryListRows.get(0).getElementsByClass(s"item-$sequenceId-additional-information-heading")
          headingRow must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation"))
          headingRow must haveSummaryValue("")

          val document1Row1 = summaryListRows.get(1).getElementsByClass(s"item-$sequenceId-additional-information-1-code")
          document1Row1 must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation.code"))
          document1Row1 must haveSummaryValue("Code1")
          document1Row1 must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalInformation.change", sequenceId)
          document1Row1 must haveSummaryActionWithPlaceholder(AdditionalInformationController.displayPage(item.id))

          val document1Row2 = summaryListRows.get(2).getElementsByClass(s"item-$sequenceId-additional-information-1-description")
          document1Row2 must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation.description"))
          document1Row2 must haveSummaryValue("Exporter1")
          document1Row2.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0

          val document2Row3 = summaryListRows.get(3).getElementsByClass(s"item-$sequenceId-additional-information-2-code")
          document2Row3 must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation.code"))
          document2Row3 must haveSummaryValue("Code2")
          document2Row3 must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalInformation.change", sequenceId)
          document2Row3 must haveSummaryActionWithPlaceholder(AdditionalInformationController.displayPage(item.id))

          val document2Row4 = summaryListRows.get(4).getElementsByClass(s"item-$sequenceId-additional-information-2-description")
          document2Row4 must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation.description"))
          document2Row4 must haveSummaryValue("Exporter2")
          document2Row4.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0
        }

        "have a 'Licenses' row" when {
          "isLicenceRequired is 'yes' (or 'no', but not None) and" when {

            "additionalDocuments is undefined" in {
              val item = anItemAfter(itemWithAnswers, withoutAdditionalDocuments())
              val view = itemSection(item, 0, STANDARD)(messages)

              val sequenceId = item.sequenceId.toString
              val summaryList = view.getElementsByClass(s"item-$sequenceId-additional-documents-summary").get(0)
              val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
              summaryListRows.size mustBe 1

              val licensesRow = summaryListRows.get(0).getElementsByClass(s"item-$sequenceId-licenses")
              licensesRow must haveSummaryKey(messages("declaration.summary.items.item.licences"))
              licensesRow must haveSummaryValue(messages("site.yes"))
              licensesRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.licences.change", sequenceId)
              licensesRow must haveSummaryActionWithPlaceholder(IsLicenceRequiredController.displayPage(item.id))
            }

            "additionalDocuments is defined (but has no documents)" should {
              "also have an 'Additional documents' row" in {
                val item = anItemAfter(itemWithAnswers, withoutAdditionalDocuments(true))
                val view = itemSection(item, 0, STANDARD)(messages)

                val sequenceId = item.sequenceId.toString
                val summaryList = view.getElementsByClass(s"item-$sequenceId-additional-documents-summary").get(0)
                val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
                summaryListRows.size mustBe 2

                val licensesRow = summaryListRows.get(0).getElementsByClass(s"item-$sequenceId-licenses")
                licensesRow must haveSummaryKey(messages("declaration.summary.items.item.licences"))
                licensesRow must haveSummaryValue(messages("site.yes"))
                licensesRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.licences.change", sequenceId)
                licensesRow must haveSummaryActionWithPlaceholder(IsLicenceRequiredController.displayPage(item.id))

                val noDocumentsRow = summaryListRows.get(1).getElementsByClass(s"item-$sequenceId-additional-documents-heading")
                noDocumentsRow must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments"))
                noDocumentsRow must haveSummaryValue(messages("site.none"))
                noDocumentsRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.change", sequenceId)
                noDocumentsRow must haveSummaryActionWithPlaceholder(AdditionalDocumentsController.displayPage(item.id))
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

            val sequenceId = item.sequenceId.toString
            val summaryList = view.getElementsByClass(s"item-$sequenceId-additional-documents-summary").get(0)
            val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
            summaryListRows.size mustBe 6

            val headingRow = summaryListRows.get(0).getElementsByClass(s"item-$sequenceId-additional-documents-heading")
            headingRow must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments"))
            headingRow must haveSummaryValue("")

            val licensesRow = summaryListRows.get(1).getElementsByClass(s"item-$sequenceId-licenses")
            licensesRow must haveSummaryKey(messages("declaration.summary.items.item.licences"))
            licensesRow must haveSummaryValue(messages("site.yes"))
            licensesRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.licences.change", sequenceId)
            licensesRow must haveSummaryActionWithPlaceholder(IsLicenceRequiredController.displayPage(item.id))

            val document1Row1 = summaryListRows.get(2).getElementsByClass(s"item-$sequenceId-document-1-code")
            document1Row1 must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.code"))
            document1Row1 must haveSummaryValue("C501")
            document1Row1 must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.change", sequenceId)
            document1Row1 must haveSummaryActionWithPlaceholder(AdditionalDocumentsController.displayPage(item.id))

            val document1Row2 = summaryListRows.get(3).getElementsByClass(s"item-$sequenceId-document-1-identifier")
            document1Row2 must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.identifier"))
            document1Row2 must haveSummaryValue("GBAEOC1342")
            document1Row2.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0

            val document2Row = summaryListRows.get(4).getElementsByClass(s"item-$sequenceId-document-2-code")
            document2Row must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.code"))
            document2Row must haveSummaryValue("A123")
            document2Row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.change", sequenceId)
            document2Row must haveSummaryActionWithPlaceholder(AdditionalDocumentsController.displayPage(item.id))

            val document3Row = summaryListRows.get(5).getElementsByClass(s"item-$sequenceId-document-3-identifier")
            document3Row must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.identifier"))
            document3Row must haveSummaryValue("GBAEOS9876")
            document3Row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.change", sequenceId)
            document3Row must haveSummaryActionWithPlaceholder(AdditionalDocumentsController.displayPage(item.id))
          }
        }
      }

      "actions are disabled using actionsEnabled = false" should {

        val view = itemSection(itemWithAnswers, 0, STANDARD, actionsEnabled = false)(messages)

        "NOT have change links (which are instead added when 'actionsEnabled' is true" in {
          view.getElementsByClass("govuk-summary-list__actions") mustBe empty
        }

        "still have an 'Item' header" in {
          val subHeader = view.getElementsByClass("govuk-heading-s").get(0)
          subHeader.text mustBe messages("declaration.summary.items.item.presentationId", "1")
        }

        "still have a 'procedure code' row" in {
          val row = view.getElementsByClass("item-1-procedureCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.procedureCode"))
          row must haveSummaryValue("1234")
        }

        "still have an 'additional procedure codes' row" in {
          val row = view.getElementsByClass("item-1-additionalProcedureCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.additionalProcedureCodes"))
          row must haveSummaryValue("000 111")
        }

        "still have an 'onward supply answer' row" in {
          val row = view.getElementsByClass("item-1-onwardSupplyRelief-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.onwardSupplyRelief"))
          row must haveSummaryValue("Yes")
        }

        "still have a 'VAT answer' row" in {
          val row = view.getElementsByClass("item-1-VATdetails-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.VATdetails"))
          row must haveSummaryValue("GB1234")
        }

        "still have a 'commodity code' row" in {
          val row = view.getElementsByClass("item-1-commodityCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.commodityCode"))
          row must haveSummaryValue("1234567890")
        }

        "still have a 'goods description' row" in {
          val row = view.getElementsByClass("item-1-goodsDescription-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.goodsDescription"))
          row must haveSummaryValue("description")
        }

        "still have an 'undangerous goods code' row" in {
          val row = view.getElementsByClass("item-1-unDangerousGoodsCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.unDangerousGoodsCode"))
          row must haveSummaryValue("345")
        }

        "still have a 'cus code' row" in {
          val row = view.getElementsByClass("item-1-cusCode-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.cusCode"))
          row must haveSummaryValue("321")
        }

        "still have a 'taric codes' row" in {
          val row = view.getElementsByClass("item-1-taricAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.taricAdditionalCodes"))
          row must haveSummaryValue("999, 888")
        }

        "still have a 'nact codes' row" in {
          val row = view.getElementsByClass("item-1-nationalAdditionalCodes-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.nationalAdditionalCodes"))
          row must haveSummaryValue("111, 222")
        }

        "still have a 'zero rated for vat' row" in {
          val row = view.getElementsByClass("item-1-zeroRatedForVat-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.zeroRatedForVat"))
          row must haveSummaryValue(messages("declaration.summary.items.item.zeroRatedForVat.VATE"))
        }

        "still have a 'statistical item value' row" in {
          val row = view.getElementsByClass("item-1-itemValue-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.itemValue"))
          row must haveSummaryValue("123")
        }

        "still have a 'package information' section" in {
          view.getElementById("package-information-1-table").getElementsByClass("govuk-table__caption").text mustBe messages(
            "declaration.summary.items.item.packageInformation"
          )
        }

        "still have a 'supplementary units' row" in {
          val row = view.getElementsByClass("item-1-supplementaryUnits-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.supplementaryUnits"))
          row must haveSummaryValue("12")
        }

        "still have a 'gross weight' row" in {
          val row = view.getElementsByClass("item-1-grossWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.grossWeight"))
          row must haveSummaryValue("666")
        }

        "still have a 'net weight' row" in {
          val row = view.getElementsByClass("item-1-netWeight-row")
          row must haveSummaryKey(messages("declaration.summary.items.item.netWeight"))
          row must haveSummaryValue("555")
        }

        "still have an 'Additional information' section" in {
          val summaryList = view.getElementsByClass("item-1-additional-information-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 3

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-additional-information-heading")
          headingRow must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation"))
          headingRow must haveSummaryValue("")

          val document1Row1 = summaryListRows.get(1).getElementsByClass("item-1-additional-information-1-code")
          document1Row1 must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation.code"))
          document1Row1 must haveSummaryValue("1234")

          val document1Row2 = summaryListRows.get(2).getElementsByClass("item-1-additional-information-1-description")
          document1Row2 must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation.description"))
          document1Row2 must haveSummaryValue("additionalDescription")
        }

        "still have an 'Additional documents' section" in {
          val summaryList = view.getElementsByClass("item-1-additional-documents-summary").get(0)
          val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
          summaryListRows.size mustBe 4

          val headingRow = summaryListRows.get(0).getElementsByClass("item-1-additional-documents-heading")
          headingRow must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments"))
          headingRow must haveSummaryValue("")

          val licensesRow = summaryListRows.get(1).getElementsByClass("item-1-licenses")
          licensesRow must haveSummaryKey(messages("declaration.summary.items.item.licences"))
          licensesRow must haveSummaryValue(messages("site.yes"))

          val document1Row1 = summaryListRows.get(2).getElementsByClass("item-1-document-1-code")
          document1Row1 must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.code"))
          document1Row1 must haveSummaryValue("C501")

          val document1Row2 = summaryListRows.get(3).getElementsByClass("item-1-document-1-identifier")
          document1Row2 must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments.identifier"))
          document1Row2 must haveSummaryValue("GBAEOC1342")
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

      "not display a 'package information section' row" in {
        view.getElementsByClass("item-1-commodityCode-row") mustBe empty
        Option(view.getElementById("package-information-1")) mustBe None
      }

      "not display an 'Additional information' row" in {
        view.getElementsByClass("item-1-additional-information-summary") mustBe empty
      }

      "not display an 'Additional documents'' row" in {
        view.getElementsByClass("item-1-additional-documents-summary") mustBe empty
      }
    }
  }
}
