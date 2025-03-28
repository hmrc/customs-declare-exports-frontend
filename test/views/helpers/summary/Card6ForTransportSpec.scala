/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.section6.routes._
import controllers.summary.routes.SummaryController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section6.InlandOrBorder.Border
import forms.section6.ModeOfTransportCode.Maritime
import forms.section6._
import models.declaration.Container
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec

class Card6ForTransportSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val declaration = aDeclaration(
    withDepartureTransport(Maritime, "10", "identifier"),
    withBorderTransport("11", "borderId"),
    withContainerData(Seq.empty: _*),
    withTransportPayment(Some(TransportPayment("A"))),
    withWarehouseIdentification(Some(WarehouseIdentification(Some("12345")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("23456")))),
    withInlandOrBorder(Some(Border)),
    withInlandModeOfTransportCode(Maritime),
    withTransportCountry(Some("GB"))
  )

  private val card6ForTransport = instanceOf[Card6ForTransport]

  private val borderTransport = "border-transport"
  private val inlandOrBorder = "inland-or-border"
  private val modeOfTransport = "mode-of-transport"
  private val transportReference = "transport-reference"
  private val activeTransportType = "active-transport-type"
  private val transportPayment = "transport-payment"
  private val warehouseId = "warehouse-id"
  private val supervisingOffice = "supervising-office"
  private val activeTransportCountry = "active-transport-country"
  private val expressConsignment = "express-consignment"

  "Transport section" should {
    val view = card6ForTransport.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.section.6")
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
      val view = card6ForTransport.eval(aDeclarationAfter(declaration, withoutTransportLeavingTheBorder))(messages)
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

    "show the transport-reference" when {
      "meansOfTransportOnDepartureIDNumber is not empty" in {

        val view = card6ForTransport.eval(
          declaration.copy(transport =
            declaration.transport.copy(
              borderModeOfTransportCode = Some(TransportLeavingTheBorder(Some(Maritime))),
              meansOfTransportOnDepartureType = Some("10"),
              meansOfTransportOnDepartureIDNumber = Some("identifier")
            )
          )
        )

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
      "meansOfTransportOnDepartureIDNumber is empty" in {

        val view = card6ForTransport.eval(
          declaration.copy(transport =
            declaration.transport.copy(
              borderModeOfTransportCode = Some(TransportLeavingTheBorder(Some(Maritime))),
              meansOfTransportOnDepartureType = Some("10"),
              meansOfTransportOnDepartureIDNumber = None
            )
          )
        )

        val row = view.getElementsByClass(transportReference)

        val call = Some(DepartureTransportController.displayPage)
        checkSummaryRow(
          row,
          "transport.departure.meansOfTransport.header",
          messages("declaration.summary.transport.departure.meansOfTransport.10"),
          call,
          "transport.departure.meansOfTransport.header"
        )
      }
      "meansOfTransportOnDepartureType is empty" in {

        val view = card6ForTransport.eval(
          declaration.copy(transport =
            declaration.transport.copy(
              borderModeOfTransportCode = Some(TransportLeavingTheBorder(Some(Maritime))),
              meansOfTransportOnDepartureType = None,
              meansOfTransportOnDepartureIDNumber = None
            )
          )
        )

        view.getElementsByClass(transportReference) mustBe empty
      }
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
      row must haveSummaryValue("None")
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

    "show the transport-country" in {
      val row = view.getElementsByClass(activeTransportCountry)

      val call = Some(TransportCountryController.displayPage)
      checkSummaryRow(row, "transport.registrationCountry", "United Kingdom, Great Britain, Northern Ireland", call, "transport.registrationCountry")
    }

    "not display an 'transport-country' row if question not answered" in {
      val view =
        card6ForTransport.eval(
          aDeclarationAfter(declaration.copy(transport = declaration.transport.copy(transportCrossingTheBorderNationality = None)))
        )(messages)
      view.getElementsByClass(activeTransportCountry) mustBe empty
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

    "display a 'Containers' section" when {
      "one or more containers have been entered" in {
        val id1 = "container1"
        val id2 = "container2"

        val container1 = Container(1, id1, List(Seal(1, "seal1"), Seal(2, "seal2")))
        val container2 = Container(2, id2, List.empty)

        val view = card6ForTransport.eval(aDeclarationAfter(declaration, withContainerData(container1, container2)))(messages)

        val rows = checkSection(view, "containers", "container", 2, 1, 4)

        val call = Some(ContainerController.displayContainerSummary)

        checkMultiRowSection(rows.get(0), List("container-1"), "container.id", id1, call, "container")
        checkMultiRowSection(rows.get(1), List("container-1-seals"), "container.securitySeals", "seal1, seal2")
        checkMultiRowSection(rows.get(2), List("container-2"), "container.id", id2, call, "container")

        val expectedValue = messages("declaration.summary.container.securitySeals.none")
        checkMultiRowSection(rows.get(3), List("container-2-seals"), "container.securitySeals", expectedValue)
      }
    }

    "display an empty 'Containers' section" when {
      "no containers have been entered" in {
        val row = view.getElementsByClass("containers-heading")
        row must haveSummaryKey(messages("declaration.summary.containers"))
        row must haveSummaryValue(messages("site.none"))
        row must haveSummaryActionWithPlaceholder(ContainerController.displayContainerSummary)

        val expectedText = s"""${messages("site.change")} ${messages("declaration.summary.container.change")}"""
        row.first.getElementsByClass(summaryActionsClassName).text must startWith(expectedText)
      }
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card6ForTransport.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }

    "Card6ForTransport.content" should {
      "return the expected CYA card" in {
        val cardContent = card6ForTransport.content(declaration)
        cardContent.getElementsByClass("transport-card").text mustBe messages("declaration.summary.section.6")
      }
    }

    "Card6ForTransport.backLink" when {
      "go to PreviousDocumentsSummaryController" in {
        card6ForTransport.backLink(journeyRequest()) mustBe ContainerController.displayContainerSummary
      }
    }

    "Card6ForTransport.continueTo" should {
      "go to ItemsSummaryController" in {
        card6ForTransport.continueTo(journeyRequest()) mustBe SummaryController.displayPage
      }
    }
  }
}
