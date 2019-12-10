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

  val view = transport_section(data)(messages, journeyRequest())

  "Transport section" should {

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, CLEARANCE) { request =>
      val view = transport_section(data)(messages, request)

      "display transport code with change button" in {

        view.getElementById("border-transport-label").text() mustBe messages("declaration.summary.transport.departure.transportCode.header")
        view.getElementById("border-transport").text() mustBe messages("declaration.summary.transport.departure.transportCode.1")
        view.getElementById("border-transport-change").text() mustBe messages("site.change")
        view.getElementById("border-transport-change") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage())
      }

      "display transport reference with change button" in {

        view.getElementById("transport-reference-label").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.header")
        view.getElementById("transport-reference-0").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.10")
        view.getElementById("transport-reference-1").text() mustBe "identifier"
        view.getElementById("transport-reference-change").text() mustBe messages("site.change")
        view.getElementById("transport-reference-change") must haveHref(controllers.declaration.routes.DepartureTransportController.displayPage())
      }

      "display active transport type with change button" in {

        view.getElementById("active-transport-type-label").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.header")
        view.getElementById("active-transport-type-0").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.11")
        view.getElementById("active-transport-type-1").text() mustBe "borderId"
        view.getElementById("active-transport-type-change").text() mustBe messages("site.change")
        view.getElementById("active-transport-type-change") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage())
      }

      "display active transport nationality with change button" in {

        view.getElementById("active-transport-nationality-label").text() mustBe messages("declaration.summary.transport.activeTransportNationality")
        view.getElementById("active-transport-nationality").text() mustBe "United Kingdom"
        view.getElementById("active-transport-nationality-change").text() mustBe messages("site.change")
        view.getElementById("active-transport-nationality-change") must haveHref(
          controllers.declaration.routes.BorderTransportController.displayPage()
        )
      }

      "display transport payment with change button" in {

        view.getElementById("transport-payment-label").text() mustBe messages("declaration.summary.transport.payment")
        view.getElementById("transport-payment").text() mustBe messages("declaration.summary.transport.payment.A")
        view.getElementById("transport-payment-change").text() mustBe messages("site.change")
        view.getElementById("transport-payment-change") must haveHref(controllers.declaration.routes.TransportPaymentController.displayPage())
      }

      "display information about containers with change button" in {

        view.getElementById("containers-label").text() mustBe messages("declaration.summary.transport.containers")
        view.getElementById("containers").text() mustBe "site.yes"
        view.getElementById("containers-change").text() mustBe messages("site.change")
        view.getElementById("containers-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
      }

      "skip containers part if empty" in {

        val view = transport_section(aDeclaration(withoutContainerData()))(messages, journeyRequest())

        view.getElementById("container") mustBe null
      }

      "display containers section if containers are not empty" in {

        val view = transport_section(aDeclaration(withContainerData(Container("123", Seq.empty))))(messages, journeyRequest())

        view.getElementById("container").text() mustNot be(empty)
      }
    }

    onJourney(SIMPLIFIED) { request =>
      val view = transport_section(data)(messages, request)

      "not display transport code" in {

        view.getElementById("border-transport-label") mustBe null
        view.getElementById("border-transport") mustBe null
        view.getElementById("border-transport-change") mustBe null
      }

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

      "display transport payment with change button" in {

        view.getElementById("transport-payment-label").text() mustBe messages("declaration.summary.transport.payment")
        view.getElementById("transport-payment").text() mustBe messages("declaration.summary.transport.payment.A")
        view.getElementById("transport-payment-change").text() mustBe messages("site.change")
        view.getElementById("transport-payment-change") must haveHref(controllers.declaration.routes.TransportPaymentController.displayPage())
      }

      "display information about containers with change button" in {

        view.getElementById("containers-label").text() mustBe messages("declaration.summary.transport.containers")
        view.getElementById("containers").text() mustBe "site.yes"
        view.getElementById("containers-change").text() mustBe messages("site.change")
        view.getElementById("containers-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
      }

      "skip containers part if empty" in {

        val view = transport_section(aDeclaration(withoutContainerData()))(messages, journeyRequest())

        view.getElementById("container") mustBe null
      }

      "display containers section if containers are not empty" in {

        val view = transport_section(aDeclaration(withContainerData(Container("123", Seq.empty))))(messages, journeyRequest())

        view.getElementById("container").text() mustNot be(empty)
      }
    }
  }
}
