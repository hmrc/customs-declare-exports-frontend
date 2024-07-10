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

import base.ExportsTestData.{eori, newUser}
import controllers.routes.DraftDeclarationController
import controllers.timeline.routes.DeclarationDetailsController
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import models.requests.JourneyRequest
import play.api.http.HeaderNames
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable
import utils.FakeRequestCSRFSupport.CSRFFakeRequest
import views.html.declaration.amendments.amendment_summary

class AmendmentSummaryViewSpec extends SummaryViewSpec {

  private val amendmentSummaryPage = instanceOf[amendment_summary]

  private val submissionId = "submissionId"
  private val declaration = aDeclaration(withStatus(AMENDMENT_DRAFT), withConsignmentReferences("ducr", "lrn"))

  def createView(declaration: ExportsDeclaration = declaration): Appendable =
    amendmentSummaryPage(submissionId)(journeyRequest(declaration), messages, minimalAppConfig)

  "AmendmentSummary page" should {
    val view = createView()

    "have references section" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.heading")
    }

    "should display correct title" in {
      view.getElementById("title").text() mustBe messages("declaration.summary.amendment-draft-header")
    }

    "should display correct back link" when {
      "referer is the /saved-declarations page" in {
        val user = newUser(eori, "12345")
        val fakeReferer = Map(HeaderNames.REFERER -> ".../saved-declarations")
        val emailRequest = buildVerifiedEmailRequest(FakeRequest("", "").withHeaders(fakeReferer.toSeq: _*).withCSRFToken, user)
        val journeyWithHeader = new JourneyRequest(emailRequest, declaration)
        val viewWithHeaders = amendmentSummaryPage(submissionId)(journeyWithHeader, messages, minimalAppConfig)
        val backButton = viewWithHeaders.getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(DraftDeclarationController.displayDeclarations().url)
      }

      "referer is otherwise i.e. /information page or does not exist" in {
        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(DeclarationDetailsController.displayPage(submissionId).url)
      }
    }

    "warning text should be displayed" in {
      val warningText = s"! ${messages("site.warning")} ${messages("declaration.summary.warning")}"
      view.getElementsByClass("govuk-warning-text").text mustBe warningText
    }

    "not allow fields to be changed" when {
      "lrn" in {
        view
          .getElementsByClass("lrn")
          .first()
          .getElementsByClass(summaryActionsClassName)
          .text() mustBe empty
      }
      "ducr" in {
        view
          .getElementsByClass("ducr")
          .first()
          .getElementsByClass(summaryActionsClassName)
          .text() mustBe empty
      }
    }

    behave like sectionsVisibility(createView)
  }
}
