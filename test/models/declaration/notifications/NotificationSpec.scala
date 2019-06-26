/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDateTime

import org.scalatest.{MustMatchers, WordSpec}

class NotificationSpec extends WordSpec with MustMatchers {
  val earlierDate = LocalDateTime.of(2019, 6, 10, 10, 10)
  val laterDate = LocalDateTime.of(2019, 6, 15, 10, 10)
  val latestDate = LocalDateTime.of(2019, 6, 20, 10, 10)
  val firstNotification = Notification("convId", "mrn", earlierDate, "01", None, Seq.empty, "payload")
  val secondNotification = Notification("convId", "mrn", laterDate, "01", None, Seq.empty, "payload")
  val thirdNotification = Notification("convId", "mrn", latestDate, "01", None, Seq.empty, "payload")

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
