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

package views.declaration.summary

import controllers.declaration.routes.{SubmissionController, SummaryController}
import controllers.timeline.routes.RejectedNotificationsController
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.{COMPLETE, DRAFT, INITIAL}
import models.declaration.submissions.EnhancedStatus.ERRORS
import play.twirl.api.HtmlFormat.Appendable
import views.html.declaration.summary.normal_summary_page

class NormalSummaryViewSpec extends SummaryViewSpec {

  private val normal_summaryPage = instanceOf[normal_summary_page]

  def createView(declaration: ExportsDeclaration = aDeclaration()): Appendable =
    normal_summaryPage(backLink)(journeyRequest(declaration), messages, minimalAppConfig)

  def createViewWithError(declaration: ExportsDeclaration = aDeclaration()): Appendable =
    normal_summaryPage(backLink, dummyFormError)(journeyRequest(declaration), messages, minimalAppConfig)

  private val declarationInDraft = aDeclarationAfter(aDeclaration().updateReadyForSubmission(false))
  private val declarationReadyForSubmission = aDeclarationAfter(aDeclaration().updateReadyForSubmission(true))
  private val declarationWithErrors = aDeclaration(withParentDeclarationEnhancedStatus(ERRORS))

  private val allDeclarationStates = List(declarationInDraft, declarationReadyForSubmission, declarationWithErrors)

  "Summary page" when {

    behave like sectionsVisibility(createView)
    behave like displayErrorSummary(createViewWithError())

    allDeclarationStates.foreach { declaration =>
      val view = createView(declaration)

      (declaration.declarationMeta.readyForSubmission, declaration.declarationMeta.parentDeclarationEnhancedStatus) match {
        case (_, Some(ERRORS)) => behave like commonBehaviour("errors", view)
        case (Some(true), _)   => behave like commonBehaviour("ready", view)
        case _                 => behave like commonBehaviour("draft", view)
      }
    }
  }

  "Summary page" should {

    "not display a 'View Declaration Errors' button" when {

      List(DRAFT, INITIAL, COMPLETE).foreach { status =>
        s"the declaration is in '$status' status and" when {
          "declaration's 'parenDeclarationId' is NOT defined" in {
            val view = createView(aDeclaration(withStatus(status)))
            val buttons = view.getElementsByClass("govuk-button--secondary")
            buttons.size mustBe 0
          }
        }
      }

      List(INITIAL, COMPLETE).foreach { status =>
        s"the declaration is in '$status' status and" when {
          "declaration's 'parenDeclarationId' is defined" in {
            val parentId = "parentId"
            val view = createView(aDeclaration(withStatus(status), withParentDeclarationId(parentId)))
            val buttons = view.getElementsByClass("govuk-button--secondary")
            buttons.size mustBe 0
          }
        }
      }
    }

    "display a 'View Declaration Errors' button" when {
      "the declaration has errors and" when {
        "declaration's 'parentDeclarationId' is defined" in {
          val parentId = "parentId"
          val view = createView(aDeclaration(withParentDeclarationEnhancedStatus(ERRORS), withParentDeclarationId(parentId)))
          val buttons = view.getElementsByClass("govuk-button--secondary")
          buttons.size mustBe 1

          val button = buttons.get(0)
          button.tagName mustBe "a"
          button.text mustBe messages("site.view.declaration.errors")
          button must haveHref(RejectedNotificationsController.displayPage(parentId))
        }
      }
    }

    "display a 'Confirm and continue' button pointing to /submit-your-declaration" when {
      "the declaration has at least one item and no errors" in {
        val item = anItem(withAdditionalInformation("code", "description"))
        val declaration = aDeclaration(withStatus(DRAFT), withReadyForSubmission(), withConsignmentReferences(), withItems(item))
        val view = createView(declaration)

        val button = view.getElementsByClass("govuk-button").get(0)
        button.tagName mustBe "a"
        button.text mustBe messages("site.confirm_and_continue")
        button must haveHref(SubmissionController.displaySubmitDeclarationPage)
      }
    }

    "display a 'Confirm and continue' button pointing to /saved-summary-no-items" when {
      "the declaration has at least one item and no errors" in {
        val view = createView(aDeclaration(withStatus(DRAFT), withReadyForSubmission(), withConsignmentReferences()))

        val button = view.getElementsByClass("govuk-button").get(0)
        button.tagName mustBe "a"
        button.text mustBe messages("site.confirm_and_continue")
        button must haveHref(SummaryController.displayPageOnNoItems)
      }
    }

    "display a 'Confirm and continue' button pointing to /saved-summary" when {
      "the declaration has errors" in {
        val item = anItem(withAdditionalInformation("code", "description"))
        val declaration = aDeclaration(withStatus(DRAFT), withReadyForSubmission(), withConsignmentReferences(), withItems(item))
        val view = createViewWithError(declaration)

        view.getElementsByClass("govuk-error-summary").size must not be 0

        val button = view.getElementsByClass("govuk-button").get(0)
        button.tagName mustBe "a"
        button.text mustBe messages("site.confirm_and_continue")
        button must haveHref(SummaryController.displayPage)
      }
    }
  }
}
