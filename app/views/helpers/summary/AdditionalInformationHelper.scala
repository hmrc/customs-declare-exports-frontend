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

import controllers.declaration.routes.{AdditionalInformationController, AdditionalInformationRequiredController}
import forms.declaration.AdditionalInformation
import models.declaration.ExportItem
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object AdditionalInformationHelper extends SummaryHelper {

  def section(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    item.additionalInformation.map { additionalInformation =>
      val summaryListRows = additionalInformation.items.zipWithIndex.flatMap { case (informationItem, index) =>
        informationItemRows(item, itemIndex, informationItem, index + 1, actionsEnabled)
      }

      if (summaryListRows.flatten.isEmpty) headingOnNoInformation(item, actionsEnabled, itemIndex)
      else heading(s"item-$itemIndex-additional-information", "item.additionalInformation") +: summaryListRows
    }
      .getOrElse(List.empty)

  private def headingOnNoInformation(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] =
    List(
      Some(
        SummaryListRow(
          key("item.additionalInformation"),
          valueKey("site.none"),
          classes = s"item-$itemIndex-additional-information-heading",
          changeLink(AdditionalInformationRequiredController.displayPage(item.id), "item.additionalInformation", actionsEnabled, Some(itemIndex))
        )
      )
    )

  private def informationItemRows(item: ExportItem, itemIndex: Int, informationItem: AdditionalInformation, index: Int, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] =
    List(
      Some(
        SummaryListRow(
          key("item.additionalInformation.code"),
          value(informationItem.code),
          classes = s"govuk-summary-list__row--no-border item-$itemIndex-additional-information-$index-code",
          actions = changeLink(AdditionalInformationController.displayPage(item.id), "item.additionalInformation", actionsEnabled, Some(itemIndex))
        )
      ),
      Some(
        SummaryListRow(
          key("item.additionalInformation.description"),
          value(informationItem.description),
          classes = s"item-$itemIndex-additional-information-$index-description"
        )
      )
    )
}
