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

import base.UnitWithMocksSpec
import config.PaginationConfig
import models.declaration.submissions.EnhancedStatus._
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import testdata.SubmissionsTestData._

class SubmissionsPagesElementsSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  implicit private val paginationConfig: PaginationConfig = mock[PaginationConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(paginationConfig)
    when(paginationConfig.itemsPerPage).thenReturn(Page.MAX_DOCUMENT_PER_PAGE)
  }

  "SubmissionsPagesElements on apply" should {

    "call AppConfig for paginationItemsPerPage" in {
      SubmissionsPagesElements(Seq.empty)
      verify(paginationConfig, times(4)).itemsPerPage
    }

    "build default SubmissionsPagesElements" when {
      "provided with empty submissions list" in {
        val expectedResult = SubmissionsPagesElements(
          otherSubmissions = Paginated(Seq.empty, Page(), 0),
          actionSubmissions = Paginated(Seq.empty, Page(), 0),
          rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
          cancelledSubmissions = Paginated(Seq.empty, Page(), 0)
        )

        SubmissionsPagesElements(Seq.empty) mustBe expectedResult
      }
    }

    "create correct Page elements inside Pagination" when {

      "provided with none" in {
        val result = SubmissionsPagesElements(Seq.empty)

        val expectedPage = Page()
        result.otherSubmissions.page mustBe expectedPage
        result.actionSubmissions.page mustBe expectedPage
        result.rejectedSubmissions.page mustBe expectedPage
        result.cancelledSubmissions.page mustBe expectedPage
      }

      "provided with SubmissionsPages" in {
        val submissionsPages = SubmissionsPages(otherPageNumber = 7, actionPageNumber = 4, rejectedPageNumber = 3, cancelledPageNumber = 2)
        val result = SubmissionsPagesElements(Seq.empty, submissionsPages)

        val expectedPageOther = Page(index = 7)
        val expectedPageAction = Page(index = 4)
        val expectedPageRejected = Page(index = 3)
        val expectedPageCancelled = Page(index = 2)

        result.otherSubmissions.page mustBe expectedPageOther
        result.actionSubmissions.page mustBe expectedPageAction
        result.rejectedSubmissions.page mustBe expectedPageRejected
        result.cancelledSubmissions.page mustBe expectedPageCancelled
      }
    }
  }

  "SubmissionsPagesElements on apply" when {

    "provided with a single type of submission" should {

      "build SubmissionsPagesElements containing rejectedSubmissions" when {

        "provided with Submission latestEnhancedStatus with Some(ERRORS) status" in {
          val rejectedSubmission = submission_2.copy(latestEnhancedStatus = Some(ERRORS))

          val rejectedSubmissions = Seq(rejectedSubmission)

          val expectedResult = SubmissionsPagesElements(
            otherSubmissions = Paginated(Seq.empty, Page(), 0),
            actionSubmissions = Paginated(Seq.empty, Page(), 0),
            rejectedSubmissions = Paginated(rejectedSubmissions, Page(), 1),
            cancelledSubmissions = Paginated(Seq.empty, Page(), 0)
          )
          SubmissionsPagesElements(rejectedSubmissions) mustBe expectedResult
        }
      }

      "build SubmissionsPagesElements containing actionSubmissions" when {
        actionRequiredStatuses.foreach { status =>
          s"provided with Submission latestEnhancedStatus with Some($status) status" in {
            val actionSubmission = submission_3.copy(latestEnhancedStatus = Some(status))

            val actionSubmissions = Seq(actionSubmission)

            val expectedResult = SubmissionsPagesElements(
              otherSubmissions = Paginated(Seq.empty, Page(), 0),
              actionSubmissions = Paginated(actionSubmissions, Page(), 1),
              rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
              cancelledSubmissions = Paginated(Seq.empty, Page(), 0)
            )
            SubmissionsPagesElements(actionSubmissions) mustBe expectedResult
          }
        }
      }

      "build SubmissionsPagesElements containing cancelledSubmissions" when {
        cancelledStatuses.foreach { status =>
          s"provided with Submission latestEnhancedStatus with Some($status) status" in {
            val cancelledSubmission = submission_3.copy(latestEnhancedStatus = Some(status))

            val cancelledSubmissions = Seq(cancelledSubmission)

            val expectedResult = SubmissionsPagesElements(
              otherSubmissions = Paginated(Seq.empty, Page(), 0),
              actionSubmissions = Paginated(Seq.empty, Page(), 0),
              rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
              cancelledSubmissions = Paginated(cancelledSubmissions, Page(), 1)
            )
            SubmissionsPagesElements(cancelledSubmissions) mustBe expectedResult
          }
        }
      }

      "build SubmissionsPagesElements containing otherSubmissions" when {
        otherStatuses.foreach { status =>
          s"provided with Submission latestEnhancedStatus with Some($status) status" in {
            val otherSubmission = submission.copy(latestEnhancedStatus = Some(status))

            val otherSubmissions = Seq(otherSubmission)

            val expectedResult = SubmissionsPagesElements(
              otherSubmissions = Paginated(otherSubmissions, Page(), 1),
              actionSubmissions = Paginated(Seq.empty, Page(), 0),
              rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
              cancelledSubmissions = Paginated(Seq.empty, Page(), 0)
            )
            SubmissionsPagesElements(otherSubmissions) mustBe expectedResult
          }
        }

        "provided with Submission latestEnhancedStatus with None status" in {
          val otherSubmission = submission_2.copy(latestEnhancedStatus = None)

          val otherSubmissions = Seq(otherSubmission)

          val expectedResult = SubmissionsPagesElements(
            otherSubmissions = Paginated(otherSubmissions, Page(), 1),
            actionSubmissions = Paginated(Seq.empty, Page(), 0),
            rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
            cancelledSubmissions = Paginated(Seq.empty, Page(), 0)
          )
          SubmissionsPagesElements(otherSubmissions) mustBe expectedResult
        }
      }
    }

    "provided with all types of submissions" should {
      "build correct SubmissionsPagesElements" in {
        val otherSubmission = submission_2.copy(latestEnhancedStatus = Some(AWAITING_EXIT_RESULTS))
        val actionSubmission = submission.copy(latestEnhancedStatus = Some(ADDITIONAL_DOCUMENTS_REQUIRED))
        val rejectedSubmission = submission_3.copy(latestEnhancedStatus = Some(ERRORS))
        val cancelSubmission = submission_4.copy(latestEnhancedStatus = Some(CANCELLED))

        val submissions = Seq(otherSubmission, actionSubmission, rejectedSubmission, cancelSubmission)

        val expectedResult = SubmissionsPagesElements(
          otherSubmissions = Paginated(Seq(otherSubmission), Page(), 1),
          actionSubmissions = Paginated(Seq(actionSubmission), Page(), 1),
          rejectedSubmissions = Paginated(Seq(rejectedSubmission), Page(), 1),
          cancelledSubmissions = Paginated(Seq(cancelSubmission), Page(), 1)
        )

        SubmissionsPagesElements(submissions) mustBe expectedResult
      }
    }
  }
}
