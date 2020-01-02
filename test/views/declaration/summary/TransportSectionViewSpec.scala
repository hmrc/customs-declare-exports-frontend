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
import models.DeclarationType._
import models.declaration.Container
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.transport_section

class TransportSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val data = aDeclaration(
    withDepartureTransport("1", "10", "identifier"),
    withBorderTransport(Some("United Kingdom"), "11", "borderId"),
    withContainerData(Container("123", Seq.empty)),
    withTransportPayment(Some(TransportPayment(Some("A"))))
  )

  "Transport section" should {

    onEveryDeclarationJourney { request =>
      "skip containers part if empty" in {

        val view = transport_section(aDeclaration(withoutContainerData()))(messages, request)

        view.getElementById("container") mustBe null
      }

      "display containers section if containers are not empty" in {

        val view = transport_section(aDeclaration(withContainerData(Container("123", Seq.empty))))(messages, request)

        view.getElementById("container").text() mustNot be(empty)
      }

    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "display transport code with change button" in {
        val view = transport_section(data)(messages, request)

        view.getElementById("border-transport-label").text() mustBe messages("declaration.summary.transport.departure.transportCode.header")
        view.getElementById("border-transport").text() mustBe messages("declaration.summary.transport.departure.transportCode.1")

        val List(change, accessibleChange) = view.getElementById("border-transport-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.departure.transportCode.header.change")

        view.getElementById("border-transport-change") must haveHref(controllers.declaration.routes.TransportLeavingTheBorderController.displayPage())
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      val view = transport_section(data)(messages, request)

      "display transport reference with change button" in {

        view.getElementById("transport-reference-label").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.header")
        view.getElementById("transport-reference-0").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.10")
        view.getElementById("transport-reference-1").text() mustBe "identifier"

        val List(change, accessibleChange) = view.getElementById("transport-reference-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.departure.meansOfTransport.header.change")

        view.getElementById("transport-reference-change") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage())
      }

      "display active transport type with change button" in {

        view.getElementById("active-transport-type-label").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.header")
        view.getElementById("active-transport-type-0").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.11")
        view.getElementById("active-transport-type-1").text() mustBe "borderId"

        val List(change, accessibleChange) = view.getElementById("active-transport-type-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.border.meansOfTransport.header.change")

        view.getElementById("active-transport-type-change") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage())
      }

      "display active transport nationality with change button" in {

        view.getElementById("active-transport-nationality-label").text() mustBe messages("declaration.summary.transport.activeTransportNationality")
        view.getElementById("active-transport-nationality").text() mustBe "United Kingdom"

        val List(change, accessibleChange) = view.getElementById("active-transport-nationality-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.activeTransportNationality.change")

        view.getElementById("active-transport-nationality-change") must haveHref(
          controllers.declaration.routes.BorderTransportController.displayPage()
        )
      }

      "display transport payment with change button" in {

        view.getElementById("transport-payment-label").text() mustBe messages("declaration.summary.transport.payment")
        view.getElementById("transport-payment").text() mustBe messages("declaration.summary.transport.payment.A")

        val List(change, accessibleChange) = view.getElementById("transport-payment-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.payment.change")

        view.getElementById("transport-payment-change") must haveHref(controllers.declaration.routes.TransportPaymentController.displayPage())
      }

      "display information about containers with change button" in {

        view.getElementById("containers-label").text() mustBe messages("declaration.summary.transport.containers")
        view.getElementById("containers").text() mustBe "site.yes"

        val List(change, accessibleChange) = view.getElementById("containers-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.containers.change")

        view.getElementById("containers-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      val view = transport_section(data)(messages, request)

      "not display transport reference" in {

        view.getElementById("transport-reference-label") mustBe null
        view.getElementById("transport-reference-0") mustBe null
        view.getElementById("transport-reference-1") mustBe null
        view.getElementById("transport-reference-change") mustBe null
      }

      "not display active transport type" in {

        view.getElementById("active-transport-type-label") mustBe null
        view.getElementById("active-transport-type-0") mustBe null
        view.getElementById("active-transport-type-1") mustBe null
        view.getElementById("active-transport-type-change") mustBe null
      }

      "not display active transport nationality" in {

        view.getElementById("active-transport-nationality-label") mustBe null
        view.getElementById("active-transport-nationality") mustBe null
        view.getElementById("active-transport-nationality-change") mustBe null
      }

      "display information about containers with change button" in {

        view.getElementById("containers-label").text() mustBe messages("declaration.summary.transport.containers")
        view.getElementById("containers").text() mustBe "site.yes"

        val List(change, accessibleChange) = view.getElementById("containers-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.containers.change")

        view.getElementById("containers-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      val view = transport_section(data)(messages, request)

      "not display transport code" in {

        view.getElementById("border-transport-label") mustBe null
        view.getElementById("border-transport") mustBe null
        view.getElementById("border-transport-change") mustBe null
      }

      "display transport payment with change button" in {

        view.getElementById("transport-payment-label").text() mustBe messages("declaration.summary.transport.payment")
        view.getElementById("transport-payment").text() mustBe messages("declaration.summary.transport.payment.A")

        val List(change, accessibleChange) = view.getElementById("transport-payment-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.transport.payment.change")

        view.getElementById("transport-payment-change") must haveHref(controllers.declaration.routes.TransportPaymentController.displayPage())
      }

    }

    onClearance { request =>
      val view = transport_section(data)(messages, request)

      "not display transport payment" in {
        view.getElementById("transport-payment-label") mustBe null
        view.getElementById("transport-payment") mustBe null
        view.getElementById("transport-payment-change") mustBe null
      }

    }

  }
}
