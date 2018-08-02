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

import scala.math.ceil

trait PaginationInterface[A] {

	def getMaxPerPage: Int

	def currentPageNo: Int

	def getResults: List[A]

	def noOfResults: Int

	def currentResults: List[A]

	def noOfPages: Int

	def currentPageResult: Page[A]
}

case class Page[A](elements: List[A], currentPage: Int, noOfPages: Int, maxPerPage: Int, resultsLength: Int)

class Pagination[A](results: List[A], currentPage: Int, maxPerPage: Int) extends PaginationInterface[A] {

	override def getMaxPerPage: Int = this.maxPerPage

	override def currentPageNo: Int = this.currentPage

	override def getResults: List[A] = this.results

	override def noOfResults: Int = this.results.length

	override def currentResults: List[A] = results.slice((currentPage - 1) * maxPerPage, maxPerPage * currentPage)

	override def noOfPages: Int = ceil(results.length.toFloat / maxPerPage).toInt

	override def currentPageResult: Page[A] = Page(currentResults, currentPageNo, noOfPages, getMaxPerPage, noOfResults)
}

//scalastyle:off magic.number
object Pagination {

	def apply[A](results: List[A], currentPage: Int = 1, maxPerPage: Int = 10): Pagination[A] =
		if(currentPage < 1 || maxPerPage < 1) throw new Exception("Incorrect values for pagination")
		else new Pagination(results, currentPage, maxPerPage)
}
//scalastyle:on magic.number