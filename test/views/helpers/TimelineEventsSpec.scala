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

import base.Injector
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, EnhancedStatus, NotificationSummary, Submission}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import views.declaration.spec.UnitViewSpec
import views.helpers.TimelineEventsSpec.{cancellationDenied, cancellationGranted, cancellationRequestNotConfirmedYet, declarationCancelled}
import views.html.components.gds.{linkButton, paragraphBody}
import views.html.components.upload_files_partial_for_timeline

import java.time.ZonedDateTime
import java.util.UUID

class TimelineEventsSpec extends UnitViewSpec with BeforeAndAfterEach with Injector {

  private val submission = Submission("id", "eori", "lrn", Some("mrn"), Some("ducr"), None, None, Seq.empty, latestDecId = "id")
  private val uploadFilesPartialForTimeline = instanceOf[upload_files_partial_for_timeline]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSfusConfig.isSfusUploadEnabled).thenReturn(true)
    when(mockSecureMessagingInboxConfig.sfusInboxLink).thenReturn("dummyInboxLink")
  }

  private def issued(days: Long): ZonedDateTime = ZonedDateTime.now.plusDays(days)

  private val timelineEvents =
    new TimelineEvents(new linkButton, new paragraphBody, mockSecureMessagingInboxConfig, mockSfusConfig, uploadFilesPartialForTimeline)

  private def genTimelineEvents(notificationSummaries: Seq[NotificationSummary]): Seq[TimelineEvent] = {
    val action = Action("id", SubmissionRequest, issued(0), Some(notificationSummaries), decId = None, versionNo = 1)
    timelineEvents.apply(submission.copy(actions = Seq(action)))
  }

  private def createTimelineFromActions(actions: Seq[Action]): Seq[TimelineEvent] =
    timelineEvents.apply(submission.copy(actions = actions))

  "TimelineEvents" should {

    "transform an empty sequence of Notifications into an empty sequence of TimelineEvent instances" in {
      assert(genTimelineEvents(List.empty[NotificationSummary]).isEmpty)
    }

    "transform an unordered sequence of NotificationSummaries into an ordered sequence of TimelineEvent instances" in {
      val issued1 = issued(1)
      val issued2 = issued(2)
      val issued3 = issued(3)
      val issued4 = issued(4)

      val notifications = List(
        NotificationSummary(UUID.randomUUID, issued2, RECEIVED),
        NotificationSummary(UUID.randomUUID, issued4, UNKNOWN),
        NotificationSummary(UUID.randomUUID, issued1, PENDING),
        NotificationSummary(UUID.randomUUID, issued3, ERRORS)
      )
      val timelineEvents = genTimelineEvents(notifications)

      timelineEvents.size mustBe 4

      timelineEvents(0).dateTime mustBe issued4
      timelineEvents(0).title mustBe messages(s"submission.enhancedStatus.$UNKNOWN")

      timelineEvents(1).dateTime mustBe issued3
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.$ERRORS")

      timelineEvents(2).dateTime mustBe issued2
      timelineEvents(2).title mustBe messages(s"submission.enhancedStatus.$RECEIVED")

      timelineEvents(3).dateTime mustBe issued1
      timelineEvents(3).title mustBe messages(s"submission.enhancedStatus.$PENDING")
    }

    "generate a sequence of TimelineEvent instances from both cancellation requests and submission requests" in {
      val action1 = Action("cancellation", CancellationRequest, issued(2), None, decId = None, 1)

      val notification2 = NotificationSummary(UUID.randomUUID, issued(1), RECEIVED)
      val action2 = Action("submission", SubmissionRequest, issued(0), Some(List(notification2)), None, 1)

      val timelineEvents = createTimelineFromActions(List(action1, action2))

      timelineEvents.size mustBe 2

      timelineEvents(0).dateTime mustBe action1.requestTimestamp
      timelineEvents(0).title mustBe messages(s"submission.enhancedStatus.$REQUESTED_CANCELLATION")

      timelineEvents(1).dateTime mustBe notification2.dateTimeIssued
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.$RECEIVED")
    }

    "generate the expected sequence of TimelineEvent instances when the cancellation request is denied" in {
      val timelineEvents = createTimelineFromActions(cancellationDenied)

      timelineEvents.size mustBe 3
      timelineEvents(0).title mustBe messages("submission.enhancedStatus.timeline.title.CUSTOMS_POSITION_DENIED")
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.$REQUESTED_CANCELLATION")
      timelineEvents(2).title mustBe messages(s"submission.enhancedStatus.$RECEIVED")
    }

    "generate the expected sequence of TimelineEvent instances when the cancellation request is granted" in {
      val timelineEvents = createTimelineFromActions(cancellationGranted)

      timelineEvents.size mustBe 3
      timelineEvents(0).title mustBe messages(s"submission.enhancedStatus.$CANCELLED")
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.$REQUESTED_CANCELLATION")
      timelineEvents(2).title mustBe messages(s"submission.enhancedStatus.$RECEIVED")
    }

    "generate the expected sequence of TimelineEvent instances when the cancellation request is not confirmed yet" in {
      val timelineEvents = createTimelineFromActions(cancellationRequestNotConfirmedYet)

      timelineEvents.size mustBe 2
      timelineEvents(0).title mustBe messages(s"submission.enhancedStatus.$REQUESTED_CANCELLATION")
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.$RECEIVED")
    }

    "generate the expected sequence of TimelineEvent instances when the declaration is cancelled" in {
      val timelineEvents = createTimelineFromActions(declarationCancelled)

      timelineEvents.size mustBe 3
      timelineEvents(0).title mustBe messages(s"submission.enhancedStatus.$CANCELLED")
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.$REQUESTED_CANCELLATION")
      timelineEvents(2).title mustBe messages(s"submission.enhancedStatus.$RECEIVED")
    }

    "generate a sequence of TimelineEvent instances" which {

      "have an Html content only" when {
        "the source notifications have submission statuses that require it (the Html content)" in {
          val notification = NotificationSummary(UUID.randomUUID, issued(1), PENDING)

          val statusesWithContent = Set(
            ADDITIONAL_DOCUMENTS_REQUIRED,
            UNDERGOING_PHYSICAL_CHECK,
            QUERY_NOTIFICATION_MESSAGE,
            ERRORS,
            CANCELLED,
            WITHDRAWN,
            EXPIRED_NO_DEPARTURE,
            EXPIRED_NO_ARRIVAL,
            CLEARED,
            RECEIVED,
            GOODS_ARRIVED_MESSAGE
          )

          EnhancedStatus.values.foreach { status =>
            withClue(s"$status has content must be ${statusesWithContent.contains(status)}") {
              val timelineEvents = genTimelineEvents(List(notification.copy(enhancedStatus = status)))
              val result = if (timelineEvents.isEmpty) false else timelineEvents(0).content.isDefined
              result mustBe statusesWithContent.contains(status)
            }
          }
        }
      }

      "does not have 'Upload files' Html content" when {
        "at least one of the notifications has REJECTED (DMSREJ) as status" in {
          val notifications = List(
            NotificationSummary(UUID.randomUUID, issued(1), ADDITIONAL_DOCUMENTS_REQUIRED),
            NotificationSummary(UUID.randomUUID, issued(2), QUERY_NOTIFICATION_MESSAGE),
            NotificationSummary(UUID.randomUUID, issued(3), UNDERGOING_PHYSICAL_CHECK),
            NotificationSummary(UUID.randomUUID, issued(4), ERRORS)
          )
          val timelineEvents = genTimelineEvents(notifications)
          assert(timelineEvents(0).content.isDefined)
          assert(timelineEvents(1).content.isEmpty)
          assert(timelineEvents(2).content.isDefined)
          assert(timelineEvents(3).content.isEmpty)
        }
      }

      "in one single instance only" should {
        "have a 'Documents required' Html content" when {
          "multiple DMSDOC and/or DMSCTL notifications are present" in {
            val notifications = List(
              NotificationSummary(UUID.randomUUID, issued(1), ADDITIONAL_DOCUMENTS_REQUIRED),
              NotificationSummary(UUID.randomUUID, issued(2), UNDERGOING_PHYSICAL_CHECK)
            )
            val timelineEvents = genTimelineEvents(notifications)
            assert(timelineEvents(0).content.isDefined)
            assert(timelineEvents(1).content.isEmpty)
          }
        }
      }

      "in one single instance only" should {
        "have a 'View queries' Html content" when {
          "multiple DMSQRY notifications are present" in {
            val notifications = List(
              NotificationSummary(UUID.randomUUID, issued(1), QUERY_NOTIFICATION_MESSAGE),
              NotificationSummary(UUID.randomUUID, issued(2), QUERY_NOTIFICATION_MESSAGE)
            )
            val timelineEvents = genTimelineEvents(notifications)
            assert(timelineEvents(0).content.isDefined)
            assert(timelineEvents(1).content.isEmpty)
          }
        }
      }

      "in one single instance only" should {
        "have a 'Fix and resubmit' Html content" when {
          "multiple DMSREJ notifications are present" in {
            val notificationSummary1 = NotificationSummary(UUID.randomUUID, issued(1), ERRORS)
            val notificationSummary2 = NotificationSummary(UUID.randomUUID, issued(2), ERRORS)
            val notifications = List(notificationSummary1, notificationSummary2)
            val timelineEvents = genTimelineEvents(notifications)
            assert(timelineEvents(0).content.isDefined)
            assert(timelineEvents(1).content.isEmpty)
          }
        }
      }

      // Test to remove once the sfus feature flag is gone
      "do not have 'Documents required' Html content" when {
        "the 'Sfus' feature flag is disabled" in {
          when(mockSfusConfig.isSfusUploadEnabled).thenReturn(false)
          val notification = NotificationSummary(UUID.randomUUID, issued(1), ADDITIONAL_DOCUMENTS_REQUIRED)
          val timelineEvents = genTimelineEvents(List(notification))
          assert(timelineEvents(0).content.isEmpty)
        }
      }
    }
  }
}

