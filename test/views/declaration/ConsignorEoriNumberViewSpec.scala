/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.consignor.ConsignorEoriNumber
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.consignor_eori_number
import views.tags.ViewTest

@ViewTest
class ConsignorEoriNumberViewSpec extends UnitViewSpec2 with ExportsTestData with Stubs with Injector {

  private val page: consignor_eori_number = instanceOf[consignor_eori_number]

  private def createView(mode: Mode = Mode.Normal, form: Form[ConsignorEoriNumber] = ConsignorEoriNumber.form())(
    implicit request: JourneyRequest[_]
  ): Document =
    page(mode, form)(request, messages)

  "Consignor Eori Number View" should {
    onJourney(DeclarationType.CLEARANCE) { implicit request =>
      val view = createView()
      "display answer input" in {
        val consignorEoriNumber = ConsignorEoriNumber.form().fill(ConsignorEoriNumber(Some(Eori("GB123456789")), YesNoAnswers.yes))
        val view = createView(form = consignorEoriNumber)

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.consignorEori.title")
        messages must haveTranslationFor("declaration.summary.parties.header")
        messages must haveTranslationFor("declaration.consignorEori.eori.label")
        messages must haveTranslationFor("declaration.consignorEori.hasEori.empty")
        messages must haveTranslationFor("declaration.consignorEori.help-item1")
      }

      "display page title" in {
        view.getElementsByClass("govuk-fieldset__heading") must containMessageForElements("declaration.consignorEori.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.summary.parties.header")
      }

      "display eori question" in {
        view.getElementsByClass("govuk-label--m") must containMessageForElements("declaration.consignorEori.eori.label")
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Exporter Details' page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.IsExsController.displayPage(Mode.Normal))
      }

      "display 'Save and continue' button" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage("site.save_and_come_back_later")
      }

      "handle invalid input" should {
        "display errors when all inputs are incorrect" in {
          val data = ConsignorEoriNumber(Some(Eori("123456789")), YesNoAnswers.yes)
          val form = ConsignorEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }

        "display errors when eori contains special characters" in {
          val data = ConsignorEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = YesNoAnswers.yes)
          val form = ConsignorEoriNumber.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }
      }
    }
  }
}
