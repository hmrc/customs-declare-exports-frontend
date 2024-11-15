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

import controllers.section4.routes.PreviousDocumentsSummaryController
import forms.section4.Document
import models.ExportsDeclaration
import play.api.i18n.Messages
import services.DocumentTypeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentsHelper @Inject() (documentTypeService: DocumentTypeService) extends SummaryHelper {

  def maybeSummarySection(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummarySection] =
    declaration.previousDocuments.map { previousDocuments =>
      val summaryListRows = previousDocuments.documents.zipWithIndex.flatMap { case (document, index) =>
        List(Some(documentTypeCode(document, index + 1, actionsEnabled)), Some(documentRef(document, index + 1)))
      }.flatten

      if (summaryListRows.isEmpty) headingOnNoDocuments(actionsEnabled)
      else SummarySection(summaryListRows, Some(SummarySectionHeading("previous-documents", "transaction.previousDocuments")))
    }

  private def headingOnNoDocuments(actionsEnabled: Boolean)(implicit messages: Messages): SummarySection =
    SummarySection(
      List(
        SummaryListRow(
          key("transaction.previousDocuments"),
          valueKey("site.none"),
          classes = "heading-on-no-data previous-documents-heading",
          changeDocuments(actionsEnabled)
        )
      )
    )

  private def documentTypeCode(document: Document, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key("transaction.previousDocuments.type"),
      value(documentTypeService.findByCode(document.documentType).asText),
      classes = s"govuk-summary-list__row--no-border previous-document-$index-type",
      changeDocuments(actionsEnabled)
    )

  private def documentRef(document: Document, index: Int)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(key("transaction.previousDocuments.reference"), value(document.documentReference), classes = s"previous-document-$index-reference")

  private def changeDocuments(actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    changeLink(PreviousDocumentsSummaryController.displayPage, "transaction.previousDocuments", actionsEnabled)
}