object TimelineEventsSpec {

  val cancellationDenied = Json
    .parse(s"""[
      |    {
      |      "id": "49cadcf1-167f-4c03-b4a4-5433cdea894e",
      |      "requestType" : "SubmissionRequest",
      |      "requestTimestamp" : "2022-05-11T09:42:41.138Z[UTC]",
      |      "notifications" : [
      |        {
      |          "notificationId" : "f0245e51-2be3-440c-9cf7-da09f3a0874b",
      |          "dateTimeIssued" : "2022-05-12T17:32:31Z[UTC]",
      |          "enhancedStatus" : "RECEIVED"
      |        }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |    },
      |    {
      |      "id": "202bcfdb-60e0-4710-949a-ecd2db0487b3",
      |      "requestType": "CancellationRequest",
      |      "requestTimestamp": "2022-05-13T12:31:03.937Z[UTC]",
      |      "notifications": [
      |        {
      |          "notificationId": "855e4c4a-2889-417b-9338-d2f487dd19c4",
      |          "dateTimeIssued": "2022-05-14T12:31:06Z[UTC]",
      |          "enhancedStatus": "CUSTOMS_POSITION_DENIED"
      |        }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |    }
      |]
      |""".stripMargin)
    .as[Seq[Action]]

  val cancellationGranted = Json
    .parse(s"""[
      |    {
      |      "id": "49cadcf1-167f-4c03-b4a4-5433cdea894e",
      |      "requestType" : "SubmissionRequest",
      |      "requestTimestamp" : "2022-05-11T09:42:41.138Z[UTC]",
      |      "notifications" : [
      |        {
      |          "notificationId" : "8ce6ea97-cd82-41e7-90b3-e016c29e8768",
      |          "dateTimeIssued" : "2022-05-15T17:33:31Z[UTC]",
      |          "enhancedStatus" : "CANCELLED"
      |        },
      |        {
      |          "notificationId" : "f0245e51-2be3-440c-9cf7-da09f3a0874b",
      |          "dateTimeIssued" : "2022-05-12T17:32:31Z[UTC]",
      |          "enhancedStatus" : "RECEIVED"
      |        }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |    },
      |    {
      |      "id": "202bcfdb-60e0-4710-949a-ecd2db0487b3",
      |      "requestType": "CancellationRequest",
      |      "requestTimestamp": "2022-05-13T12:31:03.937Z[UTC]",
      |      "notifications": [
      |        {
      |          "notificationId": "855e4c4a-2889-417b-9338-d2f487dd19c4",
      |          "dateTimeIssued": "2022-05-14T12:31:06Z[UTC]",
      |          "enhancedStatus": "CUSTOMS_POSITION_GRANTED"
      |        }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |    }
      |]
      |""".stripMargin)
    .as[Seq[Action]]

