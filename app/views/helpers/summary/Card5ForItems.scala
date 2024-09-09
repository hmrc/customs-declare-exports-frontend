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
import models.declaration.ExportItem
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.empty
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, GovukWarningText, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import views.helpers.ActionItemBuilder.actionItem
import views.helpers.summary.SummaryHelper.showItemsCard

import javax.inject.{Inject, Singleton}

@Singleton
class Card5ForItems @Inject() (
  govukSummaryList: GovukSummaryList,
  govukWarningText: GovukWarningText,
  packageInformationHelper: PackageInformationHelper
) extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true, showNoItemError: Boolean = false)(implicit messages: Messages): Html =
    if (showItemsCard(declaration, actionsEnabled)) content(declaration, actionsEnabled, showNoItemError) else empty

  // Called by the Mini CYA page
  def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    content(declaration, actionsEnabled, false)

  private def content(declaration: ExportsDeclaration, actionsEnabled: Boolean, showNoItemError: Boolean)(implicit messages: Messages): Html = {
    val cardContent = card(5).map(_.copy(actions = addItemAction(declaration, actionsEnabled)))
    val html = govukSummaryList(SummaryList(rows(declaration, actionsEnabled, showNoItemError), cardContent))
    markCardOnErrors(html, showNoItemError && !declaration.hasItems)
  }

  def backLink(implicit request: JourneyRequest[_]): Call = ItemsSummaryController.displayItemsSummaryPage

  def continueTo(implicit request: JourneyRequest[_]): Call = TransportLeavingTheBorderController.displayPage

  private def addItemAction(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (actionsEnabled && (!declaration.isType(CLEARANCE) || declaration.items.isEmpty)) addItemLink(declaration) else None

  private def addItemLink(declaration: ExportsDeclaration)(implicit messages: Messages): Option[Actions] = {
    val content = Text(messages("declaration.summary.items.add"))
    val call = if (declaration.hasItems) ItemsSummaryController.addAdditionalItem else ItemsSummaryController.displayAddItemPage
    Some(Actions(items = List(ActionItem(call.url, content, attributes = Map("id" -> SummaryHelper.addItemLinkId)))))
  }

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean, showNoItemError: Boolean)(
    implicit messages: Messages
  ): Seq[SummaryListRow] =
    if (!declaration.hasItems) noItemRow(actionsEnabled, showNoItemError)
    else
      declaration.items.sortBy(_.sequenceId).zipWithIndex.flatMap { case (item, index) =>
        if (item.isDefined) rows(declaration, item, actionsEnabled, index + 1) else List.empty
      }

  private def noItemRow(actionsEnabled: Boolean, showNoItemError: Boolean)(implicit messages: Messages): List[SummaryListRow] =
    if (!actionsEnabled) List.empty
    else {
      val warningText = WarningText(Some(messages("site.warning")), content = Text(messages("declaration.summary.items.empty")))
      val content = HtmlContent(govukWarningText(warningText))
      List(noItemError(showNoItemError), Some(SummaryListRow(Key(content), Value(classes = "hidden")))).flatten
    }

  private def noItemError(showNoItemError: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    if (!showNoItemError) None
    else Some(SummaryListRow(key("items.none", "govuk-error-message"), Value(classes = "hidden"), classes = s"govuk-summary-list__row--no-border"))

  private def rows(declaration: ExportsDeclaration, item: ExportItem, actionsEnabled: Boolean, index: Int)(
    implicit messages: Messages
  ): Seq[SummaryListRow] = {
    // Early evaluation of this attribute in order to verify if it will be displayed as a multi-rows section.
    val additionalInformationRows = AdditionalInformationHelper.section(item, actionsEnabled, index)
    val packageInformationRows = packageInformationHelper.section(item, actionsEnabled, index)

    // If it is verified and it is followed by a single-row section, we need to add some margin between the 2 sections.
    val hasAdditionalInformation = additionalInformationRows.flatten.length > 1
    val hasPackageInformation = packageInformationRows.flatten.length > 1

    (
      List(
        itemHeading(item, actionsEnabled, index),
        procedureCode(item, actionsEnabled, index),
        additionalProcedureCodes(item, actionsEnabled, index),
        fiscalInformation(item, actionsEnabled, index),
        additionalFiscalReferences(item, actionsEnabled, index),
        commodityDetails(item, actionsEnabled, index),
        goodsDescription(item, actionsEnabled, index),
        dangerousGoodsCode(item, actionsEnabled, index),
        cusCode(item, actionsEnabled, index),
        nactCodes(item, actionsEnabled, index),
        nactExemptionCode(item, actionsEnabled, index),
        statisticalValue(item, actionsEnabled, index)
      )
        ++ packageInformationRows
        ++ List(
          grossWeight(item, hasPackageInformation, actionsEnabled, index),
          netWeight(item, index),
          supplementaryUnits(declaration, item, actionsEnabled, index)
        )
        ++ additionalInformationRows
        ++ AdditionalDocumentsHelper.section(item, hasAdditionalInformation, actionsEnabled, index)
    ).flatten
  }

  private def itemHeading(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] = {
    lazy val removeItem = {
      val text = messages("declaration.summary.item.remove")
      val call = RemoveItemsSummaryController.displayRemoveItemConfirmationPage(item.id)
      actionItem(call.url, Text(text), Some(text))
    }

    val text = messages("declaration.summary.item", index)
    val topMargin = if (index > 1) 6 else 2

    Some(
      SummaryListRow(
        Key(HtmlContent(s"""<strong class="govuk-heading-s govuk-!-margin-top-$topMargin govuk-!-margin-bottom-3">$text</strong>""")),
        classes = s"govuk-summary-list__row--no-border item-$index-heading",
        actions = if (actionsEnabled) Some(Actions(items = List(removeItem))) else None
      )
    )
  }

  private def procedureCode(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.procedureCodes.flatMap(_.procedureCode).map { procedureCode =>
      SummaryListRow(
        key("item.procedureCode"),
        value(procedureCode),
        classes = s"item-$index-procedure-code",
        changeLink(ProcedureCodesController.displayPage(item.id), "item.procedureCode", actionsEnabled, Some(index))
      )
    }

  private def additionalProcedureCodes(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.procedureCodes.map { procedureCodes =>
      SummaryListRow(
        key("item.additionalProcedureCodes"),
        value(procedureCodes.additionalProcedureCodes.mkString(" ")),
        classes = s"item-$index-additional-procedure-codes",
        changeLink(AdditionalProcedureCodesController.displayPage(item.id), "item.additionalProcedureCodes", actionsEnabled, Some(index))
      )
    }

  private def fiscalInformation(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.fiscalInformation.map { fiscalInformation =>
      SummaryListRow(
        key("item.onwardSupplyRelief"),
        value(fiscalInformation.onwardSupplyRelief),
        classes = s"item-$index-onward-supply-relief",
        changeLink(FiscalInformationController.displayPage(item.id), "item.onwardSupplyRelief", actionsEnabled, Some(index))
      )
    }

  private def additionalFiscalReferences(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.additionalFiscalReferencesData.map { additionalFiscalReferences =>
      SummaryListRow(
        key("item.VATdetails"),
        valueHtml(additionalFiscalReferences.references.map(_.value).mkString("<br/>")),
        classes = s"item-$index-vat-details",
        changeLink(AdditionalFiscalReferencesController.displayPage(item.id), "item.VATdetails", actionsEnabled, Some(index))
      )
    }

  private def commodityDetails(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.commodityDetails.map { commodityDetails =>
      SummaryListRow(
        key("item.commodityCode"),
        value(commodityDetails.combinedNomenclatureCode.getOrElse("")),
        classes = s"item-$index-commodity-code",
        changeLink(CommodityDetailsController.displayPage(item.id), "item.commodityCode", actionsEnabled, Some(index))
      )
    }

  private def goodsDescription(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.commodityDetails.map { commodityDetails =>
      SummaryListRow(
        key("item.goodsDescription"),
        value(commodityDetails.descriptionOfGoods.getOrElse("")),
        classes = s"item-$index-goods-description",
        changeLink(CommodityDetailsController.displayPage(item.id), "item.goodsDescription", actionsEnabled, Some(index))
      )
    }

  private def dangerousGoodsCode(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.dangerousGoodsCode.map { dangerousGoodsCode =>
      SummaryListRow(
        key("item.unDangerousGoodsCode"),
        value(dangerousGoodsCode.dangerousGoodsCode.getOrElse(messages("site.no"))),
        classes = s"item-$index-dangerous-goods-code",
        changeLink(UNDangerousGoodsCodeController.displayPage(item.id), "item.unDangerousGoodsCode", actionsEnabled, Some(index))
      )
    }

  private def cusCode(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.cusCode.map { cusCode =>
      SummaryListRow(
        key("item.cusCode"),
        value(cusCode.cusCode.getOrElse(messages("site.no"))),
        classes = s"item-$index-cus-code",
        changeLink(CusCodeController.displayPage(item.id), "item.cusCode", actionsEnabled, Some(index))
      )
    }

  private def nactCodes(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.nactCodes.map { nactCodes =>
      SummaryListRow(
        key("item.nationalAdditionalCodes"),
        value(nactCodes.map(_.nactCode).mkString(", ")),
        classes = s"item-$index-national-additional-codes",
        changeLink(NactCodeSummaryController.displayPage(item.id), "item.nationalAdditionalCodes", actionsEnabled, Some(index))
      )
    }

  private def nactExemptionCode(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.nactExemptionCode.map { nactExemptionCode =>
      SummaryListRow(
        key("item.zeroRatedForVat"),
        valueKey(s"declaration.summary.item.zeroRatedForVat.${nactExemptionCode.nactCode}"),
        classes = s"item-$index-zero-rated-for-vat",
        changeLink(ZeroRatedForVatController.displayPage(item.id), "item.zeroRatedForVat", actionsEnabled, Some(index))
      )
    }

  private def statisticalValue(item: ExportItem, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.statisticalValue.map { statisticalValue =>
      SummaryListRow(
        key("item.itemValue"),
        value(statisticalValue.statisticalValue),
        classes = s"item-$index-item-value",
        changeLink(StatisticalValueController.displayPage(item.id), "item.itemValue", actionsEnabled, Some(index))
      )
    }

  private def grossWeight(item: ExportItem, addTopMargin: Boolean, actionsEnabled: Boolean, index: Int)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    item.commodityMeasure.map { commodityMeasure =>
      SummaryListRow(
        if (addTopMargin) keyForEmptyAttrAfterAttrWithMultipleRows("item.grossWeight") else key("item.grossWeight"),
        value(commodityMeasure.grossMass.getOrElse("")),
        classes = s"govuk-summary-list__row--no-border item-$index-gross-weight",
        changeLink(CommodityMeasureController.displayPage(item.id), "item.grossWeight", actionsEnabled, Some(index))
      )
    }

  private def netWeight(item: ExportItem, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    item.commodityMeasure.map { commodityMeasure =>
      SummaryListRow(key("item.netWeight"), value(commodityMeasure.netMass.getOrElse("")), classes = s"item-$index-net-weight")
    }

  private def supplementaryUnits(declaration: ExportsDeclaration, item: ExportItem, actionsEnabled: Boolean, index: Int)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    if (!isStandardOrSupplementary(declaration)) None
    else
      item.commodityMeasure.flatMap {
        _.supplementaryUnits.map { supplementaryUnits =>
          SummaryListRow(
            key("item.supplementaryUnits"),
            value(supplementaryUnits),
            classes = s"item-$index-supplementary-units",
            changeLink(SupplementaryUnitsController.displayPage(item.id), "item.supplementaryUnits", actionsEnabled, Some(index))
          )
        }
      }
}
