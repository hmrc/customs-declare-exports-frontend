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

import controllers.routes.DashboardController
import controllers.timeline.routes.DeclarationDetailsController
import models.Page.MAX_DOCUMENT_PER_PAGE
import models.PageOfSubmissions
import models.declaration.submissions.StatusGroup.{statusGroups, StatusGroup}
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukButton, GovukTable}
import uk.gov.hmrc.govukfrontend.views.viewmodels.button.Button
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import views.helpers.{EnhancedStatusHelper, ViewDates}
import views.html.components.gds.link
import views.html.dashboard.table

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.{Inject, Singleton}

@Singleton
class DashboardHelper @Inject() (govukTable: GovukTable, link: link) {

  def heading(statusGroup: StatusGroup)(implicit messages: Messages): Html =
    Html(s"""<h2 class="govuk-heading-m $statusGroup-heading">
        |  ${messages(s"dashboard.$statusGroup.heading")}
        |</h2>
        |""".stripMargin)

  def hint(statusGroup: StatusGroup)(implicit messages: Messages): Html =
    Html(s"""<p class="govuk-body $statusGroup-content-hint govuk-!-margin-bottom-4">
        |  ${messages(s"dashboard.$statusGroup.content.hint")}
        |</p>
        |""".stripMargin)

  def table(pageOfSubmissions: PageOfSubmissions, statusGroup: StatusGroup)(implicit messages: Messages): Appendable =
    govukTable(Table(rows(pageOfSubmissions, statusGroup), headers, classes = "sortable"))

  private def headers(implicit messages: Messages): Option[List[HeadCell]] =
    Some(
      List(
        HeadCell(Text(messages("dashboard.header.mrn"))),
        HeadCell(Text(messages("dashboard.header.ducr"))),
        HeadCell(Text(messages("dashboard.header.lrn"))),
        HeadCell(Text(messages("dashboard.header.updated.on"))),
        HeadCell(Text(messages("dashboard.header.status")))
      )
    )

  private def rows(pageOfSubmissions: PageOfSubmissions, statusGroup: StatusGroup)(implicit messages: Messages): Seq[Seq[TableRow]] =
    pageOfSubmissions.submissions.zipWithIndex.map { case (submission, row) =>
      List(
        TableRow(mrnAsLink(submission), classes = classes(statusGroup, row, "mrn")),
        TableRow(Text(submission.ducr.getOrElse("")), classes = classes(statusGroup, row, "ducr")),
        TableRow(Text(submission.lrn), classes = classes(statusGroup, row, "lrn")),
        TableRow(Text(updatedOn(submission)), classes = classes(statusGroup, row, "updatedOn", false)),
        TableRow(Text(EnhancedStatusHelper.asText(submission)), classes = classes(statusGroup, row, "status", false))
      )
    }

  private def classes(statusGroup: StatusGroup, row: Int, rowId: String, withBreakAll: Boolean = true): String = {
    val breakAll = if (withBreakAll) "govuk-table__cell_break-all " else ""
    s"${breakAll}submission-tab-$statusGroup-row$row-$rowId"
  }

  private def mrnAsLink(submission: Submission)(implicit messages: Messages) =
    HtmlContent(
      link(text = submission.mrn.getOrElse(messages("dashboard.mrn.pending")), call = DeclarationDetailsController.displayPage(submission.uuid))
    )

  private def updatedOn(submission: Submission)(implicit messages: Messages): String =
    submission.enhancedStatusLastUpdated.map(ViewDates.formatDateAtTime).getOrElse("")
}

object DashboardHelper {

  val toDashboard = Call("GET", s"${DashboardController.displayPage}?page=1")

  val DatetimeForNextPage = "datetimeForNextPage"
  val DatetimeForPreviousPage = "datetimeForPreviousPage"
  val Groups = "groups"
  val Limit = "limit"
  val Page = "page"
  val Reverse = "&reverse"

