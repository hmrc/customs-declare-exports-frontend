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

package views

import java.time.LocalDateTime
import java.util.UUID

import controllers.routes
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, RequestType, Submission, SubmissionStatus}
import org.jsoup.nodes.Document
import views.declaration.spec.AppViewSpec
import views.html.submission_notifications
import views.tags.ViewTest

@ViewTest
class NotificationsViewSpec extends AppViewSpec {

  private val page = app.injector.instanceOf[submission_notifications]
  private val actions = Action(RequestType.SubmissionRequest, UUID.randomUUID().toString)
  private val submission = Submission("id", "eori", "lrn", None, None, Seq(actions))
  private def notification(status: SubmissionStatus = SubmissionStatus.Accepted) =
    Notification("conv-id", "mrn", LocalDateTime.of(2019, 1, 1, 0, 0), status.fullCode, None, Seq.empty, "payload")

  private def createView(submission: Submission, notifications: Seq[Notification]): Document =
    page(submission, notifications)

  "View" should {
    "render notification" when {
      "status Accepted" in {
        val view = createView(submission, Seq(notification(SubmissionStatus.Accepted)))

        val rows = view.select("tbody tr")
        rows must haveSize(1)

        view must containElementWithID("submission_notifications-table-row0")
        view.getElementById("submission_notifications-table-row0-status") must containText("Accepted")
        view.getElementById("submission_notifications-table-row0-date") must containText("2019-01-01 00:00")
        view mustNot containElementWithID("submission_notifications-table-row0-amend_link")
      }

      "status Rejected" in {
        val view = createView(submission, Seq(notification(SubmissionStatus.Rejected)))

        val rows = view.select("tbody tr")
        rows must haveSize(1)

        view.getElementById("submission_notifications-table-row0-status") must containText("Rejected")
        view.getElementById("submission_notifications-table-row0-date") must containText("2019-01-01 00:00")
        view.getElementById("submission_notifications-table-row0-amend_link") must (haveTag("a") and haveHref(
          routes.SubmissionsController.amend("id")
        ))
      }
    }
  }
}
