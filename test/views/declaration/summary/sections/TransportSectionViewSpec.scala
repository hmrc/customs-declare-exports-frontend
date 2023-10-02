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

  val section = instanceOf[transport_section]

  "Transport section" should {

    val view = section(data)(messages)

    "display border transport with change button" in {
      val row = view.getElementsByClass("border-transport-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.transportCode.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.transportCode.1"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.departure.transportCode.header.change")

      row must haveSummaryActionWithPlaceholder(routes.TransportLeavingTheBorderController.displayPage)
    }

    "display transport reference with change button" in {
      val row = view.getElementsByClass("transport-reference-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(s"${messages("declaration.summary.transport.departure.meansOfTransport.10")} identifier")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.departure.meansOfTransport.header.change")

      row must haveSummaryActionWithPlaceholder(routes.DepartureTransportController.displayPage)
    }

    "display transport reference if question skipped" in {
      val view = section(
        data.copy(transport = data.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = Some("")))
      )(messages)
      val row = view.getElementsByClass("transport-reference-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue("")
      row must haveSummaryActionWithPlaceholder(routes.DepartureTransportController.displayPage)
    }

    "display transport reference if option none selected" in {
      val view = section(
        data.copy(transport =
          data.transport.copy(
            meansOfTransportOnDepartureType = Some(MockTransportCodeService.transportCodeService.NotApplicable.value),
            meansOfTransportOnDepartureIDNumber = Some("")
          )
        )
      )(messages)
      val row = view.getElementsByClass("transport-reference-row")

      row must haveSummaryKey(messages("declaration.summary.transport.departure.meansOfTransport.header"))
      row must haveSummaryValue(messages("declaration.summary.transport.departure.meansOfTransport.option_none"))
      row must haveSummaryActionWithPlaceholder(routes.DepartureTransportController.displayPage)
    }

    "display active transport type with change button" in {
      val row = view.getElementsByClass("active-transport-type-row")

      row must haveSummaryKey(messages("declaration.summary.transport.border.meansOfTransport.header"))
      row must haveSummaryValue(s"${messages("declaration.summary.transport.border.meansOfTransport.11")} borderId")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.border.meansOfTransport.header.change")

      row must haveSummaryActionWithPlaceholder(routes.BorderTransportController.displayPage)
    }

    "display active transport country with change button" when {
      List(Some("South Africa"), None).foreach { transportCountry =>
        s"transport.transportCrossingTheBorderNationality is $transportCountry" in {
          val view = section(aDeclarationAfter(data, withTransportCountry(transportCountry)))(messages)
          val row = view.getElementsByClass("active-transport-country-row")

          row must haveSummaryKey(messages("declaration.summary.transport.registrationCountry"))

          val expectedValue = transportCountry.fold(messages("declaration.summary.unknown"))(identity)
          row must haveSummaryValue(expectedValue)

          row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.registrationCountry.change")

          row must haveSummaryActionWithPlaceholder(routes.TransportCountryController.displayPage)
        }
      }
    }

    "display express consignment with change button" in {
      val row = view.getElementsByClass("expressConsignment-row")

      row must haveSummaryKey(messages("declaration.summary.transport.expressConsignment"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.expressConsignment.change")

      row must haveSummaryActionWithPlaceholder(routes.ExpressConsignmentController.displayPage)
    }

    "display transport payment with change button" in {
      val row = view.getElementsByClass("transport-payment-row")

      row must haveSummaryKey(messages("declaration.summary.transport.payment"))
      row must haveSummaryValue(messages("declaration.summary.transport.payment.A"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.payment.change")

      row must haveSummaryActionWithPlaceholder(routes.TransportPaymentController.displayPage)
    }

    "display information about containers when user said 'No' with change button" in {
      val row = view.getElementsByClass("containers-row")

      row must haveSummaryKey(messages("declaration.summary.transport.containers"))
      row must haveSummaryValue(messages("site.no"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.containers.change")

      row must haveSummaryActionWithPlaceholder(routes.TransportContainerController.displayContainerSummary)
    }

    "not display border transport if question not answered" in {
      val view = section(aDeclarationAfter(data, withoutBorderModeOfTransportCode))(messages)
      view.getElementsByClass("border-transport-row") mustBe empty
    }

    "not display transport reference if question not answered" in {
      val view = section(aDeclarationAfter(data, withoutMeansOfTransportOnDepartureType))(messages)
      view.getElementsByClass("transport-reference-row") mustBe empty
    }

    "not display active transport type if question not answered" in {
      val view = section(aDeclarationAfter(data, withoutBorderTransport))(messages)
      view.getElementsByClass("active-transport-type-row") mustBe empty
    }

    "not display active transport country if question not answered" in {
      view.getElementsByClass("active-transport-country-row") mustBe empty
    }

    "not display transport payment if question not answered" in {
      val view = section(aDeclarationAfter(data, withoutTransportPayment))(messages)
      view.getElementsByClass("transport-payment-row") mustBe empty
    }

    "skip containers part if empty" in {
      val view = section(aDeclaration(withoutContainerData()))(messages)
      view.getElementsByClass("containers-row") mustBe empty
    }

    "display containers section (but not yes/no answer) if containers are not empty" in {
      val view = section(aDeclaration(withContainerData(Container(1, "123", Seq.empty))))(messages)
      view.getElementById("container-1").text() mustNot be(empty)
    }

    "display warehouse id with change button" in {
      val row = view.getElementsByClass("warehouse-id-row")
      row must haveSummaryKey(messages("declaration.summary.transport.warehouse.id"))
      row must haveSummaryValue("12345")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.warehouse.id.change")

      row must haveSummaryActionWithPlaceholder(routes.WarehouseIdentificationController.displayPage)
    }

    "display supervising office with change button" in {
      val row = view.getElementsByClass("supervising-office-row")
      row must haveSummaryKey(messages("declaration.summary.transport.supervisingOffice"))
      row must haveSummaryValue("23456")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.supervisingOffice.change")

      row must haveSummaryActionWithPlaceholder(routes.SupervisingCustomsOfficeController.displayPage)
    }

    "display inland or border with change button" in {
      val row = view.getElementsByClass("inland-or-border-row")
      row must haveSummaryKey(messages("declaration.summary.transport.inlandOrBorder"))
      row must haveSummaryValue(messages("declaration.summary.transport.inlandOrBorder.Border"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.inlandOrBorder.change")

      row must haveSummaryActionWithPlaceholder(routes.InlandOrBorderController.displayPage)
    }

    "display mode of transport with change button" in {
      val row = view.getElementsByClass("mode-of-transport-row")
      row must haveSummaryKey(messages("declaration.summary.transport.inlandModeOfTransport"))
      row must haveSummaryValue(messages("declaration.summary.transport.inlandModeOfTransport.Maritime"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.inlandModeOfTransport.change")

      row must haveSummaryActionWithPlaceholder(routes.InlandTransportDetailsController.displayPage)
    }

    "display warehouse label when user said 'no'" in {
      val row = section(aDeclarationAfter(data, withWarehouseIdentification(Some(WarehouseIdentification(None)))))(messages)
        .getElementsByClass("warehouse-id-row")

      row must haveSummaryKey(messages("declaration.summary.transport.warehouse.no.label"))
      row must haveSummaryValue(messages("site.no"))
    }

    "not display warehouse id when question not answered" in {
      val view = section(aDeclarationAfter(data, withoutWarehouseIdentification()))(messages)
      view.getElementsByClass("warehouse-id-row") mustBe empty
    }

    "not display supervising office when question not answered" in {
      val view = section(aDeclarationAfter(data, withoutSupervisingCustomsOffice()))(messages)
      view.getElementsByClass("supervising-office-row") mustBe empty
    }

    "not display inland or border when question not answered" in {
      val view = section(aDeclarationAfter(data, withoutInlandOrBorder))(messages)
      view.getElementsByClass("inland-or-border-row") mustBe empty
    }

    "not display mode of transport when question not answered" in {
      val view = section(aDeclarationAfter(data, withoutInlandModeOfTransportCode()))(messages)
      view.getElementsByClass("mode-of-transport-row") mustBe empty
    }
  }
}
