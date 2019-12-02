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

import forms.declaration.{InlandModeOfTransportCode, SupervisingCustomsOffice, WarehouseIdentification}
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.warehouse_section

class WarehouseSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val data = aDeclaration(
    withWarehouseIdentification(Some(WarehouseIdentification(Some("12345")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("23456")))),
    withInlandModeOfTransportCode(Some(InlandModeOfTransportCode(Some("1"))))
  )
  val view = warehouse_section(data)(messages, journeyRequest())

  "Warehouse section" should {

    "display warehouse id" in {

      view.getElementById("warehouse-id-label").text() mustBe messages("declaration.summary.warehouse.id")
      view.getElementById("warehouse-id").text() mustBe "12345"
    }

    "display supervising office" in {

      view.getElementById("supervising-office-label").text() mustBe messages("declaration.summary.warehouse.supervisingOffice")
      view.getElementById("supervising-office").text() mustBe "23456"
    }

    "display mode of transport" in {

      view.getElementById("mode-of-transport-label").text() mustBe messages("declaration.summary.warehouse.inlandModeOfTransport")
      view.getElementById("mode-of-transport").text() mustBe messages("declaration.summary.warehouse.inlandModeOfTransport.1")
    }
  }
}
