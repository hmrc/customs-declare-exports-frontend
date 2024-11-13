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

import controllers.section5.routes._
import controllers.section6.routes.TransportLeavingTheBorderController
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.empty
import uk.gov.hmrc.govukfrontend.views.html.components.GovukWarningText
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import views.helpers.summary.SummaryHelper.showItemsCard
import views.html.summary.summary_card

import javax.inject.{Inject, Singleton}

@Singleton
class Card5ForItems @Inject() (
  summaryCard: summary_card,
  govukWarningText: GovukWarningText,
  itemHelper: ItemHelper
) extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true, showNoItemError: Boolean = false)(implicit messages: Messages): Html =
    if (showItemsCard(declaration, actionsEnabled)) content(declaration, actionsEnabled, showNoItemError) else empty

  // Called by the Mini CYA page
  override def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    content(declaration, actionsEnabled, false)

  override def backLink(implicit request: JourneyRequest[_]): Call = ItemsSummaryController.displayItemsSummaryPage

  override def continueTo(implicit request: JourneyRequest[_]): Call = TransportLeavingTheBorderController.displayPage

  private def content(declaration: ExportsDeclaration, actionsEnabled: Boolean, showNoItemError: Boolean)(implicit messages: Messages): Html =
    summaryCard(
      card(5, showNoItemError && !declaration.hasItems),
      rows(declaration, actionsEnabled, showNoItemError),
      addItemAction(declaration, actionsEnabled)
    )

  private def addItemAction(declaration: ExportsDeclaration, actionsEnabled: Boolean): Option[ItemSection] =
    if (actionsEnabled && (!declaration.isType(CLEARANCE) || declaration.items.isEmpty)) {
      val call = if (declaration.hasItems) ItemsSummaryController.addAdditionalItem else ItemsSummaryController.displayAddItemPage
      Some(ItemSection(0, call.url))
    }
    else None

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean, showNoItemError: Boolean)(
    implicit messages: Messages
  ): Seq[SummarySection] =
    if (!declaration.hasItems) noItemRow(actionsEnabled, showNoItemError)
    else
      declaration.items.sortBy(_.sequenceId).zipWithIndex.flatMap { case (item, itemIdx) =>
        if (item.isDefined) itemHelper.rows(item, actionsEnabled, itemIdx + 1, declaration.`type`) else List.empty
      }

  private def noItemRow(actionsEnabled: Boolean, showNoItemError: Boolean)(implicit messages: Messages): Seq[SummarySection] =
    if (actionsEnabled) {
      val warningText = WarningText(Some(messages("site.warning")), content = Text(messages("declaration.summary.items.empty")))
      val content = HtmlContent(govukWarningText(warningText))
      List(SummarySection(
        List(noItemError(showNoItemError), Some(SummaryListRow(Key(content), Value(classes = "hidden")))).flatten
      ))
    }
    else List.empty

  private def noItemError(showNoItemError: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    if (showNoItemError) {
      val classes = "govuk-summary-list__row--no-border"
      Some(SummaryListRow(key("items.none", "govuk-error-message"), Value(classes = "hidden"), classes = classes))
    }
    else None
}
