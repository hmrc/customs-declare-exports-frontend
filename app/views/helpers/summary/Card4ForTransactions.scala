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

import controllers.section4.routes._
import controllers.section5.routes.ItemsSummaryController
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.helpers.summary.SummaryHelper.hasTransactionData
import views.html.summary.summary_card

import javax.inject.{Inject, Singleton}

@Singleton
class Card4ForTransactions @Inject() (summaryCard: summary_card, documentsHelper: DocumentsHelper) extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html =
    if (hasTransactionData(declaration)) content(declaration, actionsEnabled) else HtmlFormat.empty

  // Called by the Mini CYA page
  override def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    summaryCard(card(4), rows(declaration, actionsEnabled))

  override def backLink(implicit request: JourneyRequest[_]): Call = PreviousDocumentsSummaryController.displayPage

  override def continueTo(implicit request: JourneyRequest[_]): Call = ItemsSummaryController.displayItemsSummaryPage

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummarySection] = {
    val maybeExchangeRate: Option[String] = declaration.totalNumberOfItems.flatMap(_.exchangeRate)
    List(
      maybeSummarySection(
        List(
          totalAmountInvoiced(declaration, maybeExchangeRate, actionsEnabled),
          exchangeRate(maybeExchangeRate),
          totalPackage(declaration, actionsEnabled),
          natureOfTransaction(declaration, actionsEnabled)
        )
      ),
      documentsHelper.maybeSummarySection(declaration, actionsEnabled)
    ).flatten
  }

  private def totalAmountInvoiced(declaration: ExportsDeclaration, maybeExchangeRate: Option[String], actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    if (!(declaration.isType(STANDARD) || declaration.isType(SUPPLEMENTARY))) None
    else {
      val call =
        if (declaration.isInvoiceAmountLessThan100000) InvoiceAndExchangeRateChoiceController.displayPage
        else InvoiceAndExchangeRateController.displayPage

      Some(
        SummaryListRow(
          key("transaction.itemAmount"),
          value(totalAmountInvoicedValue(declaration)),
          classes = s"""${maybeExchangeRate.fold("")(_ => "govuk-summary-list__row--no-border ")}item-amount""",
          changeLink(call, "transaction.itemAmount", actionsEnabled)
        )
      )
    }

  private def exchangeRate(maybeExchangeRate: Option[String])(implicit messages: Messages): Option[SummaryListRow] =
    maybeExchangeRate.map { exchangeRate =>
      SummaryListRow(key("transaction.exchangeRate"), value(exchangeRate), classes = "exchange-rate")
    }

  private def totalPackage(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.totalNumberOfItems.flatMap(_.totalPackage).map { totalNoOfPackages =>
      SummaryListRow(
        key("transaction.totalNoOfPackages"),
        value(totalNoOfPackages),
        classes = "total-packages",
        changeLink(TotalPackageQuantityController.displayPage, "transaction.totalNoOfPackages", actionsEnabled)
      )
    }

  private def natureOfTransaction(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.natureOfTransaction.map { natureOfTransaction =>
      SummaryListRow(
        key("transaction.natureOfTransaction"),
        valueKey(s"declaration.summary.transaction.natureOfTransaction.${natureOfTransaction.natureType}"),
        classes = "nature-of-transaction",
        changeLink(NatureOfTransactionController.displayPage, "transaction.natureOfTransaction", actionsEnabled)
      )
    }

  private def totalAmountInvoicedValue(declaration: ExportsDeclaration)(implicit messages: Messages): String =
    if (declaration.isInvoiceAmountLessThan100000) messages("declaration.totalAmountInvoiced.value.lessThan100000")
    else {
      val currencyCode = declaration.totalNumberOfItems.flatMap(_.totalAmountInvoicedCurrency).getOrElse("")
      val totalAmountInvoiced = declaration.totalNumberOfItems.flatMap(_.totalAmountInvoiced).getOrElse("")
      s"$currencyCode $totalAmountInvoiced"
    }
}
