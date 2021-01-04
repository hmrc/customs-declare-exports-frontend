/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.declaration.officeOfExit.OfficeOfExitOutsideUK
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit_outside_uk
import views.tags.ViewTest

@ViewTest
class OfficeOfExitOutsideUkViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page: office_of_exit_outside_uk = instanceOf[office_of_exit_outside_uk]

  private def createView(mode: Mode = Mode.Normal, form: Form[OfficeOfExitOutsideUK] = OfficeOfExitOutsideUK.form()): Document =
    page(mode, form)(journeyRequest(), messages)

  "Office of Exit View" should {
    val view = createView()
    onEveryDeclarationJourney() { implicit request =>
      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.officeOfExitOutsideUk.title")
        messages must haveTranslationFor("declaration.officeOfExitOutsideUk.hint")
        messages must haveTranslationFor("declaration.officeOfExitOutsideUk.empty")
        messages must haveTranslationFor("declaration.officeOfExitOutsideUk.length")
        messages must haveTranslationFor("declaration.officeOfExitOutsideUk.specialCharacters")
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLabel) must containMessageForElements("declaration.officeOfExitOutsideUk.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.3")
      }

      "display office of exit outside UK question" in {
        view.getElementsByClass(Styles.gdsPageLabel) must containMessageForElements("declaration.officeOfExitOutsideUk.title")
        view.getElementById("officeId-hint") must containMessage("declaration.officeOfExitOutsideUk.hint")
        view.getElementById("officeId").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Office of Exit' page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
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
          val data = OfficeOfExitOutsideUK("")
          val view = createView(form = OfficeOfExitOutsideUK.form().fillAndValidate(data))

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#officeId")
          view must containErrorElementWithMessageKey("declaration.officeOfExitOutsideUk.empty")
        }

        "display errors when format is incorrect" in {
          val data = OfficeOfExitOutsideUK("123456")
          val form = OfficeOfExitOutsideUK.form().fillAndValidate(data)
          val view = createView(form = form)

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#officeId")
          view must containErrorElementWithMessageKey("declaration.officeOfExitOutsideUk.format")
        }

        "display errors when office of exit contains special characters" in {
          val data = OfficeOfExitOutsideUK("12#$%^78")
          val form = OfficeOfExitOutsideUK.form().fillAndValidate(data)
          val view = createView(form = form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#officeId")
          view must containErrorElementWithMessageKey("declaration.officeOfExitOutsideUk.format")
        }
      }
    }
  }
}
