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

import base.Injector
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import org.jsoup.nodes.Document
import org.scalatest.matchers.{BeMatcher, MatchResult}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.{FakeRequest, Helpers}
import services.cache.{ExportsDeclarationBuilder, ExportsTestData}
import unit.base.UnitSpec

class UnitViewSpec extends UnitSpec with ViewMatchers {

  import utils.FakeRequestCSRFSupport._

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  implicit val messages: Messages = Helpers.stubMessages()

  val realMessagesApi = UnitViewSpec.realMessagesApi

  def validatedMessages(implicit request: Request[_]): Messages =
    new AllMessageKeysAreMandatoryMessages(realMessagesApi.preferred(request))

  def checkErrorsSummary(view: Document) = {
    view.getElementById("error-summary-heading").text() must be("error.summary.title")
    view.getElementsByClass("error-summary error-summary--show").get(0).getElementsByTag("p").text() must be("error.summary.text")
  }

  def messagesKey(key: String): BeMatcher[String] = new MessagesKeyMatcher(key)

  def onEveryDeclarationJourney(f: JourneyRequest[_] => Unit): Unit = {
    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED)(f)
  }

  def onJourney(types: DeclarationType*)(f: JourneyRequest[_] => Unit): Unit = {
    types.foreach {
      case DeclarationType.STANDARD => onStandard(f)
      case DeclarationType.SUPPLEMENTARY => onSupplementary(f)
      case DeclarationType.SIMPLIFIED => onSimplified(f)
    }
  }

  def onStandard(f: JourneyRequest[_] => Unit): Unit = {
    "on Standard journey render view" that {
      f(UnitViewSpec.standardRequest)
    }
  }

  def onSimplified(f: JourneyRequest[_] => Unit): Unit = {
    "on Supplementary journey render view" that {
      f(UnitViewSpec.supplementaryRequest)
    }
  }

  def onSupplementary(f: JourneyRequest[_] => Unit): Unit = {
    "on Simplified journey render view" that {
      f(UnitViewSpec.simplifiedRequest)
    }
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

object UnitViewSpec extends Injector with ExportsTestData {
  val realMessagesApi: MessagesApi = instanceOf[MessagesApi]

  val standardRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.STANDARD)

  val supplementaryRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.SUPPLEMENTARY)

  val simplifiedRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.SIMPLIFIED)
}

private class AllMessageKeysAreMandatoryMessages(msg: Messages) extends Messages {
  override def messages: Messages = msg.messages

  override def lang: Lang = msg.lang

  override def apply(key: String, args: Any*): String =
    if (msg.isDefinedAt(key))
      msg.apply(key, args: _*)
    else throw new AssertionError(s"Message Key is not configured for {$key}")

  override def apply(keys: Seq[String], args: Any*): String =
    if (keys.exists(key => !msg.isDefinedAt(key)))
      msg.apply(keys, args)
    else throw new AssertionError(s"Message Key is not configured for {$keys}")

  override def translate(key: String, args: Seq[Any]): Option[String] = msg.translate(key, args)

  override def isDefinedAt(key: String): Boolean = msg.isDefinedAt(key)
}
