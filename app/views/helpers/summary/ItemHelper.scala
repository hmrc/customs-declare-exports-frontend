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
import controllers.section5.routes._
import models.DeclarationType.{DeclarationType, isStandardOrSupplementary}
import models.declaration.ExportItem
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import views.html.summary.summary_section

import javax.inject.{Inject, Singleton}

@Singleton
class ItemHelper @Inject() (packageInformationHelper: PackageInformationHelper, summarySection: summary_section, appConfig: AppConfig) extends SummaryHelper {

  def content(item: ExportItem, itemIdx: Int, declarationType: DeclarationType)(implicit messages: Messages): Html = {
    val summarySections = rows(item, false, itemIdx, declarationType)
    HtmlFormat.fill(summarySections.map(s => summarySection(s.copy(maybeHeading = None))))
  }

  def rows(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int, declarationType: DeclarationType)(
    implicit messages: Messages
  ): Seq[SummarySection] = {
    // Early evaluation of this attribute in order to verify if it will be displayed as a multi-rows section.
    val maybeAdditionalInformationSection = AdditionalInformationHelper.maybeSummarySection(item, actionsEnabled, itemIdx)

    // If it is verified and it is followed by a single-row section, we need to add some margin between the 2 sections.
    val hasPackageInformation = item.packageInformation.fold(false)(_.nonEmpty)
    val hasAdditionalInformation = item.additionalInformation.fold(false)(_.items.nonEmpty)

    List(
      maybeSummarySection(
        List(
          procedureCode(item, actionsEnabled, itemIdx),
          additionalProcedureCodes(item, actionsEnabled, itemIdx),
          fiscalInformation(item, actionsEnabled, itemIdx),
          additionalFiscalReferences(item, actionsEnabled, itemIdx),
          commodityDetails(item, actionsEnabled, itemIdx),
          goodsDescription(item, actionsEnabled, itemIdx),
          dangerousGoodsCode(item, actionsEnabled, itemIdx),
          cusCode(item, actionsEnabled, itemIdx),
          nactCodes(item, actionsEnabled, itemIdx),
          nactExemptionCode(item, actionsEnabled, itemIdx),
          statisticalValue(item, actionsEnabled, itemIdx)
        ),
        Some(itemHeading(item, actionsEnabled, itemIdx))
      ),
      packageInformationHelper.maybeSummarySection(item, actionsEnabled, itemIdx),
      maybeSummarySection(
        List(
          grossWeight(item, hasPackageInformation, actionsEnabled, itemIdx),
          netWeight(item, itemIdx),
          supplementaryUnits(item, actionsEnabled, itemIdx, declarationType)
        )
      ),
      maybeAdditionalInformationSection,
      AdditionalDocumentsHelper.maybeSummarySection(item, hasAdditionalInformation, actionsEnabled, itemIdx)
    ).flatten
  }

  private def itemHeading(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int): SummarySectionHeading = {
    lazy val itemSection = {
      val topPaddingClass = if (itemIdx > 1) "item-top-padding" else "govuk-!-margin-top-4"
      if (actionsEnabled) {
        val call = RemoveItemsSummaryController.displayRemoveItemConfirmationPage(item.id)
        ItemSection(itemIdx, call.url, topPaddingClass)
      } else ItemSection(itemIdx, topPaddingClass = topPaddingClass)
    }
    SummarySectionHeading(s"item-$itemIdx", "item", Some(itemSection))
  }

