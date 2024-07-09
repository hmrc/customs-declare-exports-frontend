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
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType._
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission
import org.jsoup.nodes.Document
import org.scalatest.GivenWhenThen
import testdata.SubmissionsTestData._
import views.helpers.Confirmation
import views.html.declaration.amendments.amendment_failed
import views.common.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class AmendmentFailedViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[amendment_failed]
  private val submissionId = uuid

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  private def createView(submission: Submission, declarationType: AdditionalDeclarationType = STANDARD_FRONTIER): Document = {
    val req = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(req.email, declarationType.toString, submission, None)
    page(confirmation)(req, messages)
  }

  "Amendment Failed View" should {
    val submission = createSubmission(statuses = Seq(RECEIVED))
    val view = createView(submission)

    "display the expected heading" in {
      view.getElementsByTag("h1").get(0).text() mustBe messages("declaration.confirmation.failed.amendment.title")
    }

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

    "display the expected 'What you can do now' section" in {
      view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatYouCanDoNow.heading")

      val paragraphs = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe messages("declaration.confirmation.failed.amendment.next.1")
      paragraphs.get(1).text mustBe messages("declaration.confirmation.failed.amendment.next.2")
      paragraphs.get(2).text mustBe messages("declaration.confirmation.failed.amendment.next.3")

      val textWithLink = messages("declaration.confirmation.failed.amendment.next.4", messages("declaration.confirmation.declaration.details.link"))
      val paragraph4 = paragraphs.get(3)
      paragraph4.text mustBe textWithLink
      paragraph4.child(0) must haveHref(declarationDetailsRoute)

    }

    "display print button" in {
      val button = view.getElementsByClass("ceds-print-link")
      button.size mustBe 1
    }

    "display the expected 'Tell us what you think' section" in {
      view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
    }
  }
}
