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

package views.amendments

import base.{Injector, MockAuthAction}
import controllers.timeline.routes.DeclarationDetailsController
import forms.section1.AdditionalDeclarationType._
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission
import org.jsoup.nodes.Document
import org.scalatest.GivenWhenThen
import testdata.SubmissionsTestData._
import views.common.UnitViewSpec
import views.helpers.Confirmation
import views.html.amendments.amendment_cancelled
import views.tags.ViewTest

@ViewTest
class AmendmentCancellededViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[amendment_cancelled]

  private val submissionId = uuid
  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  val emailRequest = buildVerifiedEmailRequest(request, exampleUser)

  private def createView(submission: Submission, declarationType: AdditionalDeclarationType = STANDARD_FRONTIER): Document = {
    val confirmation = Confirmation(emailRequest.email, declarationType.toString, submission, None)
    page(confirmation)(emailRequest, messages)
  }

  "Amendment Cancelled View" when {
    val submission = createSubmission(statuses = Seq(RECEIVED))
    val view = createView(submission)

    "display the expected panel" in {
      val panels = view.getElementsByClass("govuk-panel")
      panels.size mustBe 1

      val children = panels.get(0).children

      And("which should include the expected title")
      children.get(0).tagName mustBe "h1"
      children.get(0).text mustBe messages("declaration.confirmation.amendment.cancelled.title")
    }

    displayExpectedSummaryListWithDucrLrnAndMrn(view)

    "display the expected 'What happens next' section" in {
      view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatHappensNext")

      val paragraph1 = view.getElementsByClass("govuk-body").get(0)
      paragraph1.text mustBe messages("declaration.confirmation.amendment.cancelled.next.1")

      val paragraph2 = view.getElementsByClass("govuk-body").get(1)
      paragraph2.text mustBe messages("declaration.confirmation.amendment.cancelled.next.2", emailRequest.email)
    }

    "display the 'View declaration details' button" in {
      val button = view.getElementsByClass("govuk-button").get(0)
      button.tag.getName mustBe "a"
      button must haveHref(declarationDetailsRoute)
      button.text mustBe messages("declaration.confirmation.amendment.cancelled.button")
    }

    "display the print button" in {
      val button = view.getElementsByClass("ceds-print-link")
      button.size mustBe 1
    }

    "display the expected 'Tell us what you think' section" in {
      view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
    }
  }

  private def displayExpectedSummaryListWithDucrLrnAndMrn(view: Document): Unit =
    "display expected summary list when MRN, LRN and DUCR are defined" in {

      val summaryList = view.getElementsByClass("govuk-summary-list").first()

      val rows = summaryList.getElementsByClass("govuk-summary-list__row")
      rows.size mustBe 4

      rows.get(0)
        .getElementsByClass("govuk-summary-list__key")
        .first() must containMessage("declaration.confirmation.additionalType")

      rows.get(0)
        .getElementsByClass("govuk-summary-list__value")
        .first() must containText(STANDARD_FRONTIER.toString)

      rows.get(1)
        .getElementsByClass("govuk-summary-list__key")
        .first() must containMessage("declaration.confirmation.ducr")

      rows.get(1)
        .getElementsByClass("govuk-summary-list__value")
        .first() must containText(ducr)

      rows.get(2)
        .getElementsByClass("govuk-summary-list__key")
        .first() must containMessage("declaration.confirmation.lrn")

      rows.get(2)
        .getElementsByClass("govuk-summary-list__value")
        .first() must containText(lrn)

      rows.get(3)
        .getElementsByClass("govuk-summary-list__key")
        .first() must containMessage("declaration.confirmation.mrn")

      rows.get(3)
        .getElementsByClass("govuk-summary-list__value")
        .first() must containText(mrn)
    }

}
