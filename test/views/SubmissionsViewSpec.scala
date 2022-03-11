/*
 * Copyright 2022 HM Revenue & Customs
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

import base.OverridableInjector
import config.featureFlags.SecureMessagingConfig
import controllers.routes
import forms.Choice
import forms.Choice.AllowedChoiceValues.Submissions
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import models.{Page, Paginated, SubmissionsPagesElements}
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.twirl.api.Html
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.submissions
import views.tags.ViewTest

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

@ViewTest
class SubmissionsViewSpec extends UnitViewSpec with BeforeAndAfterEach with ExportsTestData with Stubs {

  private val injector = new OverridableInjector(bind[SecureMessagingConfig].toInstance(mockSecureMessagingConfig))

  private val page = injector.instanceOf[submissions]

  private def createView(
    rejectedSubmissions: Paginated[(Submission, Seq[Notification])] = Paginated(Seq.empty, Page(), 0),
    actionSubmissions: Paginated[(Submission, Seq[Notification])] = Paginated(Seq.empty, Page(), 0),
    otherSubmissions: Paginated[(Submission, Seq[Notification])] = Paginated(Seq.empty, Page(), 0)
  ): Html =
    page(SubmissionsPagesElements(rejectedSubmissions, actionSubmissions, otherSubmissions))(request, messages)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
  }

  private val zone: ZoneId = ZoneId.of("UTC")

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
    errors = Seq.empty
  )

  val rejectedNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = ZonedDateTime.now(ZoneId.of("UTC")),
    status = SubmissionStatus.REJECTED,
    errors = Seq.empty
  )

  val actionNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = ZonedDateTime.now(ZoneId.of("UTC")),
    status = SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED,
    errors = Seq.empty
  )

  val dmsQryNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = ZonedDateTime.now(ZoneId.of("UTC")),
    status = SubmissionStatus.QUERY_NOTIFICATION_MESSAGE,
    errors = Seq.empty
  )

  def submissions(notification: Notification = acceptedNotification) =
    Paginated(Seq(submission -> Seq(notification)), Page(), 1)

  "Submission View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("submissions.title")
      messages must haveTranslationFor("site.backToSelectionPage")
      messages must haveTranslationFor("submissions.ducr.header")
      messages must haveTranslationFor("submissions.lrn.header")
      messages must haveTranslationFor("submissions.mrn.header")
      messages must haveTranslationFor("submissions.dateAndTime.header")
      messages must haveTranslationFor("submissions.status.header")
    }

    "contain the navigation banner" when {
      "the Secure Messaging flag is set to 'true'" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        val view = createView()

        val navigationBanner = view.getElementById("navigation-banner")
        assert(Option(navigationBanner).isDefined && navigationBanner.childrenSize == 2)

        val elements = navigationBanner.children

        assert(elements.first.tagName.toLowerCase == "span")

        assert(elements.last.tagName.toLowerCase == "a")
        elements.last must haveHref(routes.SecureMessagingController.displayInbox)
      }
    }

    "not contain the navigation banner" when {
      "the Secure Messaging flag is set to 'false'" in {
        Option(createView().getElementById("navigation-banner")) mustBe None
      }
    }

    "display same page title as header" in {
      val view = createView()
      view.title must startWith(view.getElementsByTag("h1").text)
    }

    "display the no-action-needed hint" when {
      "there are no submissions requiring action" in {
        createView().getElementsByClass("govuk-warning-text").text mustBe messages("submissions.hint.no.action.needed")
      }
    }

    "display the action-needed hint" when {

      "there are submissions requiring action" in {
        val warningText =
          s"! ${messages("site.warning")} ${messages("submissions.hint.action.needed.generic")}"
        val view = createView(actionSubmissions = submissions(actionNotification))

        view.getElementsByClass("govuk-warning-text").text mustBe warningText
      }

      "there are submissions with DMSQRY notification" in {
        val warningText =
          s"! ${messages("site.warning")} ${messages("submissions.hint.action.needed.queryNotificationMessage")}"
        val view = createView(actionSubmissions = submissions(dmsQryNotification))

        view.getElementsByClass("govuk-warning-text").text mustBe warningText
      }

      "there are submissions requiring action and submissions with DMSQRY notification" in {
        val warningText =
          s"! ${messages("site.warning")} ${messages("submissions.hint.action.needed.queryNotificationMessage")}"
        val view = createView(
          actionSubmissions =
            Paginated(Seq(submission -> Seq(actionNotification), submissionWithDucr("ducr_2") -> Seq(dmsQryNotification)), Page(), 2)
        )

        view.getElementsByClass("govuk-warning-text").text mustBe warningText
      }
    }

    "display the expected tab title as a link" in {
      val view = createView()

      val testExpectedTabTitleAsLink =
        (tab: String) => view.getElementsByAttributeValue("href", s"#${tab}-submissions").text == messages(s"submissions.${tab}.tab.title")

      assert(List("rejected", "action", "other").forall(testExpectedTabTitleAsLink))
    }

    "display the expected tab heading" in {
      val view = createView()

      val testExpectedTabHeading =
        (tab: String) => view.getElementById(s"${tab}-submissions").getElementsByTag("h2").text == messages(s"submissions.${tab}.content.title")

      assert(List("rejected", "action", "other").forall(testExpectedTabHeading))
    }

    "display the expected tab hints" when {
      "there are submissions" in {
        val view = createView(submissions(rejectedNotification), submissions(actionNotification), submissions(acceptedNotification))

        val testExpectedTabHint = (tab: String) => view.getElementById(s"${tab}-content-hint").text == messages(s"submissions.${tab}.content.hint")

        assert(List("rejected", "action", "other").forall(testExpectedTabHint))
      }
    }

    "display one pagination summary" when {
      "there are submissions" in {
        val view = createView(submissions(rejectedNotification), submissions(actionNotification), submissions(acceptedNotification))
        val paginationText = s"${messages("site.pagination.showing")} 1 ${messages("submissions.pagination.singular")}"

        val testPaginationSummary =
          (tab: String) => paginationText == view.getElementById(s"${tab}-submissions").getElementsByClass("ceds-pagination__summary").text

        assert(List("rejected", "action", "other").forall(testPaginationSummary))
      }
    }

    "display no pagination summary" when {
      "there are no submissions" in {
        val view = createView()
        val noDeclarations = messages("submissions.empty.tab")

        val testNoPaginationSummary =
          (tab: String) => noDeclarations == view.getElementById(s"${tab}-submissions").getElementsByClass("ceds-pagination__summary").text

        assert(List("rejected", "action", "other").forall(testNoPaginationSummary))
      }
    }

    "display table headers" in {
      val view = createView(otherSubmissions = submissions())

      tableHead(view)(0) must containMessage("submissions.mrn.header")
      tableHead(view)(1) must containMessage("submissions.ducr.header")
      tableHead(view)(2) must containMessage("submissions.lrn.header")
      tableHead(view)(3) must containMessage("submissions.dateAndTime.header")
      tableHead(view)(4) must containMessage("submissions.status.header")
    }

    "display page submissions" when {

      "all fields are populated with timestamp" when {
        "before BST" in {
          val view = tab("other", createView(otherSubmissions = submissions()))

          val mrnLink = tableCell(view)(1, 0)
          mrnLink must containText("mrn")
          mrnLink must containMessage("submissions.hidden.text", "ducr")
          tableCell(view)(1, 1).text() mustBe "ducr"
          tableCell(view)(1, 2).text() mustBe "lrn"
          tableCell(view)(1, 3).text() mustBe "1 January 2019 at 12:00pm"
          tableCell(view)(1, 4).text() mustBe messages("submission.status.ACCEPTED")
          val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
          decInformationLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage("id").url
        }

        "during BST" in {
          val bstActionSubmission =
            Action(
              requestType = SubmissionRequest,
              id = "conv-id",
              requestTimestamp = ZonedDateTime.of(LocalDateTime.of(2019, 5, 1, 12, 45, 0), zone)
            )
          val bstSubmission = Submission(
            uuid = "id",
            eori = "eori",
            lrn = "lrn",
            mrn = Some("mrn"),
            ducr = Some("ducr"),
            actions = Seq(bstActionSubmission, actionCancellation)
          )
          val view = tab("other", createView(otherSubmissions = Paginated(Seq(bstSubmission -> Seq(acceptedNotification)), Page(), 1)))

          val mrnLink = tableCell(view)(1, 0)
          mrnLink must containText("mrn")
          mrnLink must containMessage("submissions.hidden.text", "ducr")
          tableCell(view)(1, 1).text() mustBe "ducr"
          tableCell(view)(1, 2).text() mustBe "lrn"
          tableCell(view)(1, 3).text() mustBe "1 May 2019 at 1:45pm"
          tableCell(view)(1, 4).text() mustBe messages("submission.status.ACCEPTED")
          val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
          decInformationLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage("id").url
        }
      }
      "optional fields are unpopulated" in {
        val submissionWithOptionalFieldsEmpty = submission.copy(ducr = None, mrn = None)
        val view =
          tab("other", createView(otherSubmissions = Paginated(Seq(submissionWithOptionalFieldsEmpty -> Seq(acceptedNotification)), Page(), 1)))

        tableCell(view)(1, 1).text() mustBe empty
        tableCell(view)(1, 2).text() mustBe "lrn"
        tableCell(view)(1, 3).text() mustBe "1 January 2019 at 12:00pm"
        tableCell(view)(1, 4).text() mustBe messages("submission.status.ACCEPTED")
        val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
        decInformationLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage("id").url
      }

      "optional mrn field is populated with default" in {
        val submissionWithOptionalFieldsEmpty = submission.copy(ducr = None, mrn = None)
        val view =
          tab("other", createView(otherSubmissions = Paginated(Seq(submissionWithOptionalFieldsEmpty -> Seq(acceptedNotification)), Page(), 1)))

        tableCell(view)(1, 0) must containMessage("submissions.declarationDetails.mrn.pending")

      }

      "submission status is 'pending' due to missing notification" in {
        val view = tab("other", createView(otherSubmissions = Paginated(Seq(submission -> Seq.empty), Page(), 1)))

        tableCell(view)(1, 4).text() mustBe "Pending"
      }

      "submission has link when contains rejected notification" in {
        val view = tab("rejected", createView(rejectedSubmissions = submissions(rejectedNotification)))

        tableCell(view)(1, 0).text() must include(submission.ducr.get)
        tableCell(view)(1, 0).toString must include(routes.DeclarationDetailsController.displayPage(submission.uuid).url)
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
      val backButton = createView().getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(Submissions))))
    }

    "display 'Start a new declaration' link on page" in {
      val startButton = createView().getElementsByClass("govuk-button").first()
      startButton must containMessage("supplementary.startNewDec")
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
