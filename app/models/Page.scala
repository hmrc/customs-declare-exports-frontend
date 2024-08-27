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

package models

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.QueryStringBindable

case class Page(index: Int = Page.DEFAULT_INDEX, size: Int = Page.DEFAULT_MAX_DOCUMENT_PER_PAGE)

object Page {
  val DEFAULT_INDEX = 1
  val DEFAULT_MAX_DOCUMENT_PER_PAGE = 25

  implicit val format: OFormat[Page] = Json.format[Page]

  implicit val bindable: QueryStringBindable[Page] = new QueryStringBindable[Page] {
    private val intBinder = implicitly[QueryStringBindable[Int]]

    private def queryParamIndex(key: String): String = key + "-index"
    private def queryParamSize(key: String): String = key + "-size"

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Page]] = {
      val index = params.get(queryParamIndex(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DEFAULT_INDEX)
      val size = params.get(queryParamSize(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DEFAULT_MAX_DOCUMENT_PER_PAGE)
      Some(Right(Page(index, size)))
    }

    override def unbind(key: String, page: Page): String =
      intBinder.unbind(queryParamIndex(key), page.index) + "&" + intBinder.unbind(queryParamSize(key), page.size)
  }
}
