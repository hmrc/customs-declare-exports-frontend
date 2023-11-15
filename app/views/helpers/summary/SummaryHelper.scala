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

import models.ExportsDeclaration
import models.declaration.ExportItem
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.helpers.ActionItemBuilder.actionSummaryItem

trait SummaryHelper {

  val classes = Array("references", "parties", "routes-and-locations", "transaction", "items", "transport")

  def card(sectionId: Int)(implicit messages: Messages): Option[Card] = {
    require(sectionId >= 1 && sectionId <= classes.length)
    val cardTitle = messages(s"declaration.summary.section.$sectionId")
    Some(Card(Some(CardTitle(Text(cardTitle), classes = s"${classes(sectionId - 1)}-card"))))
  }

  def changeLink(call: Call, key: String, actionsEnabled: Boolean, maybeIndex: Option[Int] = None)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = messages(s"declaration.summary.$key.change", maybeIndex.getOrElse(""))
      val content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span>""")
      val actionItem = actionSummaryItem(call.url, content, Some(hiddenText))
      Some(Actions(items = List(actionItem)))
    }

  def heading(id: String, key: String)(implicit messages: Messages): Option[SummaryListRow] =
    Some(SummaryListRow(keyForAttrWithMultipleRows(key), classes = s"$id-heading"))

  def key(rowKey: String, classes: String = "")(implicit messages: Messages): Key =
    Key(Text(messages(s"declaration.summary.$rowKey")), classes)

  def keyForEmptyAttrAfterAttrWithMultipleRows(rowKey: String)(implicit messages: Messages): Key =
    keyForAttrWithMultipleRows(rowKey, "div")

  def value(rowValue: String): Value = Value(Text(rowValue))

  def valueHtml(rowValue: String): Value = Value(HtmlContent(rowValue))

  def valueKey(rowValue: String)(implicit messages: Messages): Value = Value(Text(messages(rowValue)))

  private def keyForAttrWithMultipleRows(rowKey: String, tag: String = "strong")(implicit messages: Messages): Key = {
    val key = s"declaration.summary.$rowKey"
    Key(HtmlContent(s"""<$tag class="govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-0">${messages(key)}</$tag>"""))
  }
}

object SummaryHelper {

  def hasItemData(item: ExportItem): Boolean =
    item.procedureCodes.isDefined ||
      item.fiscalInformation.isDefined ||
      item.additionalFiscalReferencesData.isDefined ||
      item.statisticalValue.isDefined ||
      item.commodityDetails.isDefined ||
      item.dangerousGoodsCode.isDefined ||
      item.cusCode.isDefined ||
      item.taricCodes.isDefined ||
      item.nactCodes.isDefined ||
      item.nactExemptionCode.isDefined ||
      item.packageInformation.isDefined ||
      item.commodityMeasure.isDefined ||
      item.additionalInformation.isDefined ||
      item.additionalDocuments.isDefined ||
      item.isLicenceRequired.isDefined

  def hasItemsData(declaration: ExportsDeclaration): Boolean =
    declaration.items.nonEmpty && declaration.items.exists(hasItemData)

  def hasTransactionData(declaration: ExportsDeclaration): Boolean =
    declaration.natureOfTransaction.isDefined ||
      hasRequiredTransactionDataOnNonEmptyItems(declaration) ||
      declaration.totalNumberOfItems.exists(totals =>
        totals.totalAmountInvoiced.isDefined ||
          totals.totalAmountInvoicedCurrency.isDefined ||
          totals.agreedExchangeRate.isDefined ||
          totals.exchangeRate.isDefined ||
          totals.totalPackage.isDefined
      )

  def hasTransportData(declaration: ExportsDeclaration): Boolean = {
    val transport = declaration.transport
    val locations = declaration.locations
    transport.expressConsignment.isDefined ||
    transport.transportPayment.isDefined ||
    transport.containers.isDefined ||
    transport.borderModeOfTransportCode.isDefined ||
    transport.meansOfTransportOnDepartureType.isDefined ||
    transport.meansOfTransportCrossingTheBorderIDNumber.isDefined ||
    transport.meansOfTransportCrossingTheBorderType.isDefined ||
    transport.transportCrossingTheBorderNationality.isDefined ||
    locations.warehouseIdentification.isDefined ||
    locations.supervisingCustomsOffice.isDefined ||
    locations.inlandModeOfTransportCode.isDefined
  }

  def showItemsCard(declaration: ExportsDeclaration, actionsEnabled: Boolean): Boolean =
    hasItemsData(declaration) || (
      actionsEnabled && (hasTransportData(declaration) || hasRequiredTransactionDataOnNonEmptyItems(declaration))
    )

  private def hasRequiredTransactionDataOnNonEmptyItems(declaration: ExportsDeclaration): Boolean =
    declaration.previousDocuments.isDefined
}
