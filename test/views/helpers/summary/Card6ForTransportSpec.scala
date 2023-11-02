/*
 * Copyright 2023 HM Revenue & Customs
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

package views.helpers.summary

import base.Injector
import controllers.declaration.routes._
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.ModeOfTransportCode.Maritime
import forms.declaration._
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card6ForTransportSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val declaration = aDeclaration(
    withDepartureTransport(ModeOfTransportCode.Maritime, "10", "identifier"),
    withBorderTransport("11", "borderId"),
    withContainerData(Seq.empty: _*),
    withTransportPayment(Some(TransportPayment("A"))),
    withWarehouseIdentification(Some(WarehouseIdentification(Some("12345")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("23456")))),
    withInlandOrBorder(Some(InlandOrBorder.Border)),
    withInlandModeOfTransportCode(ModeOfTransportCode.Maritime)
  )

  private val card6ForTransport = instanceOf[Card6ForTransport]

  "Transport section" should {
    val view = card6ForTransport.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.transport")
    }

    "show the border-transport" in {
      val row = view.getElementsByClass("borderTransport")

      val call = Some(TransportLeavingTheBorderController.displayPage)
      checkSummaryRow(
        row,
        "transport.departure.transportCode.header",
        messages("declaration.summary.transport.departure.transportCode.1"),
        call,
        "transport.departure.transportCode.header"
      )
    }

    "show the inland-mode-transport" in {
      val row = view.getElementsByClass("modeOfTransport")

      val call = Some(InlandTransportDetailsController.displayPage)
      checkSummaryRow(
        row,
        "transport.inlandModeOfTransport",
        messages(s"declaration.summary.transport.inlandModeOfTransport.$Maritime"),
        call,
        "transport.inlandModeOfTransport"
      )
    }

    "show the express-consignment" in {
      val row = view.getElementsByClass("expressConsignment")

      val call = Some(ExpressConsignmentController.displayPage)
      checkSummaryRow(row, "transport.expressConsignment", YesNoAnswers.yes, call, "transport.expressConsignment")
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card6ForTransport.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }
  }
}
