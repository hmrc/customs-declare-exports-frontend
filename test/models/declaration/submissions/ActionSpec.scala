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

package models.declaration.submissions

import base.UnitSpec
import testdata.SubmissionsTestData.{action, notificationSummary, notificationSummary_2, notificationSummary_3}

class ActionSpec extends UnitSpec {

  "latestNotificationSummary" should {
    val notifications = Some(Seq(notificationSummary, notificationSummary_2, notificationSummary_3))
    val actionWithNotifications = action.copy(notifications = notifications)

    "return latest notification summary" in {
      actionWithNotifications.latestNotificationSummary mustBe Some(notificationSummary_3)
    }

    "return None if no notifications associated with action" in {
      action.latestNotificationSummary mustBe None
    }
  }
}
