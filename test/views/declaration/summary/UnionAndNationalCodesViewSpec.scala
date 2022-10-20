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

package views.declaration.summary

import base.Injector
import forms.declaration.AdditionalInformation
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.union_and_national_codes

class UnionAndNationalCodesViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val section = instanceOf[union_and_national_codes]

  private val data = Seq(AdditionalInformation("12345", "description1"), AdditionalInformation("23456", "description2"))

  "Union and national codes" should {

    "display title only and change link" when {

      "Sequence is empty" in {

        val view = section("itemId", 1, Seq.empty)(messages)
        val row = view.getElementsByClass("additional-information-1-row")

        row must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation"))
        row must haveSummaryValue(messages("site.no"))

        row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalInformation.changeAll")

        row must haveSummaryActionsHref(controllers.declaration.routes.AdditionalInformationRequiredController.displayPage("itemId"))
      }
    }

    "display additional information with change buttons" in {

      val view = section("itemId", 1, data)(messages)
      val table = view.getElementById("additional-information-1-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.additionalInformation")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalInformation.code")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages(
        "declaration.summary.items.item.additionalInformation.information"
      )
      table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("site.change.header")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "12345"
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "description1"
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.AdditionalInformationController.displayPage("itemId"))
      row1ChangeLink must containMessage("site.change")
      row1ChangeLink must containMessage("declaration.summary.items.item.additionalInformation.change", "12345", 1)

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "23456"
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "description2"
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.AdditionalInformationController.displayPage("itemId"))
      row2ChangeLink must containMessage("site.change")
      row2ChangeLink must containMessage("declaration.summary.items.item.additionalInformation.change", "23456", 1)
    }

    "display additional information without change buttons" when {

      "actionsEnabled is false" in {

        val view = section("itemId", 1, data, actionsEnabled = false)(messages)
        val table = view.getElementById("additional-information-1-table")

        table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.additionalInformation")

        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalInformation.code")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages(
          "declaration.summary.items.item.additionalInformation.information"
        )

        val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
        row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "12345"
        row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "description1"
        val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2)
        row1ChangeLink.attr("href") mustBe empty
        row1ChangeLink.text() mustBe empty

        val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
        row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "23456"
        row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "description2"
        val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2)
        row2ChangeLink.attr("href") mustBe empty
        row2ChangeLink.text() mustBe empty
      }

    }
  }
}
