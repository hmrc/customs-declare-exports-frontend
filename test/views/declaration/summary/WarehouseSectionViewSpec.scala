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

import forms.declaration.{InlandModeOfTransportCode, ModeOfTransportCodes, SupervisingCustomsOffice, WarehouseIdentification}
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.warehouse_section

class WarehouseSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val data = aDeclaration(
    withWarehouseIdentification(Some(WarehouseIdentification(Some("12345")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("23456")))),
    withInlandModeOfTransportCode(Some(InlandModeOfTransportCode(Some(ModeOfTransportCodes.Maritime))))
  )

  "Warehouse section" should {

    val view = warehouse_section(data)(messages, journeyRequest())

    "display warehouse id with change button" in {

      view.getElementById("warehouse-id-label").text() mustBe messages("declaration.summary.warehouse.id")
      view.getElementById("warehouse-id").text() mustBe "12345"

      val List(change, accessibleChange) = view.getElementById("warehouse-id-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.warehouse.id.change")

      view.getElementById("warehouse-id-change") must haveHref(controllers.declaration.routes.WarehouseIdentificationController.displayPage())
    }

    "display supervising office with change button" in {

      view.getElementById("supervising-office-label").text() mustBe messages("declaration.summary.warehouse.supervisingOffice")
      view.getElementById("supervising-office").text() mustBe "23456"

      val List(change, accessibleChange) = view.getElementById("supervising-office-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.warehouse.supervisingOffice.change")

      view.getElementById("supervising-office-change") must haveHref(controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage())
    }

    "display mode of transport with change button" in {

      view.getElementById("mode-of-transport-label").text() mustBe messages("declaration.summary.warehouse.inlandModeOfTransport")
      view.getElementById("mode-of-transport").text() mustBe messages("declaration.summary.warehouse.inlandModeOfTransport.Maritime")

      val List(change, accessibleChange) = view.getElementById("mode-of-transport-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.warehouse.inlandModeOfTransport.change")

      view.getElementById("mode-of-transport-change") must haveHref(controllers.declaration.routes.InlandTransportDetailsController.displayPage())
    }

    "not display warehouse id when question not answered" in {

      val view = warehouse_section(aDeclarationAfter(data, withoutWarehouseIdentification()))(messages, journeyRequest())

      view.getElementById("warehouse-id-label") mustBe null
      view.getElementById("warehouse-id") mustBe null
    }

    "not display supervising office when question not answered" in {

      val view = warehouse_section(aDeclarationAfter(data, withoutSupervisingCustomsOffice()))(messages, journeyRequest())

      view.getElementById("supervising-office-label") mustBe null
      view.getElementById("supervising-office-id") mustBe null
    }

    "not display mode of transport when question not answered" in {

      val view = warehouse_section(aDeclarationAfter(data, withoutInlandModeOfTransportCode()))(messages, journeyRequest())

      view.getElementById("mode-of-transport-label") mustBe null
      view.getElementById("mode-of-transport-id") mustBe null
    }
  }

}
