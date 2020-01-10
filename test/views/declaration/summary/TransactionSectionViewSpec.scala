/*
 * Copyright 2020 HM Revenue & Customs
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

package views.declaration.summary

import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.transaction_section

class TransactionSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val data = aDeclaration(withTotalNumberOfItems(Some("123"), Some("1.23"), "12"), withNatureOfTransaction("2"), withPreviousDocuments())

  "Transaction section" should {

    val view = transaction_section(data)(messages, journeyRequest())

    "have total amount invoiced with change button" in {

      view.getElementById("item-amount-label").text() mustBe messages("declaration.summary.transaction.itemAmount")
      view.getElementById("item-amount").text() mustBe "123"

      val List(change, accessibleChange) = view.getElementById("item-amount-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transaction.itemAmount.change")

      view.getElementById("item-amount-change") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have exchange rate with change button" in {

      view.getElementById("exchange-rate-label").text() mustBe messages("declaration.summary.transaction.exchangeRate")
      view.getElementById("exchange-rate").text() mustBe "1.23"

      val List(change, accessibleChange) = view.getElementById("exchange-rate-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transaction.exchangeRate.change")

      view.getElementById("exchange-rate-change") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have total package with change button" in {

      view.getElementById("total-no-of-packages-label").text() mustBe messages("declaration.summary.transaction.totalNoOfPackages")
      view.getElementById("total-no-of-packages").text() mustBe "12"

      val List(change, accessibleChange) = view.getElementById("total-no-of-packages-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transaction.totalNoOfPackages.change")

      view.getElementById("total-no-of-packages-change") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have nature of transaction with change button" in {

      view.getElementById("nature-of-transaction-label").text() mustBe messages("declaration.summary.transaction.natureOfTransaction")
      view.getElementById("nature-of-transaction").text() mustBe messages("declaration.summary.transaction.natureOfTransaction.2")

      val List(change, accessibleChange) = view.getElementById("nature-of-transaction-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transaction.natureOfTransaction.change")

      view.getElementById("nature-of-transaction-change") must haveHref(controllers.declaration.routes.NatureOfTransactionController.displayPage())
    }

    "have related documents section" in {

      view.getElementById("previous-documents").text() mustNot be(empty)
    }

    "not display total amount invoiced when question not asked" in {
      val view = transaction_section(aDeclarationAfter(data, withoutTotalNumberOfItems()))(messages, journeyRequest())

      view.getElementById("item-amount-label") mustBe null
      view.getElementById("item-amount") mustBe null
      view.getElementById("item-amount-change") mustBe null
    }

    "not display exchange rate when question not asked" in {
      val view = transaction_section(aDeclarationAfter(data, withoutTotalNumberOfItems()))(messages, journeyRequest())

      view.getElementById("exchange-rate-label") mustBe null
      view.getElementById("exchange-rate") mustBe null
      view.getElementById("exchange-rate-change") mustBe null
    }

    "not display total package when question not asked" in {
      val view = transaction_section(aDeclarationAfter(data, withoutTotalNumberOfItems()))(messages, journeyRequest())

      view.getElementById("total-no-of-packages-label") mustBe null
      view.getElementById("total-no-of-packages") mustBe null
      view.getElementById("total-no-of-packages-change") mustBe null
    }

    "not display nature of transaction when question not asked" in {
      val view = transaction_section(aDeclarationAfter(data, withoutNatureOfTransaction()))(messages, journeyRequest())

      view.getElementById("nature-of-transaction-label") mustBe null
      view.getElementById("nature-of-transaction") mustBe null
      view.getElementById("nature-of-transaction-change") mustBe null
    }

    "not display related documents section when question not asked" in {
      val view = transaction_section(aDeclarationAfter(data, withoutPreviousDocuments()))(messages, journeyRequest())

      view.getElementById("previous-documents") mustBe null
    }

  }
}
