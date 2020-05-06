/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import base.Injector
import controllers.declaration.routes
import play.api.i18n.MessagesApi
import play.twirl.api.Html
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.session_timed_out
import views.tags.ViewTest

@ViewTest
class SessionTimedOutViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[session_timed_out]
  private def createView(): Html =
    page()(request, messages)

  "SessionTimedOut View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("sessionTimout.title")
      messages must haveTranslationFor("sessionTimout.paragraph.saved")
      messages must haveTranslationFor("sessionTimout.signin.button")
      messages must haveTranslationFor("sessionTimout.back.button")
    }

    val view = createView()

    "display same page header" in {
      view.getElementsByTag("h1").text() mustBe messages("sessionTimout.title")
    }

    "display sign-in button" in {
      val button = view.getElementsByClass("govuk-button").first()
      button.text() mustBe messages("sessionTimout.signin.button")
      button.attr("href") mustBe controllers.routes.StartController.displayStartPage().url
    }

    "display back to gov.uk link" in {
      val link = view.getElementsByClass("govuk-link").first()
      link.text() mustBe messages("sessionTimout.back.button")
      link.attr("href") mustBe "https://www.gov.uk/"
    }
  }

}
