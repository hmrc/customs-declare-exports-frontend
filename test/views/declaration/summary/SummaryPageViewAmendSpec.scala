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

import controllers.routes.{RejectedNotificationsController, SubmissionsController}
import models.DeclarationStatus._
import models.ExportsDeclaration
import org.jsoup.nodes.Document
import views.html.declaration.summary._
import views.html.declaration.summary.sections._

class SummaryPageViewAmendSpec extends SummaryPageViewSpec {

  val draftInfoPage = instanceOf[draft_info_section]

  val amend_summaryPage = instanceOf[amend_summary_page]

  def view(declaration: ExportsDeclaration = aDeclaration()): Document =
    amend_summaryPage()(journeyRequest(declaration), messages, minimalAppConfig)

  def viewWithError(declaration: ExportsDeclaration = aDeclaration()): Document =
    amend_summaryPage(dummyFormError)(journeyRequest(declaration), messages, minimalAppConfig)

  "Summary page" should {

    val document = view(aDeclaration())
    val documentWithError = viewWithError(aDeclaration())

    behave like displayErrorSummary(documentWithError)

    behave like commonBehaviour(document)

    behave like sectionsVisibility(view)

    behave like displayWarning(document)

    "should display correct title" in {
      document.getElementById("title").text mustBe messages("declaration.summary.amend-header")
    }

    "should display correct back link" in {
      val backButton = document.getElementById("back-link")

      backButton.text mustBe messages("site.back")
      backButton must haveHref(SubmissionsController.displayListOfSubmissions())
    }

    "not display a 'View Declaration Errors' button" when {
      List(DRAFT, INITIAL, COMPLETE).foreach { status =>
        s"the declaration is in '$status' status and" when {
          "declaration's 'parenDeclarationId' is NOT defined" in {
            val document = view(aDeclaration(withStatus(DRAFT)))
            val buttons = document.getElementsByClass("govuk-button--secondary")
            buttons.size mustBe 0
          }
        }
      }
    }

    "display a 'View Declaration Errors' button" when {
      "the declaration is in 'DRAFT' status and" when {
        "declaration's 'parenDeclarationId' is defined" in {
          val parentId = "parentId"
          val document = view(aDeclaration(withStatus(DRAFT), withParentDeclarationId(parentId)))
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
