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

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.QueryStringBindable

case class Page(index: Int = 1, size: Int = 50)

object Page {
  implicit val format: OFormat[Page] = Json.format[Page]
  implicit def bindable(implicit intBinder: QueryStringBindable[Int]): QueryStringBindable[Page] =
    new QueryStringBindable[Page] {
      private def queryParamIndex(key: String) = key + "-index"
      private def queryParamSize(key: String) = key + "-size"

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Page]] = {
        val index: Option[Int] = params.get(queryParamIndex(key)).flatMap(_.headOption).map(_.toInt)
        val size: Option[Int] = params.get(queryParamSize(key)).flatMap(_.headOption).map(_.toInt)
        (index, size) match {
          case (Some(i), Some(s)) => Some(Right(Page(index = i, size = s)))
          case (Some(i), None)    => Some(Right(Page(index = i)))
          case (None, Some(s))    => Some(Right(Page(size = s)))
          case _                  => Some(Right(Page()))
        }
      }

      override def unbind(key: String, page: Page): String =
        intBinder.unbind(queryParamIndex(key), page.index) + "&" + intBinder.unbind(queryParamSize(key), page.size)
    }
}
