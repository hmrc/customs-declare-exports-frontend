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
import helpers.views.declaration.ConfirmationMessages
import play.api.mvc.Flash
import play.twirl.api.Html
import views.html.declaration.confirmation_page
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class ConfirmationPageViewSpec extends ViewSpec with ConfirmationMessages {

  private def createView(): Html = confirmation_page(appConfig)(fakeRequest, flash, messages)

  "Confirmation Page View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Supplementary Declaration submission confirmation")
      assertMessage(header, "Your LRN is")
      assertMessage(information, "You declaration has been received.")
      assertMessage(whatHappensNext, "What happens next?")
      assertMessage(explanation, "Your MRN will be provided in a notification.")
      assertMessage(explanationLink, "Check your notification status in the dashboard.")
      assertMessage(submitAnother, "Submit another declaration")
    }
  }

  "Confirmation Page View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      val view = createView()

      getElementByCss(view, "article>div.govuk-box-highlight>h1").text() must be(messages(header))
      getElementByCss(view, "article>div.govuk-box-highlight>p").text() must be("-")
    }

    "display declaration status" in {

      getElementByCss(createView(), "article>p:nth-child(2)").text() must be(messages(information))
    }

    "display information about future steps" in {

      val view = createView()

      getElementByCss(view, "article>h1").text() must be(messages(whatHappensNext))
      getElementByCss(view, "article>p:nth-child(4)").text() must be(
        messages(explanation) + " " + messages(explanationLink)
      )
    }

    "display an \"Check your notification status in the dashboard\" empty link without conversationId" in {

      val view = createView()

      val link = getElementByCss(view, "article>p:nth-child(4)>a")
      link.text() must be(messages(explanationLink))
      link.attr("href") must be("/customs-declare-exports/submissions")
    }

    "display a \"Submit another declaration\" button that links to \"What do you want to do ?\" page" in {

      val view = createView()

      val button = getElementByCss(view, "article>div.section>a")
      button.text() must be(messages(submitAnother))
      button.attr("href") must be("/customs-declare-exports/choice")
    }
  }

  "Confirmation Page View when filled" should {

    "display LRN and proper link to submissions" in {

      val view = confirmation_page(appConfig)(fakeRequest, new Flash(Map("LRN" -> "12345")), messages)

      getElementByCss(view, "article>div.govuk-box-highlight>p").text() must be("12345")

      val link = getElementByCss(view, "article>p:nth-child(4)>a")
      link.text() must be(messages(explanationLink))
      link.attr("href") must be("/customs-declare-exports/submissions")
    }
  }
}
