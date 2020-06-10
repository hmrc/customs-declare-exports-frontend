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

package views

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import base.Injector
import controllers.routes
import forms.Choice
import forms.Choice.AllowedChoiceValues.Submissions
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import models.{Page, Paginated, SubmissionsPagesElements}
import org.jsoup.nodes.Element
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.submissions
import views.tags.ViewTest

@ViewTest
class SubmissionsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val zone: ZoneId = ZoneId.of("UTC")
  private val page = instanceOf[submissions]
  private def createView(
    rejectedSubmissions: Paginated[(Submission, Seq[Notification])] = Paginated(Seq.empty, Page(), 0),
    actionSubmissions: Paginated[(Submission, Seq[Notification])] = Paginated(Seq.empty, Page(), 0),
    otherSubmissions: Paginated[(Submission, Seq[Notification])] = Paginated(Seq.empty, Page(), 0),
    messages: Messages = stubMessages()
  ): Html =
    page(SubmissionsPagesElements(rejectedSubmissions, actionSubmissions, otherSubmissions))(request, messages)

  "Submission View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("submissions.title")
      messages must haveTranslationFor("site.backToSelectionPage")
      messages must haveTranslationFor("submissions.ucr.header")
      messages must haveTranslationFor("submissions.lrn.header")
      messages must haveTranslationFor("submissions.mrn.header")
      messages must haveTranslationFor("submissions.dateAndTime.header")
      messages must haveTranslationFor("submissions.status.header")
    }

    val view = createView()

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display page messages" in {
      tableHead(view)(0).text() mustBe "submissions.ucr.header"
      tableHead(view)(1).text() mustBe "submissions.lrn.header"
      tableHead(view)(2).text() mustBe "submissions.mrn.header"
      tableHead(view)(3).text() mustBe "submissions.dateAndTime.header"
      tableHead(view)(4).text() mustBe "submissions.status.header"
    }

    "display page submissions" when {
      val actionSubmission =
        Action(requestType = SubmissionRequest, id = "conv-id", requestTimestamp = ZonedDateTime.of(LocalDateTime.of(2019, 1, 1, 12, 0, 0), zone))

      val actionCancellation =
        Action(requestType = CancellationRequest, id = "conv-id", requestTimestamp = ZonedDateTime.of(LocalDateTime.of(2021, 6, 1, 12, 0, 0), zone))

      def submissionWithDucr(ducr: String = "ducr") =
        Submission(uuid = "id", eori = "eori", lrn = "lrn", mrn = Some("mrn"), ducr = Some(ducr), actions = Seq(actionSubmission, actionCancellation))

      val submission = submissionWithDucr()

      val acceptedNotification = Notification(
        actionId = "action-id",
        mrn = "mrn",
        dateTimeIssued = ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 12, 30, 0), zone),
        status = SubmissionStatus.ACCEPTED,
        errors = Seq.empty,
        payload = "payload"
      )

      val rejectedNotification = Notification(
        actionId = "actionId",
        mrn = "mrn",
        dateTimeIssued = ZonedDateTime.now(ZoneId.of("UTC")),
        status = SubmissionStatus.REJECTED,
        errors = Seq.empty,
        payload = ""
      )

      val actionNotification = Notification(
        actionId = "actionId",
        mrn = "mrn",
        dateTimeIssued = ZonedDateTime.now(ZoneId.of("UTC")),
        status = SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED,
        errors = Seq.empty,
        payload = ""
      )

      "all fields are populated with timestamp before BST" in {
        val view = tab("other", createView(otherSubmissions = Paginated(Seq(submission -> Seq(acceptedNotification)), Page(), 1)))

        tableCell(view)(1, 0).text() mustBe "ducr" + "submissions.hidden.text"
        tableCell(view)(1, 1).text() mustBe "lrn"
        tableCell(view)(1, 2).text() mustBe "mrn"
        tableCell(view)(1, 3).text() mustBe "1 January 2019 at 12:00"
        tableCell(view)(1, 4).text() mustBe "Accepted"
        val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
        decInformationLink.attr("href") mustBe routes.SubmissionsController.displayDeclarationWithNotifications("id").url
      }

      "all fields are populated with timestamp during BST" in {
        val bstActionSubmission =
          Action(requestType = SubmissionRequest, id = "conv-id", requestTimestamp = ZonedDateTime.of(LocalDateTime.of(2019, 5, 1, 12, 45, 0), zone))
        val bstSubmission = Submission(
          uuid = "id",
          eori = "eori",
          lrn = "lrn",
          mrn = Some("mrn"),
          ducr = Some("ducr"),
          actions = Seq(bstActionSubmission, actionCancellation)
        )
        val view = tab("other", createView(otherSubmissions = Paginated(Seq(bstSubmission -> Seq(acceptedNotification)), Page(), 1)))

        tableCell(view)(1, 0).text() mustBe "ducr" + "submissions.hidden.text"
        tableCell(view)(1, 1).text() mustBe "lrn"
        tableCell(view)(1, 2).text() mustBe "mrn"
        tableCell(view)(1, 3).text() mustBe "1 May 2019 at 13:45"
        tableCell(view)(1, 4).text() mustBe "Accepted"
        val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
        decInformationLink.attr("href") mustBe routes.SubmissionsController.displayDeclarationWithNotifications("id").url
      }

      "optional fields are unpopulated" in {
        val submissionWithOptionalFieldsEmpty = submission.copy(ducr = None, mrn = None)
        val view =
          tab("other", createView(otherSubmissions = Paginated(Seq(submissionWithOptionalFieldsEmpty -> Seq(acceptedNotification)), Page(), 1)))

        tableCell(view)(1, 0).text() mustBe "submissions.hidden.text"
        tableCell(view)(1, 1).text() mustBe "lrn"
        tableCell(view)(1, 2).text() mustBe empty
        tableCell(view)(1, 3).text() mustBe "1 January 2019 at 12:00"
        tableCell(view)(1, 4).text() mustBe "Accepted"
        val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
        decInformationLink.attr("href") mustBe routes.SubmissionsController.displayDeclarationWithNotifications("id").url
      }

      "submission status is 'pending' due to missing notification" in {
        val view = tab("other", createView(otherSubmissions = Paginated(Seq(submission -> Seq.empty), Page(), 1)))

        tableCell(view)(1, 4).text() mustBe "Pending"
      }

      "submission has link when contains rejected notification" in {
        val view = tab("rejected", createView(rejectedSubmissions = Paginated(Seq(submission -> Seq(rejectedNotification)), Page(), 1)))

        tableCell(view)(1, 0).text() must include(submission.ducr.get)
        tableCell(view)(1, 0).toString must include(routes.SubmissionsController.displayDeclarationWithNotifications(submission.uuid).url)
      }

      "submission date is unknown due to missing submit action" in {
        val submissionWithMissingSubmitAction = submission.copy(actions = Seq(actionCancellation))
        val view =
          tab("other", createView(otherSubmissions = Paginated(Seq(submissionWithMissingSubmitAction -> Seq(acceptedNotification)), Page(), 1)))

        tableCell(view)(1, 3).text() mustBe empty
      }

      "submissions are shown on correct tabs" in {
        val view =
          createView(
            rejectedSubmissions = Paginated(Seq(submissionWithDucr("ducr_rejected") -> Seq(rejectedNotification)), Page(), 1),
            actionSubmissions = Paginated(Seq(submissionWithDucr("ducr_action") -> Seq(actionNotification)), Page(), 1),
            otherSubmissions = Paginated(Seq(submissionWithDucr("ducr_accepted") -> Seq(acceptedNotification)), Page(), 1)
          )

        tableCell(tab("other", view))(1, 0).text() must include("ducr_accepted")
        tableCell(tab("rejected", view))(1, 0).text() must include("ducr_rejected")
        tableCell(tab("action", view))(1, 0).text() must include("ducr_action")
      }

      "submissions without status are shown on 'other' tab" in {
        val view =
          createView(otherSubmissions = Paginated(Seq(submissionWithDucr("ducr_pending") -> Seq.empty), Page(), 1))

        tableCell(tab("other", view))(1, 0).text() must include("ducr_pending")
      }
    }

    "display 'Back' button that links to 'Choice' page with Submissions selected" in {
      val backButton = view.getElementById("back-link")

      backButton must containText("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(Submissions))))
    }

    "display 'Start a new declaration' link on page" in {
      val startButton = view.getElementsByClass("govuk-button").first()
      startButton.text() mustBe "supplementary.startNewDec"
      startButton.attr("href") mustBe routes.ChoiceController.displayPage().url
    }
  }

  private def tab(tab: String, view: Html): Element = view.getElementById(s"$tab-submissions")

  private def tableCell(view: Element)(row: Int, column: Int): Element =
    view
      .select(".govuk-table__row")
      .get(row)
      .getElementsByClass("govuk-table__cell")
      .get(column)

  private def tableHead(view: Element)(column: Int): Element =
    view
      .select(".govuk-table__head")
      .first()
      .getElementsByClass("govuk-table__header")
      .get(column)
}
