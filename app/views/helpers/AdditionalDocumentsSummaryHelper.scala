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

import controllers.declaration.routes.AdditionalDocumentsController
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.ExportItem
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class AdditionalDocumentsSummaryHelper @Inject() (govukSummaryList: GovukSummaryList, linkContent: linkContent) {
  def section(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Html = {

    val summaryListRows: Seq[SummaryListRow] = item.additionalDocuments
      .map(_.documents)
      .getOrElse(Seq.empty)
      .zipWithIndex
      .flatMap { case (document, index) =>
        List(documentCode(item, document, actionsEnabled, index + 1), documentIdentifier(document, index + 1)).flatten
      }

    val noRows = summaryListRows.isEmpty

    govukSummaryList(
      SummaryList(
        rows = if (noRows) headingOnNoRows(item, actionsEnabled) else heading +: summaryListRows,
        classes = s"""${if (noRows) "" else "govuk-!-margin-top-4 "}govuk-!-margin-bottom-9 additional-documents-summary"""
      )
    )
  }

  private def heading(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.items.item.additionalDocuments")), classes = "govuk-heading-s"),
      classes = "additional-documents-heading",
      actions = Some(Actions(items = List(ActionItem())))
    )

  private def headingOnNoRows(item: ExportItem, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalDocuments")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = "additional-documents-heading",
        actions = changeHolder(item, None, actionsEnabled)
      )
    )

  private def changeHolder(item: ExportItem, maybeDocument: Option[AdditionalDocument], actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Option[Actions] =
    if (!actionsEnabled) None
    else {
        val hiddenText = maybeDocument.fold(messages("declaration.summary.transaction.previousDocuments.change")) { document =>
        messages(
          "declaration.summary.items.item.additionalDocuments.change",
          document.documentTypeCode.getOrElse(""),
          document.documentIdentifier.getOrElse("")
        )
      }
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(AdditionalDocumentsController.displayPage(item.id).url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }

  private def documentCode(item: ExportItem, document: AdditionalDocument, actionsEnabled: Boolean, index: Int)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    document.documentTypeCode map { documentTypeCode =>
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalDocuments.code"))),
        Value(Text(documentTypeCode)),
        classes = s"govuk-summary-list__row--no-border additional-documents-code-$index",
        actions = changeHolder(item, Some(document), actionsEnabled)
      )
    }

  private def documentIdentifier(document: AdditionalDocument, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    document.documentIdentifier map { documentIdentifier =>
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalDocuments.identifier"))),
        Value(Text(documentIdentifier)),
        classes = s"additional-documents-id-$index"
      )
    }
}
