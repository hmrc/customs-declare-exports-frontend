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
import forms.declaration.Document
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.transaction_section

import scala.jdk.CollectionConverters.CollectionHasAsScala

class TransactionSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val doc1 = Document("355", "ref1", None)
  private val doc2 = Document("355", "ref2", None)
  private val documents = withPreviousDocuments(doc1, doc2)

  val data = aDeclaration(
    withTotalNumberOfItems(Some("123"), Some("1.23"), Some("GBP")),
    withTotalPackageQuantity("12"),
    withNatureOfTransaction("2"),
    documents
  )

  val dataWithNoExchangeRate = aDeclaration(
    withTotalNumberOfItems(Some("123"), None, Some("GBP")),
    withTotalPackageQuantity("12"),
    withNatureOfTransaction("2"),
    withPreviousDocuments()
  )

  val section = instanceOf[transaction_section]

  "Transaction section" should {

    val view = section(data)(messages)

    "have total amount invoiced with change button" in {
      val row = view.getElementsByClass("item-amount-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.itemAmount"))
      row must haveSummaryValue("GBP 123")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.itemAmount.change")
      row must haveSummaryActionWithPlaceholder(InvoiceAndExchangeRateController.displayPage)
    }

    "have exchange rate with change button" in {
      val row = view.getElementsByClass("exchange-rate-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.exchangeRate"))
      row must haveSummaryValue("1.23")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.exchangeRate.change")
      row must haveSummaryActionWithPlaceholder(InvoiceAndExchangeRateController.displayPage)
    }

    "have total package with change button" in {
      val row = view.getElementsByClass("total-no-of-packages-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.totalNoOfPackages"))
      row must haveSummaryValue("12")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.totalNoOfPackages.change")
      row must haveSummaryActionWithPlaceholder(TotalPackageQuantityController.displayPage)
    }

    "have nature of transaction with change button" in {
      val row = view.getElementsByClass("nature-of-transaction-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.natureOfTransaction"))
      row must haveSummaryValue(messages("declaration.summary.transaction.natureOfTransaction.2"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.natureOfTransaction.change")
      row must haveSummaryActionWithPlaceholder(NatureOfTransactionController.displayPage)
    }

    "have related documents section" which {

      val summaryList = view.getElementsByClass("previous-documents-summary").first
      val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")

      "has all rows present" in {
        summaryListRows.size mustBe 5
      }

      "has heading present" in {
        val heading = summaryListRows.first.getElementsByClass("previous-documents-heading")
        heading must haveSummaryKey(messages("declaration.summary.transaction.previousDocuments"))
      }

      "answers and actions present" in {

        val doc1Type = summaryListRows.get(1).getElementsByClass("previous-documents-type")
        doc1Type must haveSummaryKey(messages("declaration.summary.transaction.previousDocuments.type"))
        doc1Type must haveSummaryValue(doc2.documentType)
        doc1Type must haveSummaryActionsTexts(
          "site.change",
          "declaration.summary.previous-documents.change",
          doc1.documentType,
          doc1.documentReference
        )
        doc1Type must haveSummaryActionWithPlaceholder(PreviousDocumentsController.displayPage)

      }
    }

    "not display exchange rate when question not asked" in {
      val view = section(aDeclarationAfter(data, withoutTotalNumberOfItems()))(messages)
      view.getElementsByClass("exchange-rate-row") mustBe empty
    }

    "not display total package when question not asked" in {
      val view = section(aDeclarationAfter(data, withoutTotalPackageQuantity))(messages)
      view.getElementsByClass("total-no-of-packages-row") mustBe empty
    }

    "not display nature of transaction when question not asked" in {
      val view = section(aDeclarationAfter(data, withoutNatureOfTransaction))(messages)
      view.getElementsByClass("nature-of-transaction-row") mustBe empty
    }

    "not display related documents section when question not asked" in {
      val view = section(aDeclarationAfter(data, withoutPreviousDocuments()))(messages)
      view.getElementsByClass("previous-documents-row") mustBe empty
    }

    "not display exchange rate row when no exchange rate given" in {
      val view = section(aDeclarationAfter(dataWithNoExchangeRate))(messages)
      view.getElementsByClass("exchange-rate-row") must be(empty)
    }

    "display 'Less than £100,000' when Yes answered on exchange rate choice page" in {
      val view = section(aDeclaration(withTotalNumberOfItems()))(messages)
      view.getElementsByClass("item-amount-row") must haveSummaryValue(messages("declaration.totalAmountInvoiced.value.lessThan100000"))
    }

    "have link to exchange rate choice page when Yes answered on exchange rate choice page" in {
      val view = section(aDeclaration(withTotalNumberOfItems()))(messages)
      view.getElementsByClass("item-amount-row") must haveSummaryActionWithPlaceholder(InvoiceAndExchangeRateChoiceController.displayPage)
    }

    "have the correct order of rows when all displayed" in {
      val keysOnPage = view.getElementsByClass("govuk-summary-list__key").eachText().asScala.toList
      val orderOfKeys = List(
        messages("declaration.summary.transaction.itemAmount"),
        messages("declaration.summary.transaction.exchangeRate"),
        messages("declaration.summary.transaction.totalNoOfPackages"),
        messages("declaration.summary.transaction.natureOfTransaction"),
        messages("declaration.summary.transaction.previousDocuments")
      )
      keysOnPage must be(orderOfKeys)
    }
  }
}
