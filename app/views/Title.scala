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

package views

import play.api.i18n.Messages

object Title {

  def createTitle(headingKey: String, sectionKey: Option[String] = None)(implicit messages: Messages): String =
    sectionKey match {
      case Some(section) =>
        messages("title.withSection.format", messages(headingKey), messages(section), messages("title.service.name"))
      case None => messages("title.format", messages(headingKey), messages("title.service.name"))
    }
}
