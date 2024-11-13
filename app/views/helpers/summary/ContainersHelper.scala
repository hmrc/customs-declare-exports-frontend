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

import controllers.section6.routes._
import models.declaration.{Container, Transport}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}

object ContainersHelper extends SummaryHelper {

  def maybeSummarySection(transport: Transport, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummarySection] =
    transport.containers.map { containers =>
      val summaryListRows = containers.zipWithIndex.flatMap { case (container, index) =>
        List(Some(containerId(container, index + 1, actionsEnabled)), Some(securitySeals(container, index + 1)))
      }.flatten
      if (summaryListRows.isEmpty) headingOnNoContainers(actionsEnabled)
      else SummarySection(summaryListRows, Some(SummarySectionHeading("containers", "container")))
    }

  private def headingOnNoContainers(actionsEnabled: Boolean)(implicit messages: Messages): SummarySection =
    SummarySection(
      List(SummaryListRow(
        key("containers"),
        valueKey("site.none"),
        classes = "heading-on-no-data containers-heading",
        changeContainer(actionsEnabled)
      ))
    )

  private def containerId(container: Container, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key("container.id"),
      value(container.id),
      classes = s"govuk-summary-list__row--no-border container-${index}-type",
      changeContainer(actionsEnabled)
    )

  private def securitySeals(container: Container, index: Int)(implicit messages: Messages): SummaryListRow = {
    val valueOfSeals =
      if (container.seals.isEmpty) messages("declaration.summary.container.securitySeals.none")
      else container.seals.map(_.id).mkString(", ")

    SummaryListRow(key("container.securitySeals"), value(valueOfSeals), classes = s"container-${index}-seals")
  }

  private def changeContainer(actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    changeLink(ContainerController.displayContainerSummary, "container", actionsEnabled)
}
