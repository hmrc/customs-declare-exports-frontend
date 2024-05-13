/*
 * Copyright 2023 HM Revenue & Customs
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

package views.cancellation

import base.ExportsTestData.mrn
import base.{Injector, MockAuthAction}
import controllers.routes.ChoiceController
import models.declaration.submissions.EnhancedStatus._
import org.scalatest.GivenWhenThen
import play.twirl.api.HtmlFormat.Appendable
import views.declaration.spec.UnitViewSpec
import views.html.cancellation_result
import views.tags.ViewTest

@ViewTest
class CancellationResultViewSpec extends UnitViewSpec with Injector with MockAuthAction with GivenWhenThen {

  private val page = instanceOf[cancellation_result]
  private val req = buildVerifiedEmailRequest(request, exampleUser)

  private def createView(status: Option[EnhancedStatus]): Appendable = page(status, mrn)(req, messages(req))

  "Result page returns expected content" when {

    "no notification has been received for the given MRN" should {
      val view = createView(None)

      "display title" in {
        view.getElementById("title").text mustBe messages("cancellation.result.unprocessed.title")
      }

      "display the 'MRN' hint" in {
        view.getElementsByClass("submission-mrn").first.text mustBe messages("mrn.heading", mrn)
      }

      "display correct paragraph" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("cancellation.result.unprocessed.p1")
      }

      "display 'Back to Choice Page' link" in {
        view.getElementById("back-to-choice").text mustBe messages("site.backToChoice")
        view.getElementById("back-to-choice") must haveHref(ChoiceController.displayPage)
      }
    }

    "unrelated notification has been received for the given MRN" should {
      val view = createView(Some(QUERY_NOTIFICATION_MESSAGE))

      "display title" in {
        view.getElementById("title").text mustBe messages("cancellation.result.unprocessed.title")
      }

      "display the 'MRN' hint" in {
        view.getElementsByClass("submission-mrn").first.text mustBe messages("mrn.heading", mrn)
      }

      "display correct paragraph" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("cancellation.result.unprocessed.p1")
      }

      "display 'Back to Choice Page' link" in {
        view.getElementById("back-to-choice").text mustBe messages("site.backToChoice")
        view.getElementById("back-to-choice") must haveHref(ChoiceController.displayPage)
      }
    }

    "notification indicating request has been denied received for the given MRN" should {
      val view = createView(Some(CUSTOMS_POSITION_DENIED))

      "display title" in {
        view.getElementById("title").text mustBe messages("cancellation.result.denied.title")
      }

      "display the 'MRN' hint" in {
        view.getElementsByClass("submission-mrn").first.text mustBe messages("mrn.heading", mrn)
      }

      "display correct paragraph" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("cancellation.result.denied.p1")
      }

      "display 'Back to Choice Page' link" in {
        view.getElementById("back-to-choice").text mustBe messages("site.backToChoice")
        view.getElementById("back-to-choice") must haveHref(ChoiceController.displayPage)
      }
    }

    "notification indicating a successful request has been received for the given MRN" should {
      val view = createView(Some(CUSTOMS_POSITION_GRANTED))

      "display panel" in {
        val panel = view.getElementsByClass("govuk-panel")
        panel.size mustBe 1

        val children = panel.get(0).children

        And("which should include the expected title")
        children.get(0).tagName mustBe "h1"
        children.get(0).text mustBe messages("cancellation.result.cancelled.title")

        And("should include content with MRN")
        children.get(1).text mustBe messages("mrn.heading", mrn)
      }

      "display correct paragraph" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("cancellation.result.cancelled.p1")
        view.getElementsByClass("govuk-body").get(1).text mustBe messages("cancellation.result.cancelled.p2")
      }

      "display 'Back to Choice Page' link" in {
        view.getElementById("back-to-choice").text mustBe messages("site.backToChoice")
        view.getElementById("back-to-choice") must haveHref(ChoiceController.displayPage)
      }
    }
  }
}
