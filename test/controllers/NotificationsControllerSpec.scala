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
import play.api.test.Helpers._

class NotificationsControllerSpec extends CustomExportsBaseSpec {

  val notificationsUri = uriWithContextPath("/notifications")
  val submissionsUri = uriWithContextPath("/submissions")
  val submissionNotificationsUri = uriWithContextPath("/notifications/1234")

  "NotificationController" should {

    "return list of notification" in {
      authorizedUser()
      listOfNotifications()

      val result = route(app, getRequest(notificationsUri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("notifications.title"))
      stringResult must include(messages("notifications.status"))
      stringResult must include(messages("notifications.dateAndTime"))
    }

    "return list of notifications for submission" in {
      authorizedUser()
      listOfSubmissionNotifications()

      val result = route(app, getRequest(submissionNotificationsUri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("notifications.title"))
      stringResult must include(messages("notifications.status"))
      stringResult must include(messages("notifications.dateAndTime"))
    }

    "return list of submissions" in {
      authorizedUser()
      listOfSubmissions()

      val result = route(app, getRequest(submissionsUri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("submissions.title"))
      stringResult must include(messages("submissions.ducr"))
      stringResult must include(messages("submissions.lrn"))
      stringResult must include(messages("submissions.mrn"))
      stringResult must include(messages("submissions.submittedTimestamp"))
      stringResult must include(messages("submissions.status"))
      stringResult must include(messages("submissions.noOfNotifications"))
    }
  }
}