  def hrefForLastPage(itemsPerPage: Int, totalPagesInGroup: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val limit = pageOfSubmissions.totalSubmissionsInGroup - (totalPagesInGroup - 1) * itemsPerPage
    s"${baseHref}&$Limit=$limit&$Page=$totalPagesInGroup"
  }

  def hrefForLoosePage(goToPage: Int, currentPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String =
    if (goToPage - currentPage == 1) hrefForNextPage(goToPage, pageOfSubmissions, baseHref)
    else if (currentPage - goToPage == 1) hrefForPreviousPage(goToPage, pageOfSubmissions, baseHref)
    else s"$baseHref&$Page=$goToPage"

  def hrefForNextPage(nextPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val hrefWithPage = s"$baseHref&$Page=$nextPage"
    pageOfSubmissions.submissions.lastOption.fold(hrefWithPage) { submission =>
      addDatetime(hrefWithPage, DatetimeForNextPage, submission.enhancedStatusLastUpdated)
    }
  }

  def hrefForPreviousPage(previousPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val hrefWithPage = s"$baseHref&$Page=$previousPage"
    if (previousPage == 1) hrefWithPage
    else
      pageOfSubmissions.submissions.headOption.fold(hrefWithPage) { submission =>
        addDatetime(hrefWithPage, DatetimeForPreviousPage, submission.enhancedStatusLastUpdated)
      }
  }

  private def addDatetime(href: String, key: String, maybeDatetime: Option[ZonedDateTime]): String =
    maybeDatetime.fold(href)(datetime => s"${href}&${key}=${toUTC(datetime)}")

  def toUTC(datetime: ZonedDateTime): Instant = datetime.withZoneSameInstant(ZoneId.of("UTC")).toInstant

  def buttonGroup(selectedStatusGroup: StatusGroup, govukButton: GovukButton)(implicit messages: Messages): Html = {
    val groupOfButtons = statusGroups.map { statusGroup =>
      govukButton(
        Button(
          content = Text(messages(s"dashboard.$statusGroup.button.text")),
          href = Some(s"/customs-declare-exports/dashboard?groups=$statusGroup&page=1"),
          attributes = Map("id" -> s"$statusGroup-submissions-button", "aria-pressed" -> (statusGroup == selectedStatusGroup).toString),
          classes =
            if (statusGroup != selectedStatusGroup) "govuk-button--secondary"
            else "govuk-button--secondary selected-status-group"
        )
      ).toString()
    }.mkString

    Html(s"""
         |<nav id="filters" aria-label="${messages("aria.label.filters")}">
         |  <div class="govuk-button-group">$groupOfButtons</div>
         |</nav>
         |""".stripMargin)
  }

  def currentPage(implicit request: Request[_]): Int = request.getQueryString(Page).fold(1)(_.toInt)

  def notification(selectedStatusGroup: StatusGroup)(implicit messages: Messages): String =
    messages("dashboard.notification.180.days", messages(s"dashboard.notification.180.days.$selectedStatusGroup"))

  def panels(pageOfSubmissions: PageOfSubmissions, totalPagesInGroup: Int, table: table)(implicit request: Request[_], messages: Messages): Html = {
    val statusGroup = pageOfSubmissions.statusGroup
    val href = s"${DashboardController.displayPage}?$Groups=$statusGroup"
    Html(s"""
          |<div id="$statusGroup-submissions">
          |  ${table(statusGroup, pageOfSubmissions, totalPagesInGroup, href).toString}
          |</div>
          |""".stripMargin)
  }

  def totalPagesInGroup(pageOfSubmissions: PageOfSubmissions, documentsPerPage: Int = MAX_DOCUMENT_PER_PAGE): Int =
    Math.max(1, Math.ceil(pageOfSubmissions.totalSubmissionsInGroup.toDouble / documentsPerPage).toInt)

  def title(selectedStatusGroup: StatusGroup)(implicit messages: Messages): String =
    messages(s"dashboard.title.$selectedStatusGroup")
}
