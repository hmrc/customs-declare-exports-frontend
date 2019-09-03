/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitStandard}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit_standard
import views.tags.ViewTest

@ViewTest
class OfficeOfExitStandardViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {
  private val page: office_of_exit_standard = new office_of_exit_standard(mainTemplate)
  private val form: Form[OfficeOfExitStandard] = OfficeOfExitForms.standardForm()
  private def createView(mode: Mode = Mode.Normal, form: Form[OfficeOfExitStandard] = form): Document =
    page(mode, form)(journeyRequest(), stubMessages())

  "Office of Exit View for standard" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.officeOfExit.title")
      messages must haveTranslationFor("declaration.summary.locations.header")
      messages must haveTranslationFor("declaration.officeOfExit")
      messages must haveTranslationFor("declaration.officeOfExit.hint")
      messages must haveTranslationFor("declaration.officeOfExit.empty")
      messages must haveTranslationFor("declaration.officeOfExit.length")
      messages must haveTranslationFor("declaration.officeOfExit.specialCharacters")
      messages must haveTranslationFor("standard.officeOfExit.presentationOffice")
      messages must haveTranslationFor("standard.officeOfExit.presentationOffice.hint")
      messages must haveTranslationFor("standard.officeOfExit.presentationOffice.length")
      messages must haveTranslationFor("standard.officeOfExit.presentationOffice.specialCharacters")
      messages must haveTranslationFor("standard.officeOfExit.circumstancesCode")
      messages must haveTranslationFor("standard.officeOfExit.circumstancesCode.hint")
      messages must haveTranslationFor("standard.officeOfExit.circumstancesCode.empty")
      messages must haveTranslationFor("standard.officeOfExit.circumstancesCode.error")
    }

    "Office of Exit View on empty page for standard" should {

      "display page title" in {
        view.getElementById("title").text() mustBe "declaration.officeOfExit.title"
      }

      "display section header" in {
        view.getElementById("section-header").text() mustBe "declaration.summary.locations.header"
      }

      "display office of exit question" in {
        view.getElementById("officeId-label").text() mustBe "declaration.officeOfExit"
        view.getElementById("officeId-hint").text() mustBe "declaration.officeOfExit.hint"
        view.getElementById("officeId").attr("value") mustBe ""
      }

      "display presentation office question" in {
        view.getElementById("presentationOfficeId-label").text() mustBe "standard.officeOfExit.presentationOffice"
        view.getElementById("presentationOfficeId-hint").text() mustBe "standard.officeOfExit.presentationOffice.hint"
        view.getElementById("presentationOfficeId").attr("value") mustBe ""
      }

      // TODO change below code to use getElementById, missing ID in radio input for legend
      "display circumstances code question" in {
        view.select("#circumstancesCode>legend>span").text() must include("")
      }

      "display 'Back' button that links to 'Location of Goods' page" in {

        val backButton = view.getElementById("link-back")

        backButton.text() mustBe "site.back"
        backButton.getElementById("link-back") must haveHref(
          controllers.declaration.routes.LocationController.displayPage(Mode.Normal)
        )
      }

      "display 'Save and continue' button" in {
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text() mustBe "site.save_and_come_back_later"
      }
    }

    "Office of Exit during standard declaration for invalid input" should {

      "display errors when all inputs are empty" in {
        val data = OfficeOfExitStandard("", None, "")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form = form)

        checkErrorsSummary(view)

        view.getElementById("officeId-error").text() mustBe "declaration.officeOfExit.empty"
        view.getElementById("error-message-officeId-input").text() mustBe "declaration.officeOfExit.empty"

        view.getElementById("circumstancesCode-error").text() mustBe "standard.officeOfExit.circumstancesCode.empty"
        view
          .getElementById("error-message-circumstancesCode-input")
          .text() mustBe "standard.officeOfExit.circumstancesCode.empty"
      }

      "display errors when all inputs are incorrect" in {
        val data = OfficeOfExitStandard("123456", Some("654321"), "Yes")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form = form)

        checkErrorsSummary(view)

        view.getElementById("officeId-error").text() mustBe "declaration.officeOfExit.length"
        view.getElementById("error-message-officeId-input").text() mustBe "declaration.officeOfExit.length"

        view
          .getElementById("presentationOfficeId-error")
          .text() mustBe "standard.officeOfExit.presentationOffice.length"
        view
          .getElementById("error-message-presentationOfficeId-input")
          .text() mustBe "standard.officeOfExit.presentationOffice.length"
      }

      "display errors when office of exit and presentation office contains special characters" in {
        val data = OfficeOfExitStandard("12#$%^78", Some("87^%$#21"), "Yes")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form = form)

        checkErrorsSummary(view)

        view.getElementById("officeId-error").text() mustBe "declaration.officeOfExit.specialCharacters"
        view.getElementById("error-message-officeId-input").text() mustBe "declaration.officeOfExit.specialCharacters"

        view
          .getElementById("presentationOfficeId-error")
          .text() mustBe "standard.officeOfExit.presentationOffice.specialCharacters"
        view
          .getElementById("error-message-presentationOfficeId-input")
          .text() mustBe "standard.officeOfExit.presentationOffice.specialCharacters"
      }
    }
  }
}
