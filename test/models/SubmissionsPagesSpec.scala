/*
 * Copyright 2020 HM Revenue & Customs
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

import unit.base.UnitSpec

class SubmissionsPagesSpec extends UnitSpec {

  "SubmissionPages binder" should {

    "bind" when {

      "no parameters present" in {

        val params = Map.empty[String, Seq[String]]
        val expectedResult = Some(Right(SubmissionsPages()))

        SubmissionsPages.binder.bind("page", params) mustBe expectedResult
      }

      "all parameters present" in {

        val params = Map("page-rejected" -> Seq("2"), "page-action" -> Seq("3"), "page-other" -> Seq("4"))
        val expectedResult = Some(Right(SubmissionsPages(rejectedPageNumber = 2, actionPageNumber = 3, otherPageNumber = 4)))

        SubmissionsPages.binder.bind("page", params) mustBe expectedResult
      }

      "only a single parameter present" when {

        "it is rejectedPageNumber" in {

          val params = Map("page-rejected" -> Seq("2"))
          val expectedResult = Some(Right(SubmissionsPages(rejectedPageNumber = 2)))

          SubmissionsPages.binder.bind("page", params) mustBe expectedResult
        }

        "it is actionPageNumber" in {

          val params = Map("page-action" -> Seq("3"))
          val expectedResult = Some(Right(SubmissionsPages(actionPageNumber = 3)))

          SubmissionsPages.binder.bind("page", params) mustBe expectedResult
        }

        "it is otherPageNumber" in {

          val params = Map("page-other" -> Seq("4"))
          val expectedResult = Some(Right(SubmissionsPages(otherPageNumber = 4)))

          SubmissionsPages.binder.bind("page", params) mustBe expectedResult
        }
      }
    }

    "unbind" when {

      "all pages are other than 1" in {

        val expectedResult = "page-rejected=2&page-action=3&page-other=4"

        SubmissionsPages.binder.unbind("page", SubmissionsPages(2, 3, 4)) mustBe expectedResult
      }

      "all pages are equal to 1" in {

        val expectedResult = ""

        SubmissionsPages.binder.unbind("page", SubmissionsPages(1, 1, 1)) mustBe expectedResult
      }

      "one page is equal to 1" when {

        "it is rejectedPageNumber" in {

          val expectedResult = "page-action=3&page-other=4"

          SubmissionsPages.binder.unbind("page", SubmissionsPages(1, 3, 4)) mustBe expectedResult
        }

        "it is actionPageNumber" in {

          val expectedResult = "page-rejected=2&page-other=4"

          SubmissionsPages.binder.unbind("page", SubmissionsPages(2, 1, 4)) mustBe expectedResult
        }

        "it is otherPageNumber" in {

          val expectedResult = "page-rejected=2&page-action=3"

          SubmissionsPages.binder.unbind("page", SubmissionsPages(2, 3, 1)) mustBe expectedResult
        }
      }
    }
  }

}
