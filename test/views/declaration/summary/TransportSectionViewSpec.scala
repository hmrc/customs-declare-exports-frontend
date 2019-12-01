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

    "display transport code" in {

      view.getElementById("border-transport-label").text() mustBe messages("declaration.summary.transport.departure.transportCode.header")
      view.getElementById("border-transport").text() mustBe messages("declaration.summary.transport.departure.transportCode.1")
    }

    "display transport reference" in {

      view.getElementById("transport-reference-label").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.header")
      view.getElementById("transport-reference-0").text() mustBe messages("declaration.summary.transport.departure.meansOfTransport.10")
      view.getElementById("transport-reference-1").text() mustBe "identifier"
    }

    "display active transport type" in {

      view.getElementById("active-transport-type-label").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.header")
      view.getElementById("active-transport-type-0").text() mustBe messages("declaration.summary.transport.border.meansOfTransport.11")
      view.getElementById("active-transport-type-1").text() mustBe "borderId"
    }

    "display active transport nationality" in {

      view.getElementById("active-transport-nationality-label").text() mustBe messages("declaration.summary.transport.activeTransportNationality")
      view.getElementById("active-transport-nationality").text() mustBe "United Kingdom"
    }

    "display transport payment" in {

      view.getElementById("transport-payment-label").text() mustBe messages("declaration.summary.transport.payment")
      view.getElementById("transport-payment").text() mustBe "Payment in cash"
    }

    "display information about containers" in {

      view.getElementById("containers-label").text() mustBe messages("declaration.summary.transport.containers")
      view.getElementById("containers").text() mustBe "Yes"
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
