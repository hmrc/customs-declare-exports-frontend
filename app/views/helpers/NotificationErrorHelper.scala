/*
 * Copyright 2021 HM Revenue & Customs
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

import config.featureFlags.ChangeErrorLinkConfig
import connectors.CodeListConnector
import controllers.routes
import models.declaration.notifications.NotificationError
import models.ExportsDeclaration
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import views.components.gds.ActionItemBuilder._
import views.html.components.gds.{heading, _}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationErrorHelper @Inject()(
  codeListConnector: CodeListConnector,
  changeErrorLinkConfig: ChangeErrorLinkConfig,
  heading: heading,
  paragraphBody: paragraphBody
) {
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

  def errorChangeAction(notificationError: NotificationError, declaration: ExportsDeclaration)(implicit messages: Messages) = {
    def constructChangeLinkAction(call: Call) = {
      val errorPattern = notificationError.pointer.map(_.pattern).getOrElse("")
      val errorMessage = messages(s"dmsError.${notificationError.validationCode}.title")
      val url = routes.SubmissionsController.amendErrors(declaration.id, call.url, errorPattern, errorMessage).url
      val action = actionItem(
        href = url,
        content = Text(messages("site.change")),
        visuallyHiddenText = Some(notificationError.pointer.map(p => messages(p.messageKey, p.sequenceArgs: _*)).getOrElse(""))
      )

      Actions(items = Seq(action))
    }

    if (changeErrorLinkConfig.isEnabled) {
      PointerHelper
        .getChangeLinkCall(notificationError.pointer, declaration)
        .fold(Some(Actions(items = Seq.empty)))(call => Some(constructChangeLinkAction(call)))
    } else None
  }

  def createSummaryListRow(declaration: ExportsDeclaration, notificationError: NotificationError, index: Int)(
    implicit messages: Messages
  ): SummaryListRow = SummaryListRow(
    key = Key(
      content = HtmlContent(notificationError.pointer.map(p => messages(p.messageKey, p.sequenceArgs: _*)).getOrElse("")),
      classes = s"rejected-field-name rejected_notifications-row-$index-name"
    ),
    value = Value(
      content = HtmlContent(
        HtmlFormat.fill(
          List(paragraphBody(messages(s"dmsError.${notificationError.validationCode}.title"), classes = "govuk-heading-s")) ++
            formattedErrorDescription(notificationError) ++
            List(paragraphBody(messages("rejected.notification.description.format", notificationError.validationCode), classes = "govuk-body-s"))
        )
      ),
      classes = s"rejected_notifications-row-$index-description"
    ),
    actions = errorChangeAction(notificationError, declaration)
  )

  def createSummaryList(declaration: ExportsDeclaration, errors: Seq[NotificationError])(implicit messages: Messages): SummaryList = SummaryList(
    errors.zipWithIndex.map {
      case (notificationError, index) => createSummaryListRow(declaration, notificationError, index)
    }
  )
}
