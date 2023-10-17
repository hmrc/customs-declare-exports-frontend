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

package views.helpers

import controllers.declaration.routes.{AdditionalInformationController, AdditionalInformationRequiredController}
import forms.declaration.AdditionalInformation
import models.declaration.ExportItem
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class AdditionalInformationSummaryHelper @Inject() (govukSummaryList: GovukSummaryList, linkContent: linkContent) {

  def section(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    item.additionalInformation.fold(HtmlFormat.empty) { additionalInformation =>
      val informationItemsRows = additionalInformation.items.zipWithIndex.flatMap { case (informationItem, index) =>
        informationItemRows(item, informationItem, index + 1, actionsEnabled)
      }

      if (informationItemsRows.isEmpty) govukSummaryList(SummaryList(noDataRow(item, actionsEnabled), classes = cssClasses(item, 0)))
      else govukSummaryList(SummaryList(heading(item, actionsEnabled) +: informationItemsRows, classes = cssClasses(item, 4)))
    }

  private def cssClasses(item: ExportItem, top: Int): String =
    s"govuk-!-margin-top-$top govuk-!-margin-bottom-0 item-${item.sequenceId}-additional-information-summary"

  private def informationItemRows(item: ExportItem, informationItem: AdditionalInformation, index: Int, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalInformation.code"))),
        Value(Text(informationItem.code)),
        classes = s"govuk-summary-list__row--no-border item-${item.sequenceId}-additional-information-$index-code",
        actions = changeLink(item, AdditionalInformationController.displayPage(item.id).url, actionsEnabled)
      ),
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalInformation.description"))),
        Value(Text(informationItem.description)),
        classes = s"item-${item.sequenceId}-additional-information-$index-description"
      )
    )

  private def heading(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.items.item.additionalInformation")), classes = "govuk-heading-s"),
      classes = s"item-${item.sequenceId}-additional-information-heading",
      actions = if (actionsEnabled) Some(Actions(items = List(ActionItem()))) else None
    )

  private def noDataRow(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalInformation")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = s"item-${item.sequenceId}-additional-information-heading",
        actions = changeLink(item, AdditionalInformationRequiredController.displayPage(item.id).url, actionsEnabled)
      )
    )

  private def changeLink(item: ExportItem, url: String, actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = messages(s"declaration.summary.items.item.additionalInformation.change", item.sequenceId)
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }
}
