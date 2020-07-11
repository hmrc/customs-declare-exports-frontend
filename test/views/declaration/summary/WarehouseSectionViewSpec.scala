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
import forms.declaration.{InlandModeOfTransportCode, ModeOfTransportCode, SupervisingCustomsOffice, WarehouseIdentification}
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.summary.warehouse_section

class WarehouseSectionViewSpec extends UnitViewSpec2 with ExportsTestData with Injector {

  val data = aDeclaration(
    withWarehouseIdentification(Some(WarehouseIdentification(Some("12345")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("23456")))),
    withInlandModeOfTransportCode(Some(InlandModeOfTransportCode(Some(ModeOfTransportCode.Maritime))))
  )

  val mode = Mode.Normal

  val section = instanceOf[warehouse_section]

  "Warehouse section" should {

    val view = section(mode, data)(messages, journeyRequest())

    "display warehouse id with change button" in {
      val row = view.getElementsByClass("warehouse-id-row")
      row must haveSummaryKey(messages("declaration.summary.warehouse.id"))
      row must haveSummaryValue("12345")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.warehouse.id.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.WarehouseIdentificationController.displayPage())
    }

    "display supervising office with change button" in {
      val row = view.getElementsByClass("supervising-office-row")
      row must haveSummaryKey(messages("declaration.summary.warehouse.supervisingOffice"))
      row must haveSummaryValue("23456")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.warehouse.supervisingOffice.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage())
    }

    "display mode of transport with change button" in {
      val row = view.getElementsByClass("mode-of-transport-row")
      row must haveSummaryKey(messages("declaration.summary.warehouse.inlandModeOfTransport"))
      row must haveSummaryValue(messages("declaration.summary.warehouse.inlandModeOfTransport.Maritime"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.warehouse.inlandModeOfTransport.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.InlandTransportDetailsController.displayPage())
    }

    "display warehouse label when user said 'no'" in {

      val row = section(mode, aDeclarationAfter(data, withWarehouseIdentification(Some(WarehouseIdentification(None)))))(messages, journeyRequest())
        .getElementsByClass("warehouse-id-row")

      row must haveSummaryKey(messages("declaration.summary.warehouse.no.label"))
      row must haveSummaryValue(messages("site.no"))
    }

    "not display warehouse id when question not answered" in {

      val view = section(mode, aDeclarationAfter(data, withoutWarehouseIdentification()))(messages, journeyRequest())

      view.getElementsByClass("warehouse-id-row") mustBe empty
    }

    "not display supervising office when question not answered" in {

      val view = section(mode, aDeclarationAfter(data, withoutSupervisingCustomsOffice()))(messages, journeyRequest())

      view.getElementsByClass("supervising-office-row") mustBe empty
    }

    "not display mode of transport when question not answered" in {

      val view = section(mode, aDeclarationAfter(data, withoutInlandModeOfTransportCode()))(messages, journeyRequest())

      view.getElementsByClass("mode-of-transport-row") mustBe empty
    }
  }

}
