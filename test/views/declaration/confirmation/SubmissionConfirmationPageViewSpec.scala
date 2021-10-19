/*
 * Copyright 2021 HM Revenue & Customs
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

package views.declaration.confirmation

import java.util.UUID

import base.{MockAuthAction, OverridableInjector}
import config.GoogleFormFeedbackLinkConfig
import controllers.routes.{ChoiceController, DeclarationDetailsController, SubmissionsController}
import models.requests.ExportsSessionKeys.{submissionDucr, submissionId}
import models.requests.JourneyRequest
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import views.declaration.spec.UnitViewSpec
import views.html.declaration.confirmation.submission_confirmation_page
import views.tags.ViewTest

@ViewTest
class SubmissionConfirmationPageViewSpec extends UnitViewSpec with MockAuthAction {

  private val googleFormFeedbackLinkConfig = mock[GoogleFormFeedbackLinkConfig]
  private val injector = new OverridableInjector(bind[GoogleFormFeedbackLinkConfig].toInstance(googleFormFeedbackLinkConfig))
  private val page = injector.instanceOf[submission_confirmation_page]

  private def createView(googleFormFeedbackLink: Option[String] = None)(implicit request: JourneyRequest[_]): Document = {
    when(googleFormFeedbackLinkConfig.googleFormFeedbackLink).thenReturn(googleFormFeedbackLink)
    page()(request, messages)
  }

  private def getHighlightBox(view: Document): Element =
    view.getElementsByClass("govuk-panel govuk-panel--confirmation").first

  private def getDecisionLink(view: Document): Element = view.getElementById("decision-link")

  "Confirmation Page View" should {
    implicit val r = new JourneyRequest(buildAuthenticatedRequest(request, exampleUser), aDeclaration())
    val view = createView()

    "display start again link" in {
      val button = view.getElementById("back-to-start-link")
      button must haveHref(ChoiceController.displayPage().url)
      button must containMessage("declaration.confirmation.submitAnotherDeclaration")
    }

    "display Exit Survey link" in {
      val exitSurvey = view.getElementById("exit-survey")
      exitSurvey must containMessage("declaration.exitSurvey.header")
    }

    "display Google Form feedback link" when {
      "GoogleFormFeedbackLinkConfig returns non-empty Option" in {
        val googleFormFeedbackLink = "googleFormFeedbackLink"

        val feedbackLink = createView(Some(googleFormFeedbackLink)).getElementById("feedback-link")
        feedbackLink must containMessage("declaration.confirmation.leaveFeedback.link")
        feedbackLink must haveHref(googleFormFeedbackLink)
      }
    }

    "not display Google Form feedback link" when {
      "GoogleFormFeedbackLinkConfig return empty Option" in {
        Option(view.getElementById("feedback-link")) mustBe None
      }
    }

    "display header with no reference to submitted declaration's LRN" when {
      "the session does not include the LRN" in {
        val highlightBox = getHighlightBox(view)

        highlightBox must containMessage("declaration.confirmation.title")
        highlightBox mustNot containMessage("declaration.confirmation.lrn")
      }
    }

    "display inset text with expected generic content and link" when {
      "the session does not include the LRN" in {
        view.getElementsByClass("govuk-inset-text").get(0) must containMessage(
          "declaration.confirmation.decision.paragraph1",
          messages("declaration.confirmation.decision.genericLink.text")
        )

        getDecisionLink(view) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }
    }
  }

  "Confirmation Page View" when {

    "the session does include the LRN" should {

      val uuid = UUID.randomUUID.toString
      val request = FakeRequest().withSession((submissionId -> uuid), (submissionDucr -> LRN.value))
      implicit val r = new JourneyRequest(buildAuthenticatedRequest(request, exampleUser), aDeclaration())
      val view = createView()

      "display header with reference to submitted declaration's LRN" in {
        val highlightBox = getHighlightBox(view)

        highlightBox must containMessage("declaration.confirmation.title")
        highlightBox must containMessage("declaration.confirmation.lrn", LRN.value)
      }

      "display inset text with expected specific content and link" in {
        view.getElementsByClass("govuk-inset-text").get(0) must containMessage(
          "declaration.confirmation.decision.paragraph1",
          messages("declaration.confirmation.decision.directLink.text")
        )
        getDecisionLink(view) must haveHref(DeclarationDetailsController.displayPage(uuid).url)
      }
    }
  }
}
