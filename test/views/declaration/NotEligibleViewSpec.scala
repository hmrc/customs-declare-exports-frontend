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

package views.declaration

import helpers.views.declaration.{CommonMessages, NotEligibleMessages}
import play.twirl.api.Html
import views.html.declaration.not_eligible
import views.declaration.spec.AppViewSpec
import views.tags.ViewTest

@ViewTest
class NotEligibleViewSpec extends AppViewSpec with NotEligibleMessages with CommonMessages {

  private val notEligiblePage = app.injector.instanceOf[not_eligible]
  private def createView(): Html = notEligiblePage()(fakeRequest, messages)

  "Not Eligible View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(pageTitle))
    }

    "display header with hint" in {

      createView().select("h1").text() must be(messages(title) + " " + messages(titleLineTwo))
    }

    "display CHIEF information" in {

      createView().select("p:nth-child(3)").text() must be(
        messages(descriptionPreUrl) + " " + messages(descriptionUrl) + " " + messages(descriptionPostUrl)
      )
    }

    "display CHIEF link" in {

      createView().select("p:nth-child(3)>a").attr("href") must be("https://secure.hmce.gov.uk/ecom/login/index.html")
    }

    "display Help and Support with description" in {

      val view = createView()

      view.select("h3").text() must be(messages(referenceTitle))
      view.select("p:nth-child(5)").text() must be(messages(referenceText))
    }

    "display 'Back' button that links to 'Make declaration' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/start")
    }
  }
}
