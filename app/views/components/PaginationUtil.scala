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

package views.components

import scala.collection.immutable.Seq

object PaginationUtil {

  def surroundingPages(pageIndex: Int, pageCount: Int, available: Int): Seq[Int] = {
    var indexes: Seq[Int] = Seq(pageIndex)
    var surrounded = surround(indexes, pageCount)
    while (surrounded.size <= available && indexes != surrounded) {
      indexes = surrounded
      surrounded = surround(indexes, pageCount)
    }
    indexes
  }

  private def surround(list: Seq[Int], pageCount: Int): Seq[Int] = {
    val max = list.max
    val min = list.min
    if (max + 1 <= pageCount && min - 1 >= 1) {
      (min - 1) +: list :+ (max + 1)
    } else if (max + 1 <= pageCount) {
      list :+ (max + 1)
    } else if (min - 1 >= 1) {
      (min - 1) +: list
    } else {
      list
    }
  }

}
