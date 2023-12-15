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

import config.PaginationConfig
import config.featureFlags.DeclarationAmendmentsConfig
import controllers.routes.DeclarationDetailsController
import controllers.routes.DashboardController
import models.PageOfSubmissions
import models.declaration.submissions.StatusGroup.{statusGroups, StatusGroup, SubmittedStatuses}
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTable
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import views.html.dashboard.pagination
import views.dashboard.DashboardHelper.{Groups, Page, Reverse}
import views.helpers.{EnhancedStatusHelper, ViewDates}
import views.html.components.gds.link
import views.html.dashboard.table

import java.time.{ZoneId, ZonedDateTime}
import javax.inject.{Inject, Singleton}

@Singleton
class DashboardHelper @Inject() (
  govukTable: GovukTable,
  link: link,
  pagination: pagination,
  paginationConfig: PaginationConfig,
  amendmentsConfig: DeclarationAmendmentsConfig
) {

  def hint(statusGroup: StatusGroup)(implicit messages: Messages): Html = {
    val key = if (amendmentsConfig.isEnabled && statusGroup == SubmittedStatuses) ".amendment" else ""
    Html(s"""<p class="govuk-body $statusGroup-content-hint">
         |  ${messages(s"dashboard.$statusGroup$key.content.hint", "<br/>")}
         |</p>
         |""".stripMargin)
  }

  def paginationComponent(pageOfSubmissions: PageOfSubmissions, baseHref: String)(implicit request: Request[_], messages: Messages): Appendable = {
    val totalPagesInGroup = Math.ceil(pageOfSubmissions.totalSubmissionsInGroup.toDouble / paginationConfig.itemsPerPage).toInt
    val currentPage = request.getQueryString(Page).fold(totalPagesInGroup)(_.toInt)

    pagination(pageOfSubmissions, baseHref, totalPagesInGroup, currentPage)
  }

  def table(pageOfSubmissions: PageOfSubmissions, statusGroup: StatusGroup)(implicit messages: Messages): Appendable =
    govukTable(Table(rows(pageOfSubmissions, statusGroup), headers(pageOfSubmissions, statusGroup), classes = "sortable"))

  private def headers(pageOfSubmissions: PageOfSubmissions, statusGroup: StatusGroup)(implicit messages: Messages): Option[List[HeadCell]] =
    Some(
      List(
        HeadCell(Text(messages("dashboard.header.mrn"))),
        HeadCell(Text(messages("dashboard.header.ducr"))),
        HeadCell(Text(messages("dashboard.header.lrn"))),
        updatedOnHeader(pageOfSubmissions, statusGroup),
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
      link(
        text = submission.mrn.getOrElse(messages("dashboard.mrn.pending")),
        call = DeclarationDetailsController.displayPage(submission.uuid),
        textHidden = Some(messages("dashboard.hidden.text", submission.ducr.getOrElse("")))
      )
    )

  private def updatedOn(submission: Submission)(implicit messages: Messages): String =
    submission.enhancedStatusLastUpdated.map(ViewDates.formatDateAtTime).getOrElse("")

  private def updatedOnHeader(pageOfSubmissions: PageOfSubmissions, statusGroup: StatusGroup)(implicit messages: Messages): HeadCell = {
    val descending = !pageOfSubmissions.reverse
    if (pageOfSubmissions.submissions.isEmpty) HeadCell(Text(messages("dashboard.header.updated.on")))
    else HeadCell(updatedOnLink(statusGroup, descending), attributes = Map("aria-sort" -> (if (descending) "descending" else "ascending")))
  }

  private def updatedOnLink(statusGroup: StatusGroup, descending: Boolean)(implicit messages: Messages): HtmlContent = {
    val reverse = if (descending) Reverse else ""
    HtmlContent(s"""<a class="govuk-link govuk-link--no-visited-state update-on-order"
         |   href="${DashboardController.displayPage}?$Groups=$statusGroup&page=1$reverse">
         |  ${messages("dashboard.header.updated.on")}
         |  <span aria-hidden="true"></span>
         |</a>
         |""".stripMargin)
  }
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
    val reverse = if (pageOfSubmissions.reverse) Reverse else ""
    val limit = pageOfSubmissions.totalSubmissionsInGroup - (totalPagesInGroup - 1) * itemsPerPage
    s"${baseHref}&$Limit=$limit$reverse"
  }

  def hrefForLoosePage(goToPage: Int, currentPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String =
    if (goToPage - currentPage == 1) hrefForNextPage(goToPage, pageOfSubmissions, baseHref)
    else if (currentPage - goToPage == 1) hrefForPreviousPage(goToPage, pageOfSubmissions, baseHref)
    else {
      val reverse = if (pageOfSubmissions.reverse) Reverse else ""
      s"$baseHref&$Page=$goToPage$reverse"
    }

  def hrefForNextPage(nextPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val reverse = if (pageOfSubmissions.reverse) Reverse else ""
    val hrefWithPage = s"$baseHref&$Page=$nextPage$reverse"
    pageOfSubmissions.submissions.lastOption.fold(hrefWithPage) { submission =>
      addDatetime(hrefWithPage, DatetimeForNextPage, submission.enhancedStatusLastUpdated)
    }
  }

  def hrefForPreviousPage(previousPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val reverse = if (pageOfSubmissions.reverse) Reverse else ""
    val hrefWithPage = s"$baseHref&$Page=$previousPage$reverse"
    if (previousPage == 1) hrefWithPage
    else
      pageOfSubmissions.submissions.headOption.fold(hrefWithPage) { submission =>
        addDatetime(hrefWithPage, DatetimeForPreviousPage, submission.enhancedStatusLastUpdated)
      }
  }

  private def addDatetime(href: String, key: String, maybeDatetime: Option[ZonedDateTime]): String =
    maybeDatetime.fold(href)(datetime => s"${href}&${key}=${toUTC(datetime)}")

  def toUTC(datetime: ZonedDateTime) = datetime.withZoneSameInstant(ZoneId.of("UTC")).toInstant

  def panels(pageOfSubmissions: PageOfSubmissions, table: table)(implicit request: Request[_], messages: Messages): Html =
    Html(statusGroups.map { statusGroup =>
      if (statusGroup != pageOfSubmissions.statusGroup)
        s"""<div id="$statusGroup-submissions" class="cds-exports-tabs__panel cds-exports-tabs__panel--hidden"></div>"""
      else s"""
          |<div id="$statusGroup-submissions" class="cds-exports-tabs__panel">
          |  ${table(statusGroup, pageOfSubmissions, s"${DashboardController.displayPage}?$Groups=$statusGroup").toString}
          |</div>
          |""".stripMargin
    }.mkString)

  def tabs(selectedStatusGroup: StatusGroup)(implicit messages: Messages): Html =
    Html(
      """<ul class="cds-exports-tabs__list">""" +
        statusGroups.map { statusGroup =>
          val (current, tabIndex) =
            if (statusGroup != selectedStatusGroup) ("", "-1")
            else (" cds-exports-tabs__list-item--selected", "0")

          s"""
             |<li class="cds-exports-tabs__list-item$current">
             |  <a id="tab_$statusGroup-submissions" class="cds-exports-tabs__tab"
             |     href="/customs-declare-exports/dashboard?groups=$statusGroup&amp;page=1">
             |    ${messages(s"dashboard.$statusGroup.tab.title")}
             |  </a>
             |</li>
             |""".stripMargin
        }.mkString +
        "</ul>"
    )
}
