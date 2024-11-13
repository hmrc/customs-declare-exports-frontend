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

import controllers.section5.routes.{AdditionalInformationController, AdditionalInformationRequiredController}
import forms.section5.AdditionalInformation
import models.declaration.ExportItem
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object AdditionalInformationHelper extends SummaryHelper {

  def maybeSummarySection(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages): Option[SummarySection] =
    item.additionalInformation.map { additionalInformationData =>
      val summaryListRows = additionalInformationData.items.zipWithIndex.flatMap { case (additionalInformation, index) =>
        informationItemRows(item, itemIndex, additionalInformation, index + 1, actionsEnabled)
      }

      if (summaryListRows.isEmpty) headingOnNoInformation(item, actionsEnabled, itemIndex)
      else {
        val heading = SummarySectionHeading(s"item-$itemIndex-additional-information", "item.additionalInformation")
        SummarySection(summaryListRows, Some(heading))
      }
    }

  private def headingOnNoInformation(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(
    implicit messages: Messages
  ): SummarySection = {
    val call = AdditionalInformationRequiredController.displayPage(item.id)
    SummarySection(
      List(SummaryListRow(
        key("item.additionalInformation"),
        valueKey("site.none"),
        classes = s"heading-on-no-data item-$itemIndex-additional-information-heading",
        changeLink(call, "item.additionalInformation", actionsEnabled, Some(itemIndex))
      ))
    )
  }

  private def informationItemRows(
    item: ExportItem, itemIndex: Int, informationItem: AdditionalInformation, index: Int, actionsEnabled: Boolean
  )(implicit messages: Messages): Seq[SummaryListRow] = {
    val call = AdditionalInformationController.displayPage(item.id)
    List(
      SummaryListRow(
        key("item.additionalInformation.code"),
        value(informationItem.code),
        classes = s"govuk-summary-list__row--no-border item-$itemIndex-additional-information-$index-code",
        actions = changeLink(call, "item.additionalInformation", actionsEnabled, Some(itemIndex))
      ),
      SummaryListRow(
        key("item.additionalInformation.description"),
        value(informationItem.description),
        classes = s"item-$itemIndex-additional-information-$index-description"
      )
    )
  }
}
