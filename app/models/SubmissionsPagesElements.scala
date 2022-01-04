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

package models

import config.PaginationConfig
import controllers.helpers.SubmissionDisplayHelper.filterSubmissions
import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}

case class SubmissionsPagesElements(
  rejectedSubmissions: Paginated[(Submission, Seq[Notification])],
  actionSubmissions: Paginated[(Submission, Seq[Notification])],
  otherSubmissions: Paginated[(Submission, Seq[Notification])]
)

object SubmissionsPagesElements {

  def apply(submissions: Seq[(Submission, Seq[Notification])], submissionsPages: SubmissionsPages = SubmissionsPages())(
    implicit paginationConfig: PaginationConfig
  ): SubmissionsPagesElements = SubmissionsPagesElements(
    rejectedSubmissions = paginateSubmissions(
      filterSubmissions(submissions, _.headOption.map(_.status).exists(SubmissionStatus.rejectedStatuses.contains)),
      submissionsPages.rejectedPageNumber
    ),
    actionSubmissions = paginateSubmissions(
      filterSubmissions(submissions, _.headOption.map(_.status).exists(SubmissionStatus.actionRequiredStatuses.contains)),
      submissionsPages.actionPageNumber
    ),
    otherSubmissions = paginateSubmissions(
      filterSubmissions(
        submissions,
        notifications => notifications.isEmpty || notifications.headOption.map(_.status).exists(SubmissionStatus.otherStatuses.contains)
      ),
      submissionsPages.otherPageNumber
    )
  )

  private def paginateSubmissions(submissions: Seq[(Submission, Seq[Notification])], pageNumber: Int)(
    implicit paginationConfig: PaginationConfig
  ): Paginated[(Submission, Seq[Notification])] = {
    val currentPage = Page(pageNumber, paginationConfig.itemsPerPage)
    val currentPageElements =
      submissions.slice((currentPage.index - 1) * currentPage.size, (currentPage.index - 1) * currentPage.size + currentPage.size)
    Paginated(currentPageElements, currentPage, submissions.size)
  }
}
