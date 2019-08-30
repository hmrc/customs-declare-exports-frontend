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

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import play.twirl.api.Html
import views.declaration.spec.ViewMatchers

import scala.collection.JavaConversions._

trait ViewValidator extends MustMatchers with ViewMatchers {

  private def asDocument(html: Html): Document = Jsoup.parse(html.toString())
  private def asDocument(page: String): Document = Jsoup.parse(page)

  //TODO Remove methods based on the CSS, stay and use those based on the ID
  @deprecated("Please use 'page.getElementsBySelector must ...'", since = "2019-08-07")
  def getElementByCss(html: Html, selector: String): Element = {

    val elements = asDocument(html).select(selector)

    if (elements.isEmpty) throw new Exception(s"Can't find element $selector on page using CSS")

    elements.first()
  }

  @deprecated("Please use 'page.getElementsBySelector must ...'", since = "2019-08-07")
  def getElementByCss(page: String, selector: String): Element = {

    val elements = asDocument(page).select(selector)

    if (elements.isEmpty) throw new Exception(s"Can't find element $selector on page using CSS")

    elements.first()
  }

  @deprecated("Please use 'page.getElementsBySelector must ...'", since = "2019-08-07")
  def getElementsByCss(html: Html, selector: String): Elements = asDocument(html).select(selector)

  @deprecated("Please use 'page.getElementsBySelector must ...'", since = "2019-08-07")
  def getElementsByCss(page: String, selector: String): Elements = asDocument(page).select(selector)

  @deprecated("Please use 'page.getElementById must ...'", since = "2019-08-07")
  def getElementById(html: Html, id: String): Element = {

    val element = asDocument(html).getElementById(id)

    if (element == null) throw new Exception(s"Can't find element $id on page by id")

    element
  }

  @deprecated("Please use 'page.getElementsByAttribute(name) must ...'", since = "2019-08-07")
  def getSelectedValue(html: Html, name: String): String =
    asDocument(html)
      .getElementById(name)
      .children()
      .map { option =>
        if (option.hasAttr("selected")) option.`val`()
        else ""
      }
      .filter(_.nonEmpty)
      .headOption
      .getOrElse("")

  @deprecated("Please use 'page.getElementsByAttribute must ...'", since = "2019-08-07")
  def getElementsByAttribute(html: Html, attributeName: String): List[Element] = {
    val elements = asDocument(html).getElementsByAttribute(attributeName)
    if (elements == null) throw new Exception(s"Can't find attribute $attributeName on page")
    elements.toList
  }

  @deprecated("Please use 'page.getElementsByTag must ...'", since = "2019-08-07")
  def getElementsByTag(html: Html, tag: String): List[Element] = {
    val elements = asDocument(html).getElementsByTag(tag)
    if (elements == null) throw new Exception(s"Can't find tag $tag on page")
    elements.toList
  }

  @deprecated("Please use 'page must haveGlobalErrorSummary'", since = "2019-08-07")
  def checkErrorsSummary(html: Html)(implicit messages: Messages): Unit = {

    getElementByCss(html, "#error-summary-heading").text() must be(messages("error.summary.title"))
    getElementByCss(html, "div.error-summary.error-summary--show>p").text() must be(messages("error.summary.text"))
  }

  @deprecated("Please use 'page must haveGlobalErrorSummary'", since = "2019-08-07")
  def checkErrorsSummary(page: String)(implicit messages: Messages): Unit = {

    getElementByCss(page, "#error-summary-heading").text() must be(messages("error.summary.title"))
    getElementByCss(page, "div.error-summary.error-summary--show>p").text() must be(messages("error.summary.text"))
  }

  @deprecated("Please use 'page must haveFieldErrorLink(...)'", since = "2019-08-07")
  def checkErrorLink(page: String, child: Int, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = getElementByCss(page, "div.error-summary.error-summary--show>ul>li:nth-child(" + child + ")>a")

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }

  @deprecated("Please use 'page must haveFieldErrorLink(...)'", since = "2019-08-07")
  def checkErrorLink(html: Html, child: Int, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = getElementByCss(html, "div.error-summary.error-summary--show>ul>li:nth-child(" + child + ")>a")

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }

  @deprecated("Please use 'page must haveFieldErrorLink(...)'", since = "2019-08-07")
  def checkErrorLink(page: String, elementId: String, error: String, href: String)(
    implicit messages: Messages
  ): Unit = {

    val errorLink = page.getElementById(elementId)

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
