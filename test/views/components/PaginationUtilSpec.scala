/*
 * Copyright 2019 HM Revenue & Customs
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

package views.components

import unit.base.UnitSpec

class PaginationUtilSpec extends UnitSpec {

  "Surrounding Pages" should {
    "return empty" in {
      PaginationUtil.surroundingPages(1, 1, 5) mustBe Seq(1)
    }

    "count upwards upto 5" in {
      PaginationUtil.surroundingPages(1, 2, 5) mustBe Seq(1, 2)
      PaginationUtil.surroundingPages(1, 3, 5) mustBe Seq(1, 2, 3)
      PaginationUtil.surroundingPages(1, 4, 5) mustBe Seq(1, 2, 3, 4)
      PaginationUtil.surroundingPages(1, 5, 5) mustBe Seq(1, 2, 3, 4, 5)
      PaginationUtil.surroundingPages(1, 6, 5) mustBe Seq(1, 2, 3, 4, 5)
      PaginationUtil.surroundingPages(1, 7, 5) mustBe Seq(1, 2, 3, 4, 5)
    }

    "count backwards upto index" in {
      PaginationUtil.surroundingPages(2, 2, 5) mustBe Seq(1, 2)
      PaginationUtil.surroundingPages(3, 3, 5) mustBe Seq(1, 2, 3)
      PaginationUtil.surroundingPages(4, 4, 5) mustBe Seq(1, 2, 3, 4)
      PaginationUtil.surroundingPages(5, 5, 5) mustBe Seq(1, 2, 3, 4, 5)
      PaginationUtil.surroundingPages(6, 6, 5) mustBe Seq(2, 3, 4, 5, 6)
      PaginationUtil.surroundingPages(7, 7, 5) mustBe Seq(3, 4, 5, 6, 7)
    }

    "center current index" in {
      PaginationUtil.surroundingPages(1, 7, 5) mustBe Seq(1, 2, 3, 4, 5)
      PaginationUtil.surroundingPages(2, 7, 5) mustBe Seq(1, 2, 3, 4, 5)
      PaginationUtil.surroundingPages(3, 7, 5) mustBe Seq(1, 2, 3, 4, 5)
      PaginationUtil.surroundingPages(4, 7, 5) mustBe Seq(2, 3, 4, 5, 6)
      PaginationUtil.surroundingPages(5, 7, 5) mustBe Seq(3, 4, 5, 6, 7)
      PaginationUtil.surroundingPages(6, 7, 5) mustBe Seq(3, 4, 5, 6, 7)
      PaginationUtil.surroundingPages(7, 7, 5) mustBe Seq(3, 4, 5, 6, 7)
    }
  }

}
