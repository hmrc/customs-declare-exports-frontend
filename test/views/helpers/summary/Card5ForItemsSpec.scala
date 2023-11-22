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

package views.helpers.summary

import base.Injector
import controllers.declaration.routes._
import forms.common.YesNoAnswer.Yes
import forms.declaration.NatureOfTransaction.BusinessPurchase
import forms.declaration._
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.declaration.CommodityMeasure
import org.jsoup.select.Elements
import org.scalatest.Assertion
import play.api.mvc.Call
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card5ForItemsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

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

  private val itemWithoutAnswers = anItem(withItemId(itemId), withSequenceId(1))

  private val declaration = aDeclaration(withItems(itemWithAnswers, itemWithoutAnswers))

  private val card5ForItems = instanceOf[Card5ForItems]

  private def createView(decl: ExportsDeclaration = declaration, actionEnabled: Boolean = true): Html =
    card5ForItems.eval(decl, actionEnabled)(messages)

  "Items card" when {

    "the declaration has items" should {
      val view = createView()

      "have the expected items heading" in {
        view.getElementsByTag("h2").first.text mustBe messages("declaration.summary.section.5")
      }

      "have the 'Add item' link" when {
        "the declaration type is NOT CLEARANCE" in {
          checkCard(view, true)

          view.getElementsByTag("strong").first.text mustBe messages("declaration.summary.item", 1)
        }
      }

      "NOT have the 'Add item' link" when {
        "the declaration type is CLEARANCE" in {
          val view = createView(aDeclaration(withType(CLEARANCE), withItems(itemWithAnswers)))
          view.getElementsByTag("form").size() mustBe 0
          view.getElementsByClass("input-submit").size() mustBe 0

          view.getElementsByTag("strong").first.text mustBe messages("declaration.summary.item", 1)
        }
      }

      "show all entered items" in {
        val item1 = anItem().copy(sequenceId = 3, cusCode = Some(CusCode(Some("code1"))))
        val item2 = anItem().copy(sequenceId = 9, cusCode = Some(CusCode(Some("code2"))))
        val view = createView(aDeclaration(withItems(item1, item2)))

        checkCard(view, true)

        val rows = view.getElementsByClass(summaryRowClassName)
        rows.size mustBe 4

        val item1Heading = view.getElementsByClass("item-1-heading")
        val call1 = Some(RemoveItemsSummaryController.displayRemoveItemConfirmationPage(item1.id))
        checkRow(item1Heading, "1", "", call1)

        val item2Heading = view.getElementsByClass("item-2-heading")
        val call2 = Some(RemoveItemsSummaryController.displayRemoveItemConfirmationPage(item2.id))
        checkRow(item2Heading, "2", "", call2)
      }

      "NOT have change links" when {
        "'actionsEnabled' is false" in {
          createView(actionEnabled = false).getElementsByClass(summaryActionsClassName) mustBe empty
        }
      }
    }

    "the declaration has no items" should {

      "NOT display the 'Items' card" when {

        "actionEnabled is false" in {
          createView(aDeclaration(), false).toString mustBe ""
        }

        "actionEnabled is true and" when {
          "declaration.previousDocuments is undefined and" when {
            "the transport section is empty" in {
              createView(aDeclaration(withNatureOfTransaction(BusinessPurchase))).toString mustBe ""
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
    }

    "the declaration has only empty items" should {

      "NOT display the 'Items' card" when {

        "actionEnabled is false" in {
          createView(aDeclaration(withItems(2)), false).toString mustBe ""
        }

        "actionEnabled is true and" when {
          "declaration.previousDocuments is undefined and" when {
            "the transport section is empty" in {
              createView(aDeclaration(withItems(2), withNatureOfTransaction(BusinessPurchase))).toString mustBe ""
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

  "Card5ForItems.content" should {
    "return the expected CYA card" in {
      val cardContent = card5ForItems.content(declaration)
      cardContent.getElementsByClass("items-card").text mustBe messages("declaration.summary.section.5")
    }
  }

  "Card5ForItems.backLink" when {
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

    val link = view.getElementsByTag("a").first
    link.text mustBe messages("declaration.summary.items.add")

    val expectedCall = if (withItems) ItemsSummaryController.addAdditionalItem else ItemsSummaryController.displayAddItemPage
    link.attr("href") mustBe expectedCall.url
  }

  def checkNoItemsCard(declaration: ExportsDeclaration): Assertion = {
    val view = createView(declaration)
    checkCard(view)

    val rows = view.getElementsByClass(summaryRowClassName)
    rows.size mustBe 1

    val warning = rows.first.getElementsByClass("govuk-warning-text").first
    warning.text must endWith(messages("declaration.summary.items.empty"))
  }

  def checkRow(row: Elements, index: String, value: String, maybeUrl: Option[Call] = None, maybeId: Option[String] = None): Assertion = {
    if (maybeUrl.isDefined) {
      val args = maybeId.fold {
        ("declaration.summary.item.remove", "declaration.summary.item.remove", List.empty[String])
      } { id =>
        ("site.change", s"declaration.summary.item.$id.change", List(index))
      }
      row must haveSummaryActionsTexts(args._1, args._2, args._3: _*)
      row must haveSummaryActionsHref(maybeUrl.get)
    }

    row must haveSummaryKey(messages(s"""declaration.summary.item${maybeId.fold("")(id => s".$id")}""", index))
    row must haveSummaryValue(value)
  }
}
