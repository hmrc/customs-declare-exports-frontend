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
import controllers.amendments.routes.AmendmentDetailsController
import controllers.summary.routes.SubmissionController
import controllers.timeline.routes.RejectedNotificationsController
import forms.section1.AdditionalDeclarationType.{AdditionalDeclarationType, SUPPLEMENTARY_SIMPLIFIED}
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType._
import models.declaration.submissions.{Action, NotificationSummary, RequestType, Submission}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import views.helpers.NotificationEvent.maxSecondsBetweenClearedAndArrived
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

  val maxSecondsBetweenClearedAndArrived = 10L
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
) extends Logging {
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

    val timelineEvents = notificationEvents.zipWithIndex.map { case (notificationEvent, index) =>
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
    addDeclarationSubmittedEvent(submission, timelineEvents)
  }
  // scalastyle:on

  private def addDeclarationSubmittedEvent(submission: Submission, timelineEvents: Seq[TimelineEvent])(
    implicit messages: Messages
  ): Seq[TimelineEvent] =
    submission.actions.find(_.requestType == SubmissionRequest).fold(timelineEvents) { action =>
      timelineEvents :+ TimelineEvent(EnhancedStatusHelper.asText(RECEIVED), action.requestTimestamp, None)
    }

  private def bodyContent(notificationEvent: NotificationEvent, declarationType: AdditionalDeclarationType)(implicit messages: Messages): Html =
    if (declarationType == SUPPLEMENTARY_SIMPLIFIED && notificationEvent.notificationSummary.enhancedStatus == CLEARED) HtmlFormat.empty
    else {
      val messageKey = s"submission.enhancedStatus.timeline.content.${notificationEvent.notificationSummary.enhancedStatus}"
      if (messages.isDefinedAt(messageKey)) paragraphBody(messages(messageKey)) else HtmlFormat.empty
    }

  private val amendmentRequests = List(AmendmentRequest, ExternalAmendmentRequest)
  private val requestTypesForReceived = amendmentRequests :+ SubmissionRequest

  private def createNotificationEvents(submission: Submission): Seq[NotificationEvent] = {
    val allEvents = submission.actions.flatMap { action =>
      val events = action.notifications.fold(amendmentEventIfEmpty(action)) { notificationSummaries =>
        val events = notificationSummaries
          .filterNot(_.enhancedStatus == RECEIVED && requestTypesForReceived.contains(action.requestType))
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

    val notificationEvents =
      if (declarationAmendmentsConfig.isEnabled) {
        // Filtering out "AMENDED" notifications generated after "external amendments" (not "user amendments"!).
        allEvents.filterNot { event =>
          event.requestType == SubmissionRequest && event.notificationSummary.enhancedStatus == AMENDED
        }
      } else allEvents.filterNot(_.requestType == ExternalAmendmentRequest)

    clearedMustBeAfterArrivedOnTimeline(notificationEvents)
  }

  private def amendmentEvent(action: Action): NotificationEvent = {
    val notificationSummary = NotificationSummary(UUID.randomUUID, action.requestTimestamp.truncatedTo(SECONDS), AMENDED)
    NotificationEvent(action.id, action.requestType, notificationSummary)
  }

  private def amendmentEventIfEmpty(action: Action): Seq[NotificationEvent] =
    if (amendmentRequests.contains(action.requestType)) List(amendmentEvent(action))
    else List.empty[NotificationEvent]

  private val goodsArrivedStatuses = List(GOODS_ARRIVED, GOODS_ARRIVED_MESSAGE)

  private def clearedMustBeAfterArrivedOnTimeline(notificationEvents: Seq[NotificationEvent]): Seq[NotificationEvent] = {
    val goodsArrivedPositionOnTimeline = notificationEvents.indexWhere { event =>
      goodsArrivedStatuses.contains(event.notificationSummary.enhancedStatus)
    }
    if (goodsArrivedPositionOnTimeline == -1) notificationEvents
    else {
      val goodsArrivedIssuedOn = notificationEvents(goodsArrivedPositionOnTimeline).notificationSummary.dateTimeIssued
      notificationEvents.zipWithIndex.find { case (event, position) =>
        event.notificationSummary.enhancedStatus == CLEARED &&
          position > goodsArrivedPositionOnTimeline &&
          SECONDS.between(event.notificationSummary.dateTimeIssued, goodsArrivedIssuedOn) <= maxSecondsBetweenClearedAndArrived
      }
        .fold(notificationEvents) { case (event, position) =>
          notificationEvents.patch(position, Nil, 1).patch(goodsArrivedPositionOnTimeline, List(event), 0)
        }
    }
  }

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

  private def fixAndResubmitContent(submission: Submission, maybeAmendmentEventIfLatest: Option[AmendmentEventIfLatest])(
    implicit messages: Messages
  ): Html =
    maybeAmendmentEventIfLatest.fold {
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

      val cancelLink =
        amendmentEventAsLatest.action.decId.fold {
          logger.warn(s"Failed/rejected amendment for submission(${submission.uuid}) cannot be cancelled due to missing action.decId")
          HtmlFormat.empty
        } { declarationId =>
          val cancelUrl = SubmissionController.cancelAmendment(declarationId)
          link(messages("declaration.details.cancel.amendment"), cancelUrl, id = Some("cancel-amendment"))
        }

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
