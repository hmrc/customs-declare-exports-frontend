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
import mock.FeatureFlagMocks
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.{Assertion, OptionValues}
import play.api.mvc.Call
import tools.Stubs
import views.helpers.CommonMessages

trait UnitViewSpec
    extends UnitWithMocksSpec with CommonMessages with FeatureFlagMocks with MessageSpec with OptionValues with Stubs with ViewMatchers {

  val itemId = "item1"
  val sequenceId = "1"

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
    maybeUrl.fold {
      val action = row.first.getElementsByClass(summaryActionsClassName)
      if (hint.isEmpty) action.size mustBe 0 else action.text mustBe ""
    } { url =>
      row must haveSummaryActionsTexts("site.change", s"declaration.summary.$hint.change", sequenceId)
      row must haveSummaryActionWithPlaceholder(url)
    }
  }
}
