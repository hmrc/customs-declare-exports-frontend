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

package views.helpers

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.MustMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.i18n.Messages
import play.twirl.api.Html

trait ViewValidator extends MustMatchers {

  private def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def getElementByCss(html: Html, selector: String): Element = {

    val elements = asDocument(html).select(selector)

    if (elements.isEmpty) throw new Exception(s"Can't find element $selector on page using CSS")

    elements.first()
  }

  def getElementsByCss(html: Html, selector: String): Elements = asDocument(html).select(selector)

  def getElementById(html: Html, id: String): Element = {

    val element = asDocument(html).getElementById(id)

    if (element == null) throw new Exception(s"Can't find element $id on page by id")

    element
  }

  def checkErrorsSummary(html: Html)(implicit messages: Messages): Unit = {

    getElementByCss(html, "#error-summary-heading").text() must be(messages("error.summary.title"))
    getElementByCss(html, "div.error-summary.error-summary--show>p").text() must be(messages("error.summary.text"))
  }

  def checkErrorLink(html: Html, child: Int, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = getElementByCss(html, "div.error-summary.error-summary--show>ul>li:nth-child(" + child + ")>a")

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }

  class HtmlContains(right: Html) extends Matcher[Html] {

    override def apply(left: Html): MatchResult = {

      val trimmed = left.toString().trim

      MatchResult(
        trimmed.contains(right.toString()),
        s"""$trimmed did not contain $right""",
        s"""$trimmed contained $right"""
      )
    }
  }

  def include(right: Html) = new HtmlContains(right)
}
