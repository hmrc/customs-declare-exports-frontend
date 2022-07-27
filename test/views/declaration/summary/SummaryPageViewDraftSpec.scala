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

import models.ExportsDeclaration
import org.jsoup.nodes.Document
import play.api.mvc.Call
import views.html.declaration.summary.draft_summary_page
import views.html.declaration.summary.sections._

class SummaryPageViewDraftSpec extends SummaryPageViewSpec {

  val backLink = Call("GET", "/backLink")

  val draftInfoPage = instanceOf[draft_info_section]

  val draft_summaryPage = instanceOf[draft_summary_page]
  def view(declaration: ExportsDeclaration = aDeclaration()): Document =
    draft_summaryPage(backLink)(journeyRequest(declaration), messages, minimalAppConfig)

  "Summary page" should {

    val document = view(aDeclaration())

    behave like commonBehaviour(document)

    behave like sectionsVisibility(view)

    behave like displayWarning(document)

    "should display correct title" in {
      document.getElementById("title").text() mustBe messages("declaration.summary.saved-header")
    }

    "should display correct back link" in {
      val backButton = document.getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(backLink.url)
    }

    "should display warning text at top of page" in {

      document.getElementsByClass("govuk-warning-text").text() must include(messages("declaration.summary.warning"))
    }

  }
}
