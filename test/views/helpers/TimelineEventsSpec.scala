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

import java.time.ZonedDateTime

import models.declaration.notifications.Notification
import models.declaration.submissions.SubmissionStatus._
import views.declaration.spec.UnitViewSpec

class TimelineEventsSpec extends UnitViewSpec {

  "TimelineEvents" should {

    "transform an unordered sequence of Notifications into an ordered sequence of TimelineEvents" in {
      val issued1st = ZonedDateTime.now
      val issued2nd = issued1st.plusDays(1L)
      val issued3rd = issued1st.plusDays(2L)
      val issued4th = issued1st.plusDays(3L)

      val notifications = List(
        Notification("ign", "ign", issued2nd, RECEIVED, Seq.empty),
        Notification("ign", "ign", issued4th, UNKNOWN, Seq.empty),
        Notification("ign", "ign", issued1st, ACCEPTED, Seq.empty),
        Notification("ign", "ign", issued3rd, REJECTED, Seq.empty)
      )

      val timelineEvents = TimelineEvents(notifications)

      timelineEvents(0).dateTime mustBe issued4th
      timelineEvents(0).title mustBe messages(s"submission.status.${UNKNOWN.toString}")

      timelineEvents(1).dateTime mustBe issued3rd
      timelineEvents(1).title mustBe messages(s"submission.status.${REJECTED.toString}")

      timelineEvents(2).dateTime mustBe issued2nd
      timelineEvents(2).title mustBe messages(s"submission.status.${RECEIVED.toString}")

      timelineEvents(3).dateTime mustBe issued1st
      timelineEvents(3).title mustBe messages(s"submission.status.${ACCEPTED.toString}")
    }

    "transform an empty sequence of Notifications into an empty sequence of TimelineEvents" in {
      assert(TimelineEvents(List.empty).isEmpty)
    }
  }
}
