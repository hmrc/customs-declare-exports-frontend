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
import play.twirl.api.Html

case class HintTextAlternatives(defaultMessageKey: String, jsEnabledMessageKey: Option[String] = None) {
  def getJsSpecificHintAttribute()(implicit messages: Messages): Html =
    jsEnabledMessageKey.map { key =>
      Html(s"""<p class="govuk-hint govuk-!-margin-top-0" withJs="${messages(key)}">${messages(defaultMessageKey)}</p>""")
    }.getOrElse {
      Html(s"""<p class="govuk-hint govuk-!-margin-top-0">${messages(defaultMessageKey)}</p>""")
    }
}
