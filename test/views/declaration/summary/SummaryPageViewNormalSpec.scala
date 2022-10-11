/*
 * Copyright 2022 HM Revenue & Customs
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
import models.DeclarationStatus.{COMPLETE, DRAFT, INITIAL}
import models.ExportsDeclaration
import models.declaration.submissions.EnhancedStatus.ERRORS
import org.jsoup.nodes.Document
import play.api.mvc.Call
import views.html.declaration.summary.normal_summary_page

class SummaryPageViewNormalSpec extends SummaryPageViewSpec {

  private val backLink = Call("GET", "/backLink")
  private val normal_summaryPage = instanceOf[normal_summary_page]

  def view(declaration: ExportsDeclaration = aDeclaration()): Document =
    normal_summaryPage(backLink)(journeyRequest(declaration), messages, minimalAppConfig)

  def viewWithError(declaration: ExportsDeclaration = aDeclaration()): Document =
    normal_summaryPage(backLink, dummyFormError)(journeyRequest(declaration), messages, minimalAppConfig)

  private val declarationInDraft = aDeclarationAfter(aDeclaration().updateReadyForSubmission(false))
  private val declarationReadyForSubmission = aDeclarationAfter(aDeclaration().updateReadyForSubmission(true))
  private val declarationWithErrors = aDeclaration(withParentDeclarationEnhancedStatus(ERRORS))
  private val documentWithFormError = viewWithError(aDeclaration())

  private val allDeclarationStates = List(declarationInDraft, declarationReadyForSubmission, declarationWithErrors)

  "Summary page" when {

    behave like sectionsVisibility(view)
    behave like displayErrorSummary(documentWithFormError)

    allDeclarationStates.foreach { declaration =>
      val document = view(declaration)

      (declaration.readyForSubmission, declaration.parentDeclarationEnhancedStatus) match {
        case (_, Some(ERRORS)) => behave like commonBehaviour("errors", document)
        case (Some(true), _)   => behave like commonBehaviour("ready", document)
        case _                 => behave like commonBehaviour("draft", document)
      }
    }

    "not display a 'View Declaration Errors' button" when {

      List(DRAFT, INITIAL, COMPLETE).foreach { status =>
        s"the declaration is in '$status' status and" when {
          "declaration's 'parenDeclarationId' is NOT defined" in {
            val document = view(aDeclaration(withStatus(status)))
            val buttons = document.getElementsByClass("govuk-button--secondary")
            buttons.size mustBe 0
          }
        }
      }

      List(INITIAL, COMPLETE).foreach { status =>
        s"the declaration is in '$status' status and" when {
          "declaration's 'parenDeclarationId' is defined" in {
            val parentId = "parentId"
            val document = view(aDeclaration(withStatus(status), withParentDeclarationId(parentId)))
            val buttons = document.getElementsByClass("govuk-button--secondary")
            buttons.size mustBe 0
          }
        }
      }
    }

    "display a 'View Declaration Errors' button" when {
      "the declaration is in 'DRAFT' status and" when {
        "declaration's 'parentDeclarationId' is defined" in {
          val parentId = "parentId"
          val document = view(aDeclaration(withParentDeclarationEnhancedStatus(ERRORS), withParentDeclarationId(parentId)))
          val buttons = document.getElementsByClass("govuk-button--secondary")
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
