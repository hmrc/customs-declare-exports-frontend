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

package views.components.gds

import base.UnitWithMocksSpec
import views.components.gds.PaginationUtil.PaginationItem
import views.components.gds.PaginationUtil.PaginationItem._

class PaginationUtilSpec extends UnitWithMocksSpec {

  private case class TestData(pagesTotal: Int, currentPage: Int, expectedResult: Seq[PaginationItem])

  "PaginationUtil on paginationItems" should {

    "throw IllegalArgumentException" when {

      "total amount of pages is 0" in {

        val pagesTotal = 0
        val currentPage = 1

        intercept[IllegalArgumentException] {
          PaginationUtil.paginationElements(currentPage, pagesTotal)
        }.getMessage mustBe "parameter pagesTotal must be bigger than 0"
      }

      "total amount of pages is -1" in {

        val pagesTotal = -1
        val currentPage = 1

        intercept[IllegalArgumentException] {
          PaginationUtil.paginationElements(currentPage, pagesTotal)
        }.getMessage mustBe "parameter pagesTotal must be bigger than 0"
      }

      "current page is 0" in {

        val pagesTotal = 1
        val currentPage = 0

        intercept[IllegalArgumentException] {
          PaginationUtil.paginationElements(currentPage, pagesTotal)
        }.getMessage mustBe "parameter currentPageIndex must be bigger than 0"
      }

      "current page is -1" in {

        val pagesTotal = 1
        val currentPage = -1

        intercept[IllegalArgumentException] {
          PaginationUtil.paginationElements(currentPage, pagesTotal)
        }.getMessage mustBe "parameter currentPageIndex must be bigger than 0"
      }

      "current page is bigger than total amount of pages" in {

        val pagesTotal = 1
        val currentPage = 2

        intercept[IllegalArgumentException] {
          PaginationUtil.paginationElements(currentPage, pagesTotal)
        }.getMessage mustBe "parameter currentPageIndex cannot be bigger than pagesTotal"
      }

      "neighbourPagesAmount is -1" in {

        val pagesTotal = 1
        val currentPage = 1
        val neighbourPagesAmount = -1

        intercept[IllegalArgumentException] {
          PaginationUtil.paginationElements(currentPage, pagesTotal, neighbourPagesAmount)
        }.getMessage mustBe "parameter neighbourPagesAmount cannot be negative"
      }
    }

    "return correct list of PaginationItems" when {

      "neighbourPagesAmount equals 1 and" when {

        val neighbourPagesAmount = 1

        def runPaginationTest(testData: TestData): Unit =
          s"current page is ${testData.currentPage}" in {
            PaginationUtil.paginationElements(testData.currentPage, testData.pagesTotal, neighbourPagesAmount) mustBe testData.expectedResult
          }

        "total amount of pages is 1 and" when {

          Seq(TestData(pagesTotal = 1, currentPage = 1, expectedResult = Seq(ActivePageNumber(1)))).foreach(runPaginationTest)
        }

        "total amount of pages is 2 and" when {

          val pagesTotal = 2
          Seq(
            TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2))),
            TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2)))
          ).foreach(runPaginationTest)
        }

        "total amount of pages is 3 and" when {

          val pagesTotal = 3
          Seq(
            TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2), PageNumber(3))),
            TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2), PageNumber(3))),
            TestData(pagesTotal, currentPage = 3, Seq(PageNumber(1), PageNumber(2), ActivePageNumber(3)))
          ).foreach(runPaginationTest)
        }

        "total amount of pages is 4 and" when {

          val pagesTotal = 4
          Seq(
            TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2), Dots, PageNumber(4))),
            TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2), PageNumber(3), PageNumber(4))),
            TestData(pagesTotal, currentPage = 3, Seq(PageNumber(1), PageNumber(2), ActivePageNumber(3), PageNumber(4))),
            TestData(pagesTotal, currentPage = 4, Seq(PageNumber(1), Dots, PageNumber(3), ActivePageNumber(4)))
          ).foreach(runPaginationTest)
        }

        "total amount of pages is 5 and" when {

          val pagesTotal = 5
          Seq(
            TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2), Dots, PageNumber(5))),
            TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2), PageNumber(3), Dots, PageNumber(5))),
            TestData(pagesTotal, currentPage = 3, Seq(PageNumber(1), PageNumber(2), ActivePageNumber(3), PageNumber(4), PageNumber(5))),
            TestData(pagesTotal, currentPage = 4, Seq(PageNumber(1), Dots, PageNumber(3), ActivePageNumber(4), PageNumber(5))),
            TestData(pagesTotal, currentPage = 5, Seq(PageNumber(1), Dots, PageNumber(4), ActivePageNumber(5)))
          ).foreach(runPaginationTest)
        }

        "total amount of pages is 6 and" when {

          val pagesTotal = 6
          Seq(
            TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2), Dots, PageNumber(6))),
            TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2), PageNumber(3), Dots, PageNumber(6))),
            TestData(pagesTotal, currentPage = 3, Seq(PageNumber(1), PageNumber(2), ActivePageNumber(3), PageNumber(4), Dots, PageNumber(6))),
            TestData(pagesTotal, currentPage = 4, Seq(PageNumber(1), Dots, PageNumber(3), ActivePageNumber(4), PageNumber(5), PageNumber(6))),
            TestData(pagesTotal, currentPage = 5, Seq(PageNumber(1), Dots, PageNumber(4), ActivePageNumber(5), PageNumber(6))),
            TestData(pagesTotal, currentPage = 6, Seq(PageNumber(1), Dots, PageNumber(5), ActivePageNumber(6)))
          ).foreach(runPaginationTest)
        }

        "total amount of pages is 7 and" when {

          val pagesTotal = 7
          Seq(
            TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2), Dots, PageNumber(7))),
            TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2), PageNumber(3), Dots, PageNumber(7))),
            TestData(pagesTotal, currentPage = 3, Seq(PageNumber(1), PageNumber(2), ActivePageNumber(3), PageNumber(4), Dots, PageNumber(7))),
            TestData(pagesTotal, currentPage = 4, Seq(PageNumber(1), Dots, PageNumber(3), ActivePageNumber(4), PageNumber(5), Dots, PageNumber(7))),
            TestData(pagesTotal, currentPage = 5, Seq(PageNumber(1), Dots, PageNumber(4), ActivePageNumber(5), PageNumber(6), PageNumber(7))),
            TestData(pagesTotal, currentPage = 6, Seq(PageNumber(1), Dots, PageNumber(5), ActivePageNumber(6), PageNumber(7))),
            TestData(pagesTotal, currentPage = 7, Seq(PageNumber(1), Dots, PageNumber(6), ActivePageNumber(7)))
          ).foreach(runPaginationTest)
        }
      }
    }

    "neighbourPagesAmount equals 2 and" when {

      val neighbourPagesAmount = 2

      def runPaginationTest(testData: TestData): Unit =
        s"current page is ${testData.currentPage}" in {
          PaginationUtil.paginationElements(testData.currentPage, testData.pagesTotal, neighbourPagesAmount) mustBe testData.expectedResult
        }

      "total amount of pages is 9 and" when {

        val pagesTotal = 9
        Seq(
          TestData(pagesTotal, currentPage = 1, Seq(ActivePageNumber(1), PageNumber(2), PageNumber(3), Dots, PageNumber(9))),
          TestData(pagesTotal, currentPage = 2, Seq(PageNumber(1), ActivePageNumber(2), PageNumber(3), PageNumber(4), Dots, PageNumber(9))),
          TestData(
            pagesTotal,
            currentPage = 3,
            Seq(PageNumber(1), PageNumber(2), ActivePageNumber(3), PageNumber(4), PageNumber(5), Dots, PageNumber(9))
          ),
          TestData(
            pagesTotal,
            currentPage = 4,
            Seq(PageNumber(1), PageNumber(2), PageNumber(3), ActivePageNumber(4), PageNumber(5), PageNumber(6), Dots, PageNumber(9))
          ),
          TestData(
            pagesTotal,
            currentPage = 5,
            Seq(PageNumber(1), Dots, PageNumber(3), PageNumber(4), ActivePageNumber(5), PageNumber(6), PageNumber(7), Dots, PageNumber(9))
          ),
          TestData(
            pagesTotal,
            currentPage = 6,
            Seq(PageNumber(1), Dots, PageNumber(4), PageNumber(5), ActivePageNumber(6), PageNumber(7), PageNumber(8), PageNumber(9))
          ),
          TestData(
            pagesTotal,
            currentPage = 7,
            Seq(PageNumber(1), Dots, PageNumber(5), PageNumber(6), ActivePageNumber(7), PageNumber(8), PageNumber(9))
          ),
          TestData(pagesTotal, currentPage = 8, Seq(PageNumber(1), Dots, PageNumber(6), PageNumber(7), ActivePageNumber(8), PageNumber(9))),
          TestData(pagesTotal, currentPage = 9, Seq(PageNumber(1), Dots, PageNumber(7), PageNumber(8), ActivePageNumber(9)))
        ).foreach(runPaginationTest)
      }
    }
  }

}
