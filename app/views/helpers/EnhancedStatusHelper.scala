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

import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType._
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.{Key, SummaryListRow, Text, Value}

object EnhancedStatusHelper {

  def asText(status: EnhancedStatus)(implicit messages: Messages): String =
    messages(s"submission.enhancedStatus.${status.toString}")

  def asText(submission: Submission)(implicit messages: Messages): String =
    asText(submission.latestEnhancedStatus.fold(PENDING)(identity))

  def asTimelineEvent(event: NotificationEvent)(implicit messages: Messages): String =
    event.requestType match {
      case AmendmentRequest if event.notificationSummary.enhancedStatus == CUSTOMS_POSITION_DENIED =>
        messages("submission.enhancedStatus.timeline.title.amendment.failed")

      case AmendmentRequest if event.notificationSummary.enhancedStatus == CUSTOMS_POSITION_GRANTED =>
        messages("submission.enhancedStatus.timeline.title.amendment.accepted")

      case AmendmentRequest if event.notificationSummary.enhancedStatus == ERRORS =>
        messages("submission.enhancedStatus.timeline.title.amendment.rejected")

      case AmendmentRequest if event.notificationSummary.enhancedStatus == AMENDED =>
        messages("submission.enhancedStatus.timeline.title.amendment.requested")

      case ExternalAmendmentRequest if event.notificationSummary.enhancedStatus == AMENDED =>
        messages("submission.enhancedStatus.timeline.title.amendment.external")

      case _ =>
        val status = event.notificationSummary.enhancedStatus
        if (status != CUSTOMS_POSITION_DENIED) asText(status)
        else messages("submission.enhancedStatus.timeline.title.CUSTOMS_POSITION_DENIED")
    }

  def extractNotificationRows(submission: Submission)(implicit messages: Messages): Seq[SummaryListRow] = {
    val actions = submission.actions.filter(_.requestType == SubmissionRequest)
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
  }

  def hasQueryNotificationMessageStatus(submission: Submission): Boolean =
    submission.actions.exists { action =>
      action.requestType == SubmissionRequest && action.notifications.exists(_.exists(_.enhancedStatus == QUERY_NOTIFICATION_MESSAGE))
    }
}
