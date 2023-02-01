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

import models.declaration.submissions.EnhancedStatus.{CUSTOMS_POSITION_DENIED, EnhancedStatus, PENDING, QUERY_NOTIFICATION_MESSAGE}
import models.declaration.submissions.{NotificationSummary, Submission, SubmissionAction}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.{Key, SummaryListRow, Text, Value}

object EnhancedStatusHelper {

  def asText(submission: Submission)(implicit messages: Messages): String =
    asText(submission.latestEnhancedStatus.fold(PENDING)(identity))

  def asText(status: EnhancedStatus)(implicit messages: Messages): String =
    messages(s"submission.enhancedStatus.${status.toString}")

  def asTimelineTitle(notification: NotificationSummary)(implicit messages: Messages): String =
    if (notification.enhancedStatus != CUSTOMS_POSITION_DENIED) asText(notification.enhancedStatus)
    else messages("submission.enhancedStatus.timeline.title.CUSTOMS_POSITION_DENIED")

  def extractNotificationRows(maybeSubmission: Option[Submission])(implicit messages: Messages): Seq[SummaryListRow] =
    maybeSubmission.map { submission =>
      val actions = submission.actions.filter {
        case _: SubmissionAction => true
        case _                   => false
      }
      actions flatMap {
        _.notifications.map {
          _.map { notification =>
            SummaryListRow(
              classes = s"${notification.enhancedStatus.toString.toLowerCase}-row",
              key = Key(content = Text(asText(notification.enhancedStatus))),
              value = Value(content = Text(ViewDates.formatDateAtTime(notification.dateTimeIssued)))
            )
          }
        }.getOrElse(List.empty)
      }
    }.getOrElse(List.empty)

  def hasQueryNotificationMessageStatus(submission: Submission): Boolean =
    submission.actions.exists {
      case SubmissionAction(_, _, notifications) =>
        notifications.exists(_.exists(_.enhancedStatus == QUERY_NOTIFICATION_MESSAGE))
      case _ => false
    }
}
