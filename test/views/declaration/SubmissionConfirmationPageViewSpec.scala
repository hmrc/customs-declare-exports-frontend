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

package views.declaration

import base.OverridableInjector
import config.GoogleFormFeedbackLinkConfig
import models.DeclarationType
import models.DeclarationType.DeclarationType
import models.responses.FlashKeys
import org.jsoup.nodes.Document
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.mvc.Flash
import views.declaration.spec.UnitViewSpec
import views.html.declaration.submission_confirmation_page
import views.tags.ViewTest

@ViewTest
class SubmissionConfirmationPageViewSpec extends UnitViewSpec with BeforeAndAfterEach {

  private val googleFormFeedbackLink = "googleFormFeedbackLink"
  private val googleFormFeedbackLinkConfig = mock[GoogleFormFeedbackLinkConfig]
  private val injector = new OverridableInjector(bind[GoogleFormFeedbackLinkConfig].toInstance(googleFormFeedbackLinkConfig))

  private val page = injector.instanceOf[submission_confirmation_page]
  private val withoutFlash = new Flash(Map.empty)
  private def withFlash(devType: DeclarationType, lrn: String, decId: String) =
    new Flash(Map(FlashKeys.decId -> decId, FlashKeys.lrn -> lrn, FlashKeys.decType -> devType.toString))

  private def createView(flash: Flash = withoutFlash): Document = page()(journeyRequest(), flash, messages)

  private def getHighlightBox(view: Document) = view.getElementsByClass("govuk-panel govuk-panel--confirmation").first()
  private def getDecisionLink(view: Document) = view.getElementById("decision-link")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(googleFormFeedbackLinkConfig)
    when(googleFormFeedbackLinkConfig.googleFormFeedbackLink).thenReturn(None)
  }

  override def afterEach(): Unit = {
    reset(googleFormFeedbackLinkConfig)
    super.afterEach()
  }

  "Confirmation Page View always" should {

    "display start again link" in {
      val button = createView().getElementById("back-to-start-link")
      button must haveHref(controllers.routes.ChoiceController.displayPage().url)
      button must containMessage("declaration.confirmation.submitAnotherDeclaration")
    }

    "display Exit Survey link" in {
      val exitSurvey = createView().getElementById("exit-survey")

      exitSurvey must containMessage("declaration.exitSurvey.header")
    }

    "display Google Form feedback link" when {
      "GoogleFormFeedbackLinkConfig returns non-empty Option" in {
        when(googleFormFeedbackLinkConfig.googleFormFeedbackLink).thenReturn(Some(googleFormFeedbackLink))

        val feedbackLink = createView().getElementById("feedback-link")

        feedbackLink must containMessage("declaration.confirmation.leaveFeedback.link")
        feedbackLink must haveHref(googleFormFeedbackLink)
      }
    }

    "not display Google Form feedback link" when {
      "GoogleFormFeedbackLinkConfig return empty Option" in {
        createView().getElementById("feedback-link") mustBe null
      }
    }
  }

  "Confirmation Page View when LRN & DecId are missing in flash cookie" should {

    "display header with no reference to submitted declaration's LRN" in {
      val highlightBox = getHighlightBox(createView())

      highlightBox must containMessage("declaration.confirmation.title")
      highlightBox mustNot containMessage("declaration.confirmation.lrn")
    }

    "display inset text with expected generic content and link" in {
      val view = createView()

      view.getElementsByClass("govuk-inset-text").get(0) must containMessage(
        "declaration.confirmation.decision.paragraph1",
        messages("declaration.confirmation.decision.genericLink.text")
      )

      getDecisionLink(view) must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions().url)
    }
  }

  "Confirmation Page View when LRN & DecId are present in flash cookie" should {

    "display header with reference to submitted declaration's LRN" in {
      val highlightBox = getHighlightBox(createView(withFlash(DeclarationType.STANDARD, "lrn1", "dec1")))

      highlightBox must containMessage("declaration.confirmation.title")
      highlightBox must containMessage("declaration.confirmation.lrn", "lrn1")
    }

    "display inset text with expected specific content and link" in {
      val view = createView(withFlash(DeclarationType.STANDARD, "lrn1", "dec1"))

      view.getElementsByClass("govuk-inset-text").get(0) must containMessage(
        "declaration.confirmation.decision.paragraph1",
        messages("declaration.confirmation.decision.directLink.text")
      )
      getDecisionLink(view) must haveHref(controllers.routes.DeclarationDetailsController.displayPage("dec1").url)
    }
  }
}
