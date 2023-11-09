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

import controllers.declaration.routes._
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.ModeOfTransportCode.Empty
import models.ExportsDeclaration
import models.declaration.{Container, Transport}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import javax.inject.{Inject, Singleton}

@Singleton
class Card7ForTransport @Inject() (govukSummaryList: GovukSummaryList) extends SummaryHelper {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val transport = declaration.transport

    val hasData = transport.expressConsignment.isDefined ||
      inlandModeOfTransportCode(declaration).isDefined ||
      declaration.locations.warehouseIdentification.isDefined ||
      declaration.locations.supervisingCustomsOffice.isDefined ||
      declaration.locations.inlandOrBorder.isDefined ||
      declaration.locations.inlandModeOfTransportCode.isDefined ||
      transport.meansOfTransportOnDepartureType.isDefined ||
      transport.meansOfTransportOnDepartureIDNumber.isDefined ||
      transport.meansOfTransportCrossingTheBorderType.isDefined ||
      transport.meansOfTransportCrossingTheBorderIDNumber.isDefined ||
      transport.transportPayment.isDefined ||
      transport.borderModeOfTransportCode.isDefined ||
      transport.transportCrossingTheBorderNationality.isDefined ||
      transport.containers.isDefined

    if (hasData) displayCard(declaration, actionsEnabled) else HtmlFormat.empty
  }

  private def displayCard(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    govukSummaryList(SummaryList(rows(declaration, actionsEnabled), card("transport")))

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    (List(
      borderTransport(declaration.transport, actionsEnabled),
      warehouseIdentification(declaration, actionsEnabled),
      supervisingCustomsOffice(declaration, actionsEnabled),
      inlandOrBorder(declaration, actionsEnabled),
      inlandModeOfTransport(declaration, actionsEnabled),
      transportReference(declaration.transport, actionsEnabled),
      activeTransportType(declaration.transport, actionsEnabled),
      transportPayment(declaration.transport, actionsEnabled),
      transportCrossingTheBorder(declaration.transport, actionsEnabled),
      expressConsignment(declaration.transport, actionsEnabled)
    ) ++ containers(declaration.transport, actionsEnabled)).flatten

  private def inlandModeOfTransportCode(declaration: ExportsDeclaration): Option[InlandModeOfTransportCode] =
    declaration.locations.inlandModeOfTransportCode.filterNot(_.inlandModeOfTransportCode contains Empty)

  private def borderTransport(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.borderModeOfTransportCode.map { borderModeOfTransportCode =>
      SummaryListRow(
        key("transport.departure.transportCode.header"),
        valueKey(s"declaration.summary.transport.departure.transportCode.${borderModeOfTransportCode.getCodeValue}"),
        classes = "border-transport",
        changeLink(TransportLeavingTheBorderController.displayPage, "transport.departure.transportCode.header", actionsEnabled)
      )
    }

  private def warehouseIdentification(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.locations.warehouseIdentification.map { warehouseIdentification =>
      SummaryListRow(
        key(
          if (warehouseIdentification.identificationNumber.isDefined) "transport.warehouse.id"
          else "transport.warehouse.no.label"
        ),
        value(warehouseIdentification.identificationNumber.getOrElse(messages("site.no"))),
        classes = "warehouse-id",
        changeLink(WarehouseIdentificationController.displayPage, "transport.warehouse.id", actionsEnabled)
      )
    }

  private def supervisingCustomsOffice(declaration: ExportsDeclaration, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    declaration.locations.supervisingCustomsOffice.flatMap(_.supervisingCustomsOffice.map { supervisingCustomsOffice =>
      SummaryListRow(
        key("transport.supervisingOffice"),
        value(supervisingCustomsOffice),
        classes = "supervising-office",
        changeLink(SupervisingCustomsOfficeController.displayPage, "transport.supervisingOffice", actionsEnabled)
      )
    })

  private def inlandOrBorder(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.locations.inlandOrBorder.map { inlandOrBorder =>
      SummaryListRow(
        key("transport.inlandOrBorder"),
        valueKey(s"declaration.summary.transport.inlandOrBorder.${inlandOrBorder.location}"),
        classes = "inland-or-border",
        changeLink(InlandOrBorderController.displayPage, "transport.inlandOrBorder", actionsEnabled)
      )
    }

  private def inlandModeOfTransport(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    inlandModeOfTransportCode(declaration).flatMap {
      _.inlandModeOfTransportCode.map { inlandModeOfTransportCode =>
        SummaryListRow(
          key("transport.inlandModeOfTransport"),
          valueKey(s"declaration.summary.transport.inlandModeOfTransport.$inlandModeOfTransportCode"),
          classes = "mode-of-transport",
          changeLink(InlandTransportDetailsController.displayPage, "transport.inlandModeOfTransport", actionsEnabled)
        )
      }
    }

  private def transportReference(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    if (transport.meansOfTransportOnDepartureType.isDefined || transport.meansOfTransportOnDepartureIDNumber.isDefined) {

      val messagesForDepartureMeansOfTransport: Seq[String] =
        transport.meansOfTransportOnDepartureType.fold[Seq[String]](Seq.empty) { meansType =>
          transport.meansOfTransportOnDepartureIDNumber map { meansId =>
            Seq(messages(s"declaration.summary.transport.departure.meansOfTransport.$meansType"), meansId)
          } getOrElse Seq(messages(s"declaration.summary.transport.departure.meansOfTransport.$meansType"))

        }

      Some(
        SummaryListRow(
          key("transport.departure.meansOfTransport.header"),
          valueHtml(messagesForDepartureMeansOfTransport.mkString("<br>")),
          classes = "transport-reference",
          changeLink(DepartureTransportController.displayPage, "transport.departure.meansOfTransport.header", actionsEnabled)
        )
      )
    } else None

  private def activeTransportType(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    if (transport.meansOfTransportCrossingTheBorderType.isDefined && transport.meansOfTransportCrossingTheBorderIDNumber.isDefined) {

      val messagesForBorderMeansOfTransport = {
        (transport.meansOfTransportCrossingTheBorderType, transport.meansOfTransportCrossingTheBorderIDNumber) match {
          case (Some(meansType), Some(meansId)) => Seq(messages(s"declaration.summary.transport.border.meansOfTransport.$meansType"), meansId)
          case _                                => Seq.empty
        }
      }

      Some(
        SummaryListRow(
          key("transport.border.meansOfTransport.header"),
          valueHtml(messagesForBorderMeansOfTransport.mkString("<br>")),
          classes = "active-transport-type",
          changeLink(BorderTransportController.displayPage, "transport.border.meansOfTransport.header", actionsEnabled)
        )
      )
    } else None

  private def transportPayment(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.transportPayment.map { transportPayment =>
      SummaryListRow(
        key("transport.payment"),
        valueKey(s"declaration.summary.transport.payment.${transportPayment.paymentMethod}"),
        classes = "transport-payment",
        changeLink(TransportPaymentController.displayPage, "transport.payment", actionsEnabled)
      )
    }

  private def transportCrossingTheBorder(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.transportCrossingTheBorderNationality.map { transportCrossingTheBorderNationality =>
      SummaryListRow(
        key("transport.registrationCountry"),
        value(transportCrossingTheBorderNationality.countryName.getOrElse(messages("declaration.summary.unknown"))),
        classes = "active-transport-country",
        changeLink(TransportCountryController.displayPage, "transport.registrationCountry", actionsEnabled)
      )
    }

  private def expressConsignment(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.expressConsignment.map { expressConsignment =>
      SummaryListRow(
        key("transport.expressConsignment"),
        valueKey(if (expressConsignment.isYes) "site.yes" else "site.no"),
        classes = "express-consignment",
        changeLink(ExpressConsignmentController.displayPage, "transport.expressConsignment", actionsEnabled)
      )
    }

  private def containers(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[List[SummaryListRow]] =
    transport.containers flatMap { containers =>
      val rows = containers.flatMap(containerRows(_, actionsEnabled))

      if (rows.isEmpty) {
        val header =
          SummaryListRow(
            key("containers"),
            valueKey("site.none"),
            classes = "containers-heading",
            changeLink(TransportContainerController.displayContainerSummary, "container", actionsEnabled)
          )
        Some(List(header))
      } else
        heading("containers", "container") map { header =>
          List(header) ++ rows
        }
    }

  private def containerRows(container: Container, actionsEnabled: Boolean)(implicit messages: Messages): List[SummaryListRow] = {

    val valueOfSeals =
      if (container.seals.isEmpty) messages("declaration.summary.container.securitySeals.none")
      else container.seals.map(_.id).mkString(", ")

    List(
      SummaryListRow(
        key("container.id"),
        value(container.id),
        classes = s"govuk-summary-list__row--no-border container container-${container.sequenceId}",
        changeLink(TransportContainerController.displayContainerSummary, "container", actionsEnabled)
      ),
      SummaryListRow(key("container.securitySeals"), value(valueOfSeals), classes = s"seal container-seals-${container.sequenceId}")
    )
  }

}
