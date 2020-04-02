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
import forms.declaration.GoodsLocationForm
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.locations_section

class LocationsSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val data = aDeclaration(withGoodsLocation(GoodsLocationForm("GBAUEMAEMAEMA")), withOfficeOfExit("123", Some("12")))

  val section = instanceOf[locations_section]

  "Locations section" must {

    val view = section(Mode.Change, data)(messages, journeyRequest())

    "have a goods location code with change button" in {

      val row = view.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.locations.goodsLocationCode"))
      row must haveSummaryValue("GBAUEMAEMAEMA")

      row must haveSummaryActionsText("site.change declaration.summary.locations.goodsLocationCode.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.LocationController.displayPage(Mode.Change))
    }

    "have office of exit id with change button" in {

      val view = section(Mode.Change, data)(messages, journeyRequest())

      val row = view.getElementsByClass("location-officeOfExit-row")
      row must haveSummaryKey(messages("declaration.summary.locations.officeOfExit"))
      row must haveSummaryValue("123")

      row must haveSummaryActionsText("site.change declaration.summary.locations.officeOfExit.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Change))

    }

    "have express consignment answer with change button" in {
      val view = section(Mode.Change, data)(messages, journeyRequest())

      val row = view.getElementsByClass("location-expressConsignment-row")
      row must haveSummaryKey(messages("declaration.summary.locations.expressConsignment"))
      row must haveSummaryValue("12")

      row must haveSummaryActionsText("site.change declaration.summary.locations.expressConsignment.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Change))

    }

    "not have answers when goods location not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutGoodsLocation()))(messages, journeyRequest())

      view.getElementsByClass("declarationType-row") mustBe empty
    }

    "not have answers when office of exit not asked" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutOfficeOfExit()))(messages, journeyRequest())

      view.getElementsByClass("location-officeOfExit-row") mustBe empty
      view.getElementsByClass("location-expressConsignment-row") mustBe empty
    }

    "have answers when office of exit not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withOptionalOfficeOfExit(None, Some("Yes"))))(messages, journeyRequest())
      val row = view.getElementsByClass("location-officeOfExit-row")

      row must haveSummaryKey(messages("declaration.summary.locations.officeOfExit"))
      row must haveSummaryValue("")

      row must haveSummaryActionsText("site.change declaration.summary.locations.officeOfExit.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
    }

    "not have answers when office of exit not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withOptionalOfficeOfExit(None, Some("No"))))(messages, journeyRequest())

      val row = view.getElementsByClass("location-officeOfExit-row")
      row must haveSummaryKey(messages("declaration.summary.locations.officeOfExit"))
      row must haveSummaryValue("")

      row must haveSummaryActionsText("site.change declaration.summary.locations.officeOfExit.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
    }
  }
}
