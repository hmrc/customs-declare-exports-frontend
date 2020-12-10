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

  "Confirmation Page View on empty page" should {

    "display header with default" in {
      val highlightBox = getHighlightBox(createView())
      highlightBox must containText("Declaration has been submitted")
      highlightBox mustNot containText("The LRN is")
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

    "display declaration status" in {
      val declarationInfo = createView().getElementById("submissions-link")
      declarationInfo must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }

    "display start again link" in {
      val button = createView().getElementById("back-to-start-link")
      button must haveHref(controllers.routes.ChoiceController.displayPage().url)
      button must containText("Create a new declaration")
    }

    "display Exit Survey link" in {
      val exitSurvey = createView().getElementById("exit-survey")

      exitSurvey must containMessage("declaration.exitSurvey.header")
    }
  }
  "Confirmation Page View when filled" should {

    "display header with declaration type Standard and LRN" in {
      val view = createView(withFlash(DeclarationType.STANDARD, "lrn1", "dec1"))
      val highlightBox = getHighlightBox(view)
      highlightBox must containText("Standard declaration has been submitted")
      highlightBox must containText("Your LRN is lrn1")
      getDecisionLink(view) must haveHref(controllers.routes.SubmissionsController.displayDeclarationWithNotifications("dec1").url)
    }

    "display header with declaration type Simplified and LRN" in {
      val view = createView(withFlash(DeclarationType.SIMPLIFIED, "lrn2", "dec2"))
      val highlightBox = getHighlightBox(view)
      highlightBox must containText("Simplified declaration has been submitted")
      highlightBox must containText("Your LRN is lrn2")
      getDecisionLink(view) must haveHref(controllers.routes.SubmissionsController.displayDeclarationWithNotifications("dec2").url)
    }

    "display header with declaration type Supplementary and LRN" in {
      val view = createView(withFlash(DeclarationType.SUPPLEMENTARY, "lrn3", "dec3"))
      val highlightBox = getHighlightBox(view)
      highlightBox must containText("Supplementary declaration has been submitted")
      highlightBox must containText("Your LRN is lrn3")
      getDecisionLink(view) must haveHref(controllers.routes.SubmissionsController.displayDeclarationWithNotifications("dec3").url)
    }

    "display header with declaration type Occasional and LRN" in {
      val view = createView(withFlash(DeclarationType.OCCASIONAL, "lrn4", "dec4"))
      val highlightBox = getHighlightBox(view)
      highlightBox must containText("Simplified declaration for occasional use has been submitted")
      highlightBox must containText("Your LRN is lrn4")
      getDecisionLink(view) must haveHref(controllers.routes.SubmissionsController.displayDeclarationWithNotifications("dec4").url)
    }

    "display header with declaration type Clearance and LRN" in {
      val view = createView(withFlash(DeclarationType.CLEARANCE, "lrn5", "dec5"))
      val highlightBox = getHighlightBox(view)
      highlightBox must containText("Customs clearance request has been submitted")
      highlightBox must containText("Your LRN is lrn5")
      getDecisionLink(view) must haveHref(controllers.routes.SubmissionsController.displayDeclarationWithNotifications("dec5").url)
    }
  }
}
