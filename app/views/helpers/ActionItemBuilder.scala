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

import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem

object ActionItemBuilder {

  val lastUrlPlaceholder = "LAST_URL_PLACEHOLDER"

  def actionSummaryItem(href: String, content: Content, visuallyHiddenText: Option[String]): ActionItem =
    ActionItem(s"$href?$lastUrlPlaceholder", content, visuallyHiddenText, "govuk-link--no-visited-state")

  def callForSummaryChangeLink(call: Call): Call = Call("GET", s"$call?$lastUrlPlaceholder")
}
