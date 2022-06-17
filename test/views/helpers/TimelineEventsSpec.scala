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

import base.Injector
import models.declaration.submissions.{Action, EnhancedStatus, NotificationSummary, Submission}
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.SubmissionRequest
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.{linkButton, paragraphBody}
import views.html.components.upload_files_partial_for_timeline

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

class TimelineEventsSpec extends UnitViewSpec with BeforeAndAfterEach with Injector {

  private val submission = Submission("id", "eori", "lrn", Some("mrn"), None, None, None, Seq.empty)
  private val uploadFilesPartialForTimeline = instanceOf[upload_files_partial_for_timeline]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSecureMessagingInboxConfig.sfusInboxLink).thenReturn("dummyInboxLink")
  }

  private def createTimeline(notificationSummaries: Seq[NotificationSummary], enableSfusConfig: Boolean = true): Seq[TimelineEvent] = {
    when(mockSfusConfig.isSfusUploadEnabled).thenReturn(enableSfusConfig)
    val subReqestAction = Action("id", SubmissionRequest, ZonedDateTime.now, Some(notificationSummaries))
    new TimelineEvents(new linkButton(), new paragraphBody(), mockSecureMessagingInboxConfig, mockSfusConfig, uploadFilesPartialForTimeline)(
      submission.copy(actions = Seq(subReqestAction))
    )
  }

  "TimelineEvents" should {

    "transform an empty sequence of Notifications into an empty sequence of TimelineEvent instances" in {
      assert(createTimeline(List.empty).isEmpty)
    }

    "transform an unordered sequence of NotificationSummaries into an ordered sequence of TimelineEvent instances" in {
      def withGBZone(dateTime: ZonedDateTime): ZonedDateTime =
        dateTime.withZoneSameInstant(ZoneId.of("Europe/London"))

      val issued1st = withGBZone(ZonedDateTime.now)
      val issued2nd = withGBZone(issued1st.plusDays(1L))
      val issued3rd = withGBZone(issued1st.plusDays(2L))
      val issued4th = withGBZone(issued1st.plusDays(3L))

      val notifications = List(
        NotificationSummary(UUID.randomUUID(), issued2nd, RECEIVED),
        NotificationSummary(UUID.randomUUID(), issued4th, UNKNOWN),
        NotificationSummary(UUID.randomUUID(), issued1st, PENDING),
        NotificationSummary(UUID.randomUUID(), issued3rd, ERRORS)
      )
      val timelineEvents = createTimeline(notifications)

      timelineEvents(0).dateTime mustBe issued4th
      timelineEvents(0).title mustBe messages(s"submission.enhancedStatus.${UNKNOWN.toString}")

      timelineEvents(1).dateTime mustBe issued3rd
      timelineEvents(1).title mustBe messages(s"submission.enhancedStatus.${ERRORS.toString}")

      timelineEvents(2).dateTime mustBe issued2nd
      timelineEvents(2).title mustBe messages(s"submission.enhancedStatus.${RECEIVED.toString}")

      timelineEvents(3).dateTime mustBe issued1st
      timelineEvents(3).title mustBe messages(s"submission.enhancedStatus.${PENDING.toString}")
    }

    "generate a sequence of TimelineEvent instances" which {

      "have an Html content only" when {
        "the source notifications have submission statuses that require it (the Html content)" in {
          val notification = NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, PENDING)

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
            val content = createTimeline(List(notification.copy(enhancedStatus = status)))(0).content
            withClue(s"$status has content must be ${statusesWithContent.contains(status)}") {
              content.isDefined mustBe statusesWithContent.contains(status)
            }
          }
        }
      }

      "does not have 'Upload files' Html content" when {
        "at least one of the notifications has REJECTED (DMSREJ) as status" in {
          val notifications = List(
            NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED),
            NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE),
            NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, UNDERGOING_PHYSICAL_CHECK),
            NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, ERRORS)
          )
          val timelineEvents = createTimeline(notifications)
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
              NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED),
              NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, UNDERGOING_PHYSICAL_CHECK),
            )
            val timelineEvents = createTimeline(notifications)
            assert(timelineEvents(0).content.isDefined)
            assert(timelineEvents(1).content.isEmpty)
          }
        }
      }

      "in one single instance only" should {
        "have a 'View queries' Html content" when {
          "multiple DMSQRY notifications are present" in {
            val notifications = List(
              NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE),
              NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE),
            )
            val timelineEvents = createTimeline(notifications)
            assert(timelineEvents(0).content.isDefined)
            assert(timelineEvents(1).content.isEmpty)
          }
        }
      }

      "in one single instance only" should {
        "have a 'Fix and resubmit' Html content" when {
          "multiple DMSREJ notifications are present" in {
            val notifications = List(
              NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, ERRORS),
              NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, ERRORS),
            )
            val timelineEvents = createTimeline(notifications)
            assert(timelineEvents(0).content.isDefined)
            assert(timelineEvents(1).content.isEmpty)
          }
        }
      }

      // Test to remove once the sfus feature flag is gone
      "do not have 'Documents required' Html content" when {
        "the 'Sfus' feature flag is disabled" in {
          val notification = NotificationSummary(UUID.randomUUID(), ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED)
          val timelineEvents = createTimeline(List(notification), false)
          assert(timelineEvents(0).content.isEmpty)
        }
      }
    }
  }
}
