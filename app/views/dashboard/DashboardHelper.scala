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

import controllers.routes.DashboardController
import models.PageOfSubmissions
import models.declaration.submissions.StatusGroup.{statusGroups, StatusGroup}
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.twirl.api.Html
import views.html.dashboard.table

import java.time.{ZoneId, ZonedDateTime}

object DashboardHelper {

  val toDashboard = Call("GET", s"${DashboardController.displayPage}?page=1")

  val DatetimeForNextPage = "datetimeForNextPage"
  val DatetimeForPreviousPage = "datetimeForPreviousPage"
  val Groups = "groups"
  val Limit = "limit"
  val Page = "page"

  def hrefForLastPage(itemsPerPage: Int, totalPagesInGroup: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val limit = pageOfSubmissions.totalSubmissionsInGroup - (totalPagesInGroup - 1) * itemsPerPage
    s"${baseHref}&${Limit}=${limit}"
  }

  def hrefForLoosePage(goToPage: Int, currentPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String =
    if (goToPage - currentPage == 1) hrefForNextPage(goToPage, pageOfSubmissions, baseHref)
    else if (currentPage - goToPage == 1) hrefForPreviousPage(goToPage, pageOfSubmissions, baseHref)
    else s"${baseHref}&${Page}=${goToPage}"

  def hrefForNextPage(nextPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val hrefWithPage = s"${baseHref}&${Page}=${nextPage}"
    pageOfSubmissions.submissions.lastOption.fold(hrefWithPage) { submission =>
      addDatetime(hrefWithPage, DatetimeForNextPage, submission.enhancedStatusLastUpdated)
    }
  }

  def hrefForPreviousPage(previousPage: Int, pageOfSubmissions: PageOfSubmissions, baseHref: String): String = {
    val hrefWithPage = s"${baseHref}&${Page}=${previousPage}"
    if (previousPage == 1) hrefWithPage
    else
      pageOfSubmissions.submissions.headOption.fold(hrefWithPage) { submission =>
        addDatetime(hrefWithPage, DatetimeForPreviousPage, submission.enhancedStatusLastUpdated)
      }
  }

  def addDatetime(href: String, key: String, maybeDatetime: Option[ZonedDateTime]): String =
    maybeDatetime.fold(href)(datetime => s"${href}&${key}=${toUTC(datetime)}")

  def toUTC(datetime: ZonedDateTime) = datetime.withZoneSameInstant(ZoneId.of("UTC")).toInstant

  def panels(pageOfSubmissions: PageOfSubmissions, table: table)(implicit request: Request[_], messages: Messages): Html =
    Html(statusGroups.map { statusGroup =>
      if (statusGroup != pageOfSubmissions.statusGroup)
        s"""<div id="$statusGroup-submissions" class="cds-exports-tabs__panel cds-exports-tabs__panel--hidden"></div>"""
      else s"""
          |<div id="$statusGroup-submissions" class="cds-exports-tabs__panel">
          |  ${table(statusGroup, pageOfSubmissions, s"${DashboardController.displayPage}?${Groups}=${statusGroup}").toString}
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
             |     href="/customs-declare-exports/dashboard?groups=$statusGroup&amp;page=1"
             |     tabindex="$tabIndex">
             |    ${messages(s"dashboard.$statusGroup.tab.title")}
             |  </a>
             |</li>
             |""".stripMargin
        }.mkString +
        "</ul>"
    )
}
