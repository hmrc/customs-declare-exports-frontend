/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.declaration.LegalDeclaration
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.legal_declaration

class LegalDeclarationViewSpec extends UnitViewSpec with Injector {

  private val emptyForm = LegalDeclaration.form()
  private val component = instanceOf[legal_declaration]
  private val view = component(emptyForm)

  "Legal Declaration View" should {

    "have header and translation for it" in {

      view.getElementsByClass("govuk-fieldset__legend").first() must containMessage("legal.declaration.heading")
      messages must haveTranslationFor("legal.declaration.heading")
    }

    "have information about declaration" in {

      view.body must include(messages("legal.declaration.info"))
      messages must haveTranslationFor("legal.declaration.info")
    }

    "have legal declaration warning" in {

      view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("site.warning")
      view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("legal.declaration.warning")
      messages must haveTranslationFor("site.warning")
      messages must haveTranslationFor("legal.declaration.warning")
    }

    "have full name input" in {

      view.getElementsByAttributeValue("for", "fullName") must containMessageForElements("legal.declaration.fullName")
      messages must haveTranslationFor("legal.declaration.fullName")
      messages must haveTranslationFor("legal.declaration.fullName.empty")
      messages must haveTranslationFor("legal.declaration.fullName.short")
      messages must haveTranslationFor("legal.declaration.fullName.long")
      messages must haveTranslationFor("legal.declaration.fullName.error")
    }

    "have job role input" in {

      view.getElementsByAttributeValue("for", "jobRole") must containMessageForElements("legal.declaration.jobRole")
      messages must haveTranslationFor("legal.declaration.jobRole")
      messages must haveTranslationFor("legal.declaration.jobRole.empty")
      messages must haveTranslationFor("legal.declaration.jobRole.short")
      messages must haveTranslationFor("legal.declaration.jobRole.long")
      messages must haveTranslationFor("legal.declaration.jobRole.error")
    }

    "have email input" in {

      view.getElementsByAttributeValue("for", "email") must containMessageForElements("legal.declaration.email")
      messages must haveTranslationFor("legal.declaration.email")
      messages must haveTranslationFor("legal.declaration.email.empty")
      messages must haveTranslationFor("legal.declaration.email.long")
      messages must haveTranslationFor("legal.declaration.email.error")
    }

    "have confirmation box" in {

      view.getElementsByAttributeValue("for", "confirmation") must containMessageForElements("legal.declaration.confirmation")
      messages must haveTranslationFor("legal.declaration.confirmation")
      messages must haveTranslationFor("legal.declaration.confirmation.missing")
    }
  }
}
