/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.routes.{ChoiceController, DashboardController}
import controllers.timeline.routes.DeclarationDetailsController
import models.PageOfSubmissions
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.StatusGroup._
import models.declaration.submissions.{Action, EnhancedStatus, Submission}
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.i18n.Lang
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{stubLangs, stubMessagesApi}
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec
import views.dashboard.DashboardHelper._
import views.helpers.ViewDates
import views.html.dashboard.dashboard
import views.tags.ViewTest

import java.time.{ZoneId, ZonedDateTime}
import scala.jdk.CollectionConverters.ListHasAsScala

@ViewTest
class DashboardViewSpec extends UnitViewSpec with BeforeAndAfterEach with ExportsTestHelper {

  private val mockPaginationConfig = mock[PaginationConfig]

  private val injector = new OverridableInjector(bind[PaginationConfig].toInstance(mockPaginationConfig))

  private val itemsPerPage = 4

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockPaginationConfig.itemsPerPage).thenReturn(itemsPerPage)
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
      actions = List(Action("actionId", SubmissionRequest, lastStatusUpdate, None, Some(uuid), 1)),
      latestDecId = Some(uuid)
    )

  def listOfSubmissions(status: EnhancedStatus = RECEIVED, size: Int = 1): Seq[Submission] =
    (1 to size).map(seconds => submission(dateTime.minusSeconds(seconds), status)).toList

  private val page = injector.instanceOf[dashboard]

  private def request(statusGroup: StatusGroup, currentPage: Int): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", s"/dashboard?$Groups=$statusGroup&$Page=$currentPage")

  private def createView(status: EnhancedStatus = RECEIVED, totalSubmissionsInPage: Int = 0, totalSubmissionsInGroup: Int = 0): Html = {
    val statusGroup = toStatusGroup(status)
    val pageOfSubmissions = PageOfSubmissions(statusGroup, totalSubmissionsInGroup, listOfSubmissions(status, totalSubmissionsInPage))
    page(pageOfSubmissions)(request(statusGroup, 1), messages)
  }

  private def createView(submissionsInPage: Seq[Submission], totalSubmissionsInGroup: Int, currentPage: Int): Html = {
    val statusGroup = toStatusGroup(submissionsInPage.head)
    page(PageOfSubmissions(statusGroup, totalSubmissionsInGroup, submissionsInPage))(request(statusGroup, currentPage), messages)
  }

  private val enhancedStatuses = EnhancedStatus.values.toList
  private val statusGroups = List(SubmittedStatuses, ActionRequiredStatuses, RejectedStatuses, CancelledStatuses)

  private val filterId = "filters"

  "Dashboard View" should {
    val view = createView()

    List(Lang("en"), Lang("cy")).foreach { lang =>
      s"have the 'lang' attribute of the '<html' tag set to ${lang.code}" in {
        val statusGroup = toStatusGroup(RECEIVED)
        val pageOfSubmissions = PageOfSubmissions(statusGroup, 0, listOfSubmissions())
        val messages = stubMessagesApi(langs = stubLangs(List(lang))).preferred(List(lang))
        val view = page(pageOfSubmissions)(request(statusGroup, 1), messages)
        view.getElementsByTag("html").get(0).attr("lang").take(2) mustBe lang.code
      }
    }

    "not have View declaration summary link" in {
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    "display 'Back' button that links to 'Choice' page with Submissions selected" in {
      val backButton = view.getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(ChoiceController.displayPage)
    }

    "display the link to the button group" in {
      val link = view.getElementsByClass("govuk-skip-link").get(1)
      link.tagName mustBe "a"
      link.text mustBe messages("dashboard.button.group.link")
      link.attr("href") mustBe s"#$filterId"
    }

    "display same page title as the header" in {
      view.title must startWith(view.getElementsByTag("h1").text)
    }

    "display the expected page title" in {
      enhancedStatuses.foreach { status =>
        val statusGroup = toStatusGroup(status)
        createView(status).getElementsByTag("h1").text mustBe messages(s"dashboard.title.$statusGroup")
      }
    }

    "display the expected 'Save for 180 days' hint" in {
      enhancedStatuses.foreach { status =>
        val statusGroup = toStatusGroup(status)
        val expectedMessage = messages("dashboard.notification.180.days", messages(s"dashboard.notification.180.days.$statusGroup"))
        createView(status).getElementsByClass("govuk-body").get(0).text mustBe expectedMessage
      }
    }

    "display the refine-the-status hint" in {
      view.getElementsByClass("govuk-body").get(1).text mustBe messages("dashboard.check.status.hint")
    }

    "display the expected group of buttons for status filtering" in {
      val navigation = view.getElementById(filterId)
      navigation.tagName mustBe "nav"

      val buttonGroup = navigation.getElementsByClass("govuk-button-group").get(0)

      val links = buttonGroup.getElementsByTag("a")
      links.size mustBe 4

      statusGroups.zipWithIndex.foreach { case (statusGroup, index) =>
        val link = links.get(index)
        assert(link.hasClass("govuk-button"))
        link.text mustBe messages(s"dashboard.$statusGroup.button.text")
        link.attr("href") mustBe s"/customs-declare-exports/dashboard?groups=$statusGroup&page=1"

        link.hasClass("selected-status-group") mustBe (statusGroup == SubmittedStatuses)
        link.attr("aria-pressed") mustBe (statusGroup == SubmittedStatuses).toString
      }
    }

    "contain a 'Start a new declaration' link" in {
      val startButton = view.getElementsByClass("govuk-button").last
      startButton must containMessage("dashboard.start.new.declaration")
      startButton.attr("href") mustBe ChoiceController.displayPage.url
    }
  }

  "Dashboard View" when {

    "there are NO submissions for the selected status group" should {
      "display a 'No declarations' message" in {
        val expectedMessage = messages("dashboard.status.group.empty")

        enhancedStatuses.foreach { status =>
          val statusGroup = toStatusGroup(status)
          val view = createView(status)

          val element = view.getElementById(s"$statusGroup-submissions")
          element.getElementsByClass("page-summary").text mustBe expectedMessage

          val title = view.getElementsByTag("title").first
          val statusText = messages(s"dashboard.title.$statusGroup")
          title.text mustBe s"$statusText - Page 1 of 1 - ${messages("service.name")} - GOV.UK"
        }
      }
    }

    "there are submissions for the selected status group" should {

      "display header and hint for the selected status group" in {
        enhancedStatuses.foreach { status =>
          val currentStatusGroup = toStatusGroup(status)
          val view = createView(status, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)

          statusGroups.foreach { statusGroup =>
            Option(view.getElementById(s"$statusGroup-submissions")).fold {
              statusGroup must not be currentStatusGroup
            } { container =>
              statusGroup mustBe currentStatusGroup
              container.getElementsByClass(s"$statusGroup-heading").text mustBe messages(s"dashboard.$statusGroup.heading")
              container.getElementsByClass(s"$statusGroup-content-hint").text mustBe messages(s"dashboard.$statusGroup.content.hint")
            }
          }
        }
      }

      "display the page summary" when {

        "there is a single submission" in {
          val expectedMessage = s"${messages("site.pagination.showing")} 1 ${messages("dashboard.pagination.singular")}"

          enhancedStatuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)
            val element = view.getElementById(s"$statusGroup-submissions")
            element.getElementsByClass("page-summary").text mustBe expectedMessage
          }
        }

        "there is a single page of submissions" in {
          val expectedMessage = s"${messages("site.pagination.showing")} $itemsPerPage ${messages("dashboard.pagination.plural")}"

          enhancedStatuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, itemsPerPage, itemsPerPage)
            val element = view.getElementById(s"$statusGroup-submissions")
            element.getElementsByClass("page-summary").text mustBe expectedMessage
          }
        }

        "there are multiple pages of submissions" in {
          val totalSubmissionInGroup = itemsPerPage * 2
          val showing = messages("site.pagination.showing")
          val of = messages("site.pagination.of")
          val plural = messages("dashboard.pagination.plural")

          val expectedMessage = s"$showing 1 – $itemsPerPage $of $totalSubmissionInGroup $plural"

          enhancedStatuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, itemsPerPage, totalSubmissionInGroup)
            val element = view.getElementById(s"$statusGroup-submissions")
            element.getElementsByClass("page-summary").text mustBe expectedMessage
          }
        }
      }

      "NOT display the pagination controls" when {
        "there is a single page of submissions" in {
          enhancedStatuses.foreach { status =>
            val statusGroup = toStatusGroup(status)
            val view = createView(status, totalSubmissionsInPage = itemsPerPage, totalSubmissionsInGroup = itemsPerPage)
            val element = view.getElementById(s"$statusGroup-submissions")
            element.getElementsByClass("ceds-pagination__controls").size mustBe 0
          }
        }
      }

      "display the pagination controls" when {
        "there are multiple pages of submissions and" when {
          val lastPage = 2
          val path = DashboardController.displayPage

          def page(element: Element, page: String, maybeHref: Option[String] = None): Assertion = {
            element.text mustBe page
            maybeHref.fold {
              assert(element.hasClass("govuk-pagination__item--current"))
            } { href =>
              element.child(0).attr("href") mustBe href
            }
          }

          def pageControls(submissions: Seq[Submission], totalSubmissionsInGroup: Int, currentPage: Int, pageOf: Int): Seq[Element] = {
            val view = createView(submissions, totalSubmissionsInGroup, currentPage)
            val statusGroup = toStatusGroup(submissions.head)

            val title = view.getElementsByTag("title").first
            val statusText = messages(s"dashboard.title.$statusGroup")
            title.text mustBe s"$statusText - Page $currentPage of $pageOf - ${messages("service.name")} - GOV.UK"

            val element = view.getElementById(s"$statusGroup-submissions")
            val pagination = element.getElementsByClass("govuk-pagination").first
            pagination.children().asScala.toList.foldLeft(List.empty[Element]) { (list, element) =>
              if (element.tagName == "div") list :+ element // Previous or Next page
              else if (element.tagName == "ul") list ++ element.getElementsByClass("govuk-pagination__item").asScala.toList
              else list
            }
          }

          "the current page is '1'" in {
            enhancedStatuses.foreach { status =>
              val submissions = listOfSubmissions(status, itemsPerPage)
              val controls = pageControls(submissions, itemsPerPage * 3 + lastPage, 1, 4)
              controls.size mustBe 5

              val statusGroup = toStatusGroup(status)
              val datetimeForNextPage = toUTC(submissions.last.enhancedStatusLastUpdated.get)
              val uuidForNextPage = submissions.last.uuid

              val expectedNextPageHref = Some(s"$path?$Groups=$statusGroup&$Page=2&$DatetimeForNextPage=$datetimeForNextPage&$Uuid=$uuidForNextPage")
              val expectedLoosePage3Href = Some(s"$path?$Groups=$statusGroup&$Page=3")
              val expectedLoosePage4Href = Some(s"$path?$Groups=$statusGroup&$Page=4")

              page(controls(0), "1")
              page(controls(1), "2", expectedNextPageHref)
              page(controls(2), "3", expectedLoosePage3Href)
              page(controls(3), "4", expectedLoosePage4Href)
              page(controls(4), "Next", expectedNextPageHref)
            }
          }

          "the current page is '3'" in {
            enhancedStatuses.foreach { status =>
              val submissions = listOfSubmissions(status, itemsPerPage)
              val controls = pageControls(submissions, itemsPerPage * 5 + lastPage, 3, 6)
              controls.size mustBe 8

              val statusGroup = toStatusGroup(status)
              val datetimeForPreviousPage = toUTC(submissions.head.enhancedStatusLastUpdated.get)
              val datetimeForNextPage = toUTC(submissions.last.enhancedStatusLastUpdated.get)
              val uuidForNextPage = submissions.last.uuid
              val uuidForPreviousPage = submissions.head.uuid

              val expectedPreviousPageHref = Some(
                s"$path?$Groups=$statusGroup&$Page=2&$DatetimeForPreviousPage=" +
                  s"$datetimeForPreviousPage&$Uuid=$uuidForPreviousPage"
              )
              val expectedFirstPageHref = Some(s"$path?$Groups=$statusGroup&$Page=1")
              val expectedNextPageHref = Some(s"$path?$Groups=$statusGroup&$Page=4&$DatetimeForNextPage=$datetimeForNextPage&$Uuid=$uuidForNextPage")
              val expectedLoosePage5Href = Some(s"$path?$Groups=$statusGroup&$Page=5")
              val expectedLoosePage6Href = Some(s"$path?$Groups=$statusGroup&$Page=6")

              page(controls(0), "Previous", expectedPreviousPageHref)
              page(controls(1), "1", expectedFirstPageHref)
              page(controls(2), "2", expectedPreviousPageHref)
              page(controls(3), "3")
              page(controls(4), "4", expectedNextPageHref)
              page(controls(5), "5", expectedLoosePage5Href)
              page(controls(6), "6", expectedLoosePage6Href)
              page(controls(7), "Next", expectedNextPageHref)
            }
          }
        }
      }

      "display the table headers" in {
        val view = createView(RECEIVED, totalSubmissionsInPage = 1, totalSubmissionsInGroup = 1)

        val headers = view.getElementsByClass("govuk-table__header")

        headers.get(0).text mustBe messages("dashboard.header.mrn")
        headers.get(1).text mustBe messages("dashboard.header.ducr")
        headers.get(2).text mustBe messages("dashboard.header.lrn")
        headers.get(3).text mustBe messages("dashboard.header.updated.on")
        headers.get(4).text mustBe messages("dashboard.header.status")
      }

      "display the submissions in rows and columns" when {
        def tableCell(view: Html, row: Int, column: Int): Element =
          view.select(".govuk-table__row").get(row).getElementsByClass("govuk-table__cell").get(column)

        "all fields are populated" in {
          val submissionsInPage = 2
          val view = createView(RECEIVED, submissionsInPage, submissionsInPage)

          (1 to submissionsInPage).foreach { row =>
            val mrnLink = tableCell(view, row, 0).getElementsByTag("a").first
            mrnLink.text mustBe mrn
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
          mrnLink.text mustBe messages("dashboard.mrn.pending")
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
