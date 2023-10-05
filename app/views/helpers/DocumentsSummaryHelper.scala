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

import controllers.declaration.routes.PreviousDocumentsSummaryController
import forms.declaration.Document
import models.ExportsDeclaration
import play.api.i18n.Messages
import play.twirl.api.Html
import services.DocumentTypeService
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentsSummaryHelper @Inject() (govukSummaryList: GovukSummaryList, linkContent: linkContent, documentTypeService: DocumentTypeService) {
  def section(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html = {

    val summaryListRows: Seq[SummaryListRow] = declaration.previousDocuments
      .map(_.documents)
      .getOrElse(Seq.empty)
      .zipWithIndex
      .flatMap { case (document, index) =>
        List(documentTypeCode(document, actionsEnabled, index + 1), documentRef(document, index + 1))
      }

    val noRows = summaryListRows.isEmpty

    govukSummaryList(
      SummaryList(
        rows = if (noRows) headingOnNoRows(actionsEnabled) else heading +: summaryListRows,
        classes = s"""${if (noRows) "" else "govuk-!-margin-top-4 "}govuk-!-margin-bottom-9 previous-documents-summary"""
      )
    )
  }

  private def heading(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.transaction.previousDocuments")), classes = "govuk-heading-s"),
      classes = "previous-documents-heading",
      actions = Some(Actions(items = List(ActionItem())))
    )

  private def headingOnNoRows(actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.transaction.previousDocuments")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = "previous-documents-heading",
        actions = changeHolder(actionsEnabled)
      )
    )

  private def changeHolder(actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = messages("declaration.summary.transaction.previousDocuments.change")
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(PreviousDocumentsSummaryController.displayPage.url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }

  private def documentTypeCode(document: Document, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.transaction.previousDocuments.type"))),
      Value(Text(documentTypeService.findByCode(document.documentType).asText)),
      classes = s"govuk-summary-list__row--no-border previous-documents-type-$index",
      actions = changeHolder(actionsEnabled)
    )

  private def documentRef(document: Document, index: Int)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.transaction.previousDocuments.reference"))),
      Value(Text(document.documentReference)),
      classes = s"previous-documents-ref-$index"
    )
}
