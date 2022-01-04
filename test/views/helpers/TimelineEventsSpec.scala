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

import java.time.{ZoneId, ZonedDateTime}

import base.Injector
import config.featureFlags.{SecureMessagingInboxConfig, SfusConfig}
import models.declaration.notifications.Notification
import models.declaration.submissions.SubmissionStatus._
import models.declaration.submissions.{Submission, SubmissionStatus}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.linkButton
import views.html.components.upload_files_partial_for_timeline

class TimelineEventsSpec extends UnitViewSpec with BeforeAndAfterEach with Injector {

  private val secureMessagingInboxConfig = mock[SecureMessagingInboxConfig]
  private val sfusConfig = mock[SfusConfig]
  private val submission = mock[Submission]
  private val uploadFilesPartialForTimeline = instanceOf[upload_files_partial_for_timeline]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(secureMessagingInboxConfig.sfusInboxLink).thenReturn("dummyInboxLink")
    when(submission.mrn).thenReturn(Some("mrn"))
    when(submission.uuid).thenReturn("id")
  }

  private def createTimeline(notifications: Seq[Notification], enableSfusConfig: Boolean = true): Seq[TimelineEvent] = {
    when(sfusConfig.isSfusUploadEnabled).thenReturn(enableSfusConfig)
    new TimelineEvents(new linkButton(), secureMessagingInboxConfig, sfusConfig, uploadFilesPartialForTimeline)(submission, notifications)
  }

  "TimelineEvents" should {

    "transform an empty sequence of Notifications into an empty sequence of TimelineEvent instances" in {
      assert(createTimeline(List.empty).isEmpty)
    }

    "transform an unordered sequence of Notifications into an ordered sequence of TimelineEvent instances" in {
      def withGBZone(dateTime: ZonedDateTime): ZonedDateTime =
        dateTime.withZoneSameInstant(ZoneId.of("Europe/London"))

      val issued1st = withGBZone(ZonedDateTime.now)
      val issued2nd = withGBZone(issued1st.plusDays(1L))
      val issued3rd = withGBZone(issued1st.plusDays(2L))
      val issued4th = withGBZone(issued1st.plusDays(3L))

      val notifications = List(
        Notification("ign", "ign", issued2nd, RECEIVED, Seq.empty),
        Notification("ign", "ign", issued4th, UNKNOWN, Seq.empty),
        Notification("ign", "ign", issued1st, ACCEPTED, Seq.empty),
        Notification("ign", "ign", issued3rd, REJECTED, Seq.empty)
      )
      val timelineEvents = createTimeline(notifications)

      timelineEvents(0).dateTime mustBe issued4th
      timelineEvents(0).title mustBe messages(s"submission.status.${UNKNOWN.toString}")

      timelineEvents(1).dateTime mustBe issued3rd
      timelineEvents(1).title mustBe messages(s"submission.status.${REJECTED.toString}")

      timelineEvents(2).dateTime mustBe issued2nd
      timelineEvents(2).title mustBe messages(s"submission.status.${RECEIVED.toString}")

      timelineEvents(3).dateTime mustBe issued1st
      timelineEvents(3).title mustBe messages(s"submission.status.${ACCEPTED.toString}")
    }

    "generate a sequence of TimelineEvent instances" which {

      "have an Html content only" when {
        "the source notifications have submission statuses that require it (the Html content)" in {
          val notification = Notification("ign", "ign", ZonedDateTime.now, ACCEPTED, Seq.empty)

          val statusesWithContent = Set(ADDITIONAL_DOCUMENTS_REQUIRED, UNDERGOING_PHYSICAL_CHECK, QUERY_NOTIFICATION_MESSAGE, REJECTED)
          SubmissionStatus.values.foreach { status =>
            val content = createTimeline(List(notification.copy(status = status)))(0).content
            content.isDefined mustBe statusesWithContent.contains(status)
          }
        }
      }

      "does not have 'Upload files' Html content" when {
        "at least one of the notifications has REJECTED (DMSREJ) as status" in {
          val notifications = List(
            Notification("ign", "ign", ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED, Seq.empty),
            Notification("ign", "ign", ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE, Seq.empty),
            Notification("ign", "ign", ZonedDateTime.now, UNDERGOING_PHYSICAL_CHECK, Seq.empty),
            Notification("ign", "ign", ZonedDateTime.now, REJECTED, Seq.empty)
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
              Notification("ign", "ign", ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED, Seq.empty),
              Notification("ign", "ign", ZonedDateTime.now, UNDERGOING_PHYSICAL_CHECK, Seq.empty),
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
              Notification("ign", "ign", ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE, Seq.empty),
              Notification("ign", "ign", ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE, Seq.empty),
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
              Notification("ign", "ign", ZonedDateTime.now, REJECTED, Seq.empty),
              Notification("ign", "ign", ZonedDateTime.now, REJECTED, Seq.empty),
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
          val notification = Notification("ign", "ign", ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED, Seq.empty)
          val timelineEvents = createTimeline(List(notification), false)
          assert(timelineEvents(0).content.isEmpty)
        }
      }
    }
  }
}
