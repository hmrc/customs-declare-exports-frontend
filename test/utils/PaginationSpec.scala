/*
 * Copyright 2018 HM Revenue & Customs
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

package utils

import org.scalatest.{Matchers, WordSpec}

// scalastyle:off magic.number
class PaginationSpec extends WordSpec with Matchers {

	val initialList = (1 to 30).toList

	"Pagination" should {
		"return correct max per page value" in {
			val maxPerPage = 10
			val pagination = new Pagination[Int](initialList, 1, maxPerPage)

			pagination.getMaxPerPage shouldBe maxPerPage
		}

		"return correct value of current page" in {
			val currentPageNo = 5
			val pagination = new Pagination[Int](initialList, currentPageNo, 5)

			pagination.currentPageNo shouldBe currentPageNo
		}

		"return correct value of results" in {
			val pagination = new Pagination[Int](initialList, 1, 10)

			pagination.getResults shouldBe initialList
		}

		"return correct number of results" in {
			val pagination = new Pagination[Int](initialList, 1, 10)

			pagination.noOfResults shouldBe initialList.length
		}

		"return 10 first elements from 30 elements list with correct settings" in {
			val expectedList = (1 to 10).toList

			val pagination = new Pagination[Int](initialList, 1, 10)

			pagination.currentResults shouldBe expectedList
		}

		"return elements 11 to 20 from 30 elements list with correct settings" in {
			val expectedList = (11 to 20).toList

			val pagination = new Pagination[Int](initialList, 2, 10)

			pagination.currentResults shouldBe expectedList
		}

		"return elements from 21 to 25 from 30 elements list with coorect setting" in {
			val expectedList = (21 to 25).toList

			val pagination = new Pagination[Int](initialList, 5, 5)

			pagination.currentResults shouldBe expectedList
		}

		"return correct number of pages for 5 elements per page" in {
			val pagination = new Pagination[Int](initialList, 1, 5)
			val expectedNoOfPages = 6

			pagination.noOfPages shouldBe expectedNoOfPages
		}

		"return correct number of pages for 7 elements per page" in {
			val pagination = new Pagination[Int](initialList, 1, 7)
			val expectedNoOfPages = 5

			pagination.noOfPages shouldBe expectedNoOfPages
		}

		"return correct number of pages for 16 elements per page" in {
			val pagination = new Pagination[Int](initialList, 1, 16)
			val expectedNoOfPages = 2

			pagination.noOfPages shouldBe expectedNoOfPages
		}

		"return correct page result" in {
			val currentPage = 3
			val pagination = new Pagination[Int](initialList, currentPage, 6)
			val expectedResults = (13 to 18).toList
			val expectedPage = Page(expectedResults, currentPage, pagination.noOfPages, 6, initialList.length)

			pagination.currentPageResult shouldBe expectedPage
		}
	}
}
//scalastyle:on magic.number
