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

import config.featureFlags.{SecureMessagingInboxConfig, SfusConfig}
import controllers.routes.{AmendDeclarationController, RejectedNotificationsController}
import models.declaration.submissions.EnhancedStatus.{uploadFilesStatuses, _}
import models.declaration.submissions.RequestType.{AmendmentRequest, CancellationRequest}
import models.declaration.submissions.{NotificationSummary, RequestType, Submission}
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.gds.{link, linkButton, paragraphBody}
import views.html.components.upload_files_partial_for_timeline

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}

case class TimelineEvent(title: String, dateTime: ZonedDateTime, content: Option[Html])

case class NotificationEvent(actionId: String, requestType: RequestType, notificationSummary: NotificationSummary)

object NotificationEvent {

  implicit val ordering: Ordering[NotificationEvent] =
    Ordering.fromLessThan[NotificationEvent] { (a, b) =>
      b.notificationSummary.dateTimeIssued.isBefore(a.notificationSummary.dateTimeIssued)
    }
}

@Singleton
class TimelineEvents @Inject() (
  link: link,
  linkButton: linkButton,
  paragraphBody: paragraphBody,
  secureMessagingInboxConfig: SecureMessagingInboxConfig,
  sfusConfig: SfusConfig,
  uploadFilesPartialForTimeline: upload_files_partial_for_timeline
) {
  def apply(submission: Submission)(implicit messages: Messages): Seq[TimelineEvent] = {

    val notificationEvents = createNotificationEvents(submission)

    val hasAmendmentRejectedAsLatest = amendmentRejectedAsLatest(submission)

    val IndexToMatchForUploadFilesContent = notificationEvents.indexWhere(_.notificationSummary.enhancedStatus in uploadFilesStatuses)
    val IndexToMatchForViewQueriesContent = notificationEvents.indexWhere(_.notificationSummary.enhancedStatus == QUERY_NOTIFICATION_MESSAGE)
    val IndexToMatchForFixResubmitContent = notificationEvents.indexWhere(_.notificationSummary.enhancedStatus == ERRORS)

    notificationEvents.zipWithIndex.map { case (notificationEvent, index) =>
      val messageKey = s"submission.enhancedStatus.timeline.content.${notificationEvent.notificationSummary.enhancedStatus}"
      val bodyContent = if (messages.isDefinedAt(messageKey)) paragraphBody(messages(messageKey)) else HtmlFormat.empty

      val actionContent = index match {
        case IndexToMatchForFixResubmitContent if notificationEvent.requestType != AmendmentRequest || hasAmendmentRejectedAsLatest =>
          val actionId = if (hasAmendmentRejectedAsLatest) Some(notificationEvent.actionId) else None
          fixAndResubmitContent(submission.uuid, actionId)

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
        title = EnhancedStatusHelper.asTimelineTitle(notificationEvent),
        dateTime = notificationEvent.notificationSummary.dateTimeIssued,
        content = if (content.body.isEmpty) None else Some(content)
      )
    }
  }

  private def amendmentRejectedAsLatest(submission: Submission): Boolean =
    submission.latestAction.exists { action =>
      val isAmendmentRejected = action.requestType == AmendmentRequest
      val hasLatestErrorNotification = action.notifications.headOption.exists(_.headOption.exists(_.enhancedStatus == ERRORS))
      submission.blockAmendments == false && isAmendmentRejected && hasLatestErrorNotification
    }

  private def createNotificationEvents(submission: Submission): Seq[NotificationEvent] =
    submission.actions.flatMap { action =>
      val events = action.notifications.fold(Seq.empty[NotificationEvent]) {
        _.map(NotificationEvent(action.id, action.requestType, _))
      }
      if (action.requestType != CancellationRequest) events
      else {
        val cancellationRequest = List(
          NotificationEvent(action.id, CancellationRequest, NotificationSummary(UUID.randomUUID, action.requestTimestamp, REQUESTED_CANCELLATION))
        )
        events.filter(_.notificationSummary.enhancedStatus == CUSTOMS_POSITION_DENIED) ++ cancellationRequest
      }
    }.sorted

  private def isIndex1Primary(index1: Int, index2: Int): Boolean = index2 < 0 || index1 < index2

  private def fixAndResubmitContent(id: String, actionIdWhenRejectedAmend: Option[String])(implicit messages: Messages): Html =
    actionIdWhenRejectedAmend.fold {
      val fixAndResubmit = RejectedNotificationsController.displayPage(id)
      linkButton("declaration.details.fix.resubmit.button", fixAndResubmit)
    } { actionId =>
      val fixAndResubmit = RejectedNotificationsController.amendmentRejected(id, actionId)
      val button = linkButton("declaration.details.fix.resubmit.button", fixAndResubmit)
      val cancelUrl = AmendDeclarationController.submit("cancel")
      val cancelLink = link(messages("declaration.details.cancel.amendment"), cancelUrl, id = Some("cancel-amendment"))
      Html(s"""<div class="govuk-button-group">${button.toString()}${cancelLink.toString()}</div>""")
    }

  private def uploadFilesContent(mrn: Option[String], isPrimary: Boolean)(implicit messages: Messages): Html =
    uploadFilesPartialForTimeline(mrn, isPrimary)

  private def viewQueriesContent(isPrimary: Boolean)(implicit messages: Messages): Html =
    linkButton(
      "declaration.details.view.queries.button",
      Call("GET", secureMessagingInboxConfig.sfusInboxLink),
      if (isPrimary) "govuk-button" else "govuk-button govuk-button--secondary"
    )
}
