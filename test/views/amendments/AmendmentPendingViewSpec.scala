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
import views.html.amendments.amendment_pending
import views.tags.ViewTest

@ViewTest
class AmendmentPendingViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[amendment_pending]
  private val submissionId = uuid

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  private def createView(submission: Submission, declarationType: AdditionalDeclarationType = STANDARD_FRONTIER): Document = {
    val verifiedEmailRequest = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(verifiedEmailRequest.email, declarationType.toString, submission, None)
    page(confirmation)(verifiedEmailRequest, messages)
  }

  "Amendment PENDING View" when {
    val submission = createSubmission(statuses = Seq(PENDING))
    val view = createView(submission)

    "display the expected header" in {
      view.getElementsByTag("h1").get(0) must containMessage("declaration.confirmation.pending.amendment.title")
    }

    displayExpectedTableWithDucrLrnAndMrn(view)

    "display the expected paragraph and link" in {
      val paragraph = view.getElementsByClass("govuk-body").get(0)
      paragraph must containMessage(
        "declaration.confirmation.pending.amendment.paragraph",
        messages("declaration.confirmation.pending.amendment.paragraph.link")
      )
      paragraph.child(0) must haveHref(declarationDetailsRoute)

    }

    displayPrintButton(view)

    "display the expected 'Tell us what you think' section" in {
      view.getElementsByTag("h2").get(0).text mustBe messages("declaration.exitSurvey.header")
    }
  }

  private def displayExpectedTableWithDucrLrnAndMrn(view: Document): Unit =
    "display expected table when MRN, LRN and DUCR are defined" in {
      val table = view.getElementsByClass("govuk-table").first()

      val rows = table.getElementsByClass("govuk-table__row")
      rows.size mustBe 4

      rows.get(0).children().get(0) must containMessage("declaration.confirmation.additionalType")
      rows.get(0).children().get(1) must containText(STANDARD_FRONTIER.toString)

      rows.get(1).children().get(0) must containMessage("declaration.confirmation.ducr")
      rows.get(1).children().get(1) must containText(ducr)

      rows.get(2).children().get(0) must containMessage("declaration.confirmation.lrn")
      rows.get(2).children().get(1) must containText(lrn)

      rows.get(3).children().get(0) must containMessage("declaration.confirmation.mrn")
      rows.get(3).children().get(1) must containText(mrn)
    }

  private def displayPrintButton(view: Document): Unit =
    "display print button" in {
      val button = view.getElementsByClass("ceds-print-link")
      button.size mustBe 1
    }
}
