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
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.transaction_section

class TransactionSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val data = aDeclaration(
    withTotalNumberOfItems(Some("123"), Some("1.23"), Some("GBP")),
    withTotalPackageQuantity("12"),
    withNatureOfTransaction("2"),
    withPreviousDocuments()
  )

  val section = instanceOf[transaction_section]

  "Transaction section" should {

    val view = section(Mode.Normal, data)(messages)

    "have total amount invoiced with change button" in {

      val row = view.getElementsByClass("item-amount-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.itemAmount"))
      row must haveSummaryValue("GBP 123")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.itemAmount.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have exchange rate with change button" in {

      val row = view.getElementsByClass("exchange-rate-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.exchangeRate"))
      row must haveSummaryValue("1.23")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.exchangeRate.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have exchange rate value displayed as 'No' if no exchange rate present" in {
      val view = section(Mode.Normal, aDeclaration(withTotalNumberOfItems(Some("123"), None, Some("GBP"))))(messages)
      val row = view.getElementsByClass("exchange-rate-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.exchangeRate"))
      row must haveSummaryValue("No")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.exchangeRate.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have total package with change button" in {

      val row = view.getElementsByClass("total-no-of-packages-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.totalNoOfPackages"))
      row must haveSummaryValue("12")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.totalNoOfPackages.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TotalPackageQuantityController.displayPage())
    }

    "have nature of transaction with change button" in {

      val row = view.getElementsByClass("nature-of-transaction-row")

      row must haveSummaryKey(messages("declaration.summary.transaction.natureOfTransaction"))
      row must haveSummaryValue(messages("declaration.summary.transaction.natureOfTransaction.2"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transaction.natureOfTransaction.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.NatureOfTransactionController.displayPage())
    }

    "have related documents section" in {

      view.getElementById("previous-documents").text() mustNot be(empty)
    }

    "not display total amount invoiced when question not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutTotalNumberOfItems()))(messages)

      view.getElementsByClass("item-amount-row") mustBe empty
    }

    "not display exchange rate when question not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutTotalNumberOfItems()))(messages)

      view.getElementsByClass("exchange-rate-row") mustBe empty
    }

    "not display total package when question not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutTotalPackageQuantity))(messages)

      view.getElementsByClass("total-no-of-packages-row") mustBe empty
    }

    "not display nature of transaction when question not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutNatureOfTransaction()))(messages)

      view.getElementsByClass("nature-of-transaction-row") mustBe empty
    }

    "not display related documents section when question not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutPreviousDocuments()))(messages)

      view.getElementsByClass("previous-documents-row") mustBe empty
    }

  }
}
