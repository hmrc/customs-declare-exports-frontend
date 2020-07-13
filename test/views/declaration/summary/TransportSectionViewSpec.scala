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
import forms.declaration.{ModeOfTransportCode, TransportCodes, TransportPayment}
import models.Mode
import models.declaration.Container
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.transport_section

class TransportSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val data = aDeclaration(
    withDepartureTransport(ModeOfTransportCode.Maritime, "10", "identifier"),
    withBorderTransport(Some("United Kingdom"), "11", "borderId"),
    withContainerData(Seq.empty),
    withTransportPayment(Some(TransportPayment(Some("A"))))
  )

  val section = instanceOf[transport_section]

  "Transport section" should {

    "display border transport with change button" in {
      val view = section(Mode.Normal, data)(messages, journeyRequest())
      val row = view.getElementsByClass("border-transport-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.transportCode.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.transportCode.1"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.departure.transportCode.header.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TransportLeavingTheBorderController.displayPage())
    }

    "display transport reference with change button" in {
      val view = section(Mode.Normal, data)(messages, journeyRequest())
      val row = view.getElementsByClass("transport-reference-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(s"${messages("declaration.summary.transport.departure.meansOfTransport.10")} identifier")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.departure.meansOfTransport.header.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.DepartureTransportController.displayPage())
    }

    "display transport reference if question skipped" in {
      val view = section(
        Mode.Normal,
        data.copy(transport = data.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = Some("")))
      )(messages, journeyRequest())
      val row = view.getElementsByClass("transport-reference-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue("")
      row must haveSummaryActionsHref(controllers.declaration.routes.DepartureTransportController.displayPage())
    }

    "display transport reference if option none selected" in {
      val view = section(
        Mode.Normal,
        data.copy(
          transport =
            data.transport.copy(meansOfTransportOnDepartureType = Some(TransportCodes.OptionNone), meansOfTransportOnDepartureIDNumber = Some(""))
        )
      )(messages, journeyRequest())
      val row = view.getElementsByClass("transport-reference-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.meansOfTransport.option_none"))
      row must haveSummaryActionsHref(controllers.declaration.routes.DepartureTransportController.displayPage())
    }

    "display active transport type with change button" in {
      val view = section(Mode.Normal, data)(messages, journeyRequest())
      val row = view.getElementsByClass("active-transport-type-row")

      row must haveSummaryKey(messages("declaration.summary.transport.border.meansOfTransport.header"))
      row must haveSummaryValue(s"${messages("declaration.summary.transport.border.meansOfTransport.11")} borderId")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.border.meansOfTransport.header.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.BorderTransportController.displayPage())
    }

    "display active transport nationality with change button" in {
      val view = section(Mode.Normal, data)(messages, journeyRequest())
      val row = view.getElementsByClass("active-transport-nationality-row")

      row must haveSummaryKey(messages("declaration.summary.transport.activeTransportNationality"))
      row must haveSummaryValue("United Kingdom")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.activeTransportNationality.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.BorderTransportController.displayPage())
    }

    "display transport payment with change button" in {
      val view = section(Mode.Normal, data)(messages, journeyRequest())
      val row = view.getElementsByClass("transport-payment-row")

      row must haveSummaryKey(messages("declaration.summary.transport.payment"))
      row must haveSummaryValue(messages("declaration.summary.transport.payment.A"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.payment.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TransportPaymentController.displayPage())
    }

    "display information about containers when user said 'No' with change button" in {
      val view = section(Mode.Normal, data)(messages, journeyRequest())
      val row = view.getElementsByClass("containers-row")

      row must haveSummaryKey(messages("declaration.summary.transport.containers"))
      row must haveSummaryValue(messages("site.no"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.containers.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
    }

    "not display border transport if question not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutBorderModeOfTransportCode()))(messages, journeyRequest())

      view.getElementsByClass("border-transport-row") mustBe empty
    }

    "not display transport reference if question not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutMeansOfTransportOnDepartureType()))(messages, journeyRequest())

      view.getElementsByClass("transport-reference-row") mustBe empty
    }

    "not display active transport type if question not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutBorderTransport()))(messages, journeyRequest())

      view.getElementsByClass("active-transport-type-row") mustBe empty
    }

    "not display active transport nationality if question not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutBorderTransport()))(messages, journeyRequest())

      view.getElementsByClass("active-transport-nationality-row") mustBe empty
    }

    "not display transport payment if question not answered" in {
      val view = section(Mode.Normal, aDeclarationAfter(data, withoutTransportPayment()))(messages, journeyRequest())

      view.getElementsByClass("transport-payment-row") mustBe empty
    }

    "skip containers part if empty" in {

      val view = section(Mode.Normal, aDeclaration(withoutContainerData()))(messages, journeyRequest())

      view.getElementsByClass("containers-row") mustBe empty
    }

    "display containers section (but not yes/no answer) if containers are not empty" in {

      val view = section(Mode.Normal, aDeclaration(withContainerData(Container("123", Seq.empty))))(messages, journeyRequest())

      view.getElementById("containers-table").text() mustNot be(empty)
    }
  }
}
