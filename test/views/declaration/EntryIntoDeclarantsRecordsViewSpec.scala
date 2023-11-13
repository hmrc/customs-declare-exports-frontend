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

package views.declaration

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{No, Yes, YesNoAnswers}
import forms.declaration.EntryIntoDeclarantsRecords.form
import models.DeclarationType.CLEARANCE
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.entry_into_declarants_records

class EntryIntoDeclarantsRecordsViewSpec extends UnitViewSpec with Injector with CommonMessages {

  private val page = instanceOf[entry_into_declarants_records]

  private def createView(frm: Form[YesNoAnswer] = form): Document =
    page(frm)(journeyRequest(CLEARANCE), messages)

  "Entry Into Declarant Records view" when {

    "on empty page" should {

      "display page title" in {
        createView().getElementsByTag("h1").first() must containMessage("declaration.entryIntoDeclarantRecords.title")
      }

      "display section header" in {
        createView().getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display radio button with yes and no options" in {
        val view = createView()

        view.getElementById("answer_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "answer_yes").first() must containMessage("declaration.entryIntoDeclarantRecords.answer.yes")
        view.getElementById("answer_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "answer_no").first() must containMessage("declaration.entryIntoDeclarantRecords.answer.no")
      }

      "display the 'Back' button" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.LinkDucrToMucrController.displayPage.url)
      }

      "not display the 'Back' button" when {
        "the declaration is in AMENDMENT_DRAFT mode" in {
          implicit val request = journeyRequest(aDeclaration(withType(CLEARANCE), withStatus(AMENDMENT_DRAFT)))
          Option(page(form).getElementById("back-link")) mustBe None
        }
      }
    }

    "on page filled" should {
      "have proper radio selected" when {

        "the answer is Yes" in {
          val eidrForm = form.fill(Yes.value)
          val view = createView(eidrForm)

          view.getElementById("answer_yes") must beSelected
          view.getElementById("answer_no") mustNot beSelected
        }

        "the answer is No" in {
          val eidrForm = form.fill(No.value)
          val view = createView(eidrForm)

          view.getElementById("answer_yes") mustNot beSelected
          view.getElementById("answer_no") must beSelected
        }
      }
    }

    "provided with form with errors" should {

      "display error summary" in {
        val formError = FormError("is-entry-into-declarant-records", "declaration.entryIntoDeclarantRecords.error")
        val eidrForm = form.withError(formError)
        createView(eidrForm) must haveGovukGlobalErrorSummary
      }

      "display error next to the radio button element" in {
        val formError = FormError("is-entry-into-declarant-records", "declaration.entryIntoDeclarantRecords.error")
        val eidrForm = form.withError(formError)
        createView(eidrForm) must haveGovukFieldError("is-entry-into-declarant-records", messages("declaration.entryIntoDeclarantRecords.error"))
      }
    }
  }
}
