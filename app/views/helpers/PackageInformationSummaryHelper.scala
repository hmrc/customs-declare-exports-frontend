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

import controllers.declaration.routes.PackageInformationSummaryController
import forms.declaration.PackageInformation
import models.declaration.ExportItem
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import services.PackageTypesService
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class PackageInformationSummaryHelper @Inject() (
  govukSummaryList: GovukSummaryList,
  linkContent: linkContent,
  packageTypesService: PackageTypesService
) {
  def section(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    item.packageInformation.fold(HtmlFormat.empty) { listOfPackageInformation =>
      val summaryListRows = listOfPackageInformation.zipWithIndex.map { case (packageInfo, index) =>
        packageInfoRows(item, packageInfo, index + 1, actionsEnabled)
      }.flatten

      if (summaryListRows.length == 0) govukSummaryList(SummaryList(headingOnNoPackageInfo(item, actionsEnabled), classes = cssClasses(item, 0)))
      else govukSummaryList(SummaryList(heading(item, actionsEnabled) +: summaryListRows, classes = cssClasses(item, 4)))
    }

  private def cssClasses(item: ExportItem, padding: Int): String =
    s"govuk-!-margin-top-$padding govuk-!-margin-bottom-$padding item-${item.sequenceId}-package-information-summary"

  private def heading(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.items.item.packageInformation")), classes = "govuk-heading-s"),
      classes = s"item-${item.sequenceId}-package-information-heading",
      actions = if (actionsEnabled) Some(Actions(items = List(ActionItem()))) else None
    )

  private def headingOnNoPackageInfo(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.packageInformation")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = s"item-${item.sequenceId}-package-information-heading",
        actions = changeLink(item, actionsEnabled)
      )
    )

  private def packageInfoRows(item: ExportItem, packageInfo: PackageInformation, index: Int, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Seq[SummaryListRow] =
    List(
      packageInfo.typesOfPackages.map { _ =>
        SummaryListRow(
          Key(Text(messages("declaration.summary.items.item.packageInformation.type")), classes = "govuk-heading-s"),
          Value(Text(packageTypesService.typesOfPackagesText(packageInfo.typesOfPackages).getOrElse(""))),
          classes = s"${noBorder(false, packageInfo)}item-${item.sequenceId}-package-information-$index-type",
          actions = changeLink(item, actionsEnabled)
        )
      },
      packageInfo.numberOfPackages.map { numberOfPackages =>
        SummaryListRow(
          Key(Text(messages("declaration.summary.items.item.packageInformation.number"))),
          Value(Text(numberOfPackages.toString)),
          classes = s"${noBorder(true, packageInfo)}item-${item.sequenceId}-package-information-$index-number",
          actions = if (packageInfo.typesOfPackages.isDefined) None else changeLink(item, actionsEnabled)
        )
      },
      packageInfo.shippingMarks.map { shippingMarks =>
        SummaryListRow(
          Key(Text(messages("declaration.summary.items.item.packageInformation.markings"))),
          Value(Text(shippingMarks)),
          classes = s"item-${item.sequenceId}-package-information-$index-markings",
          actions = if (packageInfo.typesOfPackages.isDefined || packageInfo.numberOfPackages.isDefined) None else changeLink(item, actionsEnabled)
        )
      }
    ).flatten

  private def noBorder(isNumberOfPackages: Boolean, packageInfo: PackageInformation): String =
    if (isNumberOfPackages) packageInfo.shippingMarks.fold("")(_ => "govuk-summary-list__row--no-border ")
    else if (packageInfo.numberOfPackages.isEmpty && packageInfo.shippingMarks.isEmpty) ""
    else "govuk-summary-list__row--no-border "

  private def changeLink(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = messages("declaration.summary.items.item.packageInformation.change", item.sequenceId)
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(PackageInformationSummaryController.displayPage(item.id).url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }
}
