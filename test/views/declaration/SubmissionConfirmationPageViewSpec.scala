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
import base.Injector
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import play.api.mvc.Flash
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.submission_confirmation_page
import views.tags.ViewTest

@ViewTest
class SubmissionConfirmationPageViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private def createView(flash: Flash = new Flash(Map.empty)): Document =
    new submission_confirmation_page(mainTemplate)()(journeyRequest, flash, stubMessages())

  "Confirmation Page View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest)
      messages("supplementary.confirmation.title") mustBe "Supplementary Declaration submission confirmation"
      messages("supplementary.confirmation.info") mustBe "You declaration has been received."
      messages("supplementary.confirmation.whatHappensNext") mustBe "What happens next?"
      messages("supplementary.confirmation.explanation") mustBe "Your MRN will be provided in a notification."
      messages("supplementary.confirmation.explanation.linkText") mustBe "Check your notification status in the dashboard."
      messages("supplementary.confirmation.submitAnotherDeclaration") mustBe "Submit another declaration"
      messages("supplementary.confirmation.rejection.header") mustBe "Your declaration has been rejected"
    }

    "display page title" in {

      createView().select("title").text() mustBe "supplementary.confirmation.title"
    }

    "display header" in {

      val view = createView()

      view.select("article>div.govuk-box-highlight>h1").text() mustBe "supplementary.confirmation.header"
      view.select("article>div.govuk-box-highlight>p").text() mustBe "-"
    }

    "display declaration status" in {

      createView().select("article>p:nth-child(2)").text() mustBe "supplementary.confirmation.info"
    }

    "display information about future steps" in {

      val view = createView()

      view.select("article>h1").text() mustBe "supplementary.confirmation.whatHappensNext"
      view
        .select("article>p:nth-child(4)")
        .text() mustBe "supplementary.confirmation.explanation supplementary.confirmation.explanation.linkText"
    }

    "display an 'Check your notification status in the dashboard' empty link without conversationId" in {
      val view = createView()

      val link = view.select("article>p:nth-child(4)>a")
      link.text() mustBe "supplementary.confirmation.explanation.linkText"
      view.getElementsByClass("button").get(0) must haveHref(controllers.routes.ChoiceController.displayPage())
    }

    "display a 'Submit another declaration' button that links to 'What do you want to do ?' page" in {
      val view = createView()

      val button = view.select("article>div.section>a")
      button.text() mustBe "supplementary.confirmation.submitAnotherDeclaration"

      view.getElementsByClass("button").get(0) must haveHref(controllers.routes.ChoiceController.displayPage())
    }
  }

  "Confirmation Page View when filled" should {

    "display LRN and proper link to submissions" in {

      val view = createView(new Flash(Map("LRN" -> "12345")))

      view.select("article>div.govuk-box-highlight>p").text() mustBe "12345"

      val link = view.select("article>p:nth-child(4)>a")
      link.text() mustBe "supplementary.confirmation.explanation.linkText"

      view.getElementById("submissions-link") must haveHref(
        controllers.routes.SubmissionsController.displayListOfSubmissions()
      )
    }
  }
}
