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

import forms.declaration.TransportPayment
import models.Mode
import models.declaration.Container
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.transport_section

class TransportSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val data = aDeclaration(
    withDepartureTransport("1", "10", "identifier"),
    withBorderTransport(Some("United Kingdom"), "11", "borderId"),
    withContainerData(Seq.empty),
    withTransportPayment(Some(TransportPayment(Some("A"))))
  )

  "Transport section" should {

    "display border transport with change button" in {
      val view = transport_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("border-transport-label").text() mustBe messages("declaration.summary.transport.departure.transportCode.header")
      view.getElementById("border-transport").text() mustBe messages("declaration.summary.transport.departure.transportCode.1")

      val List(change, accessibleChange) = view.getElementById("border-transport-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transport.departure.transportCode.header.change")

      view.getElementById("border-transport-change") must haveHref(controllers.declaration.routes.TransportLeavingTheBorderController.displayPage())
    }

    "display transport reference with change button" in {
      val view = transport_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("transport-reference-label").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.header")
      view.getElementById("transport-reference-0").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.10")
      view.getElementById("transport-reference-1").text() mustBe "identifier"

      val List(change, accessibleChange) = view.getElementById("transport-reference-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transport.departure.meansOfTransport.header.change")

      view.getElementById("transport-reference-change") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage())
    }

    "display active transport type with change button" in {
      val view = transport_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("active-transport-type-label").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.header")
      view.getElementById("active-transport-type-0").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.11")
      view.getElementById("active-transport-type-1").text() mustBe "borderId"

      val List(change, accessibleChange) = view.getElementById("active-transport-type-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transport.border.meansOfTransport.header.change")

      view.getElementById("active-transport-type-change") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage())
    }

    "display active transport nationality with change button" in {
      val view = transport_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("active-transport-nationality-label").text() mustBe messages("declaration.summary.transport.activeTransportNationality")
      view.getElementById("active-transport-nationality").text() mustBe "United Kingdom"

      val List(change, accessibleChange) = view.getElementById("active-transport-nationality-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transport.activeTransportNationality.change")

      view.getElementById("active-transport-nationality-change") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage())
    }

    "display transport payment with change button" in {
      val view = transport_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("transport-payment-label").text() mustBe messages("declaration.summary.transport.payment")
      view.getElementById("transport-payment").text() mustBe messages("declaration.summary.transport.payment.A")

      val List(change, accessibleChange) = view.getElementById("transport-payment-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transport.payment.change")

      view.getElementById("transport-payment-change") must haveHref(controllers.declaration.routes.TransportPaymentController.displayPage())
    }

    "display information about containers when user said 'No' with change button" in {
      val view = transport_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("containers-label").text() mustBe messages("declaration.summary.transport.containers")
      view.getElementById("containers").text() mustBe "site.no"

      val List(change, accessibleChange) = view.getElementById("containers-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.transport.containers.change")

      view.getElementById("containers-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
    }

    "not display border transport if question not answered" in {
      val view = transport_section(Mode.Normal, aDeclarationAfter(data, withoutBorderModeOfTransportCode()))(messages, journeyRequest())

      view.getElementById("border-transport-label") mustBe null
      view.getElementById("border-transport") mustBe null
      view.getElementById("border-transport-change") mustBe null
    }

    "not display transport reference if question not answered" in {
      val view = transport_section(Mode.Normal, aDeclarationAfter(data, withoutBorderTransport()))(messages, journeyRequest())

      view.getElementById("transport-reference-label") mustBe null
      view.getElementById("transport-reference-0") mustBe null
      view.getElementById("transport-reference-1") mustBe null
      view.getElementById("transport-reference-change") mustBe null
    }

    "not display active transport type if question not answered" in {
      val view = transport_section(Mode.Normal, aDeclarationAfter(data, withoutMeansOfTransportOnDepartureType()))(messages, journeyRequest())

      view.getElementById("active-transport-type-label") mustBe null
      view.getElementById("active-transport-type-0") mustBe null
      view.getElementById("active-transport-type-1") mustBe null
      view.getElementById("active-transport-type-change") mustBe null
    }

    "not display active transport nationality if question not answered" in {
      val view = transport_section(Mode.Normal, aDeclarationAfter(data, withoutBorderTransport()))(messages, journeyRequest())

      view.getElementById("active-transport-nationality-label") mustBe null
      view.getElementById("active-transport-nationality") mustBe null
      view.getElementById("active-transport-nationality-change") mustBe null
    }

    "not display transport payment if question not answered" in {
      val view = transport_section(Mode.Normal, aDeclarationAfter(data, withoutTransportPayment()))(messages, journeyRequest())

      view.getElementById("transport-payment-label") mustBe null
      view.getElementById("transport-payment") mustBe null
      view.getElementById("transport-payment-change") mustBe null
    }

    "skip containers part if empty" in {

      val view = transport_section(Mode.Normal, aDeclaration(withoutContainerData()))(messages, journeyRequest())

      view.getElementById("container") mustBe null
    }

    "display containers section (but not yes/no answer) if containers are not empty" in {

      val view = transport_section(Mode.Normal, aDeclaration(withContainerData(Container("123", Seq.empty))))(messages, journeyRequest())

      view.getElementById("container").text() mustNot be(empty)

      view.getElementById("containers-label") mustBe null
      view.getElementById("containers-change") mustBe null
    }
  }
}
