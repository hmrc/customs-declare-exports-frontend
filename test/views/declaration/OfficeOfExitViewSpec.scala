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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.officeOfExit.OfficeOfExitInsideUK
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit
import views.tags.ViewTest

@ViewTest
class OfficeOfExitViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page: office_of_exit = instanceOf[office_of_exit]

  private def createView(mode: Mode = Mode.Normal, form: Form[OfficeOfExitInsideUK] = OfficeOfExitInsideUK.form()): Document =
    page(mode, form)(journeyRequest(), messages)

  "Office of Exit View" should {
    val view = createView()
    onEveryDeclarationJourney() { implicit request =>
      "display answer input" in {
        val officeOfExit = OfficeOfExitInsideUK.form().fill(OfficeOfExitInsideUK(Some("officeId"), YesNoAnswers.yes))
        val view = createView(form = officeOfExit)

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.officeOfExit.title")
        messages must haveTranslationFor("declaration.summary.locations.header")
        messages must haveTranslationFor("declaration.officeOfExit")
        messages must haveTranslationFor("declaration.officeOfExit.hint")
        messages must haveTranslationFor("declaration.officeOfExit.empty")
        messages must haveTranslationFor("declaration.officeOfExit.length")
        messages must haveTranslationFor("declaration.officeOfExit.specialCharacters")
      }

      "display page title" in {
        view.getElementsByClass("govuk-fieldset__heading") must containMessageForElements("declaration.officeOfExit.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.summary.locations.header")
      }

      "display office of exit question" in {
        view.getElementById("officeId-label") must containMessage("declaration.officeOfExit")
        view.getElementById("isUkOfficeOfExit-hint") must containMessage("declaration.officeOfExit.hint")
        view.getElementById("officeId").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Location of Goods' page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.LocationController.displayPage(Mode.Normal))
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

        "display errors when all inputs are empty" in {
          val data = OfficeOfExitInsideUK(None, "")
          val view = createView(form = OfficeOfExitInsideUK.form().fillAndValidate(data))

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#isUkOfficeOfExit")
          view must containErrorElementWithMessageKey("declaration.officeOfExit.isUkOfficeOfExit.empty")
        }

        "display errors when all inputs are incorrect" in {
          val data = OfficeOfExitInsideUK(Some("123456"), "Yes")
          val form = OfficeOfExitInsideUK.form().fillAndValidate(data)
          val view = createView(form = form)

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

          view
            .getElementsByClass("govuk-list govuk-error-summary__list")
            .get(0)
            .getElementsByTag("li")
            .get(0) must containMessage("declaration.officeOfExit.length")
          view.getElementById("error-message-officeId-input") must containMessage("declaration.officeOfExit.length")
        }

        "display errors when office of exit contains special characters" in {
          val data = OfficeOfExitInsideUK(Some("12#$%^78"), "Yes")
          val form = OfficeOfExitInsideUK.form().fillAndValidate(data)
          val view = createView(form = form)

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

          view
            .getElementsByClass("govuk-list govuk-error-summary__list")
            .get(0)
            .getElementsByTag("li")
            .get(0) must containMessage("declaration.officeOfExit.specialCharacters")
          view.getElementById("error-message-officeId-input") must containMessage("declaration.officeOfExit.specialCharacters")
        }
      }
    }
  }
}
