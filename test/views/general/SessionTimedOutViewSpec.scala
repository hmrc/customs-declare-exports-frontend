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

package views.general

import base.Injector
import controllers.general.routes.RootController
import views.common.UnitViewSpec
import views.html.general.session_timed_out
import views.tags.ViewTest

@ViewTest
class SessionTimedOutViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[session_timed_out]

  private val view = page()(request, messages)

  "SessionTimedOut View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("sessionTimout.title")
      messages must haveTranslationFor("sessionTimout.paragraph.saved")
      messages must haveTranslationFor("sessionTimout.signin.button")
    }

    "display same page header" in {
      view.getElementsByTag("h1").text() mustBe messages("sessionTimout.title")
    }

    "display sign-in button" in {
      val button = view.getElementsByClass("govuk-button").first()
      button.text() mustBe messages("sessionTimout.signin.button")
      button.attr("href") mustBe RootController.displayPage.url
    }

    "display back to gov.uk link" in {
      val link = view.getElementById("govuk-link")
      link.text() mustBe messages("site.backToGovUk")
      link.attr("href") mustBe "https://www.gov.uk"
    }
  }
}
