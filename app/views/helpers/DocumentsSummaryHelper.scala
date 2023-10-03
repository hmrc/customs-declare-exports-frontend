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

import controllers.declaration.routes.PreviousDocumentsController
import forms.declaration.Document
import models.ExportsDeclaration
import play.api.i18n.Messages
import play.twirl.api.Html
import services.view.HolderOfAuthorisationCodes
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentsSummaryHelper @Inject() (
  govukSummaryList: GovukSummaryList,
  linkContent: linkContent,
  holderOfAuthorisationCodes: HolderOfAuthorisationCodes
) {
  def section(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html = {

    val summaryListRows: Seq[SummaryListRow] = declaration.previousDocuments
      .map(_.documents)
      .getOrElse(Seq.empty)
      .zipWithIndex
      .flatMap { case (document, index) =>
        List(documentTypeCode(document, actionsEnabled, index + 1), documentRef(document, actionsEnabled, index + 1))
      }

    val noHolders = summaryListRows.isEmpty

    govukSummaryList(
      SummaryList(
        rows = if (noHolders) headingOnNoHolders(actionsEnabled) else heading +: summaryListRows,
        classes = s"""${if (noHolders) "" else "govuk-!-margin-top-4 "}govuk-!-margin-bottom-9 authorisation-holders-summary"""
      )
    )
  }

  private def heading(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.parties.holders")), classes = "govuk-heading-s"),
      classes = "authorisation-holder-heading",
      actions = Some(Actions(items = List(ActionItem())))
    )

  private def headingOnNoHolders(actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.parties.holders")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = "authorisation-holder-heading",
        actions = changeHolder(None, actionsEnabled)
      )
    )

  private def changeHolder(maybeDocument: Option[Document], actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = maybeDocument.fold(messages("declaration.summary.parties.holders.empty.change")) { document =>
        messages("declaration.summary.parties.holders.change", document.documentType, document.documentReference)
      }
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(PreviousDocumentsController.displayPage.url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }

  private def documentTypeCode(document: Document, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.parties.holders.type"))),
      Value(Text(holderOfAuthorisationCodes.codeDescription(messages.lang.toLocale, document.documentType))),
      classes = s"govuk-summary-list__row--no-border authorisation-holder-type-$index",
      actions = changeHolder(Some(document), actionsEnabled)
    )

  private def documentRef(document: Document, actionsEnabled: Boolean, index: Int)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.parties.holders.eori"))),
      Value(Text(document.documentReference)),
      classes = s"authorisation-holder-eori-$index",
      actions = changeHolder(Some(document), actionsEnabled)
    )
}
