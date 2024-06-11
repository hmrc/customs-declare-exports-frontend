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

package views.declaration.confirmation

import base.{Injector, MockAuthAction}
import controllers.routes.DeclarationDetailsController
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission
import org.jsoup.nodes.Document
import org.scalatest.GivenWhenThen
import testdata.SubmissionsTestData._
import views.declaration.spec.UnitViewSpec
import views.helpers.Confirmation
import views.html.declaration.amendments.amendment_accepted
import views.tags.ViewTest

@ViewTest
class AmendmentAcceptedViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[amendment_accepted]
  private val submissionId = uuid

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  private def createView(submission: Submission, declarationType: AdditionalDeclarationType = STANDARD_FRONTIER): Document = {
    val req = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(req.email, declarationType.toString, submission, None)
    page(confirmation)(req, messages)
  }

  "Amendment Accepted View" when {
    val submission = createSubmission(statuses = Seq(RECEIVED))
    val view = createView(submission)

    "display the expected panel" in {
      val panels = view.getElementsByClass("govuk-panel")
      panels.size mustBe 1

      val children = panels.get(0).children

      And("which should include the expected title")
      children.get(0).tagName mustBe "h1"
      children.get(0).text mustBe messages("declaration.confirmation.accepted.amendment.title")
    }

    displayExpectedTableWithDucrLrnAndMrn(view)

    "display the expected 'What happens next' section" in {
      view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatHappensNext")

      val paragraph1 = view.getElementsByClass("govuk-body").get(0)
      paragraph1.text mustBe messages("declaration.confirmation.accepted.amendment.next.1")

      val paragraph2 = view.getElementsByClass("govuk-body").get(1)
      paragraph2.text must include("example@example.com")
      paragraph2.text must include(messages("declaration.confirmation.declaration.details.link"))
      paragraph2.child(1) must haveHref(declarationDetailsRoute)
    }

    displayPrintButton(view)

    "display the expected 'Tell us what you think' section" in {
      view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
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
