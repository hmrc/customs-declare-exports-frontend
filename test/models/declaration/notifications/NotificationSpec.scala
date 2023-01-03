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

package models.declaration.notifications

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import models.declaration.submissions.SubmissionStatus
import base.UnitSpec

class NotificationSpec extends UnitSpec {

  private val zone: ZoneId = ZoneId.of("Europe/London")
  private val earlierDate = ZonedDateTime.of(LocalDateTime.of(2019, 6, 10, 10, 10), zone)
  private val laterDate = ZonedDateTime.of(LocalDateTime.of(2019, 6, 15, 10, 10), zone)
  private val latestDate = ZonedDateTime.of(LocalDateTime.of(2019, 6, 20, 10, 10), zone)

  val firstNotification = Notification("convId", "mrn", earlierDate, SubmissionStatus.UNKNOWN, Seq.empty)
  val secondNotification = Notification("convId", "mrn", laterDate, SubmissionStatus.UNKNOWN, Seq.empty)
  val thirdNotification = Notification("convId", "mrn", latestDate, SubmissionStatus.UNKNOWN, Seq.empty)

  "Notification compare method" should {

    "return 0" when {
      "the two notifications are the same" in {
        firstNotification.compare(firstNotification) must be(0)
      }
    }

    "return 1" when {
      "the second notification is dated after the first" in {
        secondNotification.compare(firstNotification) must be(1)
      }
    }

    "return -1" when {
      "the second notification is dated before the first" in {
        firstNotification.compare(secondNotification) must be(-1)
      }
    }

    "allow to sort list of notifications" in {
      val listOfNotifications = List(thirdNotification, firstNotification, secondNotification)
      listOfNotifications.sorted must be(List(firstNotification, secondNotification, thirdNotification))
    }
  }
}
