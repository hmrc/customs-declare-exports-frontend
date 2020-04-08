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

import base.Injector
import forms.declaration.AdditionalInformation
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.union_and_national_codes

class UnionAndNationalCodesViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val section = instanceOf[union_and_national_codes]

  "Union and national codes" should {

    "display title only and change link" when {

      "Sequence is empty" in {

        val view = section(Mode.Normal, "itemId", 1, Seq.empty)(messages, journeyRequest())
        val row = view.getElementsByClass("additional-information-1-row")

        row must haveSummaryKey(messages("declaration.summary.items.item.additionalInformation"))
        row must haveSummaryValue("")

        row must haveSummaryActionsText("site.change declaration.summary.items.item.additionalInformation.change")

        row must haveSummaryActionsHref(controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId"))
      }
    }

    "display additional information with change buttons" in {

      val data = Seq(AdditionalInformation("12345", "description1"), AdditionalInformation("23456", "description2"))
      val view = section(Mode.Normal, "itemId", 1, data)(messages, journeyRequest())
      val table = view.getElementById("additional-information-1-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.additionalInformation")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalInformation.code")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages(
        "declaration.summary.items.item.additionalInformation.information"
      )

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "12345"
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "description1"
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId"))
      row1ChangeLink.text() mustBe "site.change " + messages("declaration.summary.items.item.additionalInformation.change", 1)

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "23456"
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "description2"
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId"))
      row2ChangeLink.text() mustBe "site.change " + messages("declaration.summary.items.item.additionalInformation.change", 1)
    }
  }
}
