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

package views.declaration.spec

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

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.matchers._
import play.api.mvc.{Call, Result}
import play.api.test.Helpers.contentAsString
import play.twirl.api.Html
import play.api.test.Helpers._

import scala.collection.JavaConverters
import scala.collection.JavaConverters._
import scala.concurrent.Future

//noinspection ScalaStyle
trait ViewMatchers {

  implicit private def elements2Scala(elements: Elements): Iterator[Element] = elements.iterator().asScala
  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())
  implicit protected def htmlBodyOf(page: String): Document = Jsoup.parse(page)
  implicit protected def htmlBodyOf(result: Future[Result]): Document = htmlBodyOf(contentAsString(result))

    private def actualContentWas(node: Element): String =
    if (node == null) {
      "Element did not exist"
    } else {
      s"\nActual content was:\n${node.html}\n"
    }

  private def actualContentWas(node: Elements): String =
    if (node == null) {
      "Elements did not exist"
    } else {
      s"\nActual content was:\n${node.html}\n"
    }

  class ContainElementWithIDMatcher(id: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementById(id) != null,
        s"Document did not contain element with ID {$id}\n${actualContentWas(left)}",
        s"Document contained an element with ID {$id}"
      )
  }

  class ContainElementWithClassMatcher(name: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByClass(name).size() > 0,
        s"Document did not contain element with class {$name}\n${actualContentWas(left)}",
        s"Document contained an element with class {$name}"
      )
  }

  class ContainElementWithAttribute(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && !left.getElementsByAttributeValue(key, value).isEmpty,
        s"Document did not contain element with Attribute {$key=$value}\n${actualContentWas(left)}",
        s"Document contained an element with Attribute {$key=$value}"
      )
  }

  class ContainElementWithTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && !left.getElementsByTag(tag).isEmpty,
        s"Document did not contain element with Tag {$tag}\n${actualContentWas(left)}",
        s"Document contained an element with Tag {$tag}"
      )
  }

  class ElementHasClassMatcher(clazz: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.classNames().contains(clazz),
        s"Element did not have class {$clazz}\n${actualContentWas(left)}",
        s"Element had class {$clazz}"
      )
  }

  class ElementContainsTextMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.text().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(left)}",
        s"Element contained {$content}"
      )
  }

  class ElementContainsHtmlMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.html().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(left)}",
        s"Element contained {$content}"
      )
  }

  class ElementContainsChildWithTextMatcher(tag: String, content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {

      val matches = left != null && left.getElementsByTag(tag).exists(_.text().contains(content))
      MatchResult(
        matches,
        s"Element did not contain text {$content}\n${actualContentWas(left.getElementsByTag(tag))}",
        s"Element contained text {$content}"
      )
    }
  }

  class ElementContainsChildWithAttributeMatcher(tag: String, key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByTag(tag).exists(_.attr(key) == value),
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
  }

  class ElementHasAttributeValueMatcher(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
  }

  class ElementHasAttributeMatcher(key: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.hasAttr(key),
        s"Element didnt have attribute {$key}",
        s"Element had attribute {$key}"
      )
  }

  class ElementHasChildCountMatcher(count: Int) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.children().size() == count,
        s"Element had child count {${left.children().size()}}, expected {$count}",
        s"Element had child count {$count}"
      )
  }

  class ElementsHasSizeMatcher(size: Int) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult =
      MatchResult(
        left != null && left.size() == size,
        s"Elements had size {${left.size()}}, expected {$size}",
        s"Elements had size {$size}"
      )
  }

  class ElementTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.tagName() == tag,
        s"Elements had tag {${left.tagName()}}, expected {$tag}",
        s"Elements had tag {$tag}"
      )
  }

  class ElementContainsFieldError(fieldName: String, content: String = "") extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val element = left.getElementById(s"error-message-$fieldName-input")
      val fieldErrorElement = if (element == null) left else element
      MatchResult(
        fieldErrorElement.text().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(fieldErrorElement)}",
        s"Element contained {$content}"
      )
    }
  }

  class ChildMatcherBuilder(tag: String) {
    def containingText(text: String) = new ElementContainsChildWithTextMatcher(tag, text)
    def withAttribute(key: String, value: String) = new ElementContainsChildWithAttributeMatcher(tag, key, value)
    def withName(value: String) = new ElementContainsChildWithAttributeMatcher(tag, "name", value)
  }

  def containElementWithID(id: String): Matcher[Element] = new ContainElementWithIDMatcher(id)
  def containElementWithClass(name: String): Matcher[Element] = new ContainElementWithClassMatcher(name)
  def containElementWithAttribute(key: String, value: String): Matcher[Element] =
    new ContainElementWithAttribute(key, value)
  def containElementWithTag(tag: String): Matcher[Element] = new ContainElementWithTagMatcher(tag)
  def containText(text: String): Matcher[Element] = new ElementContainsTextMatcher(text)
  def haveClass(text: String): Matcher[Element] = new ElementHasClassMatcher(text)
  def containHtml(text: String): Matcher[Element] = new ElementContainsHtmlMatcher(text)
  def haveSize(size: Int): Matcher[Elements] = new ElementsHasSizeMatcher(size)
  def haveAttribute(key: String, value: String): Matcher[Element] = new ElementHasAttributeValueMatcher(key, value)
  def haveAttribute(key: String): Matcher[Element] = new ElementHasAttributeMatcher(key)
  def haveId(value: String): Matcher[Element] = new ElementHasAttributeValueMatcher("id", value)
  def haveHref(value: String): Matcher[Element] = new ElementHasAttributeValueMatcher("href", value)
  def haveHref(value: Call): Matcher[Element] = new ElementHasAttributeValueMatcher("href", value.url)
  def haveTag(tag: String): Matcher[Element] = new ElementTagMatcher(tag)
  def haveChildCount(count: Int): Matcher[Element] = new ElementHasChildCountMatcher(count)
  def containElement(tag: String) = new ChildMatcherBuilder(tag)

  def haveFieldError(fieldName: String, content: String): Matcher[Element] =
    new ContainElementWithIDMatcher(s"error-message-$fieldName-input") and new ElementContainsFieldError(
      fieldName,
      content
    )

  def haveFieldErrorLink(fieldName: String, link: String): Matcher[Element] =
    new ContainElementWithIDMatcher(s"error-message-$fieldName-input") and new ElementHasAttributeValueMatcher(
      "href",
      link
    )

  def haveGlobalErrorSummary: Matcher[Element] = new ContainElementWithIDMatcher("error-summary-heading")
}
