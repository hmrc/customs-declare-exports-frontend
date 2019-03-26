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
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class NotEligibleViewSpec extends ViewSpec with NotEligibleMessages with CommonMessages {

  private def createView(): Html = not_eligible(appConfig)(fakeRequest, messages)

  "Not Eligible View" should {

    "have proper messages for labels" in {

      assertMessage(title, "You need to use a different")
      assertMessage(titleLineTwo, "service")
      assertMessage(
        descriptionPreUrl,
        "This service is only for exports being dispatched outside the EU. You will need to use"
      )
      assertMessage(descriptionUrl, "CHIEF")
      assertMessage(descriptionPostUrl, "to make your declaration.")
      assertMessage(referenceTitle, "Help and support")
      assertMessage(
        referenceText,
        "If you are having problems with making a declaration, phone: 0300 333 3333. Open 8am to 6pm, Monday to Friday (closed bank holidays)."
      )
    }
  }

  "Not Eligible View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header with hint" in {

      getElementByCss(createView(), "h1").text() must be(messages(title) + " " + messages(titleLineTwo))
    }

    "display CHIEF information" in {

      getElementsByCss(createView(), "p:nth-child(3)").text() must be(
        messages(descriptionPreUrl) + " " + messages(descriptionUrl) + " " + messages(descriptionPostUrl)
      )
    }

    "display CHIEF link" in {

      getElementByCss(createView(), "p:nth-child(3)>a").attr("href") must be(
        "https://secure.hmce.gov.uk/ecom/login/index.html"
      )
    }

    "display Help and Support with description" in {

      val view = createView()

      getElementsByCss(view, "h3").text() must be(messages(referenceTitle))
      getElementsByCss(view, "p:nth-child(5)").text() must be(messages(referenceText))
    }

    "display \"Back\" button that links to \"Make declaration\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/start")
    }
  }
}
