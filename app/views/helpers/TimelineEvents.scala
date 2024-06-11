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

import config.featureFlags.{DeclarationAmendmentsConfig, SfusConfig}
import controllers.declaration.amendments.routes.AmendmentDetailsController
import controllers.declaration.routes.SubmissionController
import controllers.routes.RejectedNotificationsController
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{AdditionalDeclarationType, SUPPLEMENTARY_SIMPLIFIED}
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType._
import models.declaration.submissions.{Action, NotificationSummary, RequestType, Submission}
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.gds.{link, linkButton, paragraphBody}
import views.html.components.upload_files_partial_for_timeline

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.SECONDS
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

// scalastyle:off
@Singleton
class TimelineEvents @Inject() (
  link: link,
  linkButton: linkButton,
  paragraphBody: paragraphBody,
  sfusConfig: SfusConfig,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig,
  uploadFilesPartialForTimeline: upload_files_partial_for_timeline
) {
  def apply(submission: Submission, declarationType: AdditionalDeclarationType)(implicit messages: Messages): Seq[TimelineEvent] = {
    val notificationEvents = createNotificationEvents(submission)

    val amendmentFailedIfLatest = getAmendmentFailedIfLatest(submission)

    val IndexToMatchForUploadFilesContent = notificationEvents.indexWhere(_.notificationSummary.enhancedStatus in uploadFilesStatuses)
    val IndexToMatchForViewQueriesContent = notificationEvents.indexWhere(_.notificationSummary.enhancedStatus == QUERY_NOTIFICATION_MESSAGE)

    val IndexToMatchForExternalAmendmentContent = notificationEvents.indexWhere { event =>
      event.requestType == ExternalAmendmentRequest && event.notificationSummary.enhancedStatus == AMENDED
    }
    val IndexToMatchForFixResubmitContent =
      amendmentFailedIfLatest match {
        case Some(AmendmentFailed(_)) => notificationEvents.indexWhere(_.notificationSummary.enhancedStatus == CUSTOMS_POSITION_DENIED)
        case _                        => notificationEvents.indexWhere(_.notificationSummary.enhancedStatus == ERRORS)
      }

    notificationEvents.zipWithIndex.map { case (notificationEvent, index) =>
      val actionContent = index match {
        case IndexToMatchForFixResubmitContent if notificationEvent.requestType != AmendmentRequest || amendmentFailedIfLatest.isDefined =>
          fixAndResubmitContent(submission, amendmentFailedIfLatest)

        case IndexToMatchForExternalAmendmentContent =>
          Html(
            paragraphBody(
              messages("submission.enhancedStatus.timeline.content.external.amendment"),
              "govuk-body govuk-!-margin-bottom-2"
            ).toString + viewAmendmentDetails(notificationEvent.actionId, true).toString
          )

        case IndexToMatchForUploadFilesContent if sfusConfig.isSfusUploadEnabled && IndexToMatchForFixResubmitContent < 0 =>
          uploadFilesContent(submission.mrn, isIndex1Primary(IndexToMatchForUploadFilesContent, IndexToMatchForViewQueriesContent))

        case IndexToMatchForViewQueriesContent =>
          val noDmsrejNotification = IndexToMatchForFixResubmitContent < 0
          val dmsqryMoreRecentThanDmsdoc = isIndex1Primary(IndexToMatchForViewQueriesContent, IndexToMatchForUploadFilesContent)
          viewQueriesContent(noDmsrejNotification && dmsqryMoreRecentThanDmsdoc)

        case _ =>
          val showAmendDetails = notificationEvent.requestType == AmendmentRequest && notificationEvent.notificationSummary.enhancedStatus == AMENDED
          if (showAmendDetails) viewAmendmentDetails(notificationEvent.actionId, false)
          else HtmlFormat.empty
      }

      val content = new Html(List(bodyContent(notificationEvent, declarationType), actionContent))

      TimelineEvent(
        title = EnhancedStatusHelper.asTimelineEvent(notificationEvent),
        dateTime = notificationEvent.notificationSummary.dateTimeIssued,
        content = if (content.body.isEmpty) None else Some(content)
      )
    }
  }
  // scalastyle:on

  private def bodyContent(notificationEvent: NotificationEvent, declarationType: AdditionalDeclarationType)(implicit messages: Messages): Html =
    if (declarationType == SUPPLEMENTARY_SIMPLIFIED && notificationEvent.notificationSummary.enhancedStatus == CLEARED) HtmlFormat.empty
    else {
      val messageKey = s"submission.enhancedStatus.timeline.content.${notificationEvent.notificationSummary.enhancedStatus}"
      if (messages.isDefinedAt(messageKey)) paragraphBody(messages(messageKey)) else HtmlFormat.empty
    }

  private val amendmentRequests = List(AmendmentRequest, ExternalAmendmentRequest)

  private def createNotificationEvents(submission: Submission): Seq[NotificationEvent] = {
    val allEvents = submission.actions.flatMap { action =>
      val events = action.notifications.fold(amendmentEventIfEmpty(action)) { notificationSummaries =>
        val events = notificationSummaries
          .filterNot(summary => amendmentRequests.contains(action.requestType) && summary.enhancedStatus == RECEIVED)
          .map(NotificationEvent(action.id, action.requestType, _))
        if (amendmentRequests.contains(action.requestType)) events :+ amendmentEvent(action) else events
      }

      if (action.requestType != CancellationRequest) events
      else {
        val notificationSummary = NotificationSummary(UUID.randomUUID(), action.requestTimestamp, REQUESTED_CANCELLATION)
        val cancellationRequest = List(NotificationEvent(action.id, CancellationRequest, notificationSummary))
        events.filter(_.notificationSummary.enhancedStatus == CUSTOMS_POSITION_DENIED) ++ cancellationRequest
      }
    }.sorted

    if (declarationAmendmentsConfig.isEnabled) {
      // Filtering out "AMENDED" notifications generated after "external amendments" (not "user amendments"!).
      allEvents.filterNot { event =>
        event.requestType == SubmissionRequest && event.notificationSummary.enhancedStatus == AMENDED
      }
    } else allEvents.filterNot(_.requestType == ExternalAmendmentRequest)
  }

  private def amendmentEvent(action: Action): NotificationEvent = {
    val notificationSummary = NotificationSummary(UUID.randomUUID, action.requestTimestamp.truncatedTo(SECONDS), AMENDED)
    NotificationEvent(action.id, action.requestType, notificationSummary)
  }

  private def amendmentEventIfEmpty(action: Action): Seq[NotificationEvent] =
    if (amendmentRequests.contains(action.requestType)) List(amendmentEvent(action))
    else List.empty[NotificationEvent]

  private abstract class AmendmentEventIfLatest { val action: Action }
  private case class AmendmentFailed(action: Action) extends AmendmentEventIfLatest
  private case class AmendmentRejected(action: Action) extends AmendmentEventIfLatest

  private def getAmendmentFailedIfLatest(submission: Submission): Option[AmendmentEventIfLatest] =
    if (submission.blockAmendments) None
    else
      submission.latestAction.flatMap { latestAction =>
        if (latestAction.requestType != AmendmentRequest) None
        else
          latestAction.notifications.flatMap { notifications =>
            notifications.headOption.flatMap {
              _.enhancedStatus match {
                case CUSTOMS_POSITION_DENIED => Some(AmendmentFailed(latestAction))
                case ERRORS                  => Some(AmendmentRejected(latestAction))
                case _                       => None
              }
            }
          }
      }

  private def fixAndResubmitContent(submission: Submission, amendmentEventIfLatest: Option[AmendmentEventIfLatest])(
    implicit messages: Messages
  ): Html =
    amendmentEventIfLatest.fold {
      linkButton("declaration.details.fix.resubmit.button", RejectedNotificationsController.displayPage(submission.uuid))
    } { amendmentEventAsLatest =>
      val button = amendmentEventAsLatest match {
        case _: AmendmentFailed =>
          linkButton("declaration.details.resubmit.button", SubmissionController.displayResubmitAmendmentPage)

        case _: AmendmentRejected | _ =>
          linkButton(
            "declaration.details.fix.resubmit.button",
            RejectedNotificationsController.displayPageOnUnacceptedAmendment(amendmentEventAsLatest.action.id)
          )
      }

      val cancelUrl = SubmissionController.cancelAmendment
      val cancelLink = link(messages("declaration.details.cancel.amendment"), cancelUrl, id = Some("cancel-amendment"))
      Html(s"""<div class="govuk-button-group">${button.toString()}${cancelLink.toString()}</div>""")
    }

  private def isIndex1Primary(index1: Int, index2: Int): Boolean = index2 < 0 || index1 < index2

  private def uploadFilesContent(mrn: Option[String], isPrimary: Boolean)(implicit messages: Messages): Html =
    uploadFilesPartialForTimeline(mrn, isPrimary)

  private def viewAmendmentDetails(actionId: String, isExternalAmendment: Boolean)(implicit messages: Messages): Html = {
    val key =
      if (isExternalAmendment) "declaration.details.view.external.amendment.details"
      else "declaration.details.view.amendment.details"

    link(messages(key), AmendmentDetailsController.displayPage(actionId))
  }

  private def viewQueriesContent(isPrimary: Boolean)(implicit messages: Messages): Html =
    linkButton(
      "declaration.details.view.queries.button",
      Call("GET", sfusConfig.sfusInboxLink),
      if (isPrimary) "govuk-button" else "govuk-button govuk-button--secondary"
    )
}
