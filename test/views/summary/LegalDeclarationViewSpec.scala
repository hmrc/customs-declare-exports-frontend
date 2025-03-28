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

package views.summary

import base.Injector
import controllers.summary.routes.{SubmissionController, SummaryController}
import forms.summary.LegalDeclaration
import forms.summary.LegalDeclaration.{confirmationKey, emailKey, jobRoleKey, nameKey}
import views.html.summary.legal_declaration
import views.common.UnitViewSpec

class LegalDeclarationViewSpec extends UnitViewSpec with Injector {

  private val legalDeclarationPage = instanceOf[legal_declaration]

  private val view = legalDeclarationPage(LegalDeclaration.form)

  "Legal Declaration View" should {

    "have correct header" in {
      view.getElementById("title") must containMessage("declaration.summary.legal-header")
    }

    "go back to normal summary page" in {
      view.getElementById("back-link") must haveHref(SummaryController.displayPage.url)
    }

    "have the expected title" in {
      view.getElementsByTag("h1").text mustBe messages("declaration.summary.legal-header")
    }

    "have legal declaration warning" in {
      view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("site.warning")
      view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("legal.declaration.warning")
    }

    "have full name input field" in {
      view.getElementsByAttributeValue("for", nameKey) must containMessageForElements("legal.declaration.fullName")
      view.getElementById("fullName").attr("autocomplete") mustBe "name"
      messages must haveTranslationFor("legal.declaration.fullName.empty")
      messages must haveTranslationFor("legal.declaration.fullName.short")
      messages must haveTranslationFor("legal.declaration.fullName.long")
      messages must haveTranslationFor("legal.declaration.fullName.error")
    }

    "have job role input field" in {
      view.getElementsByAttributeValue("for", jobRoleKey) must containMessageForElements("legal.declaration.jobRole")
      messages must haveTranslationFor("legal.declaration.jobRole.empty")
      messages must haveTranslationFor("legal.declaration.jobRole.short")
      messages must haveTranslationFor("legal.declaration.jobRole.long")
      messages must haveTranslationFor("legal.declaration.jobRole.error")
    }

    "have email input field" in {
      view.getElementsByAttributeValue("for", emailKey) must containMessageForElements("legal.declaration.email")
      view.getElementById("email").attr("autocomplete") mustBe "email"
      messages must haveTranslationFor("legal.declaration.email.empty")
      messages must haveTranslationFor("legal.declaration.email.long")
      messages must haveTranslationFor("legal.declaration.email.error")
    }

    "have confirmation box" in {
      view.getElementsByAttributeValue("for", confirmationKey) must containMessageForElements("legal.declaration.confirmation")
      messages must haveTranslationFor("legal.declaration.confirmation.missing")
    }

    "have the expected 'Submit' button" in {
      view.getElementById("submit") must containMessage("site.acceptAndSubmitDeclaration")
    }

    "have a form with the expected action for an amendment cancellation" in {
      val action = view.getElementsByTag("form").get(0).attr("action")
      action mustBe SubmissionController.submitDeclaration.url
    }
  }
}
