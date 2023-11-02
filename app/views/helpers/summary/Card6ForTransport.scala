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
import models.declaration.Transport
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import javax.inject.{Inject, Singleton}

@Singleton
class Card6ForTransport @Inject() (govukSummaryList: GovukSummaryList) extends SummaryHelper {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val transport = declaration.transport

    val hasData = transport.expressConsignment.isDefined ||
      inlandModeOfTransportCode(declaration).isDefined ||
      transport.borderModeOfTransportCode.isDefined

    if (hasData) displayCard(declaration, actionsEnabled) else HtmlFormat.empty
  }

  private def displayCard(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    govukSummaryList(SummaryList(rows(declaration, actionsEnabled), card("transport")))

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      borderTransport(declaration.transport, actionsEnabled),
      inlandModeOfTransport(declaration, actionsEnabled),
      expressConsignment(declaration.transport, actionsEnabled)
    ).flatten

  private def inlandModeOfTransportCode(declaration: ExportsDeclaration): Option[InlandModeOfTransportCode] =
    declaration.locations.inlandModeOfTransportCode.filterNot(_.inlandModeOfTransportCode contains Empty)

  private def borderTransport(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.borderModeOfTransportCode.map { borderModeOfTransportCode =>
      SummaryListRow(
        key("transport.departure.transportCode.header"),
        valueKey(s"declaration.summary.transport.departure.transportCode.${borderModeOfTransportCode.getCodeValue}"),
        classes = "borderTransport",
        changeLink(TransportLeavingTheBorderController.displayPage, "transport.departure.transportCode.header", actionsEnabled)
      )
    }

  private def inlandModeOfTransport(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    inlandModeOfTransportCode(declaration)
      .map(inlandModeOfTransportCode =>
        SummaryListRow(
          key("transport.inlandModeOfTransport"),
          valueKey(
            inlandModeOfTransportCode.inlandModeOfTransportCode
              .map(code => s"declaration.summary.transport.inlandModeOfTransport.$code")
              .getOrElse("")
          ),
          classes = "modeOfTransport",
          changeLink(InlandTransportDetailsController.displayPage, "transport.inlandModeOfTransport", actionsEnabled)
        )
      )

  private def expressConsignment(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    transport.expressConsignment.map { expressConsignment =>
      SummaryListRow(
        key("transport.expressConsignment"),
        valueKey(if (expressConsignment.isYes) "site.yes" else "site.no"),
        classes = "expressConsignment",
        changeLink(ExpressConsignmentController.displayPage, "transport.expressConsignment", actionsEnabled)
      )
    }

}
