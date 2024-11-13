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

import models.ExportsDeclaration
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.helpers.summary.SummaryHelper.classes

trait SummaryHelper {

  def card(sectionId: Int, hasErrors: Boolean = false)(implicit messages: Messages): Card = {
    require(sectionId >= 1 && sectionId <= classes.length)
    val cardTitle = messages(s"declaration.summary.section.$sectionId")
    val errorClass = if (hasErrors) "govuk-summary-card--error " else ""
    Card(Some(CardTitle(Text(cardTitle), classes = s"$errorClass${classes(sectionId - 1)}-card")))
  }

  def changeLink(call: Call, key: String, actionsEnabled: Boolean, maybeIndex: Option[Int] = None)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = messages(s"declaration.summary.$key.change", maybeIndex.getOrElse(""))
      val content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span>""")
      val actionItem = actionSummaryItem(call.url, content, Some(hiddenText))
      Some(Actions(items = List(actionItem)))
    }

  def key(rowKey: String, classes: String = "")(implicit messages: Messages): Key =
    Key(Text(messages(s"declaration.summary.$rowKey")), classes)

  def maybeSummarySection(
    rowList: Seq[Option[SummaryListRow]],
    maybeHeading: Option[SummarySectionHeading] = None
  ): Option[SummarySection] =
    rowList.flatten match {
      case Nil  => None
      case rows => Some(SummarySection(rows, maybeHeading))
    }

  def value(rowValue: String)(implicit messages: Messages): Value =
    Value(Text(if (rowValue.trim.isEmpty) messages("site.none") else rowValue))

  def valueHtml(rowValue: String)(implicit messages: Messages): Value =
    Value(HtmlContent(if (rowValue.trim.isEmpty) messages("site.none") else rowValue))

  def valueKey(rowValue: String)(implicit messages: Messages): Value = Value(Text(messages(rowValue)))
}

object SummaryHelper {

  val addItemLinkId = "add-item"

  val anchorPlaceholder = "#"

  val classes = Array("references", "parties", "routes-and-locations", "transaction", "items", "transport")

  val continuePlaceholder = "continue-saved-declaration"

  val lrnDuplicateError = FormError("lrn", "declaration.consignmentReferences.lrn.error.notExpiredYet")
  val noItemsError = FormError(addItemLinkId, "declaration.summary.items.none")

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
    val locations = declaration.locations
    val transport = declaration.transport

    locations.supervisingCustomsOffice.isDefined ||
    locations.warehouseIdentification.isDefined ||
    locations.inlandOrBorder.isDefined ||
    locations.inlandModeOfTransportCode.isDefined ||
    transport.expressConsignment.isDefined ||
    transport.transportPayment.isDefined ||
    transport.containers.isDefined ||
    transport.borderModeOfTransportCode.isDefined ||
    transport.meansOfTransportOnDepartureIDNumber.isDefined ||
    transport.meansOfTransportOnDepartureType.isDefined ||
    transport.meansOfTransportCrossingTheBorderIDNumber.isDefined ||
    transport.meansOfTransportCrossingTheBorderType.isDefined ||
    transport.transportCrossingTheBorderNationality.isDefined
  }

  def showItemsCard(declaration: ExportsDeclaration, actionsEnabled: Boolean): Boolean =
    declaration.hasItems || (
      actionsEnabled && (hasTransportData(declaration) || hasRequiredTransactionDataOnNonEmptyItems(declaration))
    )

  private def hasRequiredTransactionDataOnNonEmptyItems(declaration: ExportsDeclaration): Boolean =
    declaration.previousDocuments.isDefined
}
