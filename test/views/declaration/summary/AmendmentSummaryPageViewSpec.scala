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

import models.ExportsDeclaration
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import org.jsoup.nodes.Document
import views.html.declaration.amendments.amendment_summary

class AmendmentSummaryPageViewSpec extends SummaryPageViewSpec {

  private val amendmentSummaryPage = instanceOf[amendment_summary]

  private val dec = aDeclaration(withStatus(AMENDMENT_DRAFT), withConsignmentReferences("ducr", "lrn"))

  def view(declaration: ExportsDeclaration = dec): Document =
    amendmentSummaryPage("submissionUuid")(journeyRequest(declaration), messages, minimalAppConfig)

  "Summary page" should {

    val document = view()

    "have references section" in {
      document.getElementById("declaration-references-summary").text mustNot be(empty)
    }

    "should display correct title" in {
      document.getElementById("title").text() mustBe messages("declaration.summary.amendment-draft-header")
    }

    "should display correct back link" in {
      val backButton = document.getElementById("back-link")

      backButton.text() mustBe messages("site.backToDeclarations")
      backButton must haveHref(controllers.routes.DeclarationDetailsController.displayPage("submissionUuid").url)
    }

    "warning text should be displayed" in {
      val warningText = s"! ${messages("site.warning")} ${messages("declaration.summary.warning")}"
      document.getElementsByClass("govuk-warning-text").text mustBe warningText
    }

    "not allow fields to be changed" when {
      "lrn" in {
        document
          .getElementsByClass("lrn-row")
          .first()
          .getElementsByClass("govuk-summary-list__actions")
          .text() mustBe empty
      }
      "ducr" in {
        document
          .getElementsByClass("ducr-row")
          .first()
          .getElementsByClass("govuk-summary-list__actions")
          .text() mustBe empty
      }
    }

    behave like sectionsVisibility(view)
  }
}
