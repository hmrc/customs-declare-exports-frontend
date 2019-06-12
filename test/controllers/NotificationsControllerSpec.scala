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

package controllers

import base.CustomExportsBaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.Helpers._

class NotificationsControllerSpec extends CustomExportsBaseSpec {

  val notificationsUri = uriWithContextPath("/notifications")
  val submissionsUri = uriWithContextPath("/submissions")
  val submissionNotificationsUri = uriWithContextPath("/notifications/1234")

  "NotificationController" should {

    "return list of all notifications" in {
      authorizedUser()
      listOfNotifications()

      val result = route(app, getRequest(notificationsUri)).get
      val stringResult = contentAsString(result)

      status(result) mustBe OK
      stringResult must include(messages("notifications.title"))
      stringResult must include(messages("notifications.status"))
      stringResult must include(messages("notifications.dateAndTime"))

      verify(mockCustomsDeclareExportsConnector, times(1)).fetchNotifications()(any(), any())
    }

    "return list of notifications for single submission" in {
      authorizedUser()
      listOfSubmissionNotifications()

      val result = route(app, getRequest(submissionNotificationsUri)).get
      val stringResult = contentAsString(result)

      status(result) mustBe OK
      stringResult must include(messages("notifications.title"))
      stringResult must include(messages("notifications.status"))
      stringResult must include(messages("notifications.dateAndTime"))

      verify(mockCustomsDeclareExportsConnector, times(1)).fetchNotificationsByConversationId(any())(any(), any())
    }
  }
}
