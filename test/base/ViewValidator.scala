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

package base

import org.scalatest.MustMatchers
import play.api.i18n.Messages
import play.twirl.api.Html
import views.declaration.spec.ViewMatchers

trait ViewValidator extends MustMatchers with ViewMatchers {

  @deprecated("Please use 'page must haveGlobalErrorSummary'", since = "2019-08-07")
  def checkErrorsSummary(html: Html)(implicit messages: Messages): Unit = {

    html.select("#error-summary-heading").text() must be(messages("error.summary.title"))
    html.select("div.error-summary.error-summary--show>p").text() must be(messages("error.summary.text"))
  }

  @deprecated("Please use 'page must haveFieldErrorLink(...)'", since = "2019-08-07")
  def checkErrorLink(html: Html, child: Int, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = html.select("div.error-summary.error-summary--show>ul>li:nth-child(" + child + ")>a")

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }

  @deprecated("Please use 'page must haveFieldErrorLink(...)'", since = "2019-08-07")
  def checkErrorLink(html: Html, elementId: String, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = html.getElementById(elementId)

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }
}
