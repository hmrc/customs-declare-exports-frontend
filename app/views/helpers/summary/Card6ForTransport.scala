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

import config.AppConfig
import controllers.section6.routes._
import controllers.summary.routes.SummaryController
import forms.section6.ModeOfTransportCode.Empty
import models.ExportsDeclaration
import models.declaration.{Locations, Transport}
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.helpers.CountryHelper
import views.helpers.summary.SummaryHelper.hasTransportData
import views.html.summary.summary_card

import javax.inject.{Inject, Singleton}

@Singleton
class Card6ForTransport @Inject() (summaryCard: summary_card, countryHelper: CountryHelper, appConfig: AppConfig) extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html =
    if (hasTransportData(declaration)) content(declaration, actionsEnabled) else HtmlFormat.empty

  // Called by the Mini CYA page
  override def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    summaryCard(card(6), rows(declaration, actionsEnabled))

  override def backLink(implicit request: JourneyRequest[_]): Call =
    ContainerController.displayContainerSummary

  override def continueTo(implicit request: JourneyRequest[_]): Call =
    SummaryController.displayPage

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummarySection] =
    List(
      maybeSummarySection(
        List(
          borderTransport(declaration.transport, actionsEnabled),
          warehouseIdentification(declaration.locations, actionsEnabled),
          supervisingCustomsOffice(declaration.locations, actionsEnabled),
          inlandOrBorder(declaration.locations, actionsEnabled),
          inlandModeOfTransport(declaration.locations, actionsEnabled),
          transportReference(declaration.transport, actionsEnabled),
          activeTransportType(declaration.transport, actionsEnabled),
          transportCrossingTheBorder(declaration.transport, actionsEnabled, appConfig),
          expressConsignment(declaration.transport, actionsEnabled),
          transportPayment(declaration.transport, actionsEnabled)
        )
      ),
      ContainersHelper.maybeSummarySection(declaration.transport, actionsEnabled, appConfig)
    ).flatten

  private def borderTransport(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.borderModeOfTransportCode.map { borderModeOfTransportCode =>
      SummaryListRow(
        key("transport.departure.transportCode.header"),
        valueKey(s"declaration.summary.transport.departure.transportCode.${borderModeOfTransportCode.getCodeValue}"),
        classes = "border-transport",
        changeLink(TransportLeavingTheBorderController.displayPage, "transport.departure.transportCode.header", actionsEnabled)
      )
    }

  private def warehouseIdentification(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.warehouseIdentification.map { warehouseIdentification =>
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

  private def supervisingCustomsOffice(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.supervisingCustomsOffice.flatMap(_.supervisingCustomsOffice.map { supervisingCustomsOffice =>
      SummaryListRow(
        key("transport.supervisingOffice"),
        value(supervisingCustomsOffice),
        classes = "supervising-office",
        changeLink(SupervisingCustomsOfficeController.displayPage, "transport.supervisingOffice", actionsEnabled)
      )
    })

  private def inlandOrBorder(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.inlandOrBorder.map { inlandOrBorder =>
      SummaryListRow(
        key("transport.inlandOrBorder"),
        valueKey(s"declaration.summary.transport.inlandOrBorder.${inlandOrBorder.location}"),
        classes = "inland-or-border",
        changeLink(InlandOrBorderController.displayPage, "transport.inlandOrBorder", actionsEnabled)
      )
    }

  private def inlandModeOfTransport(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.inlandModeOfTransportCode.flatMap {
      _.inlandModeOfTransportCode.filterNot(_ == Empty).map { inlandModeOfTransportCode =>
        SummaryListRow(
          key("transport.inlandModeOfTransport"),
          valueKey(s"declaration.summary.transport.inlandModeOfTransport.$inlandModeOfTransportCode"),
          classes = "mode-of-transport",
          changeLink(InlandTransportDetailsController.displayPage, "transport.inlandModeOfTransport", actionsEnabled)
        )
      }
    }

  private def transportReference(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    val meansOfTransportOnDeparture = List(
      transport.meansOfTransportOnDepartureType.map(meansType => messages(s"declaration.summary.transport.departure.meansOfTransport.$meansType")),
      transport.meansOfTransportOnDepartureIDNumber.map(identity)
    ).flatten

    if (meansOfTransportOnDeparture.nonEmpty)
      Some(
        SummaryListRow(
          key("transport.departure.meansOfTransport.header"),
          valueHtml(meansOfTransportOnDeparture.mkString("<br>")),
          classes = "transport-reference",
          changeLink(DepartureTransportController.displayPage, "transport.departure.meansOfTransport.header", actionsEnabled)
        )
      )
    else None
  }

  private def activeTransportType(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.meansOfTransportCrossingTheBorderType.flatMap { meansType =>
      transport.meansOfTransportCrossingTheBorderIDNumber.map { meansId =>
        SummaryListRow(
          key("transport.border.meansOfTransport.header"),
          valueHtml(s"${messages(s"declaration.summary.transport.border.meansOfTransport.$meansType")}<br>$meansId"),
          classes = "active-transport-type",
          changeLink(BorderTransportController.displayPage, "transport.border.meansOfTransport.header", actionsEnabled)
        )
      }
    }

  private def transportPayment(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.transportPayment.map { transportPayment =>
      SummaryListRow(
        key("transport.payment"),
        valueKey(s"declaration.summary.transport.payment.${transportPayment.paymentMethod}"),
        classes = "transport-payment",
        changeLink(TransportPaymentController.displayPage, "transport.payment", actionsEnabled)
      )
    }

  private def transportCrossingTheBorder(transport: Transport, actionsEnabled: Boolean, appConfig: AppConfig)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    transport.transportCrossingTheBorderNationality.map { transportCrossingTheBorderNationality =>
      if (appConfig.isOptionalFieldsEnabled) {
        lazy val country = transportCrossingTheBorderNationality.countryCode.map { value =>
          if (value.trim.isEmpty) {
            messages("declaration.summary.not.provided")
          } else
            countryHelper
              .getShortNameForCountryCode(value)
              .getOrElse(messages("declaration.summary.unknown"))
        }.getOrElse(messages("declaration.summary.unknown"))

        SummaryListRow(
          key("transport.registrationCountry"),
          value(country),
          classes = "active-transport-country",
          changeLink(TransportCountryController.displayPage, "transport.registrationCountry", actionsEnabled)
        )
      } else {
        lazy val country = transportCrossingTheBorderNationality.countryCode
          .flatMap(countryHelper.getShortNameForCountryCode)
          .getOrElse(messages("declaration.summary.unknown"))

        SummaryListRow(
          key("transport.registrationCountry"),
          value(country),
          classes = "active-transport-country",
          changeLink(TransportCountryController.displayPage, "transport.registrationCountry", actionsEnabled)
        )
      }
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
}
