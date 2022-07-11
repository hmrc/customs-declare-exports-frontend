/*
 * Copyright 2022 HM Revenue & Customs
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

import config.featureFlags.{SecureMessagingInboxConfig, SfusConfig}
import models.declaration.submissions.EnhancedStatus.{uploadFilesStatuses, _}
import models.declaration.submissions.RequestType.CancellationRequest
import models.declaration.submissions.{NotificationSummary, Submission}
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.gds.{linkButton, paragraphBody}
import views.html.components.upload_files_partial_for_timeline

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}

case class TimelineEvent(title: String, dateTime: ZonedDateTime, content: Option[Html])

@Singleton
class TimelineEvents @Inject() (
  linkButton: linkButton,
  paragraphBody: paragraphBody,
  secureMessagingInboxConfig: SecureMessagingInboxConfig,
  sfusConfig: SfusConfig,
  uploadFilesPartialForTimeline: upload_files_partial_for_timeline
) {
  def apply(submission: Submission)(implicit messages: Messages): Seq[TimelineEvent] = {

    val allNotificationSummaries = submission.actions.flatMap { action =>
      val summaries = action.notifications.fold(Seq.empty[NotificationSummary])(identity)
      if (action.requestType != CancellationRequest) summaries
      else summaries ++ Seq(NotificationSummary(UUID.randomUUID, action.requestTimestamp, REQUESTED_CANCELLATION))
    }.sorted

    val notificationSummaries =
      if (!allNotificationSummaries.exists(_.enhancedStatus == CANCELLED)) allNotificationSummaries
      else allNotificationSummaries.filterNot(_.enhancedStatus == CUSTOMS_POSITION_GRANTED)

    val IndexToMatchForUploadFilesContent = notificationSummaries.indexWhere(_.enhancedStatus in uploadFilesStatuses)
    val IndexToMatchForViewQueriesContent = notificationSummaries.indexWhere(_.enhancedStatus == QUERY_NOTIFICATION_MESSAGE)
    val IndexToMatchForFixResubmitContent = notificationSummaries.indexWhere(_.enhancedStatus == ERRORS)

    notificationSummaries.zipWithIndex.map { case (notificationSummary, index) =>
      val messageKey = s"submission.enhancedStatus.timeline.content.${notificationSummary.enhancedStatus}"
      val bodyContent = if (messages.isDefinedAt(messageKey)) paragraphBody(messages(messageKey)) else HtmlFormat.empty

      val actionContent = index match {
        case IndexToMatchForFixResubmitContent => fixAndResubmitContent(submission.uuid)

        case IndexToMatchForUploadFilesContent if sfusConfig.isSfusUploadEnabled && IndexToMatchForFixResubmitContent < 0 =>
          uploadFilesContent(submission.mrn, isIndex1Primary(IndexToMatchForUploadFilesContent, IndexToMatchForViewQueriesContent))

        case IndexToMatchForViewQueriesContent =>
          val noDmsrejNotification = IndexToMatchForFixResubmitContent < 0
          val dmsqryMoreRecentThanDmsdoc = isIndex1Primary(IndexToMatchForViewQueriesContent, IndexToMatchForUploadFilesContent)
          viewQueriesContent(noDmsrejNotification && dmsqryMoreRecentThanDmsdoc)

        case _ => HtmlFormat.empty
      }

      val content = new Html(List(bodyContent, actionContent))

      TimelineEvent(
        title = EnhancedStatusHelper.asTimelineTitle(notificationSummary),
        dateTime = notificationSummary.dateTimeIssued,
        content = if (content.body.isEmpty) None else Some(content)
      )
    }
  }

  private def isIndex1Primary(index1: Int, index2: Int): Boolean = index2 < 0 || index1 < index2

  private def fixAndResubmitContent(declarationID: String)(implicit messages: Messages): Html = {
    val call = controllers.routes.RejectedNotificationsController.displayPage(declarationID)
    linkButton("submissions.declarationDetails.fix.resubmit.button", call)
  }

  private def uploadFilesContent(mrn: Option[String], isPrimary: Boolean)(implicit messages: Messages): Html =
    uploadFilesPartialForTimeline(mrn, isPrimary)

  private def viewQueriesContent(isPrimary: Boolean)(implicit messages: Messages): Html =
    linkButton(
      "submissions.declarationDetails.view.queries.button",
      Call("GET", secureMessagingInboxConfig.sfusInboxLink),
      if (isPrimary) "govuk-button" else "govuk-button govuk-button--secondary"
    )
}
