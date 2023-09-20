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

package views.declaration.amendments

import base.Injector
import controllers.declaration.routes.{SubmissionController, SummaryController}
import controllers.routes.DeclarationDetailsController
import forms.declaration.AmendmentSubmission.{confirmationKey, emailKey, form, jobRoleKey, nameKey, reasonKey}
import play.twirl.api.HtmlFormat.Appendable
import services.view.AmendmentAction.{AmendmentAction, Cancellation, Resubmission, Submission}
import views.declaration.spec.UnitViewSpec
import views.html.declaration.amendments.amendment_submission

class AmendmentSubmissionViewSpec extends UnitViewSpec with Injector {

  private val amendmentSubmissionPage = instanceOf[amendment_submission]

  private def view(amendmentAction: AmendmentAction = Submission): Appendable =
    amendmentSubmissionPage(form(amendmentAction == Cancellation), amendmentAction)

  "Amendment Submission View" should {

    "go back to declaration details page for an amendment cancellation" in {
      view(Cancellation).getElementById("back-link") must haveHref(DeclarationDetailsController.displayPage("").url)
    }

    "go back to declaration details page for an amendment resubmission" in {
      view(Resubmission).getElementById("back-link") must haveHref(DeclarationDetailsController.displayPage("").url)
    }

    "go back to normal summary page for an amendment submission" in {
      view().getElementById("back-link") must haveHref(SummaryController.displayPage.url)
    }

    "have the expected title for an amendment cancellation" in {
      view(Cancellation).getElementsByTag("h1").text mustBe messages("amendment.cancellation.heading")
    }

    "have the expected title for an amendment resubmission" in {
      view(Resubmission).getElementsByTag("h1").text mustBe messages("amendment.resubmission.heading")
    }

    "have the expected title for an amendment submission" in {
      view().getElementsByTag("h1").text mustBe messages("amendment.submission.heading")
    }

    "have legal declaration warning" in {
      view().getElementsByClass("govuk-warning-text__text") must containMessageForElements("site.warning")
      view().getElementsByClass("govuk-warning-text__text") must containMessageForElements("legal.declaration.warning")
    }

    "have full name input field" in {
      view().getElementsByAttributeValue("for", nameKey) must containMessageForElements("amendment.submission.fullName")
      messages must haveTranslationFor("amendment.submission.fullName.empty")
      messages must haveTranslationFor("amendment.submission.fullName.short")
      messages must haveTranslationFor("amendment.submission.fullName.long")
      messages must haveTranslationFor("amendment.submission.fullName.error")
    }

    "have job role input field" in {
      view().getElementsByAttributeValue("for", jobRoleKey) must containMessageForElements("amendment.submission.jobRole")
      messages must haveTranslationFor("amendment.submission.jobRole.empty")
      messages must haveTranslationFor("amendment.submission.jobRole.short")
      messages must haveTranslationFor("amendment.submission.jobRole.long")
      messages must haveTranslationFor("amendment.submission.jobRole.error")
    }

    "have email input field" in {
      view().getElementsByAttributeValue("for", emailKey) must containMessageForElements("amendment.submission.email")
      messages must haveTranslationFor("amendment.submission.email.empty")
      messages must haveTranslationFor("amendment.submission.email.long")
      messages must haveTranslationFor("amendment.submission.email.error")
    }

    "have reason text-box field for an amendment cancellation" in {
      view(Cancellation).getElementsByAttributeValue("for", reasonKey) must containMessageForElements("amendment.cancellation.reason")
    }

    "have reason text-box field for an amendment submission" in {
      view().getElementsByAttributeValue("for", reasonKey) must containMessageForElements("amendment.submission.reason")
    }

    "have confirmation box" in {
      view().getElementsByAttributeValue("for", confirmationKey) must containMessageForElements("amendment.submission.confirmation")
      messages must haveTranslationFor("amendment.submission.confirmation.missing")
    }

    "have the expected 'Submit' button for an amendment cancellation" in {
      view(Cancellation).getElementById("submit") must containMessage("site.submit")
    }

    "have the expected 'Submit' button for an amendment resubmission" in {
      view(Resubmission).getElementById("submit") must containMessage("amendment.resubmission.button")
    }

    "have the expected 'Submit' button for an amendment submission" in {
      view().getElementById("submit") must containMessage("amendment.submission.button")
    }

    "have a form with the expected action for an amendment cancellation" in {
      val action = view(Cancellation).getElementsByTag("form").get(0).attr("action")
      action mustBe SubmissionController.submitAmendment("Cancellation").url
    }

    "have a form with the expected action for an amendment resubmission" in {
      val action = view(Resubmission).getElementsByTag("form").get(0).attr("action")
      action mustBe SubmissionController.submitAmendment("Resubmission").url
    }

    "have a form with the expected action for an amendment submission" in {
      val action = view().getElementsByTag("form").get(0).attr("action")
      action mustBe SubmissionController.submitAmendment("Submission").url
    }
  }
}
