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
import controllers.section5.routes.PackageInformationSummaryController
import forms.section5.PackageInformation
import models.declaration.ExportItem
import play.api.i18n.Messages
import services.PackageTypesService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import javax.inject.{Inject, Singleton}

@Singleton
class PackageInformationHelper @Inject() (packageTypesService: PackageTypesService, appConfig: AppConfig) extends SummaryHelper {

  def maybeSummarySection(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages):
  Option[SummarySection] =
    item.packageInformation.map { listOfPackageInformation =>
      val summaryListRows = listOfPackageInformation.zipWithIndex.flatMap { case (packageInfo, index) =>
        if(appConfig.isOptionalFieldsEnabled){
        packageInfoRowsOpt(item, itemIndex, packageInfo, index + 1, actionsEnabled)
        }
        else {
          packageInfoRows(item, itemIndex, packageInfo, index + 1, actionsEnabled)
        }
      }.flatten

      if (summaryListRows.isEmpty) headingOnNoPackageInfo(item, actionsEnabled, itemIndex)
      else SummarySection(summaryListRows, Some(SummarySectionHeading(s"item-$itemIndex-package-information", "item.packageInformation")))
    }

  private def headingOnNoPackageInfo(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages): SummarySection =
    SummarySection(
      List(
        SummaryListRow(
          key("item.packageInformation"),
          valueKey("site.none"),
          classes = s"summary-row-border-bottom summary-row-border-top item-$itemIndex-package-information-heading",
          actions = changePackageInformation(item, actionsEnabled, itemIndex)
        )
      )
    )

  private def packageInfoRows(item: ExportItem, itemIndex: Int, packageInfo: PackageInformation, index: Int, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] =
    List(
      packageInfo.typesOfPackages.map { _ =>
        SummaryListRow(
          key("item.packageInformation.type"),
          value(packageTypesService.typesOfPackagesText(packageInfo.typesOfPackages).getOrElse("")),
          classes = s"${noBorder(false, packageInfo)}item-$itemIndex-package-information-$index-type",
          changePackageInformation(item, actionsEnabled, itemIndex)
        )
      },
      packageInfo.numberOfPackages.map { numberOfPackages =>
        SummaryListRow(
          key("item.packageInformation.number"),
          value(numberOfPackages.toString),
          classes = s"${noBorder(true, packageInfo)}item-$itemIndex-package-information-$index-number",
          packageInfo.typesOfPackages.fold(changePackageInformation(item, actionsEnabled, itemIndex))(_ => None)
        )
      },
      packageInfo.shippingMarks.map { shippingMarks =>
        SummaryListRow(
          key("item.packageInformation.markings"),
          value(shippingMarks),
          classes = s"package-info item-$itemIndex-package-information-$index-markings", {
            val changeLinkOnShippingMarks = packageInfo.typesOfPackages.isEmpty && packageInfo.numberOfPackages.isEmpty
            if (changeLinkOnShippingMarks) changePackageInformation(item, actionsEnabled, itemIndex) else None
          }
        )
      }
    )

// Remove above, implement below when removing flag
  private def packageInfoRowsOpt(item: ExportItem, itemIndex: Int, packageInfo: PackageInformation, index: Int, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] =
    List(
      packageInfo.numberOfPackages.map { numberOfPackages =>
        SummaryListRow(
          key("item.packageInformation.number"),
          value(numberOfPackages.toString),
          classes = s"${noBorder(false, packageInfo)}item-$itemIndex-package-information-$index-number",
          changePackageInformation(item, actionsEnabled, itemIndex)
        )
      },
      packageInfo.typesOfPackages.map { _ =>
        SummaryListRow(
          key("item.packageInformation.type"),
          value(packageTypesService.typesOfPackagesText(packageInfo.typesOfPackages).getOrElse("")),
          classes = s"${noBorder(false, packageInfo)}item-$itemIndex-package-information-$index-type"
        )
      }.orElse(Some(SummaryListRow(
        key("item.packageInformation.type"),
        value(messages("declaration.summary.not.provided")),
        classes = s"${noBorder(false, packageInfo)}item-$itemIndex-package-information-$index-type"
      ))),
      packageInfo.shippingMarks.map { shippingMarks =>
        SummaryListRow(
          key("item.packageInformation.markings"),
          value(shippingMarks),
          classes = s"package-info item-$itemIndex-package-information-$index-markings", {
            val changeLinkOnShippingMarks = packageInfo.typesOfPackages.isEmpty && packageInfo.numberOfPackages.isEmpty
            if (changeLinkOnShippingMarks) changePackageInformation(item, actionsEnabled, itemIndex) else None
          }
        )
      }.orElse(Some(SummaryListRow(
        key("item.packageInformation.markings"),
        value(messages("declaration.summary.not.provided")),
        classes = s"package-info item-$itemIndex-package-information-$index-markings", {
          val changeLinkOnShippingMarks = packageInfo.typesOfPackages.isEmpty && packageInfo.numberOfPackages.isEmpty
          if (changeLinkOnShippingMarks) changePackageInformation(item, actionsEnabled, itemIndex) else None
        }
      )))
    )

  private def noBorder(isNumberOfPackages: Boolean, packageInfo: PackageInformation): String =
    if (isNumberOfPackages) packageInfo.shippingMarks.fold("")(_ => "govuk-summary-list__row--no-border ")
    else if (packageInfo.numberOfPackages.isEmpty && packageInfo.shippingMarks.isEmpty) ""
    else "govuk-summary-list__row--no-border "

  private def changePackageInformation(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages): Option[Actions] =
    changeLink(PackageInformationSummaryController.displayPage(item.id), "item.packageInformation.number", actionsEnabled, Some(itemIndex))
}
