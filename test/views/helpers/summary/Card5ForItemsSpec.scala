/*
 * Copyright 2024 HM Revenue & Customs
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

package views.helpers.summary

import base.Injector
import controllers.section5.routes._
import controllers.section6.routes.TransportLeavingTheBorderController
import forms.common.YesNoAnswer.Yes
import forms.section4.Document
import forms.section4.NatureOfTransaction.BusinessPurchase
import forms.section5.{CommodityDetails, CusCode, NactCode, UNDangerousGoodsCode}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.{AdditionalDocuments, AdditionalInformationData, CommodityMeasure}
import org.scalatest.Assertion
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec

class Card5ForItemsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val commodityMeasure = CommodityMeasure(Some("12"), Some(false), Some("666"), Some("555"))

  private val itemWithAnswers = anItem(
    withItemId(itemId),
    withSequenceId(1),
    withProcedureCodes(Some("1234"), Seq("000", "111")),
    withFiscalInformation(),
    withAdditionalFiscalReferenceData(),
    withStatisticalValue("123"),
    withCommodityDetails(CommodityDetails(Some("1234567890"), Some("description"))),
    withUNDangerousGoodsCode(UNDangerousGoodsCode(Some("345"))),
    withCUSCode(CusCode(Some("321"))),
    withNactCodes(NactCode("111"), NactCode("222")),
    withNactExemptionCode(NactCode("VATE")),
    withPackageInformation("PC", 10, "marks"),
    withCommodityMeasure(commodityMeasure),
    withAdditionalInformation("1234", "additionalDescription"),
    withIsLicenseRequired(),
    withAdditionalDocuments(Yes, withAdditionalDocument("C501", "GBAEOC1342"))
  )

  private val itemWithoutAnswers = anItem(withItemId(itemId), withSequenceId(1))

  private val declaration = aDeclaration(withItems(itemWithAnswers, itemWithoutAnswers))

  private val card5ForItems = instanceOf[Card5ForItems]

  private def finalCyaView(decl: ExportsDeclaration, actionEnabled: Boolean = true, showNoItemError: Boolean = false): Html =
    card5ForItems.eval(decl, actionEnabled, showNoItemError)(messages)

  private def miniCyaView(decl: ExportsDeclaration = declaration, actionEnabled: Boolean = true): Html =
    card5ForItems.content(decl, actionEnabled)(messages)

  "Card5ForItems.content" should {
    "return the expected CYA card" in {
      val card = miniCyaView().getElementsByTag("h2").first
      card.text mustBe messages("declaration.summary.section.5")
      assert(card.hasClass("items-card"))
    }
  }

  "Card5ForItems" when {

    "the declaration has items" should {
      val view = miniCyaView()

      "have the 'Add item' link" when {
        "the declaration type is NOT CLEARANCE" in {
          checkCard(view, true)
        }
      }

      "NOT have the 'Add item' link" when {
        "the declaration type is CLEARANCE" in {
          val view = miniCyaView(aDeclaration(withType(CLEARANCE), withItems(itemWithAnswers)))
          view.getElementsByClass("govuk-summary-card__actions").size mustBe 0
        }
      }

      "show all entered items" in {
        val item1 = anItem().copy(sequenceId = 3, cusCode = Some(CusCode(Some("code1"))))
        val item2 = anItem().copy(sequenceId = 9, cusCode = Some(CusCode(Some("code2"))))
        val view = miniCyaView(aDeclaration(withItems(item1, item2)))

        checkCard(view, true)

        val headings = view.getElementsByTag("h3")
        headings.size mustBe 2

        assert(headings.first.hasClass("item-1-heading"))
        val href1 = RemoveItemsSummaryController.displayRemoveItemConfirmationPage(item1.id)
        headings.first must haveSummaryAction(href1)

        assert(headings.last.hasClass("item-2-heading"))
        val href2 = RemoveItemsSummaryController.displayRemoveItemConfirmationPage(item2.id)
        headings.last must haveSummaryAction(href2)

        val call1 = Some(CusCodeController.displayPage(item1.id))
        val row1 = checkSection(view, "item-1", "Declaration item 1 Remove item", 2, 0, 1).first
        checkMultiRowSection(row1, List("item-1-cus-code"), "item.cusCode", "code1", call1, "item.cusCode")

        val call2 = Some(CusCodeController.displayPage(item2.id))
        val row2 = checkSection(view, "item-2", "Declaration item 2 Remove item", 2, 1, 1).first
        checkMultiRowSection(row2, List("item-2-cus-code"), "item.cusCode", "code2", call2, "item.cusCode")
      }

      "NOT have change links" when {
        "'actionsEnabled' is false" in {
          miniCyaView(actionEnabled = false).getElementsByClass(summaryActionsClassName) mustBe empty
        }
      }
    }

    "the declaration has no items" should {

      "NOT display the 'Items' card" when {

        "actionEnabled is false" in {
          finalCyaView(aDeclaration(), false).toString mustBe ""
        }

        "actionEnabled is true and" when {
          "declaration.previousDocuments is undefined and" when {
            "the transport section is empty" in {
              finalCyaView(aDeclaration(withNatureOfTransaction(BusinessPurchase))).toString mustBe ""
            }
          }
        }
      }

      "display the 'Items' card and, inside the card, the 'Add item' warning text" when {
        "actionEnabled is true and" when {

          "declaration.previousDocuments is defined" in {
            checkNoItemsCard(aDeclaration(withPreviousDocuments(Document("355", "reference", None))))
          }

          "the transport section is NOT empty" in {
            checkNoItemsCard(aDeclaration(withTransportCountry(Some("Some country"))))
          }
        }
      }

      "display the 'Items' card and, inside the card, the expected error messages" when {
        "trying to submit the declaration" in {
          val view = finalCyaView(aDeclaration(withBorderTransport()), true, true)
          checkCard(view)

          val summaryCard = view.getElementsByClass("govuk-summary-card").get(0)
          assert(summaryCard.hasClass("govuk-summary-card--error"))

          val rows = view.getElementsByClass(summaryRowClassName)
          rows.size mustBe 2

          val message = rows.first.getElementsByClass("govuk-error-message").first
          message.text mustBe messages("declaration.summary.items.none")

          val warning = rows.last.getElementsByClass("govuk-warning-text").first
          warning.text must endWith(messages("declaration.summary.items.empty"))
        }
      }
    }

    "the declaration has only empty items" should {

      "NOT display the 'Items' card" when {

        "actionEnabled is false" in {
          finalCyaView(aDeclaration(withItems(2)), false).toString mustBe ""
        }

        "actionEnabled is true and" when {
          "declaration.previousDocuments is undefined and" when {
            "the transport section is empty" in {
              finalCyaView(aDeclaration(withItems(2), withNatureOfTransaction(BusinessPurchase))).toString mustBe ""
            }
          }
        }
      }

      "display the 'Items' card and, inside the card, the 'Add item' warning text" when {
        "actionEnabled is true and" when {

          "declaration.previousDocuments is defined" in {
            checkNoItemsCard(aDeclaration(withItems(2), withPreviousDocuments(Document("355", "reference", None))))
          }

          "the transport section is NOT empty" in {
            checkNoItemsCard(aDeclaration(withItems(2), withTransportCountry(Some("Some country"))))
          }
        }
      }
    }
  }

  "Card5ForItems.backLink" should {
    "go to ItemsSummaryController" in {
      card5ForItems.backLink(journeyRequest()) mustBe ItemsSummaryController.displayItemsSummaryPage
    }
  }

  "Card5ForItems.continueTo" should {
    "go to TransportLeavingTheBorderController" in {
      card5ForItems.continueTo(journeyRequest()) mustBe TransportLeavingTheBorderController.displayPage
    }
  }

  def checkCard(view: Appendable, withItems: Boolean = false): Assertion = {
    view.getElementsByTag("h2").text mustBe messages("declaration.summary.section.5")

    val actions = view.getElementsByClass("govuk-summary-card__actions")
    actions.size mustBe 1

    val expectedCall = if (withItems) ItemsSummaryController.addAdditionalItem else ItemsSummaryController.displayAddItemPage
    actions.first must haveSummaryAction(expectedCall, messages("declaration.summary.items.add"))
  }

  def checkNoItemsCard(declaration: ExportsDeclaration): Assertion = {
    val view = miniCyaView(declaration)
    checkCard(view)

    val rows = view.getElementsByClass(summaryRowClassName)
    rows.size mustBe 1

    val warning = rows.first.getElementsByClass("govuk-warning-text").first
    warning.text must endWith(messages("declaration.summary.items.empty"))
  }

  "Items card" when {
    "the declaration has items" should {
      val view = miniCyaView()

      "show 'Procedure code'" in {
        val row = view.getElementsByClass("item-1-procedure-code")
        val call = Some(ProcedureCodesController.displayPage(itemId))
        checkSummaryRow(row, "item.procedureCode", "1234", call, "item.procedureCode")
      }

      "show 'Additional procedure codes'" in {
        val row = view.getElementsByClass("item-1-additional-procedure-codes")
        val call = Some(AdditionalProcedureCodesController.displayPage(itemId))
        checkSummaryRow(row, "item.additionalProcedureCodes", "000 111", call, "item.additionalProcedureCodes")
      }

      "show 'Onward Supply Relief'" in {
        val row = view.getElementsByClass("item-1-onward-supply-relief")
        val call = Some(FiscalInformationController.displayPage(itemId))
        checkSummaryRow(row, "item.onwardSupplyRelief", messages("site.yes"), call, "item.onwardSupplyRelief")
      }

      "show 'VAT details'" in {
        val row = view.getElementsByClass("item-1-vat-details")
        val call = Some(AdditionalFiscalReferencesController.displayPage(itemId))

        val expected = s"${fiscalReference.country}${fiscalReference.reference}"
        checkSummaryRow(row, "item.VATdetails", expected, call, "item.VATdetails")
      }

      "show 'Commodity code'" in {
        val row = view.getElementsByClass("item-1-commodity-code")
        val call = Some(CommodityDetailsController.displayPage(itemId))
        checkSummaryRow(row, "item.commodityCode", "1234567890", call, "item.commodityCode")
      }

      "show 'Goods description'" in {
        val row = view.getElementsByClass("item-1-goods-description")
        val call = Some(CommodityDetailsController.displayPage(itemId))
        checkSummaryRow(row, "item.goodsDescription", "description", call, "item.goodsDescription")
      }

      "show 'UN dangerous goods code'" in {
        val row = view.getElementsByClass("item-1-dangerous-goods-code")
        val call = Some(UNDangerousGoodsCodeController.displayPage(itemId))
        checkSummaryRow(row, "item.unDangerousGoodsCode", "345", call, "item.unDangerousGoodsCode")
      }

      "show 'CUS code'" in {
        val row = view.getElementsByClass("item-1-cus-code")
        val call = Some(CusCodeController.displayPage(itemId))
        checkSummaryRow(row, "item.cusCode", "321", call, "item.cusCode")
      }

      "show 'National additional codes'" in {
        val row = view.getElementsByClass("item-1-national-additional-codes")
        val call = Some(NactCodeSummaryController.displayPage(itemId))
        checkSummaryRow(row, "item.nationalAdditionalCodes", "111, 222", call, "item.nationalAdditionalCodes")
      }

      "show 'VAT zero rating'" in {
        val row = view.getElementsByClass("item-1-zero-rated-for-vat")
        val call = Some(ZeroRatedForVatController.displayPage(itemId))
        checkSummaryRow(row, "item.zeroRatedForVat", "No, exempt", call, "item.zeroRatedForVat")
      }

      "show 'Item value'" in {
        val row = view.getElementsByClass("item-1-item-value")
        val call = Some(StatisticalValueController.displayPage(itemId))
        checkSummaryRow(row, "item.itemValue", "123", call, "item.itemValue")
      }

      "show the 'Packing details' section when without data" in {
        val item = itemWithAnswers.copy(packageInformation = Some(List.empty))
        val view = miniCyaView(declaration.copy(items = List(item)))

        val row = view.getElementsByClass("item-1-package-information-heading")
        val call = Some(PackageInformationSummaryController.displayPage(itemId))
        checkSummaryRow(row, "item.packageInformation", messages("site.none"), call, "item.packageInformation")
      }

      "show the 'Packing details' section" in {
        val heading = view.getElementsByClass("item-1-package-information-heading").first
        heading.tagName mustBe "h3"
        heading.text mustBe messages("declaration.summary.item.packageInformation")

        val summaryLists = view.getElementsByClass("govuk-summary-list")
        summaryLists.size mustBe 5

        val summaryList = summaryLists.get(1)
        summaryList.childrenSize mustBe 3

        val rows = summaryList.children

        val call = Some(PackageInformationSummaryController.displayPage(itemId))
        val keyId = "item.packageInformation"

        checkMultiRowSection(rows.get(0), List("item-1-package-information-1-type"), s"$keyId.type", "Parcel (PC)", call, keyId)
        checkMultiRowSection(rows.get(1), List("item-1-package-information-1-number"), s"$keyId.number", "10")
        checkMultiRowSection(rows.get(2), List("item-1-package-information-1-markings"), s"$keyId.markings", "marks")
      }

      "show the 'Packing details' section when the 'type' is undefined" in {
        val packageInformation = itemWithAnswers.packageInformation.value.head.copy(typesOfPackages = None)
        val item = itemWithAnswers.copy(packageInformation = Some(List(packageInformation)))
        val view = miniCyaView(declaration.copy(items = List(item)))

        view.getElementsByClass("item-1-package-information-1-type").size mustBe 0

        val row1 = view.getElementsByClass("item-1-package-information-1-number")
        val call = Some(PackageInformationSummaryController.displayPage(itemId))
        checkSummaryRow(row1, "item.packageInformation.number", "10", call, "item.packageInformation")
        assert(row1.first.hasClass("govuk-summary-list__row--no-border"))

        val row2 = view.getElementsByClass("item-1-package-information-1-markings")
        checkSummaryRow(row2, "item.packageInformation.markings", "marks", None, "")
      }

      "show the 'Packing details' section when the 'type' and 'number' are undefined" in {
        val packageInformation = itemWithAnswers.packageInformation.value.head.copy(typesOfPackages = None, numberOfPackages = None)
        val item = itemWithAnswers.copy(packageInformation = Some(List(packageInformation)))
        val view = miniCyaView(declaration.copy(items = List(item)))

        view.getElementsByClass("item-1-package-information-1-type").size mustBe 0
        view.getElementsByClass("item-1-package-information-1-number").size mustBe 0

        val row = view.getElementsByClass("item-1-package-information-1-markings")
        val call = Some(PackageInformationSummaryController.displayPage(itemId))
        checkSummaryRow(row, "item.packageInformation.markings", "marks", call, "item.packageInformation")
      }

      "show 'Gross weight in kilograms'" in {
        val row = view.getElementsByClass("item-1-gross-weight")
        val call = Some(CommodityMeasureController.displayPage(itemId))
        checkSummaryRow(row, "item.grossWeight", "666", call, "item.grossWeight")
        assert(row.first.hasClass("govuk-summary-list__row--no-border"))
      }

      "show 'Net weight in kilograms'" in {
        val row = view.getElementsByClass("item-1-net-weight")
        checkSummaryRow(row, "item.netWeight", "555", None, "ign")
      }

      "show 'Supplementary units' only for STANDARD & SUPPLEMENTARY declarations" in {
        val call = Some(SupplementaryUnitsController.displayPage(itemId))

        allDeclarationTypes.foreach { declarationType =>
          val view = miniCyaView(declaration.copy(`type` = declarationType))
          val row = view.getElementsByClass("item-1-supplementary-units")
          declarationType match {
            case STANDARD | SUPPLEMENTARY =>
              checkSummaryRow(row, "item.supplementaryUnits", "12", call, "item.supplementaryUnits")

            case _ => row.size mustBe 0
          }
        }
      }

      "show the 'Additional Information' section when without data" in {
        val item = itemWithAnswers.copy(additionalInformation = Some(AdditionalInformationData(items = List.empty)))
        val view = miniCyaView(declaration.copy(items = List(item)))

        val row = view.getElementsByClass("item-1-additional-information-heading")
        val call = Some(AdditionalInformationRequiredController.displayPage(itemId))
        checkSummaryRow(row, "item.additionalInformation", messages("site.none"), call, "item.additionalInformation")
      }

      "show the 'Additional Information' section" in {
        val heading = view.getElementsByClass("item-1-additional-information-heading").first
        heading.tagName mustBe "h3"
        heading.text mustBe messages("declaration.summary.item.additionalInformation")

        val summaryLists = view.getElementsByClass("govuk-summary-list")
        summaryLists.size mustBe 5

        val summaryList = summaryLists.get(3)
        summaryList.childrenSize mustBe 2

        val rows = summaryList.children

        val call = Some(AdditionalInformationController.displayPage(itemId))
        val keyId = "item.additionalInformation"

        checkMultiRowSection(rows.get(0), List("item-1-additional-information-1-code"), s"$keyId.code", "1234", call, keyId)
        checkMultiRowSection(rows.get(1), List("item-1-additional-information-1-description"), s"$keyId.description", "additionalDescription")
      }

      val addDocs = "item.additionalDocuments"

      "show the 'Additional Documents' section when without data, preceded by the 'License' row" in {
        val item = itemWithAnswers.copy(additionalDocuments = Some(AdditionalDocuments(None, List.empty)))
        val view = miniCyaView(declaration.copy(items = List(item)))

        val rows = view.getElementsByClass("govuk-summary-list__row")
        assert(rows.get(rows.size - 2).hasClass("item-1-licences"))
        assert(rows.get(rows.size - 1).hasClass("item-1-additional-documents-heading"))

        val licenses = view.getElementsByClass("item-1-licences")
        val call1 = Some(IsLicenceRequiredController.displayPage(itemId))
        checkSummaryRow(licenses, "item.licences", messages("site.yes"), call1, "item.licences")

        val heading = view.getElementsByClass("item-1-additional-documents-heading")
        val call2 = Some(AdditionalDocumentsController.displayPage(itemId))
        checkSummaryRow(heading, addDocs, messages("site.none"), call2, addDocs)
      }

      "show the 'Additional Documents' section, with the 'License' row as first" in {
        val rows = checkSection(view, "item-1-additional-documents", addDocs, 5, 4, 3)

        val call1 = Some(IsLicenceRequiredController.displayPage(itemId))
        checkMultiRowSection(rows.get(0), List("item-1-licences"), "item.licences", messages("site.yes"), call1, "item.licences")

        val call2 = Some(AdditionalDocumentsController.displayPage(itemId))
        checkMultiRowSection(rows.get(1), List("item-1-additional-document-1-code"), s"$addDocs.code", "C501", call2, addDocs)

        checkMultiRowSection(rows.get(2), List("item-1-additional-document-1-identifier"), s"$addDocs.identifier", "GBAEOC1342")
      }

      "show the 'Additional Documents' section when 'document code' is undefined" in {
        val item = anItem(withNoLicense, withAdditionalDocuments(None, withAdditionalDocument(None, Some("GBAEOC1342"))))
        val view = miniCyaView(aDeclaration(withItems(item)))

        view.getElementsByClass("item-1-licences").size mustBe 0
        view.getElementsByClass("item-1-additional-document-1-code").size mustBe 0

        val row = view.getElementsByClass("item-1-additional-document-1-identifier")
        val call = Some(AdditionalDocumentsController.displayPage(item.id))
        checkSummaryRow(row, s"$addDocs.identifier", "GBAEOC1342", call, addDocs)
      }
    }
  }
}
