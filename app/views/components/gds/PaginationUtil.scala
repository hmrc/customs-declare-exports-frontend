/*
 * Copyright 2024 HM Revenue & Customs
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

import java.lang.Math.abs

import views.components.gds.PaginationUtil.PaginationItem._

object PaginationUtil {

  def paginationElements(currentPageIndex: Int, pagesTotal: Int, neighbourPagesAmount: Int = 1): Seq[PaginationItem] = {

    def isCurrentPage(pageNumber: Int): Boolean = pageNumber == currentPageIndex

    if (currentPageIndex < 1)
      throw new IllegalArgumentException("parameter currentPageIndex must be bigger than 0")
    if (pagesTotal < 1)
      throw new IllegalArgumentException("parameter pagesTotal must be bigger than 0")
    if (currentPageIndex > pagesTotal)
      throw new IllegalArgumentException("parameter currentPageIndex cannot be bigger than pagesTotal")
    if (neighbourPagesAmount < 0)
      throw new IllegalArgumentException("parameter neighbourPagesAmount cannot be negative")

    val neighbourPagesNumbers = ((currentPageIndex - neighbourPagesAmount) to (currentPageIndex + neighbourPagesAmount))
      .filter(pageNumber => pageNumber > 0 && pageNumber <= pagesTotal)

    val leadingElements: Seq[PaginationItem] = neighbourPagesNumbers.take(1).flatMap { firstNeighbourPageNumber =>
      abs(firstNeighbourPageNumber - 1) match {
        case 0 => Nil
        case 1 => List(PageNumber(1))
        case _ => List(PageNumber(1), Dots)
      }
    }

    val neighbourElements: Seq[PaginationItem] = neighbourPagesNumbers.map { pageNumber =>
      if (isCurrentPage(pageNumber))
        ActivePageNumber(currentPageIndex)
      else
        PageNumber(pageNumber)
    }

    val trailingElements: Seq[PaginationItem] = neighbourPagesNumbers.takeRight(1).flatMap { lastNeighbourPageNumber =>
      abs(lastNeighbourPageNumber - pagesTotal) match {
        case 0 => Nil
        case 1 => List(PageNumber(pagesTotal))
        case _ => List(Dots, PageNumber(pagesTotal))
      }
    }

    leadingElements ++ neighbourElements ++ trailingElements
  }

  sealed abstract class PaginationItem(val value: String)
  object PaginationItem {
    case class PageNumber(pageNumber: Int) extends PaginationItem(pageNumber.toString)
    case class ActivePageNumber(pageNumber: Int) extends PaginationItem(pageNumber.toString)
    case object Dots extends PaginationItem("...")
  }

}
