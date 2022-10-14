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
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, Submission}
import models.{Page, Paginated, SubmissionsPagesElements}
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import play.api.inject.bind
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.submissions
import views.tags.ViewTest

import java.time.{ZoneId, ZonedDateTime}

@ViewTest
class SubmissionsViewSpec extends UnitViewSpec with ExportsTestHelper {

  private val injector = new OverridableInjector(bind[SecureMessagingConfig].toInstance(mockSecureMessagingConfig))

  private val page = injector.instanceOf[submissions]

  private def createView(
    otherSubmissions: Paginated[Submission] = Paginated(Seq.empty, Page(), 0),
    actionSubmissions: Paginated[Submission] = Paginated(Seq.empty, Page(), 0),
    rejectedSubmissions: Paginated[Submission] = Paginated(Seq.empty, Page(), 0),
    cancelledSubmissions: Paginated[Submission] = Paginated(Seq.empty, Page(), 0)
  ): Html =
    page(SubmissionsPagesElements(otherSubmissions, actionSubmissions, rejectedSubmissions, cancelledSubmissions))(request, messages)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
  }

  private val zone: ZoneId = ZoneId.of("UTC")

  val actionSubmission =
    Action(requestType = SubmissionRequest, id = "conv-id", requestTimestamp = ZonedDateTime.of(2019, 1, 1, 12, 0, 0, 0, zone), notifications = None)

  val actionCancellation = Action(
    requestType = CancellationRequest,
    id = "conv-id",
    requestTimestamp = ZonedDateTime.of(2021, 6, 1, 12, 0, 0, 0, zone),
    notifications = None
  )

  def submissionWithStatus(status: EnhancedStatus = GOODS_ARRIVED, ducr: String = "ducr") =
    Submission(
      uuid = "id",
      eori = "eori",
      lrn = "lrn",
      mrn = Some("mrn"),
      ducr = Some(ducr),
      latestEnhancedStatus = Some(status),
      actions = List(actionSubmission, actionCancellation)
    )

  def submissions(status: EnhancedStatus = GOODS_ARRIVED): Paginated[Submission] =
    Paginated(List(submissionWithStatus(status)), Page(), 1)

  def submissions(submissions: List[Submission]): Paginated[Submission] = Paginated(submissions, Page(), 1)
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

    "display the expected tab title as a link" in {
      val view = createView()

      val testExpectedTabTitleAsLink =
        (tab: String) => view.getElementsByAttributeValue("href", s"#${tab}-submissions").text == messages(s"submissions.${tab}.tab.title")

      assert(List("submitted", "action", "rejected", "cancelled").forall(testExpectedTabTitleAsLink))
    }

    "display the expected tab hints" when {
      "there are submissions" in {
        val view = createView(submissions(RECEIVED), submissions(ADDITIONAL_DOCUMENTS_REQUIRED), submissions(ERRORS), submissions(CANCELLED))

        view.getElementById("submitted-content-hint").text must include(messages(s"submissions.submitted.content.hint"))
        view.getElementById("action-content-hint").text must include(Html(messages(s"submissions.action.content.hint", "<br />")).text)
        view.getElementById("rejected-content-hint").text must include(Html(messages(s"submissions.rejected.content.hint", "<br />")).text)
        view.getElementById("cancelled-content-hint").text must include(messages(s"submissions.cancelled.content.hint"))
      }
    }

    "display one pagination summary" when {
      "there are submissions" in {
        val view = createView(submissions(RECEIVED), submissions(ADDITIONAL_DOCUMENTS_REQUIRED), submissions(ERRORS), submissions(CANCELLED))
        val paginationText = s"${messages("site.pagination.showing")} 1 ${messages("submissions.pagination.singular")}"

        val testPaginationSummary =
          (tab: String) => paginationText == view.getElementById(s"${tab}-submissions").getElementsByClass("ceds-pagination__summary").text

        assert(List("submitted", "action", "rejected", "cancelled").forall(testPaginationSummary))
      }
    }

    "display no pagination summary" when {
      "there are no submissions" in {
        val view = createView()
        val noDeclarations = messages("submissions.empty.tab")

        val testNoPaginationSummary =
          (tab: String) => noDeclarations == view.getElementById(s"${tab}-submissions").getElementsByClass("ceds-pagination__summary").text

        assert(List("submitted", "action", "rejected", "cancelled").forall(testNoPaginationSummary))
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
          val view = tab("submitted", createView(otherSubmissions = submissions()))

          val mrnLink = tableCell(view)(1, 0)
          mrnLink must containText("mrn")
          mrnLink must containMessage("submissions.hidden.text", "ducr")
          tableCell(view)(1, 1).text mustBe "ducr"
          tableCell(view)(1, 2).text mustBe "lrn"
          tableCell(view)(1, 3).text mustBe "1 January 2019 at 12:00pm"
          tableCell(view)(1, 4).text mustBe messages("submission.enhancedStatus.GOODS_ARRIVED")
          val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
          decInformationLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage("id").url
        }

        "during BST" in {
          val bstActionSubmission = Action(
            requestType = SubmissionRequest,
            id = "conv-id",
            requestTimestamp = ZonedDateTime.of(2019, 5, 1, 12, 45, 0, 0, zone),
            notifications = None
          )
          val bstSubmission = Submission(
            uuid = "id",
            eori = "eori",
            lrn = "lrn",
            mrn = Some("mrn"),
            ducr = Some("ducr"),
            latestEnhancedStatus = Some(GOODS_ARRIVED),
            actions = List(bstActionSubmission, actionCancellation)
          )
          val view = tab("submitted", createView(otherSubmissions = submissions(List(bstSubmission))))

          val mrnLink = tableCell(view)(1, 0)
          mrnLink must containText("mrn")
          mrnLink must containMessage("submissions.hidden.text", "ducr")
          tableCell(view)(1, 1).text mustBe "ducr"
          tableCell(view)(1, 2).text mustBe "lrn"
          tableCell(view)(1, 3).text mustBe "1 May 2019 at 1:45pm"
          tableCell(view)(1, 4).text mustBe messages("submission.enhancedStatus.GOODS_ARRIVED")
          val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
          decInformationLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage("id").url
        }
      }

      "optional fields are unpopulated" in {
        val submissionWithOptionalFieldsEmpty = submissionWithStatus().copy(ducr = None, mrn = None)
        val view = tab("submitted", createView(otherSubmissions = submissions(List(submissionWithOptionalFieldsEmpty))))

        tableCell(view)(1, 1).text mustBe empty
        tableCell(view)(1, 2).text mustBe "lrn"
        tableCell(view)(1, 3).text mustBe "1 January 2019 at 12:00pm"
        tableCell(view)(1, 4).text mustBe messages("submission.enhancedStatus.GOODS_ARRIVED")
        val decInformationLink = tableCell(view)(1, 0).getElementsByTag("a").first()
        decInformationLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage("id").url
      }

      "optional mrn field is populated with default" in {
        val submissionWithOptionalFieldsEmpty = submissionWithStatus().copy(ducr = None, mrn = None)
        val view = tab("submitted", createView(otherSubmissions = submissions(List(submissionWithOptionalFieldsEmpty))))
        tableCell(view)(1, 0) must containMessage("submissions.declarationDetails.mrn.pending")
      }

      "submission status is 'pending' due to missing notification" in {
        val submission = submissionWithStatus().copy(latestEnhancedStatus = None)
        val view = tab("submitted", createView(otherSubmissions = submissions(List(submission))))
        tableCell(view)(1, 4).text mustBe "Pending"
      }

      "submission has link when contains rejected notification" in {
        val submission = submissionWithStatus(EXPIRED_NO_ARRIVAL)
        val view = tab("rejected", createView(rejectedSubmissions = submissions(List(submission))))

        tableCell(view)(1, 0).text must include(submission.ducr.get)
        tableCell(view)(1, 0).toString must include(routes.DeclarationDetailsController.displayPage(submission.uuid).url)
      }

      "submission date is unknown due to missing submit action" in {
        val submissionWithMissingSubmitAction = submissionWithStatus().copy(actions = List(actionCancellation))
        val view = tab("submitted", createView(otherSubmissions = submissions(List(submissionWithMissingSubmitAction))))
        tableCell(view)(1, 3).text mustBe empty
      }

      "submissions are shown on correct tabs" when {
        "submitted" in {

          val otherSubmissions =
            List(
              submissionWithStatus(RECEIVED, ducr = "ducr_accepted_received"),
              submissionWithStatus(GOODS_ARRIVED_MESSAGE, ducr = "ducr_accepted_arrived_msg"),
              submissionWithStatus(GOODS_ARRIVED, ducr = "ducr_accepted_msg"),
              submissionWithStatus(CLEARED, ducr = "ducr_accepted_cleared"),
              submissionWithStatus(AWAITING_EXIT_RESULTS, ducr = "ducr_accepted_awaiting"),
              submissionWithStatus(GOODS_HAVE_EXITED, ducr = "ducr_accepted_exited"),
              submissionWithStatus(RELEASED, ducr = "ducr_accepted_released"),
              submissionWithStatus(UNDERGOING_PHYSICAL_CHECK, ducr = "ducr_accepted_check"),
              submissionWithStatus(DECLARATION_HANDLED_EXTERNALLY, ducr = "ducr_accepted_external"),
              submissionWithStatus(UNKNOWN, ducr = "ducr_accepted_unknown"),
              submissionWithStatus(PENDING, ducr = "ducr_accepted_pending"),
              submissionWithStatus(AMENDED, ducr = "ducr_accepted_amended")
            )

          val view = createView(
            otherSubmissions = submissions(otherSubmissions),
            actionSubmissions = submissions(List()),
            rejectedSubmissions = submissions(List()),
            cancelledSubmissions = submissions(List())
          )

          tab("action", view).text must include(messages("submissions.empty.tab"))
          tab("rejected", view).text must include(messages("submissions.empty.tab"))
          tab("cancelled", view).text must include(messages("submissions.empty.tab"))

          otherSubmissions.zipWithIndex.foreach { case (submissions, index) =>
            tableCell(tab("submitted", view))(index + 1, 0).text must include(submissions.ducr.get)
          }

        }
        "action needed" in {

          val actionSubmissions =
            List(
              submissionWithStatus(ADDITIONAL_DOCUMENTS_REQUIRED, "ducr_action_docs"),
              submissionWithStatus(QUERY_NOTIFICATION_MESSAGE, "ducr_action_query")
            )

          val view = createView(
            otherSubmissions = submissions(List()),
            actionSubmissions = submissions(actionSubmissions),
            rejectedSubmissions = submissions(List()),
            cancelledSubmissions = submissions(List())
          )

          tab("submitted", view).text must include(messages("submissions.empty.tab"))
          tab("rejected", view).text must include(messages("submissions.empty.tab"))
          tab("cancelled", view).text must include(messages("submissions.empty.tab"))

          tableCell(tab("action", view))(1, 0).text must include("ducr_action_docs")
          tableCell(tab("action", view))(2, 0).text must include("ducr_action_query")
        }
        "rejected" in {

          val rejectedSubmission = List(submissionWithStatus(ERRORS, "ducr_rejected"))

          val view = createView(
            otherSubmissions = submissions(List.empty),
            actionSubmissions = submissions(List.empty),
            rejectedSubmissions = submissions(rejectedSubmission),
            cancelledSubmissions = submissions(List.empty)
          )

          tab("submitted", view).text must include(messages("submissions.empty.tab"))
          tab("action", view).text must include(messages("submissions.empty.tab"))
          tab("cancelled", view).text must include(messages("submissions.empty.tab"))

          tableCell(tab("rejected", view))(1, 0).text must include("ducr_rejected")
        }
        "cancelled" in {

          val cancelledSubmission = List(
            submissionWithStatus(CANCELLED, "ducr_cancelled"),
            submissionWithStatus(WITHDRAWN, "ducr_cancelled_withdrawn"),
            submissionWithStatus(EXPIRED_NO_DEPARTURE, "ducr_cancelled_no_departure"),
            submissionWithStatus(EXPIRED_NO_ARRIVAL, "ducr_cancelled_no_arrival")
          )

          val view = createView(
            otherSubmissions = submissions(List.empty),
            actionSubmissions = submissions(List.empty),
            rejectedSubmissions = submissions(List.empty),
            cancelledSubmissions = submissions(cancelledSubmission)
          )

          tab("submitted", view).text must include(messages("submissions.empty.tab"))
          tab("action", view).text must include(messages("submissions.empty.tab"))
          tab("rejected", view).text must include(messages("submissions.empty.tab"))

          tableCell(tab("cancelled", view))(1, 0).text must include("ducr_cancelled")
          tableCell(tab("cancelled", view))(2, 0).text must include("ducr_cancelled_withdrawn")
          tableCell(tab("cancelled", view))(3, 0).text must include("ducr_cancelled_no_departure")
          tableCell(tab("cancelled", view))(4, 0).text must include("ducr_cancelled_no_arrival")
        }
      }

      "submissions without status are shown on 'other' tab" in {
        val view = createView(otherSubmissions = submissions(List(submissionWithStatus(CLEARED, "ducr_pending"))))
        tableCell(tab("submitted", view))(1, 0).text must include("ducr_pending")
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