  val cancellationRequestNotConfirmedYet = Json
    .parse(s"""[
      |    {
      |      "id": "49cadcf1-167f-4c03-b4a4-5433cdea894e",
      |      "requestType" : "SubmissionRequest",
      |      "requestTimestamp" : "2022-05-11T09:42:41.138Z[UTC]",
      |      "notifications" : [
      |        {
      |          "notificationId" : "f0245e51-2be3-440c-9cf7-da09f3a0874b",
      |          "dateTimeIssued" : "2022-05-12T17:32:31Z[UTC]",
      |          "enhancedStatus" : "RECEIVED"
      |        }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |    },
      |    {
      |      "id": "202bcfdb-60e0-4710-949a-ecd2db0487b3",
      |      "requestType": "CancellationRequest",
      |      "requestTimestamp": "2022-05-13T12:31:03.937Z[UTC]",
      |      "notifications": [
      |        {
      |          "notificationId": "855e4c4a-2889-417b-9338-d2f487dd19c4",
      |          "dateTimeIssued": "2022-05-14T12:31:06Z[UTC]",
      |          "enhancedStatus": "CUSTOMS_POSITION_GRANTED"
      |        }
      |      ],
      |      "decId" : "id",
      |      "versionNo": 1
      |    }
      |]
      |""".stripMargin)
    .as[Seq[Action]]

