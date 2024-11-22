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

package views.helpers

import play.api.i18n.Messages

case class Title(
  headingKey: String,
  sectionKey: String = "",
  headingArg: String = "",
  headingArgs: Seq[String] = List.empty,
  hasErrors: Boolean = false
) {
  def toString(implicit messages: Messages): String = {
    val args = if (headingArgs.nonEmpty) headingArgs else List(headingArg)
    val key = if (hasErrors) ".hasErrors" else ""

    if (sectionKey.isEmpty) {
      messages(s"title$key.format", messages(headingKey, args: _*), messages("service.name"))
    } else {
      messages(s"title$key.withSection.format", messages(headingKey, args: _*), messages(sectionKey), messages("service.name"))
    }
  }
}

object Title {

  def pageOf(titleOfPage: String, currentPage: Int, totalPages: Int): Title =
    Title("pagination.page.of", headingArgs = List(titleOfPage, currentPage.toString, totalPages.toString))
}
