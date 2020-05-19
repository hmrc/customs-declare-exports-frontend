/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

import controllers.NotificationsController
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.{notifications, submission_notifications}

import scala.concurrent.Future
import scala.concurrent.Future.successful

class NotificationControllerSpec extends ControllerSpec {

  private val notification =
    Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.UNKNOWN, Seq.empty, "payload")
  private val submission = Submission(
    uuid = UUID.randomUUID().toString,
    eori = "eori",
    lrn = "lrn",
    mrn = None,
    ducr = None,
    actions = Seq(Action(requestType = SubmissionRequest, id = "conversationID", requestTimestamp = ZonedDateTime.now(ZoneOffset.UTC)))
  )

  trait SetUp {
    val notificationPage = new notifications(mainTemplate)
    val submissionNotificationsPage = new submission_notifications(mainTemplate)

    val controller = new NotificationsController(
      mockAuthAction,
      mockCustomsDeclareExportsConnector,
      stubMessagesControllerComponents(),
      notificationPage,
      submissionNotificationsPage
    )(ec)

    authorizedUser()
  }

  "List Notifications" should {
    "return OK" in new SetUp {
      given(mockCustomsDeclareExportsConnector.fetchNotifications()(any(), any()))
        .willReturn(successful(Seq(notification)))

      val result: Future[Result] = controller.listOfNotifications()(getRequest())

      status(result) must be(OK)
    }
  }

  "List Submission Notifications" should {
    "return OK" when {
      "submission found" in new SetUp {
        given(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .willReturn(successful(Some(submission)))
        given(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any()))
          .willReturn(successful(Seq(notification)))

        private val request: Request[AnyContentAsEmpty.type] = getRequest()
        val result: Future[Result] = controller.listOfNotificationsForSubmission("id")(request)

        status(result) must be(OK)
        viewOf(result) must be(submissionNotificationsPage(submission, Seq(notification))(request, controller.messagesApi.preferred(request)))
      }
    }

    "redirect" when {
      "submission not found" in new SetUp {
        given(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .willReturn(successful(None))

        val result: Future[Result] = controller.listOfNotificationsForSubmission("id")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url))
      }
    }
  }
}
