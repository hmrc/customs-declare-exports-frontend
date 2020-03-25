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
import forms.declaration._
import helpers.views.declaration.CommonMessages
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.commodity_measure_section

class CommodityMeasureSectionViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  val commodityMeasure = CommodityMeasure(Some("12"), Some("666"), Some("555"))
  val item = anItem(withItemId("itemId"), withSequenceId(1), withCommodityMeasure(CommodityMeasure(Some("12"), Some("666"), Some("555"))))

  def createView(journeyRequest: JourneyRequest[_]) = commodity_measure_section(Mode.Normal, item, commodityMeasure)(messages, journeyRequest)

  "CommodityMeasure section" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView(request)

      "have gross weight with change button" in {

        view.getElementById("item-1-grossWeight-label").text() mustBe messages("declaration.summary.items.item.grossWeight")
        view.getElementById("item-1-grossWeight").text() mustBe "666"

        val List(change, accessibleChange) = view.getElementById("item-1-grossWeight-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.grossWeight.change", 1)

        view.getElementById("item-1-grossWeight-change") must haveHref(
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        )
      }

      "have net weight with change button" in {

        view.getElementById("item-1-netWeight-label").text() mustBe messages("declaration.summary.items.item.netWeight")
        view.getElementById("item-1-netWeight").text() mustBe "555"

        val List(change, accessibleChange) = view.getElementById("item-1-netWeight-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.netWeight.change", 1)

        view.getElementById("item-1-netWeight-change") must haveHref(
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        )
      }

    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView(request)

      "have supplementary units with change button" in {

        view.getElementById("item-1-supplementaryUnits-label").text() mustBe messages("declaration.summary.items.item.supplementaryUnits")
        view.getElementById("item-1-supplementaryUnits").text() mustBe "12"

        val List(change, accessibleChange) = view.getElementById("item-1-supplementaryUnits-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.supplementaryUnits.change", 1)

        view.getElementById("item-1-supplementaryUnits-change") must haveHref(
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        )
      }

    }

    onJourney(CLEARANCE) { implicit request =>
      val view = createView(request)

      "not have supplementary units with change button" in {

        view.getElementById("item-1-supplementaryUnits-label") mustBe (null)
        view.getElementById("item-1-supplementaryUnits") mustBe (null)
        view.getElementById("item-1-supplementaryUnits-change") mustBe (null)
      }

    }
  }
}
