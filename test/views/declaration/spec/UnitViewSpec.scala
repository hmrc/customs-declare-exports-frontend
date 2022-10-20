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

import base.{Injector, JourneyTypeTestRunner, UnitWithMocksSpec}
import mock.FeatureFlagMocks
import org.jsoup.nodes.Document
import org.scalatest.matchers.{BeMatcher, MatchResult}
import org.scalatest.{Assertion, OptionValues}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request
import services.cache.ExportsTestHelper
import tools.Stubs
import views.helpers.CommonMessages

trait UnitViewSpec
    extends UnitWithMocksSpec with CommonMessages with FeatureFlagMocks with JourneyTypeTestRunner with OptionValues with Stubs with ViewMatchers {
  val itemId = "item1"

  implicit val request = journeyRequest()

  protected implicit def messages(implicit request: Request[_]): Messages =
    new AllMessageKeysAreMandatoryMessages(realMessagesApi.preferred(request))

  protected def messages(key: String, args: Any*)(implicit request: Request[_]): String = messages(request)(key, args: _*)

  val realMessagesApi = UnitViewSpec.realMessagesApi

  def checkErrorsSummary(view: Document): Assertion = {
    view.getElementById("error-summary-heading").text() must be("error.summary.title")
    view.getElementsByClass("error-summary error-summary--show").get(0).getElementsByTag("p").text() must be("error.summary.text")
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

  def checkSaveAndReturnToErrorsButtonIsDisplayed(createView: Mode => Document): Unit =
    "display 'Save and return to errors' button in ErrorFix mode" in {
      val view = createView(ErrorFix)
      val saveAndReturnToSummaryButton = view.getElementById("save_and_return_to_errors")
      saveAndReturnToSummaryButton must containMessage(saveAndReturnToErrorsCaption)
    }

  def checkAllSaveButtonsAreDisplayed(createView: Mode => Document): Unit = {
    val view = createView()

    checkSaveAndContinueButtonIsDisplayed(view)
    checkExitAndReturnLinkIsDisplayed(view)
    checkSaveAndReturnToErrorsButtonIsDisplayed(createView)
  }
}

class MessagesKeyMatcher(key: String) extends BeMatcher[String] {
  override def apply(left: String): MatchResult =
    if (left == key) {
      val missing = MessagesKeyMatcher.langs.find(lang => !UnitViewSpec.realMessagesApi.isDefinedAt(key)(lang))
      val language = missing.map(_.toLocale.getDisplayLanguage())
      MatchResult(
        missing.isEmpty,
        s"${language.getOrElse("None of languages")} does not have translation for $key",
        s"$key have translation for ${language.getOrElse("every language")}"
      )
    } else {
      MatchResult(matches = false, s"$left is not $key", s"$left is $key")
    }
}

object MessagesKeyMatcher {
  val langs: Seq[Lang] = Seq(Lang("en"))
}

object UnitViewSpec extends Injector with ExportsTestHelper {
  val realMessagesApi: MessagesApi = instanceOf[MessagesApi]
}

private class AllMessageKeysAreMandatoryMessages(msg: Messages) extends Messages {

  override def asJava: play.i18n.Messages = msg.asJava

  override def messages: Messages = msg.messages

  override def lang: Lang = msg.lang

  override def apply(key: String, args: Any*): String =
    if (msg.isDefinedAt(key))
      msg.apply(key, args: _*)
    else {
      new AssertionError(s"Message Key is not configured for {$key}").printStackTrace()
      throw new AssertionError(s"Message Key is not configured for {$key}")
    }

  override def apply(keys: Seq[String], args: Any*): String =
    if (keys.exists(key => !msg.isDefinedAt(key)))
      msg.apply(keys, args)
    else throw new AssertionError(s"Message Key is not configured for {$keys}")

  override def translate(key: String, args: Seq[Any]): Option[String] = msg.translate(key, args)

  override def isDefinedAt(key: String): Boolean = msg.isDefinedAt(key)
}
