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

import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation}
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.onward_supply_relief

class OnwardSupplyReliefViewSpec extends UnitViewSpec with ExportsTestData {

  "Onward supply relief" should {

    "display just onward supply answer with change button" when {

      "answer is No" in {

        val view = onward_supply_relief(Mode.Normal, "itemId", 1, Some(FiscalInformation("No")), None)(messages, journeyRequest())

        view.getElementById("item-1-onwardSupplyRelief-label").text() mustBe messages("declaration.summary.items.item.onwardSupplyRelief")
        view.getElementById("item-1-onwardSupplyRelief").text() mustBe "No"

        val List(change, accessibleChange) = view.getElementById("item-1-onwardSupplyRelief-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.onwardSupplyRelief.change", 1)

        view.getElementById("item-1-onwardSupplyRelief-change") must haveHref(
          controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, "itemId")
        )
      }
    }

    "display onward supply relief field and VAT details with change buttons" when {

      "answer is Yes and user provided Additional VAT Details" in {

        val view =
          onward_supply_relief(
            Mode.Normal,
            "itemId",
            1,
            Some(FiscalInformation("Yes")),
            Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "123456789"))))
          )(messages, journeyRequest())

        view.getElementById("item-1-onwardSupplyRelief-label").text() mustBe messages("declaration.summary.items.item.onwardSupplyRelief")
        view.getElementById("item-1-onwardSupplyRelief").text() mustBe "Yes"

        val List(change1, accessibleChange1) = view.getElementById("item-1-onwardSupplyRelief-change").text().split(" ").toList

        change1 mustBe messages("site.change")
        accessibleChange1 mustBe messages("declaration.summary.items.item.onwardSupplyRelief.change", 1)

        view.getElementById("item-1-onwardSupplyRelief-change") must haveHref(
          controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, "itemId")
        )
        view.getElementById("item-1-VATdetails-label").text() mustBe messages("declaration.summary.items.item.VATdetails")
        view.getElementById("item-1-VATdetails").text() mustBe "GB123456789"

        val List(change2, accessibleChange2) = view.getElementById("item-1-VATdetails-change").text().split(" ").toList

        change2 mustBe messages("site.change")
        accessibleChange2 mustBe messages("declaration.summary.items.item.VATdetails.change", 2)

        view.getElementById("item-1-VATdetails-change") must haveHref(
          controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, "itemId")
        )
      }
    }
  }
}
