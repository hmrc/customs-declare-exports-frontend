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

import connectors.CodeListConnector
import controllers.routes.SubmissionsController
import models.ExportsDeclaration
import models.declaration.notifications.NotificationError
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import ActionItemBuilder.actionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.components.gds.paragraphBody

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationErrorHelper @Inject() (codeListConnector: CodeListConnector, paragraphBody: paragraphBody) {
  import NotificationErrorHelper._

  def createSummaryList(declaration: ExportsDeclaration, isAmendment: Boolean, errors: Seq[NotificationError])(
    implicit messages: Messages
  ): SummaryList = {
    val errorRows = createRowsFromErrors(declaration, isAmendment, errors)
    val groupedErrorRows = groupRowsByErrorCode(errorRows)
    val redactedErrorRows = removeRepeatFieldNameAndDescriptions(groupedErrorRows).flatten

    SummaryList(redactedErrorRows.zipWithIndex.map { case (errorRow, index) =>
      createSummaryListRow(errorRow, index)
    })
  }

  def formattedErrorDescription(notificationError: NotificationError)(implicit messages: Messages): List[Html] = {
    val description = codeListConnector
      .getDmsErrorCodesMap(messages.lang.toLocale)
      .get(notificationError.validationCode)
      .map(dmsErrorCode => dmsErrorCode.description)
      .getOrElse(messages("error.unknown"))

    if (description.trim.isEmpty)
      List(Text("").asHtml)
    else {
      val desc = description.last match {
        case '.' | '?' | '!'           => description
        case _ if description.nonEmpty => s"$description."
        case _                         => ""
      }

      desc
        .split("\n")
        .toList
        .flatMap(text => List(Text(text).asHtml, Html("<br>")))
        .filter(_.body.nonEmpty)
        .dropRight(1)
    }
  }

  def groupRowsByErrorCode(rows: ErrorRows): List[ErrorRows] =
    rows.foldLeft(List.empty[ErrorRows]) { (acc, row) =>
      acc.lastOption match {
        case Some(lastGrouping) if lastGrouping.last.code == row.code =>
          val newLastGrouping = lastGrouping.:+(row)
          acc.dropRight(1).:+(newLastGrouping)
        case _ => acc.:+(List(row))
      }
    }

  def removeRepeatFieldNameAndDescriptions(groupedErrorRows: List[ErrorRows]): List[ErrorRows] =
    groupedErrorRows.map { errorRows =>
      if (errorRows.forall(_.action.isEmpty)) {
        // no change links in group so just allow first item to have fieldName & description
        errorRows.head :: errorRows.tail.map(row => row.removeFieldNameAndDescription())
      } else {
        // some change links in group so only allow the first item with a change link to have fieldName & description
        val redactedWithFlag = errorRows.foldLeft((List.empty[ErrorRow], false)) { (acc, row) =>
          acc match {
            case (_, true) => (acc._1 :+ row.removeFieldNameAndDescription(), true)
            case _ =>
              if (row.action.isEmpty)
                (acc._1 :+ row.removeFieldNameAndDescription(), false)
              else
                (acc._1 :+ row, true)
          }
        }

        redactedWithFlag._1
      }
    }

  private def errorChangeAction(notificationError: NotificationError, declaration: ExportsDeclaration, isAmendment: Boolean)(
    implicit messages: Messages
  ): Option[Actions] = {
    def constructChangeLinkAction(call: Call): Actions = {
      val errorPattern = notificationError.pointer.map(_.pattern).getOrElse("")
      val errorMessage = messages(s"dmsError.${notificationError.validationCode}.title")
      val url = SubmissionsController.amendErrors(declaration.id, errorPattern, errorMessage, isAmendment, RedirectUrl(call.url)).url
      val action = actionItem(
        href = url,
        content = Text(messages("site.change")),
        visuallyHiddenText = Some(notificationError.pointer.map(p => messages(p.messageKey, p.sequenceArgs: _*)).getOrElse(""))
      )

      Actions(items = Seq(action))
    }

    PointerHelper
      .getChangeLinkCall(notificationError.pointer, declaration)
      .map(call => constructChangeLinkAction(call))
  }

  private def createSummaryListRow(errorRow: ErrorRow, index: Int)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = HtmlContent(errorRow.fieldName.getOrElse("")), classes = s"rejected-field-name rejected_notifications-row-$index-name"),
      value = Value(
        content = HtmlContent(
          HtmlFormat.fill(
            List(paragraphBody(errorRow.title, classes = "govuk-heading-s")) ++
              errorRow.description ++
              List(paragraphBody(messages("rejected.notification.description.format", errorRow.code), classes = "govuk-body-s"))
          )
        ),
        classes = s"rejected_notifications-row-$index-description"
      ),
      actions = errorRow.action
    )

  private def createRowsFromErrors(declaration: ExportsDeclaration, isAmendment: Boolean, errors: Seq[NotificationError])(
    implicit messages: Messages
  ): ErrorRows =
    errors.map { notificationError =>
      val maybeFieldName = notificationError.pointer.map(p => messages(p.messageKey, p.sequenceArgs: _*))
      val maybeAction = errorChangeAction(notificationError, declaration, isAmendment)
      val code = notificationError.validationCode

      ErrorRow(
        code,
        maybeFieldName,
        messages(s"dmsError.${notificationError.validationCode}.title"),
        formattedErrorDescription(notificationError),
        maybeAction
      )
    }.toList
}

object NotificationErrorHelper {

  case class ErrorRow(code: String, fieldName: Option[String], title: String, description: List[Html], action: Option[Actions]) {
    def removeFieldNameAndDescription(): ErrorRow = this.copy(fieldName = None, description = List.empty)
  }

  private type ErrorRows = List[ErrorRow]
}
