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

import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitStandard}
import helpers.views.declaration.{CommonMessages, OfficeOfExitMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.declaration.office_of_exit_standard
import views.tags.ViewTest

@ViewTest
class OfficeOfExitStandardViewSpec extends AppViewSpec with OfficeOfExitMessages with CommonMessages {

  private val form: Form[OfficeOfExitStandard] = OfficeOfExitForms.standardForm()
  private val officeOfExitStandardPage = app.injector.instanceOf[office_of_exit_standard]
  private def createView(form: Form[OfficeOfExitStandard] = form): Html =
    officeOfExitStandardPage(Mode.Normal, form)(fakeRequest, messages)

  "Office of Exit View for standard" should {

    "Office of Exit View on empty page for standard" should {

      "display page title" in {

        createView().getElementById("title").text() mustBe messages(title)
      }

      "display section header" in {

        createView().getElementById("section-header").text() mustBe "Locations"
      }

      "display office of exit question" in {
        val view = createView()

        view.getElementById("officeId-label").text() mustBe messages(officeOfExit)
        view.getElementById("officeId-hint").text() mustBe messages(hint)
        view.getElementById("officeId").attr("value") mustBe ""
      }

      "display presentation office question" in {
        val view = createView()
        view.getElementById("presentationOfficeId-label").text() mustBe messages(presentationOffice)
        view.getElementById("presentationOfficeId-hint").text() mustBe messages(presentationOfficeHint)
        view.getElementById("presentationOfficeId").attr("value") mustBe ""
      }

      // TODO change below code to use getElementById, missing ID in radio input for legend
      "display circumstances code question" in {
        val view = createView()
        view.select("#circumstancesCode>legend>span").text() must include(messages(circumstancesCode))
      }

      "display 'Back' button that links to 'Location of Goods' page" in {

        val backButton = createView().getElementById("link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe "/customs-declare-exports/declaration/location-of-goods"
      }

      "display 'Save and continue' button" in {
        val saveButton = createView().getElementById("submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = createView().getElementById("submit_and_return")
        saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
      }
    }

    "Office of Exit during standard declaration for invalid input" should {

      "display errors when all inputs are empty" in {
        val data = OfficeOfExitStandard("", None, "")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form)

        view.getElementById("error-summary-heading").text() mustNot be(empty)

        view.getElementById("officeId-error").text() mustBe messages(officeOfExitEmpty)
        view.getElementById("error-message-officeId-input").text() mustBe messages(officeOfExitEmpty)

        view.getElementById("circumstancesCode-error").text() must be(messages(circumstancesCodeEmpty))
        view.getElementById("error-message-circumstancesCode-input").text() mustBe messages(circumstancesCodeEmpty)
      }

      "display errors when all inputs are incorrect" in {
        val data = OfficeOfExitStandard("123456", Some("654321"), "Yes")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form)

        view.getElementById("error-summary-heading").text() mustNot be(empty)

        view.getElementById("officeId-error").text() mustBe messages(officeOfExitLength)
        view.getElementById("error-message-officeId-input").text() mustBe messages(officeOfExitLength)

        view.getElementById("presentationOfficeId-error").text() mustBe messages(presentationOfficeLength)
        view.getElementById("error-message-presentationOfficeId-input").text() mustBe messages(presentationOfficeLength)
      }

      "display errors when office of exit and presentation office contains special characters" in {
        val data = OfficeOfExitStandard("12#$%^78", Some("87^%$#21"), "Yes")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form)

        view.getElementById("error-summary-heading").text() mustNot be(empty)

        view.getElementById("officeId-error").text() mustBe messages(officeOfExitSpecialCharacters)
        view.getElementById("error-message-officeId-input").text() mustBe messages(officeOfExitSpecialCharacters)

        view.getElementById("presentationOfficeId-error").text() mustBe messages(presentationOfficeSpecialCharacters)
        view.getElementById("error-message-presentationOfficeId-input").text() mustBe messages(
          presentationOfficeSpecialCharacters
        )
      }
    }
  }
}
