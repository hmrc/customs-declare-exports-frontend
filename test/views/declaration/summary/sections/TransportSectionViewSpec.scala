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

package views.declaration.summary.sections

import base.{Injector, MockTransportCodeService}
import controllers.declaration.routes
import forms.declaration.InlandOrBorder.Border
import forms.declaration._
import models.declaration.Container
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.transport_section

class TransportSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val data = aDeclaration(
    withDepartureTransport(ModeOfTransportCode.Maritime, "10", "identifier"),
    withBorderTransport("11", "borderId"),
    withContainerData(Seq.empty: _*),
    withTransportPayment(Some(TransportPayment("A"))),
    withWarehouseIdentification(Some(WarehouseIdentification(Some("12345")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("23456")))),
    withInlandOrBorder(Some(Border)),
    withInlandModeOfTransportCode(ModeOfTransportCode.Maritime)
  )

  val transportSection = instanceOf[transport_section]

  "Transport section" should {
    val view = transportSection(data)(messages)

    "display a 'border transport' row" in {
      val row = view.getElementsByClass("border-transport-row")
      row must haveSummaryKey(messages("declaration.summary.transport.departure.transportCode.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.transportCode.1"))
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.departure.transportCode.header.change")
      row must haveSummaryActionWithPlaceholder(routes.TransportLeavingTheBorderController.displayPage)
    }

    "not display a 'border transport' row if question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutBorderModeOfTransportCode))(messages)
      view.getElementsByClass("border-transport-row") mustBe empty
    }

    "display a 'warehouse id' row" in {
      val row = view.getElementsByClass("warehouse-id-row")
      row must haveSummaryKey(messages("declaration.summary.transport.warehouse.id"))
      row must haveSummaryValue("12345")
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.warehouse.id.change")
      row must haveSummaryActionWithPlaceholder(routes.WarehouseIdentificationController.displayPage)
    }

    "display a 'warehouse' label when user said 'no'" in {
      val view = transportSection(aDeclarationAfter(data, withWarehouseIdentification(Some(WarehouseIdentification(None)))))(messages)
      val row = view.getElementsByClass("warehouse-id-row")
      row must haveSummaryKey(messages("declaration.summary.transport.warehouse.no.label"))
      row must haveSummaryValue(messages("site.no"))
    }

    "not display a 'warehouse id' row when question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutWarehouseIdentification()))(messages)
      view.getElementsByClass("warehouse-id-row") mustBe empty
    }

    "display a 'supervising office' row" in {
      val row = view.getElementsByClass("supervising-office-row")
      row must haveSummaryKey(messages("declaration.summary.transport.supervisingOffice"))
      row must haveSummaryValue("23456")
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.supervisingOffice.change")
      row must haveSummaryActionWithPlaceholder(routes.SupervisingCustomsOfficeController.displayPage)
    }

    "not display a 'supervising office' row when question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutSupervisingCustomsOffice()))(messages)
      view.getElementsByClass("supervising-office-row") mustBe empty
    }

    "display a 'inland or border' row" in {
      val row = view.getElementsByClass("inland-or-border-row")
      row must haveSummaryKey(messages("declaration.summary.transport.inlandOrBorder"))
      row must haveSummaryValue(messages("declaration.summary.transport.inlandOrBorder.Border"))
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.inlandOrBorder.change")
      row must haveSummaryActionWithPlaceholder(routes.InlandOrBorderController.displayPage)
    }

    "not display a 'inland or border' row when question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutInlandOrBorder))(messages)
      view.getElementsByClass("inland-or-border-row") mustBe empty
    }

    "display a 'mode of transport' row" in {
      val row = view.getElementsByClass("mode-of-transport-row")
      row must haveSummaryKey(messages("declaration.summary.transport.inlandModeOfTransport"))
      row must haveSummaryValue(messages("declaration.summary.transport.inlandModeOfTransport.Maritime"))
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.inlandModeOfTransport.change")
      row must haveSummaryActionWithPlaceholder(routes.InlandTransportDetailsController.displayPage)
    }

    "not display a 'mode of transport' row when question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutInlandModeOfTransportCode()))(messages)
      view.getElementsByClass("mode-of-transport-row") mustBe empty
    }

    "display a 'transport reference' row" in {
      val row = view.getElementsByClass("transport-reference-row")
      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(s"${messages("declaration.summary.transport.departure.meansOfTransport.10")} identifier")
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.departure.meansOfTransport.header.change")
      row must haveSummaryActionWithPlaceholder(routes.DepartureTransportController.displayPage)
    }

    "not display a 'transport reference' row if question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutMeansOfTransportOnDepartureType))(messages)
      view.getElementsByClass("transport-reference-row") mustBe empty
    }

    "display a 'transport reference' row if question skipped" in {
      val transport = data.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = Some(""))
      val view = transportSection(data.copy(transport = transport))(messages)

      val row = view.getElementsByClass("transport-reference-row")
      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue("")
      row must haveSummaryActionWithPlaceholder(routes.DepartureTransportController.displayPage)
    }

    "display a 'transport reference' row if option none selected" in {
      val transport = data.transport.copy(
        meansOfTransportOnDepartureType = Some(MockTransportCodeService.transportCodeService.NotApplicable.value),
        meansOfTransportOnDepartureIDNumber = Some("")
      )
      val view = transportSection(data.copy(transport = transport))(messages)

      val row = view.getElementsByClass("transport-reference-row")
      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.meansOfTransport.option_none"))
      row must haveSummaryActionWithPlaceholder(routes.DepartureTransportController.displayPage)
    }

    "display an 'active transport type' row" in {
      val row = view.getElementsByClass("active-transport-type-row")
      row must haveSummaryKey(messages("declaration.summary.transport.border.meansOfTransport.header"))
      row must haveSummaryValue(s"${messages("declaration.summary.transport.border.meansOfTransport.11")} borderId")
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.border.meansOfTransport.header.change")
      row must haveSummaryActionWithPlaceholder(routes.BorderTransportController.displayPage)
    }

    "not display a 'active transport type' row if question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutBorderTransport))(messages)
      view.getElementsByClass("active-transport-type-row") mustBe empty
    }

    "display a 'active transport country' row" when {
      List(Some("South Africa"), None).foreach { transportCountry =>
        s"transport.transportCrossingTheBorderNationality is $transportCountry" in {
          val view = transportSection(aDeclarationAfter(data, withTransportCountry(transportCountry)))(messages)

          val row = view.getElementsByClass("active-transport-country-row")
          row must haveSummaryKey(messages("declaration.summary.transport.registrationCountry"))

          val expectedValue = transportCountry.fold(messages("declaration.summary.unknown"))(identity)
          row must haveSummaryValue(expectedValue)

          row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.registrationCountry.change")
          row must haveSummaryActionWithPlaceholder(routes.TransportCountryController.displayPage)
        }
      }
    }

    "not display an 'active transport country' row if question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutTransportCountry))(messages)
      view.getElementsByClass("active-transport-country-row") mustBe empty
    }

    "display an 'express consignment' row" in {
      val row = view.getElementsByClass("expressConsignment-row")
      row must haveSummaryKey(messages("declaration.summary.transport.expressConsignment"))
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.expressConsignment.change")
      row must haveSummaryActionWithPlaceholder(routes.ExpressConsignmentController.displayPage)
    }

    "not display an 'express consignment' row if question not answered" in {
      val declaration = data.copy(transport = data.transport.copy(expressConsignment = None))
      val view = transportSection(aDeclarationAfter(declaration))(messages)
      view.getElementsByClass("expressConsignment-row") mustBe empty
    }

    "display a 'transport payment' row" in {
      val row = view.getElementsByClass("transport-payment-row")
      row must haveSummaryKey(messages("declaration.summary.transport.payment"))
      row must haveSummaryValue(messages("declaration.summary.transport.payment.A"))
      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.payment.change")
      row must haveSummaryActionWithPlaceholder(routes.TransportPaymentController.displayPage)
    }

    "not display a 'transport payment' row if question not answered" in {
      val view = transportSection(aDeclarationAfter(data, withoutTransportPayment))(messages)
      view.getElementsByClass("transport-payment-row") mustBe empty
    }

    "display an empty 'Containers' section" when {
      "no containers have been entered" in {
        val row = view.getElementsByClass("containers-heading")
        row must haveSummaryKey(messages("declaration.summary.containers"))
        row must haveSummaryValue(messages("site.no"))
        row must haveSummaryActionsTexts("site.change", "declaration.summary.container.change")
        row must haveSummaryActionWithPlaceholder(routes.TransportContainerController.displayContainerSummary)
      }
    }

    "display a 'Containers' section" when {
      "one or more containers have been entered" in {
        val id1 = "container1"
        val id2 = "container2"

        val container1 = Container(1, id1, List(Seal(1, "seal1"), Seal(2, "seal2")))
        val container2 = Container(2, id2, List.empty)
        val declaration = aDeclarationAfter(data, withContainerData(container1, container2))

        val view = transportSection(declaration)(messages)

        val summaryList = view.getElementsByClass("containers-summary").get(0)
        val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
        summaryListRows.size mustBe 5

        val heading = summaryListRows.get(0).getElementsByClass("containers-heading")
        heading must haveSummaryKey(messages("declaration.summary.container"))
        heading must haveSummaryValue("")

        val container1Id = summaryListRows.get(1).getElementsByClass("container-1")
        container1Id must haveSummaryKey(messages("declaration.summary.container.id"))
        container1Id must haveSummaryValue(id1)
        container1Id must haveSummaryActionsTexts("site.change", "declaration.summary.container.change")
        container1Id must haveSummaryActionWithPlaceholder(routes.TransportContainerController.displayContainerSummary)

        val container1Seals = summaryListRows.get(2).getElementsByClass("container-seals-1")
        container1Seals must haveSummaryKey(messages("declaration.summary.container.securitySeals"))
        container1Seals must haveSummaryValue("seal1, seal2")
        container1Seals.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0

        val container2Id = summaryListRows.get(3).getElementsByClass("container-2")
        container2Id must haveSummaryKey(messages("declaration.summary.container.id"))
        container2Id must haveSummaryValue(id2)
        container2Id must haveSummaryActionsTexts("site.change", "declaration.summary.container.change")
        container2Id must haveSummaryActionWithPlaceholder(routes.TransportContainerController.displayContainerSummary)

        val container2Seals = summaryListRows.get(4).getElementsByClass("container-seals-2")
        container2Seals must haveSummaryKey(messages("declaration.summary.container.securitySeals"))
        container2Seals must haveSummaryValue(messages("declaration.summary.container.securitySeals.none"))
        container2Seals.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0
      }
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = transportSection(data, false)(messages)
        view.getElementsByClass("govuk-summary-list__actions") mustBe empty
      }
    }
  }
}
