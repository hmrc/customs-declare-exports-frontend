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
import controllers.section4.routes._
import controllers.section5.routes.ItemsSummaryController
import forms.section4.{Document, PreviousDocumentsData}
import models.DeclarationType._
import models.declaration.InvoiceAndPackageTotals
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec

class Card4ForTransactionsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val totalAmountInvoiced = "123"
  private val exchangeRate = "1.23"
  private val currency = "GBP"
  private val packages = "12"
  private val natureOfTransaction = "2"

  private val document1 = Document("355", "ref1", None)
  private val document2 = Document("705", "ref2", None)

  private val declaration = aDeclaration(
    withTotalNumberOfItems(Some(totalAmountInvoiced), Some(exchangeRate), Some(currency)),
    withTotalPackageQuantity(packages),
    withNatureOfTransaction(natureOfTransaction),
    withPreviousDocuments(document1, document2)
  )

  private val card4ForTransactions = instanceOf[Card4ForTransactions]

  "Transactions section" should {
    val view = card4ForTransactions.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.section.4")
    }

    "show the total amount invoiced" when {
      standardAndSupplementary.foreach { declarationType =>
        s"the declaration type is $declarationType and" when {

          "totalAmountInvoiced is NON-empty" in {
            val declaration1 = declaration.copy(`type` = declarationType)
            val row = card4ForTransactions.eval(declaration1)(messages).getElementsByClass("item-amount")
            assert(row.hasClass("govuk-summary-list__row--no-border"))

            val expectedValue = s"$currency $totalAmountInvoiced"
            val expectedCall = Some(InvoiceAndExchangeRateController.displayPage)
            checkSummaryRow(row, "transaction.itemAmount", expectedValue, expectedCall, "transaction.itemAmount")
          }

          "totalAmountInvoiced is empty" in {
            val invoiceAndPackageTotals = Some(InvoiceAndPackageTotals(Some(""), Some(currency)))
            val declaration1 = declaration.copy(`type` = declarationType, totalNumberOfItems = invoiceAndPackageTotals)
            val row = card4ForTransactions.eval(declaration1)(messages).getElementsByClass("item-amount")

            val expectedValue = messages("declaration.totalAmountInvoiced.value.lessThan100000")
            val expectedCall = Some(InvoiceAndExchangeRateChoiceController.displayPage)
            checkSummaryRow(row, "transaction.itemAmount", expectedValue, expectedCall, "transaction.itemAmount")
          }
        }
      }
    }

    "NOT show the total amount invoiced" when {
      List(CLEARANCE, OCCASIONAL, SIMPLIFIED).foreach { declarationType =>
        s"the declaration type is $declarationType" in {
          val declaration1 = declaration.copy(`type` = declarationType)
          card4ForTransactions.eval(declaration1)(messages).getElementsByClass("item-amount").size mustBe 0
        }
      }
    }

    "show the exchange rate" in {
      val row = view.getElementsByClass("exchange-rate")
      checkSummaryRow(row, "transaction.exchangeRate", exchangeRate)
    }

    "show the total number of Packages" in {
      val row = view.getElementsByClass("total-packages")

      val expectedCall = Some(TotalPackageQuantityController.displayPage)
      checkSummaryRow(row, "transaction.totalNoOfPackages", packages, expectedCall, "transaction.totalNoOfPackages")
    }

    "show the nature of transaction" in {
      val row = view.getElementsByClass("nature-of-transaction")

      val expectedValue = messages(s"declaration.summary.transaction.natureOfTransaction.$natureOfTransaction")
      val expectedCall = Some(NatureOfTransactionController.displayPage)
      checkSummaryRow(row, "transaction.natureOfTransaction", expectedValue, expectedCall, "transaction.natureOfTransaction")
    }

    "have an empty previous documents section" when {
      "no previous documents have been entered" in {
        val previousDocumentsData = withPreviousDocumentsData(Some(PreviousDocumentsData(List.empty)))
        val view = card4ForTransactions.eval(aDeclarationAfter(declaration, previousDocumentsData))(messages)

        val row = view.getElementsByClass("previous-documents-heading")

        val call = Some(PreviousDocumentsSummaryController.displayPage)
        checkSummaryRow(row, "transaction.previousDocuments", messages("site.none"), call, "transaction.previousDocuments")
      }
    }

    "show a previous documents section" in {
      val heading = view.getElementsByClass("previous-documents-heading")
      checkSummaryRow(heading, "transaction.previousDocuments", "", None, "")

      val call = Some(PreviousDocumentsSummaryController.displayPage)

      val document1Type = view.getElementsByClass("previous-document-1-type")
      assert(document1Type.hasClass("govuk-summary-list__row--no-border"))
      val expectedType1 = "Entry Summary Declaration (ENS) - 355"
      checkSummaryRow(document1Type, "transaction.previousDocuments.type", expectedType1, call, "transaction.previousDocuments")

      val document1Ref = view.getElementsByClass("previous-document-1-reference")
      assert(!document1Ref.hasClass("govuk-summary-list__row--no-border"))
      checkSummaryRow(document1Ref, "transaction.previousDocuments.reference", "ref1", None, "ign")

      val document2Type = view.getElementsByClass("previous-document-2-type")
      assert(document2Type.hasClass("govuk-summary-list__row--no-border"))
      val expectedType2 = "Bill of Lading - 705"
      checkSummaryRow(document2Type, "transaction.previousDocuments.type", expectedType2, call, "transaction.previousDocuments")

      val document2Ref = view.getElementsByClass("previous-document-2-reference")
      assert(!document2Ref.hasClass("govuk-summary-list__row--no-border"))
      checkSummaryRow(document2Ref, "transaction.previousDocuments.reference", "ref2", None, "ign")
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card4ForTransactions.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }
  }

  "Card4ForTransactions.content" should {
    "return the expected CYA card" in {
      val cardContent = card4ForTransactions.content(declaration)
      cardContent.getElementsByClass("transaction-card").text mustBe messages("declaration.summary.section.4")
    }
  }

  "Card4ForTransactions.backLink" when {
    "go to PreviousDocumentsSummaryController" in {
      card4ForTransactions.backLink(journeyRequest()) mustBe PreviousDocumentsSummaryController.displayPage
    }
  }

  "Card4ForTransactions.continueTo" should {
    "go to ItemsSummaryController" in {
      card4ForTransactions.continueTo(journeyRequest()) mustBe ItemsSummaryController.displayItemsSummaryPage
    }
  }
}
