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

import controllers.declaration.routes.{AdditionalDocumentsController, IsLicenceRequiredController}
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.ExportItem
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}

object AdditionalDocumentsHelper extends SummaryHelper {

  def section(item: ExportItem, hasAdditionalInformation: Boolean, actionsEnabled: Boolean, itemIndex: Int)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] = {
    val hasDocuments = item.additionalDocuments.exists(_.documents.nonEmpty)
    if (hasDocuments || item.isLicenceRequired.isDefined) showSection(item, hasAdditionalInformation, hasDocuments, actionsEnabled, itemIndex)
    else List.empty
  }

  private def showSection(item: ExportItem, hasAdditionalInformation: Boolean, hasDocuments: Boolean, actionsEnabled: Boolean, itemIndex: Int)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] = {
    lazy val documentRows = item.additionalDocuments.fold(Seq.empty[Option[SummaryListRow]]) { additionalDocuments =>
      additionalDocuments.documents.zipWithIndex.flatMap { case (document, index) =>
        documentCodeAndIdRows(item, document, index + 1, actionsEnabled, itemIndex)
      }
    }
    lazy val headingAndRows = List(
      heading(s"item-$itemIndex-additional-documents", "item.additionalDocuments"),
      licenseRow(item, false, actionsEnabled, itemIndex)
    ) ++ documentRows

    lazy val licenseAndNoDocuments =
      List(licenseRow(item, hasAdditionalInformation, actionsEnabled, itemIndex), headingOnNoDocuments(item, actionsEnabled, itemIndex))

    if (hasDocuments) headingAndRows else licenseAndNoDocuments
  }

  private def headingOnNoDocuments(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages): Option[SummaryListRow] =
    // When 'headingOnNoDocuments' is called we know that 'item.additionalDocuments.documents' is NOT defined.
    // However, to verify if the user has already landed, or not, on the section's pages we also have to check
    // 'item.additionalDocuments'. If defined, and only if defined, we need then to show the 'No documents' row.
    item.additionalDocuments.map { _ =>
      SummaryListRow(
        key("item.additionalDocuments"),
        valueKey("site.none"),
        classes = s"item-$itemIndex-additional-documents-heading",
        changeDocuments(item, actionsEnabled, itemIndex)
      )
    }

  private def licenseRow(item: ExportItem, hasAdditionalInformation: Boolean, actionsEnabled: Boolean, itemIndex: Int)(
    implicit messages: Messages
  ): Option[SummaryListRow] =
    item.isLicenceRequired.map { isLicenceRequired =>
      SummaryListRow(
        key("item.licences"),
        valueKey(if (isLicenceRequired) "site.yes" else "site.no"),
        classes = s"item-$itemIndex-licences${if (hasAdditionalInformation) " govuk-!-margin-top-4" else ""}",
        changeLink(IsLicenceRequiredController.displayPage(item.id), "item.licences", actionsEnabled, Some(itemIndex))
      )
    }

  private def documentCodeAndIdRows(item: ExportItem, document: AdditionalDocument, index: Int, actionsEnabled: Boolean, itemIndex: Int)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] =
    List(
      document.documentTypeCode.map { typeCode =>
        SummaryListRow(
          key("item.additionalDocuments.code"),
          value(typeCode),
          classes = s"""${noBorderForDocumentCode(document)}item-$itemIndex-additional-document-$index-code""",
          changeDocuments(item, actionsEnabled, itemIndex)
        )
      },
      document.documentIdentifier.map { identifier =>
        SummaryListRow(
          key("item.additionalDocuments.identifier"),
          value(identifier),
          classes = s"item-$itemIndex-additional-document-$index-identifier",
          document.documentTypeCode.fold(changeDocuments(item, actionsEnabled, itemIndex))(_ => None)
        )
      }
    )

  private def noBorderForDocumentCode(document: AdditionalDocument): String =
    document.documentIdentifier.fold("")(_ => "govuk-summary-list__row--no-border ")

  private def changeDocuments(item: ExportItem, actionsEnabled: Boolean, itemIndex: Int)(implicit messages: Messages): Option[Actions] =
    changeLink(AdditionalDocumentsController.displayPage(item.id), "item.additionalDocuments", actionsEnabled, Some(itemIndex))
}
