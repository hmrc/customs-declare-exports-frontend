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

case class HtmlTableRow(label: String, values: Seq[Option[String]])

object HtmlTableRow {

  def apply(label: String, values: Option[String]): HtmlTableRow = new HtmlTableRow(label, Seq(values))

  def apply(label: String, values: String): HtmlTableRow = new HtmlTableRow(label, Seq(Some(values)))
}
