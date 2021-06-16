/*
 * Copyright 2021 HM Revenue & Customs
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
import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import testdata.SubmissionsTestData._

class SubmissionsPagesElementsSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  implicit private val paginationConfig: PaginationConfig = mock[PaginationConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(paginationConfig)
    when(paginationConfig.itemsPerPage).thenReturn(Page.DEFAULT_MAX_SIZE)
  }

  "SubmissionsPagesElements on apply" should {

    "call AppConfig for paginationItemsPerPage" in {

      val input = Seq.empty[(Submission, Seq[Notification])]

      SubmissionsPagesElements(input)

      verify(paginationConfig, times(3)).itemsPerPage
    }

    "build default SubmissionsPagesElements" when {

      "provided with empty submissions list" in {

        val input = Seq.empty[(Submission, Seq[Notification])]
        val expectedResult = SubmissionsPagesElements(
          rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
          actionSubmissions = Paginated(Seq.empty, Page(), 0),
          otherSubmissions = Paginated(Seq.empty, Page(), 0)
        )

        SubmissionsPagesElements(input) mustBe expectedResult
      }
    }

    "build correct SubmissionsPagesElements" when {

      "provided with a single type of submission" when {

        "it is rejected submission" in {

          val rejectedSubmission = submission_2 -> Seq(notification.copy(status = SubmissionStatus.REJECTED))

          val input = Seq(rejectedSubmission)

          val expectedResult = SubmissionsPagesElements(
            rejectedSubmissions = Paginated(Seq(rejectedSubmission), Page(), 1),
            actionSubmissions = Paginated(Seq.empty, Page(), 0),
            otherSubmissions = Paginated(Seq.empty, Page(), 0)
          )

          SubmissionsPagesElements(input) mustBe expectedResult
        }

        "it is action submission" in {

          val actionSubmission = submission_3 -> Seq(
            notification.copy(status = SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED, actionId = conversationId)
          )

          val input = Seq(actionSubmission)

          val expectedResult = SubmissionsPagesElements(
            rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
            actionSubmissions = Paginated(Seq(actionSubmission), Page(), 1),
            otherSubmissions = Paginated(Seq.empty, Page(), 0)
          )

          SubmissionsPagesElements(input) mustBe expectedResult
        }

        "it is other submission" in {

          val otherSubmission = submission -> Seq(notification)

          val input = Seq(otherSubmission)

          val expectedResult = SubmissionsPagesElements(
            rejectedSubmissions = Paginated(Seq.empty, Page(), 0),
            actionSubmissions = Paginated(Seq.empty, Page(), 0),
            otherSubmissions = Paginated(Seq(otherSubmission), Page(), 1)
          )

          SubmissionsPagesElements(input) mustBe expectedResult
        }
      }

      "provided with all types of submissions" in {

        val otherSubmission = submission -> Seq(notification)
        val rejectedSubmission = submission_2 -> Seq(notification.copy(status = SubmissionStatus.REJECTED))
        val actionSubmission = submission_3 -> Seq(
          notification.copy(status = SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED, actionId = conversationId)
        )

        val input = Seq(otherSubmission, rejectedSubmission, actionSubmission)

        val expectedResult = SubmissionsPagesElements(
          rejectedSubmissions = Paginated(Seq(rejectedSubmission), Page(), 1),
          actionSubmissions = Paginated(Seq(actionSubmission), Page(), 1),
          otherSubmissions = Paginated(Seq(otherSubmission), Page(), 1)
        )

        SubmissionsPagesElements(input) mustBe expectedResult
      }
    }

    "create correct Page elements inside Pagination" when {

      "provided with none" in {

        val input = Seq.empty[(Submission, Seq[Notification])]
        val expectedPage = Page()

        val result = SubmissionsPagesElements(input)

        result.rejectedSubmissions.page mustBe expectedPage
        result.actionSubmissions.page mustBe expectedPage
        result.otherSubmissions.page mustBe expectedPage
      }

      "provided with SubmissionsPages" in {

        val input = Seq.empty[(Submission, Seq[Notification])]
        val submissionsPages = SubmissionsPages(rejectedPageNumber = 3, actionPageNumber = 4, otherPageNumber = 7)

        val expectedPageRejected = Page(index = 3)
        val expectedPageAction = Page(index = 4)
        val expectedPageOther = Page(index = 7)

        val result = SubmissionsPagesElements(input, submissionsPages)

        result.rejectedSubmissions.page mustBe expectedPageRejected
        result.actionSubmissions.page mustBe expectedPageAction
        result.otherSubmissions.page mustBe expectedPageOther
      }
    }
  }

}
