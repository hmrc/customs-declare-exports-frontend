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

package views.common

import base.{MessageSpec, UnitWithMocksSpec}
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.{Assertion, OptionValues}
import play.api.mvc.Call
import play.twirl.api.Html
import tools.Stubs
import views.helpers.CommonMessages

import scala.jdk.CollectionConverters.ListHasAsScala

trait UnitViewSpec extends UnitWithMocksSpec with CommonMessages with MessageSpec with OptionValues with Stubs with ViewMatchers {

  val itemId = "item1"

  def checkMessages(keys: String*): Unit =
    "check messages present including Welsh" in {
      keys.map { key =>
        val messageInWelsh = messagesCy.messages(key)
        messages must haveTranslationFor(key)
        assert(!messageInWelsh.isBlank)
      }
    }

  def checkErrorsSummary(view: Document): Assertion = {
    view.getElementById("error-summary-heading").text() must be("error.summary.title")
    view.getElementsByClass("error-summary error-summary--show").first.getElementsByTag("p").text() must be("error.summary.text")
  }

  def checkExitAndReturnLinkIsDisplayed(view: Document): Unit =
    "display 'Exit and return' button" in {
      val exitAndReturnButton = view.getElementById("exit-and-complete-later")
      exitAndReturnButton must containMessage(exitAndReturnCaption)
    }

  def checkSaveAndContinueButtonIsDisplayed(view: Document): Unit =
    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton must containMessage("site.save_and_continue")
    }

  def checkAllSaveButtonsAreDisplayed(view: Document): Unit = {
    checkSaveAndContinueButtonIsDisplayed(view)
    checkExitAndReturnLinkIsDisplayed(view)
  }

  def checkSummaryRow(row: Elements, labelKey: String, value: String, maybeUrl: Option[Call] = None, hint: String = ""): Assertion = {
    row must haveSummaryKey(messages(s"declaration.summary.$labelKey"))
    row must haveSummaryValue(value)

    val action = row.first.getElementsByClass(summaryActionsClassName)

    maybeUrl.fold(if (hint.isEmpty) action.size mustBe 0 else action.text mustBe "") { url =>
      val expectedText = s"""${messages("site.change")} ${messages(s"declaration.summary.$hint.change")}"""
      action.text must startWith(expectedText.replace(" {0}", ""))
      row must haveSummaryActionWithPlaceholder(url)
    }
  }

  def checkSection(
    view: Html,
    headingId: String,
    headingText: String,
    expectedSummaryLists: Int,
    wantedSummaryList: Int,
    expectedSummaryRows: Int
  ): Elements = {
    val heading = view.getElementsByClass(s"$headingId-heading").first
    heading.tagName mustBe "h3"
    messages.isDefinedAt(s"declaration.summary.$headingText") match {
      case false => heading.text mustBe headingText
      case _     => heading.text mustBe messages(s"declaration.summary.$headingText")
    }

    val summaryLists = view.getElementsByClass("govuk-summary-list")
    summaryLists.size mustBe expectedSummaryLists

    val summaryList = summaryLists.get(wantedSummaryList)
    summaryList.childrenSize mustBe expectedSummaryRows

    val rows = summaryList.children
    assert(rows.asScala.forall(_.hasClass("govuk-summary-list__row")))
    rows
  }

  def checkMultiRowSection(
    section: Element,
    expectedClasses: Seq[String] = List.empty,
    labelKey: String,
    value: String,
    maybeUrl: Option[Call] = None,
    hint: String = ""
  ): Assertion = {
    assert(expectedClasses.forall(section.hasClass))
    section must hasSummaryKey(messages(s"declaration.summary.$labelKey"))
    section must hasSummaryValue(value)

    val action = section.getElementsByClass(summaryActionsClassName)

    maybeUrl.fold(if (hint.isEmpty) action.size mustBe 0 else action.text mustBe "") { url =>
      val expectedText = s"""${messages("site.change")} ${messages(s"declaration.summary.$hint.change")}"""
      section must haveSummaryActionWithPlaceholder(url, expectedText.replace(" {0}", ""))
    }
  }
}
