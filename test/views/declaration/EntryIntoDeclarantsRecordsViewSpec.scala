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
import forms.declaration.EntryIntoDeclarantsRecords
import models.DeclarationType
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.entry_into_declarants_records

class EntryIntoDeclarantsRecordsViewSpec extends UnitViewSpec with Injector with CommonMessages {

  private val page = instanceOf[entry_into_declarants_records]
  private def createView(form: Form[YesNoAnswer] = EntryIntoDeclarantsRecords.form): Document =
    page(form)(journeyRequest(DeclarationType.CLEARANCE), messages)

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

      "display 'Back' button" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.LinkDucrToMucrController.displayPage.url)
      }

    }

    "on page filled" should {

      "have proper radio selected" when {

        "the answer is Yes" in {

          val form = EntryIntoDeclarantsRecords.form.fill(Yes.value)
          val view = createView(form)

          view.getElementById("answer_yes") must beSelected
          view.getElementById("answer_no") mustNot beSelected
        }

        "the answer is No" in {

          val form = EntryIntoDeclarantsRecords.form.fill(No.value)
          val view = createView(form)

          view.getElementById("answer_yes") mustNot beSelected
          view.getElementById("answer_no") must beSelected
        }
      }
    }

    "provided with form with errors" should {

      "display error summary" in {

        val form =
          EntryIntoDeclarantsRecords.form.withError(FormError("is-entry-into-declarant-records", "declaration.entryIntoDeclarantRecords.error"))
        createView(form) must haveGovukGlobalErrorSummary
      }

      "display error next to the radio button element" in {

        val form =
          EntryIntoDeclarantsRecords.form.withError(FormError("is-entry-into-declarant-records", "declaration.entryIntoDeclarantRecords.error"))
        createView(form) must haveGovukFieldError("is-entry-into-declarant-records", messages("declaration.entryIntoDeclarantRecords.error"))
      }
    }
  }

}
