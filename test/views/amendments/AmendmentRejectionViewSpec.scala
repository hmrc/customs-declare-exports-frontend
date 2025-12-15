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
import views.html.amendments.amendment_rejection
import views.tags.ViewTest

@ViewTest
class AmendmentRejectionViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[amendment_rejection]
  private val submissionId = uuid

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  private def createView(submission: Submission, declarationType: AdditionalDeclarationType = STANDARD_FRONTIER): Document = {
    val req = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(req.email, declarationType.toString, submission, None)
    page(confirmation)(req, messages)
  }

  "Confirmation View" when {

    "status of last received notification is 'RECEIVED'" should {
      val submission = createSubmission(statuses = Seq(RECEIVED))
      val view = createView(submission)

      "display the h1" in {

        val h1 = view.getElementsByTag("h1").first()

        h1.tagName mustBe "h1"
        h1.text mustBe messages("declaration.confirmation.rejected.amendment.title")
      }

      displayExpectedSummaryListWithDucrLrnAndMrn(view)

      "display the expected 'What happens next' section" in {
        view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatHappensNext")

        val paragraph1 = view.getElementsByClass("govuk-body").get(0)
        val paragraph2 = view.getElementsByClass("govuk-body").get(1)

        paragraph1.text mustBe messages("declaration.confirmation.rejected.amendment.next.1")
        paragraph2.text mustBe messages("declaration.confirmation.rejected.amendment.next.2")

        val paragraph3 = view.getElementsByClass("govuk-body").get(2)
        paragraph3.text must include(messages("declaration.confirmation.declaration.details.link"))
        paragraph3.child(0) must haveHref(declarationDetailsRoute)

      }

      displayPrintButton(view)

      "display the expected 'Tell us what you think' section" in {
        view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
      }
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


  private def displayPrintButton(view: Document): Unit =
    "display print button" in {
      val button = view.getElementsByClass("ceds-print-link")
      button.size mustBe 1
    }
}
