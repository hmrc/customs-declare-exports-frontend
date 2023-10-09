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

import controllers.declaration.routes.{AdditionalDocumentsController, IsLicenceRequiredController}
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
class AdditionalDocumentSummaryHelper @Inject() (govukSummaryList: GovukSummaryList, linkContent: linkContent) {

  def section(item: ExportItem)(implicit messages: Messages): Html = {
    val hasDocuments = item.additionalDocuments.exists(_.documents.nonEmpty)

    if (hasDocuments || item.isLicenceRequired.isDefined) showSection(item, hasDocuments)
    else Html(s"""<div class="govuk-!-margin-bottom-9"></div>""")
  }

  private def showSection(item: ExportItem, hasDocuments: Boolean)(implicit messages: Messages): Html = {
    lazy val documentRows = item.additionalDocuments.fold(Seq.empty[Option[SummaryListRow]]) { additionalDocuments =>
      additionalDocuments.documents.zipWithIndex.flatMap { case (document, index) =>
        documentCodeAndIdRows(item, document, index + 1)
      }
    }
    val summaryListRows =
      if (hasDocuments) List(heading(item), licenseRow(item)) ++ documentRows
      else List(licenseRow(item), noDocumentsRow(item))

    govukSummaryList(
      SummaryList(
        rows = summaryListRows.flatten,
        classes = s"govuk-!-margin-top-4 govuk-!-margin-bottom-9 item-${item.sequenceId}-additional-documents-summary"
      )
    )
  }

  private def heading(item: ExportItem)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalDocuments")), classes = "govuk-heading-s"),
        classes = s"item-${item.sequenceId}-additional-documents-heading",
        actions = Some(Actions(items = List(ActionItem())))
      )
    )

  private def licenseRow(item: ExportItem)(implicit messages: Messages): Option[SummaryListRow] =
    item.isLicenceRequired.map { isLicenceRequired =>
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.licences"))),
        Value(Text(messages(if (isLicenceRequired) "site.yes" else "site.no"))),
        classes = s"item-${item.sequenceId}-licenses",
        actions = changeLink(item.sequenceId, IsLicenceRequiredController.displayPage(item.id).url, "licences")
      )
    }

  private def noDocumentsRow(item: ExportItem)(implicit messages: Messages): Option[SummaryListRow] =
    // When 'noDocumentsRow' is called we know that 'item.additionalDocuments.documents' is NOT defined.
    // However, to verify if the user has already landed, or not, on the section's pages we also have to check
    // 'item.additionalDocuments'. If defined, and only if defined, we need then to show the 'No documents' row.
    item.additionalDocuments.map { _ =>
      SummaryListRow(
        Key(Text(messages("declaration.summary.items.item.additionalDocuments")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = s"item-${item.sequenceId}-additional-documents-heading",
        actions = changeLink(item.sequenceId, AdditionalDocumentsController.displayPage(item.id).url)
      )
    }

  private def documentCodeAndIdRows(item: ExportItem, document: AdditionalDocument, index: Int)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] =
    List(
      document.documentTypeCode.map { typeCode =>
        SummaryListRow(
          Key(Text(messages("declaration.summary.items.item.additionalDocuments.code"))),
          Value(Text(typeCode)),
          classes = s"""${noBorderForDocumentCode(document)}item-${item.sequenceId}-document-$index-code""",
          actions = changeLink(item.sequenceId, AdditionalDocumentsController.displayPage(item.id).url)
        )
      },
      document.documentIdentifier.map { identifier =>
        SummaryListRow(
          Key(Text(messages("declaration.summary.items.item.additionalDocuments.identifier"))),
          Value(Text(identifier)),
          classes = s"item-${item.sequenceId}-document-$index-identifier",
          actions =
            if (document.documentTypeCode.isDefined) None
            else changeLink(item.sequenceId, AdditionalDocumentsController.displayPage(item.id).url)
        )
      }
    )

  private def noBorderForDocumentCode(document: AdditionalDocument): String =
    document.documentIdentifier.fold("")(_ => "govuk-summary-list__row--no-border ")

  private def changeLink(sequenceId: Int, url: String, key: String = "additionalDocuments")(implicit messages: Messages): Option[Actions] = {
    val hiddenText = messages(s"declaration.summary.items.item.$key.change", sequenceId)
    val content = HtmlContent(linkContent(messages("site.change")))
    val actionItem = actionSummaryItem(url, content, Some(hiddenText))

    Some(Actions(items = List(actionItem)))
  }
}
