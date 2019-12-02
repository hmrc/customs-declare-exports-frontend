/*
 * Copyright 2019 HM Revenue & Customs
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

  val view = transaction_section(data)(messages, journeyRequest())

  "Transaction section" should {

    "have total amount invoiced with change button" in {

      view.getElementById("item-amount-label").text() mustBe messages("declaration.summary.transaction.itemAmount")
      view.getElementById("item-amount").text() mustBe "123"
      view.getElementById("item-amount-change").text() mustBe messages("site.change")
      view.getElementById("item-amount-change") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have exchange rate with change button" in {

      view.getElementById("exchange-rate-label").text() mustBe messages("declaration.summary.transaction.exchangeRate")
      view.getElementById("exchange-rate").text() mustBe "1.23"
      view.getElementById("exchange-rate-change").text() mustBe messages("site.change")
      view.getElementById("exchange-rate-change") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have total package with change button" in {

      view.getElementById("total-no-of-packages-label").text() mustBe messages("declaration.summary.transaction.totalNoOfPackages")
      view.getElementById("total-no-of-packages").text() mustBe "12"
      view.getElementById("total-no-of-packages-change").text() mustBe messages("site.change")
      view.getElementById("total-no-of-packages-change") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage())
    }

    "have nature of transaction with change button" in {

      view.getElementById("nature-of-transaction-label").text() mustBe messages("declaration.summary.transaction.natureOfTransaction")
      view.getElementById("nature-of-transaction").text() mustBe messages("declaration.summary.transaction.natureOfTransaction.2")
      view.getElementById("nature-of-transaction-change").text() mustBe messages("site.change")
      view.getElementById("nature-of-transaction-change") must haveHref(controllers.declaration.routes.NatureOfTransactionController.displayPage())
    }

    "have related documents section" in {

      view.getElementById("previous-documents").text() mustNot be(empty)
    }
  }
}
