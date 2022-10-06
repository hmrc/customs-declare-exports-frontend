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
import models.declaration.submissions.EnhancedStatus.EnhancedStatus
import models.declaration.submissions.{EnhancedStatus, Submission}

case class SubmissionsPagesElements(
  otherSubmissions: Paginated[Submission],
  actionSubmissions: Paginated[Submission],
  rejectedSubmissions: Paginated[Submission],
  cancelledSubmissions: Paginated[Submission]
)

object SubmissionsPagesElements {

  def apply(submissions: Seq[Submission], submissionsPages: SubmissionsPages = SubmissionsPages())(
    implicit paginationConfig: PaginationConfig
  ): SubmissionsPagesElements =
    SubmissionsPagesElements(
      otherSubmissions =
        paginateSubmissions(
          excludeSubmissions(submissions, EnhancedStatus.rejectedStatuses ++ EnhancedStatus.cancelledStatuses ++ EnhancedStatus.actionRequiredStatuses),
          submissionsPages.otherPageNumber
      ),
      actionSubmissions =
        paginateSubmissions(filterSubmissions(submissions, EnhancedStatus.actionRequiredStatuses), submissionsPages.actionPageNumber),

      rejectedSubmissions =
        paginateSubmissions(filterSubmissions(submissions, EnhancedStatus.rejectedStatuses), submissionsPages.rejectedPageNumber),

      cancelledSubmissions =
        paginateSubmissions(filterSubmissions(submissions, EnhancedStatus.cancelledStatuses), submissionsPages.cancelledPageNumber)
    )

  private def filterSubmissions(submissions: Seq[Submission], enhancedStatuses: Set[EnhancedStatus]): Seq[Submission] =
    submissions.filter(_.latestEnhancedStatus.exists(_ in enhancedStatuses))

  private def excludeSubmissions(submissions: Seq[Submission], enhancedStatuses: Set[EnhancedStatus]): Seq[Submission] =
    submissions.filter(!_.latestEnhancedStatus.exists(_ in enhancedStatuses))

  private def paginateSubmissions(submissions: Seq[Submission], pageNumber: Int)(
    implicit paginationConfig: PaginationConfig
  ): Paginated[Submission] = {
    val currentPage = Page(pageNumber, paginationConfig.itemsPerPage)
    val currentPageElements =
      submissions.slice((currentPage.index - 1) * currentPage.size, (currentPage.index - 1) * currentPage.size + currentPage.size)
    Paginated(currentPageElements, currentPage, submissions.size)
  }
}
