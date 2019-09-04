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

import base.Injector
import controllers.routes
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, Submission}
import org.jsoup.nodes.Element
import play.api.i18n.MessagesApi
import play.twirl.api.Html
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.submissions
import views.tags.ViewTest

@ViewTest
class SubmissionsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new submissions(mainTemplate)
  private def createView(data: Seq[(Submission, Seq[Notification])] = Seq.empty): Html = page(data)

  "Submission View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.totalNumberOfItems.title")
      messages must haveTranslationFor("submissions.title")
      messages must haveTranslationFor("submissions.eori")
      messages must haveTranslationFor("submissions.conversationId")
      messages must haveTranslationFor("submissions.ducr")
      messages must haveTranslationFor("submissions.lrn")
      messages must haveTranslationFor("submissions.mrn")
      messages must haveTranslationFor("submissions.submittedTimestamp")
      messages must haveTranslationFor("submissions.status")
      messages must haveTranslationFor("submissions.noOfNotifications")
    }

    val view = createView()

    "display page messages" in {
      view.select("title").text() mustBe "submissions.title"
      tableCell(view)(0, 0).text() mustBe "submissions.ducr"
      tableCell(view)(0, 1).text() mustBe "submissions.lrn"
      tableCell(view)(0, 2).text() mustBe "submissions.mrn"
      tableCell(view)(0, 3).text() mustBe "submissions.submittedTimestamp"
      tableCell(view)(0, 4).text() mustBe "submissions.status"
      tableCell(view)(0, 5).text() mustBe "submissions.noOfNotifications"
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
      val backButton = view.getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton.attr("href") mustBe routes.ChoiceController.displayPage().url
    }

    "display 'Start a new declaration' link on page" in {
      val startButton = view.select(".button")
      startButton.text() mustBe "supplementary.startNewDec"
      startButton.attr("href") mustBe routes.ChoiceController.displayPage().url
    }
  }

  private def tableCell(view: Html)(row: Int, column: Int): Element =
    view
      .select(".table-row")
      .get(row)
      .getElementsByClass("table-cell")
      .get(column)
}
