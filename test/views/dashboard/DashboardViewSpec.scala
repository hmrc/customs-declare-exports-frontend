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

package views.dashboard

import base.OverridableInjector
import config.PaginationConfig
import config.featureFlags.SecureMessagingConfig
import controllers.routes
import controllers.routes.{ChoiceController, DashboardController, DeclarationDetailsController}
import forms.Choice
import forms.Choice.AllowedChoiceValues.Dashboard
import models.PageOfSubmissions
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.StatusGroup._
import models.declaration.submissions.{EnhancedStatus, Submission, SubmissionAction}
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatest.Assertion
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import views.dashboard.DashboardHelper._
import views.declaration.spec.UnitViewSpec
import views.helpers.ViewDates
import views.html.dashboard.dashboard
import views.tags.ViewTest

import java.time.{ZoneId, ZonedDateTime}

@ViewTest
class DashboardViewSpec extends UnitViewSpec with ExportsTestHelper {

  private val mockPaginationConfig = mock[PaginationConfig]

  private val injector = new OverridableInjector(
    bind[PaginationConfig].toInstance(mockPaginationConfig),
    bind[SecureMessagingConfig].toInstance(mockSecureMessagingConfig)
  )

  private val itemsPerPage = 4

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockPaginationConfig.itemsPerPage).thenReturn(itemsPerPage)
    when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
  }

  private val uuid = "id"
  private val lrn = "lrn"
  private val mrn = "mrn"
  private val ducr = "ducr"

  private val dateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))

  def submission(lastStatusUpdate: ZonedDateTime, status: EnhancedStatus = RECEIVED): Submission =
    Submission(
      uuid = uuid,
      eori = "eori",
      lrn = lrn,
      mrn = Some(mrn),
      ducr = Some(ducr),
      latestEnhancedStatus = Some(status),
      enhancedStatusLastUpdated = Some(lastStatusUpdate),
      actions = List(SubmissionAction("actionId", lastStatusUpdate, None, uuid))
    )

  def listOfSubmissions(status: EnhancedStatus = RECEIVED, size: Int = 1): Seq[Submission] =
    (1 to size).map(seconds => submission(dateTime.minusSeconds(seconds), status)).toList

  private val page = injector.instanceOf[dashboard]

  private def request(statusGroup: StatusGroup, currentPage: Int): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", s"/dashboard?${Groups}=${statusGroup}&${Page}=${currentPage}")

  private def createView(status: EnhancedStatus = RECEIVED, totalSubmissionsInPage: Int = 0, totalSubmissionsInGroup: Int = 0): Html = {
    val statusGroup = toStatusGroup(status)
    val pageOfSubmissions = PageOfSubmissions(statusGroup, totalSubmissionsInGroup, listOfSubmissions(status, totalSubmissionsInPage))
    page(pageOfSubmissions)(request(statusGroup, 1), messages)
  }

  private def createView(submissionsInPage: Seq[Submission], totalSubmissionsInGroup: Int, currentPage: Int): Html = {
    val statusGroup = toStatusGroup(submissionsInPage.head)
    page(PageOfSubmissions(statusGroup, totalSubmissionsInGroup, submissionsInPage))(request(statusGroup, currentPage), messages)
  }

  private val statuses = EnhancedStatus.values.toList
  private val statusGroups = List(SubmittedStatuses, ActionRequiredStatuses, RejectedStatuses, CancelledStatuses)

  "Dashboard View" should {
    val view = createView()

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
        Option(view.getElementById("navigation-banner")) mustBe None
      }
    }

    "not have View declaration summary link" in {
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    "display 'Back' button that links to 'Choice' page with Submissions selected" in {
      val backButton = view.getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(ChoiceController.displayPage(Some(Choice(Dashboard))))
    }

    "display same page title as header" in {
      view.title must startWith(view.getElementsByTag("h1").text)
    }

    "display the expected page title" in {
      view.getElementsByTag("h1").text mustBe messages("dashboard.title")
    }

    "display the expected tab titles as links" in {
      statusGroups.foreach { statusGroup =>
        val href = view.getElementsByAttributeValue("href", s"#${statusGroup}-submissions")
        href.text == messages(s"dashboard.${statusGroup}.tab.title")
      }
    }

    "contain a 'Start a new declaration' link" in {
      val startButton = view.getElementsByClass("govuk-button").first()
      startButton must containMessage("dashboard.start.new.declaration")
      startButton.attr("href") mustBe ChoiceController.displayPage().url
    }
  }

  "Dashboard View" should {

    "have NO table for a tab when not the current one" in {
      statuses.foreach { currentStatus =>
        val currentStatusGroup = toStatusGroup(currentStatus)
        val view = createView(currentStatus, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)

        statuses.filterNot(toStatusGroup(_) == currentStatusGroup).foreach { otherStatus =>
          val statusGroup = toStatusGroup(otherStatus)
          val table = view.getElementById(s"${statusGroup}-submissions")
          table.childrenSize mustBe 0
        }
      }
    }

    "display the submissions in the expected tab" in {
      statuses.foreach { status =>
        val currentStatusGroup = toStatusGroup(status)
        val view: Document = createView(status, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)

        statusGroups.foreach { statusGroup =>
          val childrenSize = view.getElementById(s"${statusGroup}-submissions").childrenSize
          if (childrenSize > 0) statusGroup mustBe currentStatusGroup else statusGroup must not be currentStatusGroup
        }
      }
    }
  }

  "Dashboard View" when {

    "there are NO submissions for the selected tab" should {

      "display the no-action-needed hint" in {
        createView().getElementsByClass("govuk-warning-text").text mustBe messages("dashboard.hint.no.action.needed")
      }

      "display a 'No declarations' message" in {
        val expectedMessage = messages("dashboard.empty.tab")

        statuses.foreach { status =>
          val statusGroup = toStatusGroup(status)
          val element = createView(status).getElementById(s"${statusGroup}-submissions")
          element.getElementsByClass("ceds-pagination__summary").text mustBe expectedMessage
        }
      }
    }

    "there are submissions for the selected tab" should {

      "display the expected tab hints" in {
        statuses.foreach { status =>
          val view = createView(status, totalSubmissionsInPage = 2, totalSubmissionsInGroup = 2)
          val statusGroup = toStatusGroup(status)
          val expectedMessage = messages(s"dashboard.${statusGroup}.content.hint").replace("{0}", "")
          view.getElementById(s"${statusGroup}-content-hint").text mustBe expectedMessage
        }
      }

      "display the page summary" when {

        "there is a single submission" in {
          val expectedMessage = s"${messages("site.pagination.showing")} 1 ${messages("dashboard.pagination.singular")}"

          statuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)
            val element = view.getElementById(s"${statusGroup}-submissions")
            element.getElementsByClass("ceds-pagination__summary").text mustBe expectedMessage
          }
        }

        "there is a single page of submissions" in {
          val expectedMessage = s"${messages("site.pagination.showing")} $itemsPerPage ${messages("dashboard.pagination.plural")}"

          statuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, itemsPerPage, itemsPerPage)
            val element = view.getElementById(s"${statusGroup}-submissions")
            element.getElementsByClass("ceds-pagination__summary").text mustBe expectedMessage
          }
        }

        "there are multiple pages of submissions" in {
          val totalSubmissionInGroup = itemsPerPage * 2
          val showing = messages("site.pagination.showing")
          val of = messages("site.pagination.of")
          val plural = messages("dashboard.pagination.plural")

          val expectedMessage = s"$showing 1 – $itemsPerPage $of $totalSubmissionInGroup $plural"

          statuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, itemsPerPage, totalSubmissionInGroup)
            val element = view.getElementById(s"${statusGroup}-submissions")
            element.getElementsByClass("ceds-pagination__summary").text mustBe expectedMessage
          }
        }
      }

      "NOT display the pagination controls" when {
        "there is a single page of submissions" in {
          statuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, totalSubmissionsInPage = itemsPerPage, totalSubmissionsInGroup = itemsPerPage)
            val element = view.getElementById(s"${statusGroup}-submissions")
            element.getElementsByClass("ceds-pagination__controls").size mustBe 0
          }
        }
      }

      "display the pagination controls" when {
        val lastPage = 2
        val path = DashboardController.displayPage

        def page(element: Element, page: String, maybeHref: Option[String] = None): Assertion = {
          element.text mustBe page
          maybeHref.fold {
            assert(element.hasClass("ceds-pagination__item--active"))
          } { href =>
            element.child(0).attr("href") mustBe href
          }
        }

        def pageControls(submissions: Seq[Submission], totalSubmissionsInGroup: Int, currentPage: Int): Elements = {
          val view = createView(submissions, totalSubmissionsInGroup, currentPage)
          val statusGroup = toStatusGroup(submissions.head)
          val element = view.getElementById(s"${statusGroup}-submissions")
          val pagination = element.selectFirst("ul.ceds-pagination__items")
          pagination.children()
        }

        "there are multiple pages of submissions and" when {

          "the current page is '1'" in {
            statuses.foreach { status =>
              val submissions = listOfSubmissions(status, itemsPerPage)
              val controls = pageControls(submissions, itemsPerPage * 3 + lastPage, 1)
              controls.size mustBe 5

              val statusGroup = toStatusGroup(status)
              val datetimeForNextPage = toUTC(submissions.last.enhancedStatusLastUpdated.get)

              val expectedNextPageHref = Some(s"${path}?${Groups}=${statusGroup}&${Page}=2&${DatetimeForNextPage}=${datetimeForNextPage}")
              val expectedLoosePageHref = Some(s"${path}?${Groups}=${statusGroup}&${Page}=3")
              val expectedLastPageHref = Some(s"${path}?${Groups}=${statusGroup}&${Limit}=${lastPage}")

              page(controls.get(0), "1")
              page(controls.get(1), "2", expectedNextPageHref)
              page(controls.get(2), "3", expectedLoosePageHref)
              page(controls.get(3), "4", expectedLastPageHref)
              page(controls.get(4), "Next", expectedNextPageHref)
            }
          }

          "the current page is '3'" in {
            statuses.foreach { status =>
              val submissions = listOfSubmissions(status, itemsPerPage)
              val controls = pageControls(submissions, itemsPerPage * 5 + lastPage, 3)
              controls.size mustBe 8

              val statusGroup = toStatusGroup(status)
              val datetimeForPreviousPage = toUTC(submissions.head.enhancedStatusLastUpdated.get)
              val datetimeForNextPage = toUTC(submissions.last.enhancedStatusLastUpdated.get)

              val expectedPreviousPageHref = Some(s"${path}?${Groups}=${statusGroup}&${Page}=2&${DatetimeForPreviousPage}=${datetimeForPreviousPage}")
              val expectedFirstPageHref = Some(s"${path}?${Groups}=${statusGroup}&${Page}=1")
              val expectedNextPageHref = Some(s"${path}?${Groups}=${statusGroup}&${Page}=4&${DatetimeForNextPage}=${datetimeForNextPage}")
              val expectedLoosePageHref = Some(s"${path}?${Groups}=${statusGroup}&${Page}=5")
              val expectedLastPageHref = Some(s"${path}?${Groups}=${statusGroup}&${Limit}=${lastPage}")

              page(controls.get(0), "Previous", expectedPreviousPageHref)
              page(controls.get(1), "1", expectedFirstPageHref)
              page(controls.get(2), "2", expectedPreviousPageHref)
              page(controls.get(3), "3")
              page(controls.get(4), "4", expectedNextPageHref)
              page(controls.get(5), "5", expectedLoosePageHref)
              page(controls.get(6), "6", expectedLastPageHref)
              page(controls.get(7), "Next", expectedNextPageHref)
            }
          }
        }
      }

      "display the table headers" in {
        val view = createView(RECEIVED, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)

        def tableHead(column: Int): Element =
          view.select(".govuk-table__head").first.getElementsByClass("govuk-table__header").get(column)

        tableHead(0).text mustBe messages("dashboard.header.mrn")
        tableHead(1).text mustBe messages("dashboard.header.ducr")
        tableHead(2).text mustBe messages("dashboard.header.lrn")
        tableHead(3).text mustBe messages("dashboard.header.dateAndTime")
        tableHead(4).text mustBe messages("dashboard.header.status")
      }

      "display the submissions in rows and columns" when {
        // def tab(statusGroup: StatusGroup): Element = view.getElementById(s"$statusGroup-submissions")

        def tableCell(view: Html, row: Int, column: Int): Element =
          view.select(".govuk-table__row").get(row).getElementsByClass("govuk-table__cell").get(column)

        "all fields are populated" in {
          val submissionsInPage = 2
          val view = createView(RECEIVED, submissionsInPage, submissionsInPage)

          (1 to submissionsInPage).foreach { row =>
            val mrnLink = tableCell(view, row, 0).getElementsByTag("a").first
            mrnLink.child(0).text mustBe mrn
            mrnLink.child(1).text mustBe messages("dashboard.hidden.text", ducr)
            mrnLink.attr("href") mustBe DeclarationDetailsController.displayPage(uuid).url

            tableCell(view, row, 1).text mustBe ducr
            tableCell(view, row, 2).text mustBe lrn
            tableCell(view, row, 3).text mustBe ViewDates.formatDateAtTime(dateTime.minusSeconds(row))
            tableCell(view, row, 4).text mustBe messages("submission.enhancedStatus.RECEIVED")
          }
        }

        "optional fields are unpopulated" in {
          val lastStatusUpdate = ZonedDateTime.now
          val view = createView(List(submission(lastStatusUpdate).copy(ducr = None, mrn = None, latestEnhancedStatus = None)), 1, 1)

          val mrnLink = tableCell(view, 1, 0).getElementsByTag("a").first
          mrnLink.child(0).text mustBe messages("dashboard.mrn.pending")
          mrnLink.attr("href") mustBe DeclarationDetailsController.displayPage(uuid).url

          tableCell(view, 1, 1).text mustBe empty
          tableCell(view, 1, 2).text mustBe lrn
          tableCell(view, 1, 3).text mustBe ViewDates.formatDateAtTime(lastStatusUpdate)
          tableCell(view, 1, 4).text mustBe messages("submission.enhancedStatus.PENDING")
        }
      }
    }
  }
}
