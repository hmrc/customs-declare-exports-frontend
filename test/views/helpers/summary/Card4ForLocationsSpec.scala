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
import controllers.declaration.routes.{LocationOfGoodsController, OfficeOfExitController}
import forms.declaration.LocationOfGoods
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card4ForLocationsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("GBAUEMAEMAEMA")), withOfficeOfExit("123"))

  private val card4ForLocations = instanceOf[Card4ForLocations]

  "Locations section" should {
    val view = card4ForLocations.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.locations")
    }

    "show the goods location code" in {
      val row = view.getElementsByClass("goods-location-code")

      val call = Some(LocationOfGoodsController.displayPage)
      checkSummaryRow(row, "locations.goodsLocationCode", "GBAUEMAEMAEMA", call, "locations.goodsLocationCode")
    }

    "show the 'RRS01' additional information" when {
      "the goods location code ends with 'GVM'" in {
        val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("GBAUABDABDABDGVM")), withOfficeOfExit("123"))
        val view = card4ForLocations.eval(declaration)(messages)
        val row = view.getElementsByClass("rrs01-additional-information")

        val expectedValue = messages("declaration.summary.locations.rrs01AdditionalInformation.text")
        checkSummaryRow(row, "locations.rrs01AdditionalInformation", expectedValue, None, "ign")
      }
    }

    "NOT show the 'RRS01' additional information" when {
      "the goods location code does not end with 'GVM'" in {
        view.getElementsByClass("locations.rrs01AdditionalInformation").size mustBe 0
      }
    }

    "show the office of exit" in {
      val row = view.getElementsByClass("office-of-exit")

      val call = Some(OfficeOfExitController.displayPage)
      checkSummaryRow(row, "locations.officeOfExit", "123", call, "locations.officeOfExit")
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card4ForLocations.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }
  }
}
