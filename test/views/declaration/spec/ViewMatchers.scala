/*
 * Copyright 2022 HM Revenue & Customs
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

import scala.concurrent.Future
import scala.util.Try
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.matchers._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.i18n.Messages
import play.api.mvc.{Call, Result}
import play.api.test.Helpers.{contentAsString, _}
import play.twirl.api.Html
import views.helpers.ActionItemBuilder.callForSummaryChangeLink

import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.language.implicitConversions

//noinspection ScalaStyle
trait ViewMatchers {

  implicit private def elements2Scala(elements: Elements): Iterator[Element] = elements.iterator().asScala
  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())
  implicit protected def htmlBodyOf(page: String): Document = Jsoup.parse(page)
  implicit protected def htmlBodyOf(result: Future[Result]): Document = htmlBodyOf(contentAsString(result))

  def removeBlanksIfAnyBeforeDot(s: String): String = s.replace(" .", ".")

  def removeLineBreakIfAny(s: String): String = s.replace("<br>", " ")
  def removeNewLinesIfAny(s: String): String = s.replaceAll("\n *", " ")

  implicit class PageComplexChecks(document: Document) {
    def checkErrorsSummary(): Unit = {
      document.getElementById("error-summary-heading").text() mustBe "error.summary.title"
      document.select("div.error-summary.error-summary--show>p").text() mustBe "error.summary.text"
    }
  }

  def containElementWithID(id: String): Matcher[Element] = new ContainElementWithIDMatcher(id)

  def containElementWithClass(name: String): Matcher[Element] = new ContainElementWithClassMatcher(name)

  def containErrorElementWithTagAndHref(tag: String, href: String): Matcher[Element] = new ContainErrorElementWithClassMatcher(tag, href)

  def containErrorElementWithMessage(text: String): Matcher[Element] = new ContainErrorElementWithMessage(text)
  def containErrorElementWithMessageKey(key: String)(implicit messages: Messages): Matcher[Element] =
    new ContainErrorElementWithMessage(messages(key))

  def containElementWithAttribute(key: String, value: String): Matcher[Element] =
    new ContainElementWithAttribute(key, value)

  def containElementWithTag(tag: String): Matcher[Element] = new ContainElementWithTagMatcher(tag)

  def containText(text: String): Matcher[Element] = new ElementContainsTextMatcher(text)

  def containMessage(key: String, args: Any*)(implicit messages: Messages): Matcher[Element] =
    new ElementContainsMessageMatcher(key, args)

  def containMessageForElements(key: String, args: Any*)(implicit messages: Messages): Matcher[Elements] =
    new ElementsContainsMessageMatcher(key, args)

  def beSelected: Matcher[Element] = new ElementSelectedMatcher(true)

  def haveClass(text: String): Matcher[Element] = new ElementHasClassMatcher(text)

  def containHtml(text: String): Matcher[Element] = new ElementContainsHtmlMatcher(text)

  def haveSize(size: Int): Matcher[Elements] = new ElementsHasSizeMatcher(size)

  def haveAttribute(key: String, value: String): Matcher[Element] = new ElementHasAttributeValueMatcher(key, value)

  def haveAttribute(key: String): Matcher[Element] = new ElementHasAttributeMatcher(key)

  def haveId(value: String): Matcher[Element] = new ElementHasAttributeValueMatcher("id", value)

  def haveHref(value: String): Matcher[Element] = new ElementHasAttributeValueMatcher("href", value)

  def haveHref(value: Call): Matcher[Element] = new ElementHasAttributeValueMatcher("href", value.url)

  def haveHrefWithPlaceholder(value: Call): Matcher[Element] =
    new ElementHasAttributeValueMatcher("href", callForSummaryChangeLink(value).url)

  def haveTag(tag: String): Matcher[Element] = new ElementTagMatcher(tag)

  def haveSummaryKey(value: String) = new ElementsHasElementsContainingTextMatcher("govuk-summary-list__key", value)
  def haveSummaryValue(value: String) = new ElementsHasElementsContainingTextMatcher("govuk-summary-list__value", value)
  def haveSummaryActionsText(value: String) = new ElementsHasElementsContainingTextMatcher("govuk-summary-list__actions", value)

  def haveSummaryActionsTexts(label: String, hint: String, hintArgs: String*)(implicit messages: Messages) =
    haveSummaryActionsText(s"${messages(label)} ${messages(hint, hintArgs: _*)}")

  def haveSummaryActionsHref(value: Call) =
    new ElementsHasSummaryActionMatcher(value)

  def haveSummaryActionWithPlaceholder(value: Call) =
    new ElementsHasSummaryActionMatcher(callForSummaryChangeLink(value))

  def haveChildCount(count: Int): Matcher[Element] = new ElementHasChildCountMatcher(count)

  def containElement(tag: String) = new ChildMatcherBuilder(tag)

  def haveFieldError(fieldName: String, content: String): Matcher[Element] =
    new ContainElementWithIDMatcher(s"error-message-$fieldName-input") and new ElementContainsFieldError(fieldName, content)

  def haveGovukFieldError(fieldName: String, content: String): Matcher[Element] =
    new ContainElementWithIDMatcher(s"$fieldName-error") and new ElementContainsGovukFieldError(fieldName, content)

  def haveFieldErrorLink(fieldName: String, link: String): Matcher[Element] =
    new ElementContainsFieldErrorLink(fieldName, link)

  def haveGlobalErrorSummary: Matcher[Document] = new ContainElementWithIDMatcher("error-summary-heading")

  def haveGovukGlobalErrorSummary: Matcher[Document] = new ContainElementWithClassMatcher("govuk-error-summary")

  def test: Matcher[Document] = new ContainErrorElementWithClassMatcher("govuk-list govuk-error-summary__list", "#value")

  def haveTranslationFor(key: String): Matcher[Messages] = new TranslationKeyMatcher(key)

  def submitTo(path: String): Matcher[Element] = new FormSubmitTo(path)

  def submitTo(call: Call): Matcher[Element] = new FormSubmitTo(call.url)

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

  class TranslationKeyMatcher(key: String) extends Matcher[Messages] {
    override def apply(left: Messages): MatchResult = MatchResult(
      matches = left.isDefinedAt(key),
      rawFailureMessage = s"$key is not defined in Messages",
      rawNegatedFailureMessage = s"$key is defined in Messages"
    )
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

  class ContainErrorElementWithClassMatcher(tag: String, href: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByClass("govuk-list govuk-error-summary__list").get(0).getElementsByTag(tag).eachAttr("href").contains(href),
        s"Document did not contain element with class {$tag}\n${actualContentWas(left)}",
        s"Document contained an element with class {$tag}"
      )
  }

  class ContainErrorElementWithMessage(text: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByClass("govuk-error-summary__list").html().contains(text),
        s"Document did not contain error element with message {$text}\n${actualContentWas(left)}",
        s"Document contained an error element with message {$text}"
      )
  }

  class ElementSelectedMatcher(expected: Boolean) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val isChecked = left.getElementsByAttribute("checked").size() == 1
      MatchResult(left != null && isChecked == expected, s"Element was not selected\n${actualContentWas(left)}", "Element was selected")
    }
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

  class ElementContainsMessageMatcher(key: String, args: Seq[Any])(implicit messages: Messages) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val message = messages(key, args: _*)
      MatchResult(
        left != null && left.text().contains(message),
        s"Element did not contain message {$message}\n${actualContentWas(left)}",
        s"Element contained message {$message}"
      )
    }
  }

  class ElementsContainsMessageMatcher(key: String, args: Seq[Any])(implicit messages: Messages) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult = {
      val message = messages(key, args: _*)
      MatchResult(
        left != null && left.text().contains(message),
        s"Elements did not contain message {$message}\n${actualContentWas(left)}",
        s"Elements contained message {$message}"
      )
    }
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
      MatchResult(left != null && left.hasAttr(key), s"Element didnt have attribute {$key}", s"Element had attribute {$key}")
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
      MatchResult(left != null && left.size() == size, s"Elements had size {${left.size()}}, expected {$size}", s"Elements had size {$size}")
  }

  class ElementTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(left != null && left.tagName() == tag, s"Elements had tag {${left.tagName()}}, expected {$tag}", s"Elements had tag {$tag}")
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

  class ElementContainsGovukFieldError(fieldName: String, content: String = "") extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val element = left.getElementById(s"$fieldName-error")
      val fieldErrorElement = if (element == null) left else element
      MatchResult(
        fieldErrorElement.text().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(fieldErrorElement)}",
        s"Element contained {$content}"
      )
    }
  }

  class ElementContainsFieldErrorLink(fieldName: String, link: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val element = left.getElementById(s"$fieldName-error")
      MatchResult(element != null && element.attr("href") == link, s"View not contains $fieldName with $link", s"View contains $fieldName with $link")
    }
  }

  class ElementsHasElementsContainingTextMatcher(elementsClass: String, value: String) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult =
      MatchResult(
        left != null && left.first().getElementsByClass(elementsClass).text() == value,
        s"Elements with class {$elementsClass} had text {${left.first().getElementsByClass(elementsClass).text()}}, expected {$value}",
        s"Element with class {$elementsClass} had text {${left.first().getElementsByClass(elementsClass).text()}}"
      )
  }

  class ElementsHasSummaryActionMatcher(value: Call) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult = {
      val actionElement = Try(left.first().getElementsByClass("govuk-link").first()).toOption.orNull

      MatchResult(
        left != null && actionElement != null && actionElement.attr("href") == value.url,
        s"Elements had no summary action {$value}\n${actualContentWas(actionElement)}",
        s"Element had summary action {$value}"
      )
    }
  }

  class ChildMatcherBuilder(tag: String) {
    def containingText(text: String) = new ElementContainsChildWithTextMatcher(tag, text)
    def withAttribute(key: String, value: String) = new ElementContainsChildWithAttributeMatcher(tag, key, value)
    def withName(value: String) = new ElementContainsChildWithAttributeMatcher(tag, "name", value)
  }

  class FormSubmitTo(path: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val action = left.attr("action")
      val formaction = left.attr("formaction")
      MatchResult(action == path || formaction == path, s"Element ${left} does not submit to {$path}", s"Element ${left} does submit to {$path}")
    }
  }
}
