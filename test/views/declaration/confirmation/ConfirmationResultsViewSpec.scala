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
import views.helpers.ConfirmationResultsHelper.getConfirmationPageMessageKey
import views.html.declaration.confirmation.confirmation_results_page
import views.tags.ViewTest

@ViewTest
class ConfirmationResultsViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[confirmation_results_page]
  private val submissionId = uuid

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  checkMessages(
    "declaration.confirmation.submitted.title",
    "declaration.confirmation.additionalType",
    "declaration.confirmation.ducr",
    "declaration.confirmation.lrn",
    "declaration.confirmation.mrn",
    "declaration.confirmation.whatHappensNext",
    "declaration.confirmation.submitted.whatHappensNext.paragraph",
    "declaration.confirmation.checkDetails.title",
    "declaration.confirmation.submitted.checkDetails.paragraph",
    "declaration.confirmation.submitted.checkDetails.link",
    "declaration.confirmation.checkDetails.link",
    "declaration.confirmation.actionRequired.title",
    "declaration.confirmation.actionRequired.warning",
    "declaration.confirmation.actionRequired.paragraph1",
    "declaration.confirmation.actionRequired.paragraph2",
    "declaration.confirmation.pendingNotification.title",
    "declaration.confirmation.pendingNotification.paragraph1",
    "declaration.confirmation.pendingNotification.paragraph2",
    "declaration.confirmation.prelodged",
    "declaration.confirmation.arrived",
    "declaration.confirmation.simplified",
    "declaration.confirmation.eidr"
  )

  private def createView(
    submission: Submission,
    declarationType: AdditionalDeclarationType = STANDARD_FRONTIER,
    goodsLocationCode: Option[String] = Some("goodsLocationCode")
  ): Document = {
    val req = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(req.email, declarationType.toString, submission, goodsLocationCode)
    page(confirmation)(req, messages)
  }

  private def checkSubmittedPageIsCorrect(status: EnhancedStatus, declarationType: AdditionalDeclarationType): Unit =
    s"status of last received notification is '$status' and dec type is '$declarationType''" should {
      val submission = createSubmission(statuses = Seq(status))
      val view = createView(submission, declarationType)

      "display the expected panel" in {
        val panels = view.getElementsByClass("govuk-panel")
        panels.size mustBe 1

        val children = panels.get(0).children

        And("which should include the expected title")
        children.get(0).tagName mustBe "h1"
        children.get(0).text mustBe messages("declaration.confirmation.submitted.title")
      }

      displayExpectedTable(view, declarationType, submission.mrn)

      "display the expected 'What happens next' section" in {
        view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatHappensNext")

        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages("declaration.confirmation.submitted.whatHappensNext.paragraph")
      }

      "display the expected 'Now check the declaration details page' section" in {
        view.getElementsByTag("h2").get(1) must containMessage("declaration.confirmation.checkDetails.title")

        view.getElementsByClass("govuk-body").get(1) must containMessage("declaration.confirmation.submitted.checkDetails.paragraph")

        And("display link to declaration details")
        val link = view.getElementsByClass("govuk-link").get(3)
        link must containMessage("declaration.confirmation.submitted.checkDetails.link")
        link must haveHref(declarationDetailsRoute)
      }

      displayPrintButton(view)

      "display the expected 'Tell us what you think' section" in {
        view.getElementsByTag("h2").get(2).text mustBe messages("declaration.exitSurvey.header")
      }
    }

  "Confirmation View" when {

    for {
      status <- Seq(RECEIVED, GOODS_ARRIVED_MESSAGE, GOODS_ARRIVED)
      declarationType <- allAdditionalDeclarationTypes
    } checkSubmittedPageIsCorrect(status, declarationType)

    List(STANDARD_FRONTIER, SIMPLIFIED_FRONTIER, OCCASIONAL_FRONTIER, CLEARANCE_FRONTIER).foreach { declarationType =>
      checkSubmittedPageIsCorrect(CLEARED, declarationType)
    }

    Seq(UNDERGOING_PHYSICAL_CHECK, ADDITIONAL_DOCUMENTS_REQUIRED).foreach { enhancedStatus =>
      s"status of last received notification is '${enhancedStatus}'" should {
        val submission = createSubmission(statuses = Seq(enhancedStatus), specifiedMrn = None)
        val view = createView(submission)

        "display the expected panel" in {
          val panels = view.getElementsByClass("govuk-panel")
          panels.size mustBe 1

          panels.attr("style") mustBe "background: #f3f2f1; color: #0b0c0c;"

          val children = panels.get(0).children

          And("which should include the expected title")
          children.get(0).tagName mustBe "h1"
          children.get(0).text mustBe messages("declaration.confirmation.actionRequired.title")
        }

        displayExpectedTable(view, STANDARD_FRONTIER, submission.mrn)

        "display the 'WHat happens next' heading" in {
          view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatHappensNext")
        }

        "display the expected warning paragraph" in {
          val expectedText = s"! ${messages("site.warning")} ${messages("declaration.confirmation.actionRequired.warning")}"
          view.getElementsByClass("govuk-warning-text").get(0).text mustBe expectedText
        }

        "display the expected first and second body paragraphs" in {
          val para1 = view.getElementsByClass("govuk-body").get(0).text
          val para2 = view.getElementsByClass("govuk-body").get(1).text
          para1 mustBe messages("declaration.confirmation.actionRequired.paragraph1")
          para2 mustBe messages("declaration.confirmation.actionRequired.paragraph2")
        }

        "display the expected link" in {
          val link = view.getElementsByClass("govuk-link").get(3)
          link must containMessage("declaration.confirmation.checkDetails.link")
          link must haveHref(declarationDetailsRoute)
        }

        displayPrintButton(view)

        "display the expected 'Tell us what you think' section" in {
          view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
        }
      }
    }

    "no notification has been received yet" should {
      val view = createView(createSubmission(statuses = Seq.empty[EnhancedStatus], specifiedMrn = None))

      "display the expected title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.pendingNotification.title")
      }

      "display the expected panel" in {
        val panels = view.getElementsByClass("govuk-panel")
        panels.size mustBe 1

        panels.attr("style") mustBe "background: #f3f2f1; color: #0b0c0c;"

        val children = panels.get(0).children

        And("which should include the expected title")
        children.get(0).tagName mustBe "h1"
        children.get(0).text mustBe messages("declaration.confirmation.pendingNotification.title")
      }

      displayExpectedTable(view, STANDARD_FRONTIER, None)

      "display the 'WHat happens next' heading" in {
        view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.whatHappensNext")
      }

      "display the expected first and second body paragraphs" in {
        val para1 = view.getElementsByClass("govuk-body").get(0).text
        val para2 = view.getElementsByClass("govuk-body").get(1).text
        para1 mustBe messages("declaration.confirmation.pendingNotification.paragraph1")
        para2 mustBe messages("declaration.confirmation.pendingNotification.paragraph2")
      }

      "display the expected link" in {
        val link = view.getElementsByClass("govuk-link").get(3)
        link must containMessage("declaration.confirmation.checkDetails.link")
        link must haveHref(declarationDetailsRoute)
      }

      displayPrintButton(view)

      "display the expected 'Tell us what you think' section" in {
        view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
      }
    }
  }

  private def displayExpectedTable(view: Document, declarationType: AdditionalDeclarationType, maybeMrn: Option[String]): Unit =
    "display expected table when dec type, MRN, LRN and DUCR are defined" in {
      val table = view.getElementsByClass("govuk-table").first()

      val rows = table.getElementsByClass("govuk-table__row")
      if (maybeMrn.isDefined) rows.size mustBe 4
      else rows.size mustBe 3

      rows.get(0).children().get(0) must containMessage("declaration.confirmation.additionalType")
      rows.get(0).children().get(1) must containMessage(s"${getConfirmationPageMessageKey(declarationType.toString)}")

      rows.get(1).children().get(0) must containMessage("declaration.confirmation.ducr")
      rows.get(1).children().get(1) must containText(ducr)

      rows.get(2).children().get(0) must containMessage("declaration.confirmation.lrn")
      rows.get(2).children().get(1) must containText(lrn)

      if (maybeMrn.isDefined) {
        rows.get(3).children().get(0) must containMessage("declaration.confirmation.mrn")
        rows.get(3).children().get(1) must containText(mrn)
      }
    }

  private def displayPrintButton(view: Document): Unit =
    "display print button" in {
      val button = view.getElementsByClass("ceds-print-link")
      button.size mustBe 1
    }
}
