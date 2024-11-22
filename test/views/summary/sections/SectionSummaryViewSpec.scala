/*
 * Copyright 2024 HM Revenue & Customs
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

package views.summary.sections

import controllers.routes.SavedDeclarationsController
import models.requests.{JourneyRequest, SessionHelper}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.Call
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import views.summary.SummaryViewSpec
import views.helpers.summary.SummaryCard
import views.html.summary.sections.section_summary

class SectionSummaryViewSpec extends SummaryViewSpec {

  private val sectionSummaryPage = instanceOf[section_summary]
  private val summaryCard = mock[SummaryCard]

  when(summaryCard.continueTo(any())).thenReturn(Call("GET", "/go"))

  when(summaryCard.backLink(any())).thenReturn(Call("GET", "/back"))

  when(summaryCard.content(any(), any())(any())).thenReturn(Html("content"))

  private def createView(implicit request: JourneyRequest[_]): Appendable = sectionSummaryPage(summaryCard)

  "SectionSummary page" should {
    val declaration = aDeclaration(withSummaryWasVisited())
    implicit val request = journeyRequest(declaration, (SessionHelper.declarationUuid, declaration.id))
    val view = createView

    "render view declaration summary link" in {
      val link = view.getElementById("view_declaration_summary")
      link must haveHref(SavedDeclarationsController.displayDeclaration(declaration.id).url)
    }

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.summary.normal-header")
    }

    "should display correct back link" in {
      val backButton = view.getElementById("back-link")
      backButton.text() mustBe messages("site.backToPreviousQuestion")
      backButton must haveHref("/back")
    }

    "should display content" in {
      view.getElementsByTag("main").first() must containText("content")
    }

    "display 'Continue' button on page" in {
      val continueButton = view.getElementsByClass("govuk-button").first
      continueButton must containMessage(continueCaption)
      continueButton must haveHref("/go")
    }

    checkExitAndReturnLinkIsDisplayed(view)
  }
}
