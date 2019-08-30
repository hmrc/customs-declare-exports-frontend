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

import controllers.routes
import helpers.views.declaration.{CommonMessages, SubmissionsMessages}
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, Submission}
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import org.jsoup.nodes.Element
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.submissions
import views.tags.ViewTest

@ViewTest
class SubmissionsViewSpec extends AppViewSpec with SubmissionsMessages with CommonMessages {

  private val submissionsPage = app.injector.instanceOf[submissions]
  private def createView(data: Seq[(Submission, Seq[Notification])] = Seq.empty): Html = submissionsPage(data)

  "Submission View" should {

    "display page messages" in {
      val view = createView()

      view.select("title").text() must be(messages(title))
      tableCell(view)(0, 0).text() must be(messages(ducr))
      tableCell(view)(0, 1).text() must be(messages(lrn))
      tableCell(view)(0, 2).text() must be(messages(mrn))
      tableCell(view)(0, 3).text() must be(messages(submittedTimestamp))
      tableCell(view)(0, 4).text() must be(messages(status))
      tableCell(view)(0, 5).text() must be(messages(notificationCount))
    }

    "display page submissions" when {
      val actionSubmission = Action(
        requestType = SubmissionRequest,
        conversationId = "conv-id",
        requestTimestamp = LocalDateTime.of(2019, 1, 1, 0, 0, 0)
      )

      val actionCancellation = Action(
        requestType = CancellationRequest,
        conversationId = "conv-id",
        requestTimestamp = LocalDateTime.of(2021, 1, 1, 0, 0, 0)
      )

      val submission = Submission(
        uuid = "id",
        eori = "eori",
        lrn = "lrn",
        mrn = Some("mrn"),
        ducr = Some("ducr"),
        actions = Seq(actionSubmission, actionCancellation)
      )

      val notification = Notification(
        conversationId = "conv-id",
        mrn = "mrn",
        dateTimeIssued = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
        functionCode = "01",
        nameCode = None,
        errors = Seq.empty,
        payload = "payload"
      )

      "all fields are populated" in {
        val view = createView(Seq(submission -> Seq(notification)))

        tableCell(view)(1, 0).text() mustBe "ducr"
        tableCell(view)(1, 1).text() mustBe "lrn"
        tableCell(view)(1, 2).text() mustBe "mrn"
        tableCell(view)(1, 3).text() mustBe "2019-01-01 00:00"
        tableCell(view)(1, 4).text() mustBe "Accepted"
        tableCell(view)(1, 5).text() mustBe "1"
        val anchorSubmission = tableCell(view)(1, 5).getElementsByTag("a").first()
        anchorSubmission.attr("href") mustBe routes.NotificationsController.listOfNotificationsForSubmission("id").url
      }

      "optional fields are unpopulated" in {
        val submissionWithOptionalFieldsEmpty = submission.copy(ducr = None, mrn = None)
        val view = createView(Seq(submissionWithOptionalFieldsEmpty -> Seq(notification)))

        tableCell(view)(1, 0).text() mustBe ""
        tableCell(view)(1, 1).text() mustBe "lrn"
        tableCell(view)(1, 2).text() mustBe ""
        tableCell(view)(1, 3).text() mustBe "2019-01-01 00:00"
        tableCell(view)(1, 4).text() mustBe "Accepted"
        tableCell(view)(1, 5).text() mustBe "1"
        val anchorSubmission = tableCell(view)(1, 5).getElementsByTag("a").first()
        anchorSubmission.attr("href") mustBe routes.NotificationsController.listOfNotificationsForSubmission("id").url
      }

      "submission status is unknown due to missing notification" in {
        val view = createView(Seq(submission -> Seq.empty))

        tableCell(view)(1, 4).text() mustBe "Unknown status"
      }
      "submission status is unknown due to invalid notification functionCode" in {
        val notificationWithUnknownStatus = notification.copy(functionCode = "abc")
        val view = createView(Seq(submission -> Seq(notificationWithUnknownStatus)))

        tableCell(view)(1, 4).text() mustBe "Unknown status"
      }

      "submission status is unknown due to invalid notification nameCode" in {
        val notificationWithUnknownStatus = notification.copy(nameCode = Some("abc"))
        val view = createView(Seq(submission -> Seq(notificationWithUnknownStatus)))

        tableCell(view)(1, 4).text() mustBe "Unknown status"
      }

      "submission date is unknown due to missing submit action" in {
        val submissionWithMissingSubmitAction = submission.copy(actions = Seq(actionCancellation))
        val view = createView(Seq(submissionWithMissingSubmitAction -> Seq(notification)))

        tableCell(view)(1, 3).text() mustBe ""
      }
    }

    "display 'Back' button that links to 'Choice' page" in {
      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(routes.ChoiceController.displayPage().url)
    }

    "display 'Start a new declaration' link on page" in {
      val view = createView()

      val startButton = view.select(".button")
      startButton.text() must be(messages(startNewDeclaration))
      startButton.attr("href") must be(routes.ChoiceController.displayPage().url)
    }
  }

  private def tableCell(view: Html)(row: Int, column: Int): Element =
    view
      .select(".table-row")
      .get(row)
      .getElementsByClass("table-cell")
      .get(column)
}
