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

import base.Injector
import controllers.declaration.routes
import forms.declaration.LegalDeclaration
import forms.declaration.LegalDeclaration.{amendReasonKey, confirmationKey, emailKey, jobRoleKey, nameKey}
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.legal_declaration_page

class LegalDeclarationPageViewSpec extends UnitViewSpec with Injector {

  private val emptyForm = LegalDeclaration.form
  private val legalDeclarationPage = instanceOf[legal_declaration_page]
  private val view = legalDeclarationPage(emptyForm, amend = false)
  private val amendView = legalDeclarationPage(emptyForm, amend = true)

  "Legal Declaration View" when {

    "for both amendments and submissions" should {

      "have legal declaration warning" in {
        view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("site.warning")
        view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("legal.declaration.warning")
        messages must haveTranslationFor("site.warning")
        messages must haveTranslationFor("legal.declaration.warning")
      }

      "have full name input" in {
        view.getElementsByAttributeValue("for", nameKey) must containMessageForElements("legal.declaration.fullName")
        messages must haveTranslationFor("legal.declaration.fullName")
        messages must haveTranslationFor("legal.declaration.fullName.empty")
        messages must haveTranslationFor("legal.declaration.fullName.short")
        messages must haveTranslationFor("legal.declaration.fullName.long")
        messages must haveTranslationFor("legal.declaration.fullName.error")
      }

      "have job role input" in {
        view.getElementsByAttributeValue("for", jobRoleKey) must containMessageForElements("legal.declaration.jobRole")
        messages must haveTranslationFor("legal.declaration.jobRole")
        messages must haveTranslationFor("legal.declaration.jobRole.empty")
        messages must haveTranslationFor("legal.declaration.jobRole.short")
        messages must haveTranslationFor("legal.declaration.jobRole.long")
        messages must haveTranslationFor("legal.declaration.jobRole.error")
      }

      "have email input" in {
        view.getElementsByAttributeValue("for", emailKey) must containMessageForElements("legal.declaration.email")
        messages must haveTranslationFor("legal.declaration.email")
        messages must haveTranslationFor("legal.declaration.email.empty")
        messages must haveTranslationFor("legal.declaration.email.long")
        messages must haveTranslationFor("legal.declaration.email.error")
      }

      "have confirmation box" in {
        view.getElementsByAttributeValue("for", confirmationKey) must containMessageForElements("legal.declaration.confirmation")
        messages must haveTranslationFor("legal.declaration.confirmation")
        messages must haveTranslationFor("legal.declaration.confirmation.missing")
      }
    }

    "for submissions only" should {
      "go back to normal summary page" in {
        view.getElementById("back-link") must haveHref(routes.SummaryController.displayPage.url)
      }
      "have correct header" in {
        view.getElementById("title") must containMessage("declaration.summary.legal-header")
      }
      "have correct button" in {
        view.getElementById("submit") must containMessage("site.acceptAndSubmitDeclaration")
      }
      "NOT have free text 'Reason for Amend' input" in {
        view.getElementsByAttributeValue("for", amendReasonKey) mustBe empty
      }
    }

    "for amendments only" should {
      "go back to normal summary page" in {
        amendView.getElementById("back-link") must haveHref(routes.AmendmentSummaryController.displayPage.url)
      }
      "have correct header" in {
        amendView.getElementById("title") must containMessage("legal.declaration.amend.heading")
      }
      "have correct button" in {
        amendView.getElementById("submit") must containMessage("legal.declaration.amend.button")
      }
      "have free text 'Reason for Amend' input" in {
        amendView.getElementsByAttributeValue("for", amendReasonKey) must containMessageForElements("legal.declaration.amend.reason")
      }
    }
  }
}
