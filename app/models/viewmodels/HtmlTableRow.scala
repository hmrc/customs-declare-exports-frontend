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

package models.viewmodels

import scala.collection.Iterable

class HtmlTableRow(val label: String, val values: Seq[Option[String]])

object HtmlTableRow {

  def apply[T](label: String, value: Iterable[T]): HtmlTableRow = new HtmlTableRow(label, adjust(value))

  def apply[T](label: String, value: Seq[String]): HtmlTableRow = new HtmlTableRow(label, value.map(Option.apply))

  def apply(label: String, value: String): HtmlTableRow = new HtmlTableRow(label, Seq(Some(value)))

  // TODO review adjust method, working wrong
  private def adjust[T](iterable: Iterable[T]): Seq[Option[String]] = iterable match {
    case Nil => Seq.empty
    case _ =>
      iterable.map {
        case str: String       => Some(str)
        case Some(str: String) => Some(str)
        case _                 => None
      }.filter(_.isDefined).toSeq
  }

}
