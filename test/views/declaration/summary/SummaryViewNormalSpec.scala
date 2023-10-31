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

import controllers.routes.RejectedNotificationsController
import models.declaration.DeclarationStatus.{COMPLETE, DRAFT, INITIAL}
import models.ExportsDeclaration
import models.declaration.submissions.EnhancedStatus.ERRORS
import play.twirl.api.HtmlFormat.Appendable
import play.api.mvc.Call
import views.html.declaration.summary.normal_summary_page

class SummaryViewNormalSpec extends SummaryViewSpec {

  private val backLink = Call("GET", "/backLink")
  private val normal_summaryPage = instanceOf[normal_summary_page]

  def createView(declaration: ExportsDeclaration = aDeclaration()): Appendable =
    normal_summaryPage(backLink)(journeyRequest(declaration), messages, minimalAppConfig)

  def viewWithError(declaration: ExportsDeclaration = aDeclaration()): Appendable =
    normal_summaryPage(backLink, dummyFormError)(journeyRequest(declaration), messages, minimalAppConfig)

  private val declarationInDraft = aDeclarationAfter(aDeclaration().updateReadyForSubmission(false))
  private val declarationReadyForSubmission = aDeclarationAfter(aDeclaration().updateReadyForSubmission(true))
  private val declarationWithErrors = aDeclaration(withParentDeclarationEnhancedStatus(ERRORS))
  private val documentWithFormError = viewWithError(aDeclaration())

  private val allDeclarationStates = List(declarationInDraft, declarationReadyForSubmission, declarationWithErrors)

  "Summary page" when {

    behave like sectionsVisibility(createView)
    behave like displayErrorSummary(documentWithFormError)

    allDeclarationStates.foreach { declaration =>
      val view = createView(declaration)

      (declaration.declarationMeta.readyForSubmission, declaration.declarationMeta.parentDeclarationEnhancedStatus) match {
        case (_, Some(ERRORS)) => behave like commonBehaviour("errors", view)
        case (Some(true), _)   => behave like commonBehaviour("ready", view)
        case _                 => behave like commonBehaviour("draft", view)
      }
    }

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
  }
}
