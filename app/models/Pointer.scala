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

package models

import play.api.libs.json.{Format, JsString, Reads, Writes}

import scala.util.{Failure, Success, Try}

case class Pointer(sections: List[String]) {
  lazy val value: String = sections.mkString(".")
  lazy val pattern: String = sections.map { s =>
    Try(s.toInt) match {
      case Success(_) => "$"
      case Failure(_) => s
    }
  }.mkString(".")

  override def toString: String = value
}

object Pointer {
  implicit val format: Format[Pointer] = Format(
    Reads(js => js.validate[JsString].map(string => Pointer(string.value))),
    Writes(pointer => JsString(pointer.value))
  )

  def apply(value: String): Pointer = Pointer(value.split("\\.").toList)
}
