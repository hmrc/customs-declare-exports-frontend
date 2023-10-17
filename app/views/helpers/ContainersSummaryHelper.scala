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

import controllers.declaration.routes.TransportContainerController
import models.ExportsDeclaration
import models.declaration.Container
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class ContainersSummaryHelper @Inject() (govukSummaryList: GovukSummaryList, linkContent: linkContent) {

  def section(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    declaration.transport.containers.fold(HtmlFormat.empty) { containers =>
      val summaryListRows = containers.flatMap(containerRows(_, actionsEnabled))
      val noContainers = summaryListRows.length == 0

      govukSummaryList(
        SummaryList(
          rows = if (noContainers) headingOnNoContainers(actionsEnabled) else heading(actionsEnabled) +: summaryListRows,
          classes = s"""${if (noContainers) "" else "govuk-!-margin-top-4 "}govuk-!-margin-bottom-9 containers-summary"""
        )
      )
    }

  private def heading(actionsEnabled: Boolean)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.container")), classes = "govuk-heading-s"),
      classes = "containers-heading",
      actions = if (actionsEnabled) Some(Actions(items = List(ActionItem()))) else None
    )

  private def headingOnNoContainers(actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.containers")), classes = "govuk-heading-s"),
        Value(Text(messages("site.no"))),
        classes = "containers-heading",
        actions = changeContainer(actionsEnabled)
      )
    )

  private def containerRows(container: Container, actionsEnabled: Boolean)(implicit messages: Messages): List[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.container.id")), classes = "govuk-heading-s"),
        Value(Text(container.id)),
        classes = s"govuk-summary-list__row--no-border container-${container.sequenceId}",
        actions = changeContainer(actionsEnabled)
      ),
      SummaryListRow(
        Key(Text(messages("declaration.summary.container.securitySeals"))),
        Value(Text(valueOfSeals(container))),
        classes = s"container-seals-${container.sequenceId}"
      )
    )

  private def valueOfSeals(container: Container)(implicit messages: Messages): String =
    if (container.seals.isEmpty) messages("declaration.summary.container.securitySeals.none")
    else container.seals.map(_.id).mkString(", ")

  private def changeContainer(actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = messages("declaration.summary.container.change")
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(TransportContainerController.displayContainerSummary.url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }
}