  private def procedureCode(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.procedureCodes.flatMap(_.procedureCode).map { procedureCode =>
      SummaryListRow(
        key("item.procedureCode"),
        value(procedureCode),
        classes = s"item-$itemIdx-procedure-code",
        changeLink(ProcedureCodesController.displayPage(item.id), "item.procedureCode", actionsEnabled, Some(itemIdx))
      )
    }

  private def additionalProcedureCodes(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.procedureCodes.map { procedureCodes =>
      SummaryListRow(
        key("item.additionalProcedureCodes"),
        value(procedureCodes.additionalProcedureCodes.mkString(" ")),
        classes = s"item-$itemIdx-additional-procedure-codes",
        changeLink(AdditionalProcedureCodesController.displayPage(item.id), "item.additionalProcedureCodes", actionsEnabled, Some(itemIdx))
      )
    }

  private def fiscalInformation(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.fiscalInformation.map { fiscalInformation =>
      SummaryListRow(
        key("item.onwardSupplyRelief"),
        value(fiscalInformation.onwardSupplyRelief),
        classes = s"item-$itemIdx-onward-supply-relief",
        changeLink(FiscalInformationController.displayPage(item.id), "item.onwardSupplyRelief", actionsEnabled, Some(itemIdx))
      )
    }

  private def additionalFiscalReferences(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    item.additionalFiscalReferencesData.map { additionalFiscalReferences =>
      SummaryListRow(
        key("item.VATdetails"),
        valueHtml(additionalFiscalReferences.references.map(_.value).mkString("<br/>")),
        classes = s"item-$itemIdx-vat-details",
        changeLink(AdditionalFiscalReferencesController.displayPage(item.id), "item.VATdetails", actionsEnabled, Some(itemIdx))
      )
    }

  private def commodityDetails(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.commodityDetails.map { commodityDetails =>
      SummaryListRow(
        key("item.commodityCode"),
        value(commodityDetails.combinedNomenclatureCode.getOrElse("")),
        classes = s"item-$itemIdx-commodity-code",
        changeLink(CommodityDetailsController.displayPage(item.id), "item.commodityCode", actionsEnabled, Some(itemIdx))
      )
    }

  private def goodsDescription(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.commodityDetails.map { commodityDetails =>
      SummaryListRow(
        key("item.goodsDescription"),
        value(commodityDetails.descriptionOfGoods.getOrElse("")),
        classes = s"item-$itemIdx-goods-description",
        changeLink(CommodityDetailsController.displayPage(item.id), "item.goodsDescription", actionsEnabled, Some(itemIdx))
      )
    }

  private def dangerousGoodsCode(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.dangerousGoodsCode.map { dangerousGoodsCode =>
      SummaryListRow(
        key("item.unDangerousGoodsCode"),
        value(dangerousGoodsCode.dangerousGoodsCode.getOrElse(messages("site.no"))),
        classes = s"item-$itemIdx-dangerous-goods-code",
        changeLink(UNDangerousGoodsCodeController.displayPage(item.id), "item.unDangerousGoodsCode", actionsEnabled, Some(itemIdx))
      )
    }

  private def cusCode(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.cusCode.map { cusCode =>
      SummaryListRow(
        key("item.cusCode"),
        value(cusCode.cusCode.getOrElse(messages("site.no"))),
        classes = s"item-$itemIdx-cus-code",
        changeLink(CusCodeController.displayPage(item.id), "item.cusCode", actionsEnabled, Some(itemIdx))
      )
    }

  private def nactCodes(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.nactCodes.map { nactCodes =>
      SummaryListRow(
        key("item.nationalAdditionalCodes"),
        value(nactCodes.map(_.nactCode).mkString(", ")),
        classes = s"item-$itemIdx-national-additional-codes",
        changeLink(NactCodeSummaryController.displayPage(item.id), "item.nationalAdditionalCodes", actionsEnabled, Some(itemIdx))
      )
    }

  private def nactExemptionCode(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.nactExemptionCode.map { nactExemptionCode =>
      SummaryListRow(
        key("item.zeroRatedForVat"),
        valueKey(s"declaration.summary.item.zeroRatedForVat.${nactExemptionCode.nactCode}"),
        classes = s"item-$itemIdx-zero-rated-for-vat",
        changeLink(ZeroRatedForVatController.displayPage(item.id), "item.zeroRatedForVat", actionsEnabled, Some(itemIdx))
      )
    }

  private def statisticalValue(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int)
                              (implicit messages: Messages): Option[SummaryListRow] =
    item.statisticalValue.map { statisticalValue =>
      if(statisticalValue.statisticalValue.trim.isEmpty && appConfig.isOptionalFieldsEnabled) {
        SummaryListRow(
          key("item.itemValue"),
          value(messages("declaration.summary.not.provided")),
          classes = s"item-$itemIdx-item-value",
          changeLink(StatisticalValueController.displayPage(item.id), "item.itemValue", actionsEnabled, Some(itemIdx))
        )
      }else{
        SummaryListRow(
          key("item.itemValue"),
          value(statisticalValue.statisticalValue),
          classes = s"item-$itemIdx-item-value",
          changeLink(StatisticalValueController.displayPage(item.id), "item.itemValue", actionsEnabled, Some(itemIdx))
        )
      }

    }

  private def grossWeight(item: ExportItem, hasPackageInformation: Boolean, actionsEnabled: Boolean, itemIdx: Int)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    item.commodityMeasure.map { commodityMeasure =>
      lazy val keyOnPackageInformation = {
        val text = messages("declaration.summary.item.grossWeight")
        Key(HtmlContent(s"""<div class="govuk-!-margin-top-4 govuk-!-margin-bottom-0">$text</div>"""))
      }
      SummaryListRow(
        if (hasPackageInformation) keyOnPackageInformation else key("item.grossWeight"),
        value(commodityMeasure.grossMass.getOrElse("")),
        classes = s"govuk-summary-list__row--no-border item-$itemIdx-gross-weight",
        changeLink(CommodityMeasureController.displayPage(item.id), "item.grossWeight", actionsEnabled, Some(itemIdx))
      )
    }

  private def netWeight(item: ExportItem, itemIdx: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.commodityMeasure.map { commodityMeasure =>
      SummaryListRow(key("item.netWeight"), value(commodityMeasure.netMass.getOrElse("")), classes = s"item-$itemIdx-net-weight")
    }

  private def supplementaryUnits(item: ExportItem, actionsEnabled: Boolean, itemIdx: Int, declarationType: DeclarationType)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    if (!isStandardOrSupplementary(declarationType)) None
    else
      item.commodityMeasure.flatMap {
        _.supplementaryUnits.map { supplementaryUnits =>
          SummaryListRow(
            key("item.supplementaryUnits"),
            value(supplementaryUnits),
            classes = s"item-$itemIdx-supplementary-units",
            changeLink(SupplementaryUnitsController.displayPage(item.id), "item.supplementaryUnits", actionsEnabled, Some(itemIdx))
          )
        }
      }
}