  val declarationCancelled = Json
    .parse(s"""[
      |  {
      |      "id" : "8fdfd197-04ee-488e-910c-786cbfa63edf",
      |      "requestType" : "SubmissionRequest",
      |      "requestTimestamp" : "2022-08-01T07:59:36.406Z[UTC]",
      |      "notifications" : [
      |          {
      |              "notificationId" : "b843f297-1092-41bb-83f6-dcc49e181594",
      |              "dateTimeIssued" : "2022-08-01T08:08:08Z[UTC]",
      |              "enhancedStatus" : "RECEIVED"
      |          },
      |          {
      |              "notificationId" : "d843f297-1092-41bb-83f6-dcc49e181599",
      |              "dateTimeIssued" : "2022-10-01T08:08:08Z[UTC]",
      |              "enhancedStatus" : "CANCELLED"
      |          }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |  },
      |  {
      |      "id" : "9fdfd197-04ee-488e-910c-786cbfa63eda",
      |      "requestType" : "CancellationRequest",
      |      "requestTimestamp" : "2022-09-01T07:59:36.406Z[UTC]",
      |      "notifications" : [
      |          {
      |              "notificationId" : "c843f297-1092-41bb-83f6-dcc49e181596",
      |              "dateTimeIssued" : "2022-09-01T07:59:36.406Z[UTC]",
      |              "enhancedStatus" : "RECEIVED"
      |          }
      |      ],
      |      "decId" : "id",
      |      "versionNo" : 1
      |  }
      |]""".stripMargin)
    .as[Seq[Action]]
}
