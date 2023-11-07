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

import base.{Injector, MockTransportCodeService}
import controllers.declaration.routes._
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.ModeOfTransportCode.Maritime
import forms.declaration._
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card7ForTransportSpec extends UnitViewSpec with ExportsTestHelper with Injector {

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

  private val card6ForTransport = instanceOf[Card7ForTransport]

  private val borderTransport = "borderTransport"
  private val inlandOrBorder = "inlandOrBorder"
  private val modeOfTransport = "modeOfTransport"
  private val transportReference = "transportReference"
  private val activeTransportType = "activeTransportType"
  private val transportPayment = "transportPayment"
  private val warehouseId = "warehouseId"
  private val supervisingOffice = "supervisingOffice"
  private val expressConsignment = "expressConsignment"

  "Transport section" should {
    val view = card6ForTransport.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.transport")
    }

    "show the border-transport" in {
      val row = view.getElementsByClass(borderTransport)

      val call = Some(TransportLeavingTheBorderController.displayPage)
      checkSummaryRow(
        row,
        "transport.departure.transportCode.header",
        messages("declaration.summary.transport.departure.transportCode.1"),
        call,
        "transport.departure.transportCode.header"
      )
    }

    "not display a border-transport row if question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutBorderModeOfTransportCode))(messages)
      view.getElementsByClass(borderTransport) mustBe empty
    }

    "show the inland-or-border" in {
      val row = view.getElementsByClass(inlandOrBorder)

      val call = Some(InlandOrBorderController.displayPage)
      checkSummaryRow(
        row,
        "transport.inlandOrBorder",
        messages(s"declaration.summary.transport.inlandOrBorder.Border"),
        call,
        "transport.inlandOrBorder"
      )
    }

    "not display a 'inland or border' row when question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutInlandOrBorder))(messages)
      view.getElementsByClass(inlandOrBorder) mustBe empty
    }

    "show the inland-mode-transport" in {
      val row = view.getElementsByClass(modeOfTransport)

      val call = Some(InlandTransportDetailsController.displayPage)
      checkSummaryRow(
        row,
        "transport.inlandModeOfTransport",
        messages(s"declaration.summary.transport.inlandModeOfTransport.$Maritime"),
        call,
        "transport.inlandModeOfTransport"
      )
    }

    "not display a 'mode of transport' row when question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutInlandModeOfTransportCode))(messages)
      view.getElementsByClass(modeOfTransport) mustBe empty
    }

    "show the transport-reference" in {
      val row = view.getElementsByClass(transportReference)

      val call = Some(DepartureTransportController.displayPage)
      checkSummaryRow(
        row,
        "transport.departure.meansOfTransport.header",
        s"${messages("declaration.summary.transport.departure.meansOfTransport.10")} identifier",
        call,
        "transport.departure.meansOfTransport.header"
      )
    }

    "not display a 'transport reference' row if question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutMeansOfTransportOnDepartureType))(messages)
      view.getElementsByClass(transportReference) mustBe empty
    }

    "display a 'transport reference' row if question skipped" in {
      val transport = declaration.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = Some(""))
      val view = card6ForTransport.eval(declaration.copy(transport = transport))(messages)

      val row = view.getElementsByClass(transportReference)
      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue("")
      row must haveSummaryActionWithPlaceholder(DepartureTransportController.displayPage)
    }

    "display a 'transport reference' row if option none selected" in {
      val transport = declaration.transport.copy(
        meansOfTransportOnDepartureType = Some(MockTransportCodeService.transportCodeService.NotApplicable.value),
        meansOfTransportOnDepartureIDNumber = Some("")
      )
      val view = card6ForTransport.eval(declaration.copy(transport = transport))(messages)

      val row = view.getElementsByClass(transportReference)
      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.meansOfTransport.option_none"))
      row must haveSummaryActionWithPlaceholder(DepartureTransportController.displayPage)
    }

    "show the active-transport-type" in {
      val row = view.getElementsByClass(activeTransportType)

      val call = Some(BorderTransportController.displayPage)
      checkSummaryRow(
        row,
        "transport.border.meansOfTransport.header",
        s"${messages("declaration.summary.transport.border.meansOfTransport.11")} borderId",
        call,
        "transport.border.meansOfTransport.header"
      )
    }

    "not display a 'active transport type' row if question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutBorderTransport))(messages)
      view.getElementsByClass(activeTransportType) mustBe empty
    }

    "show the transport-payment" in {
      val row = view.getElementsByClass(transportPayment)

      val call = Some(TransportPaymentController.displayPage)
      checkSummaryRow(row, "transport.payment", messages("declaration.summary.transport.payment.A"), call, "transport.payment")
    }

    "not display a 'transport payment' row if question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutTransportPayment))(messages)
      view.getElementsByClass(transportPayment) mustBe empty
    }

    "show the express-consignment" in {
      val row = view.getElementsByClass(expressConsignment)

      val call = Some(ExpressConsignmentController.displayPage)
      checkSummaryRow(row, "transport.expressConsignment", YesNoAnswers.yes, call, "transport.expressConsignment")
    }

    "not display an 'express consignment' row if question not answered" in {
      val view =
        card6ForTransport.eval(aDeclarationAfter(declaration.copy(transport = declaration.transport.copy(expressConsignment = None))))(messages)
      view.getElementsByClass(expressConsignment) mustBe empty
    }

    "show the warehouse-id" in {
      val row = view.getElementsByClass(warehouseId)

      val call = Some(WarehouseIdentificationController.displayPage)
      checkSummaryRow(row, "transport.warehouse.id", "12345", call, "transport.warehouse.id")
    }

    "display a 'warehouse' label when user said 'no'" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withWarehouseIdentification(Some(WarehouseIdentification(None)))))(messages)
      val row = view.getElementsByClass(warehouseId)
      row must haveSummaryKey(messages("declaration.summary.transport.warehouse.no.label"))
      row must haveSummaryValue(messages("site.no"))
    }

    "not display a 'warehouse id' row when question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutWarehouseIdentification))(messages)
      view.getElementsByClass(warehouseId) mustBe empty
    }

    "show the supervising-office" in {
      val row = view.getElementsByClass(supervisingOffice)
      val call = Some(SupervisingCustomsOfficeController.displayPage)
      checkSummaryRow(row, "transport.supervisingOffice", "23456", call, "transport.supervisingOffice")
    }

    "not display a 'supervising office' row when question not answered" in {
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutSupervisingCustomsOffice))(messages)
      view.getElementsByClass(supervisingOffice) mustBe empty
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card6ForTransport.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }
  }
}
