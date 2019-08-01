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
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.office_of_exit_standard
import views.tags.ViewTest

@ViewTest
class OfficeOfExitStandardViewSpec extends ViewSpec with OfficeOfExitMessages with CommonMessages {

  private val form: Form[OfficeOfExitStandard] = OfficeOfExitForms.standardForm()
  private val officeOfExitStandardPage = app.injector.instanceOf[office_of_exit_standard]
  private def createView(form: Form[OfficeOfExitStandard] = form): Html =
    officeOfExitStandardPage(form)(fakeRequest, messages)

  "Office of Exit View for standard" should {

    "have proper messages for labels" in {

      assertMessage(officeOfExit, "5/12 Where is the office of exit?")
      assertMessage(title, "Office of exit")
      assertMessage(hint, "This is an 8 digit code")
      assertMessage(presentationOffice, "5/26 Where is the office of presentation?")
      assertMessage(presentationOfficeHint, "Enter the 8 digit reference")
      assertMessage(circumstancesCode, "1/7 Does the declaration relate to an express consignment?")
    }

    "have proper messages for error labels" in {

      assertMessage(presentationOfficeLength, "The code must be 8 characters")
      assertMessage(presentationOfficeSpecialCharacters, "Enter a reference in the correct format")
      assertMessage(circumstancesCodeEmpty, "Please provide an answer on this question")
    }

    "Office of Exit View on empty page for standard" should {

      "display page title" in {

        getElementById(createView(), "title").text() mustBe messages(title)
      }

      "display section header" in {

        getElementById(createView(), "section-header").text() mustBe "Locations"
      }

      "display office of exit question" in {
        val view = createView()

        getElementById(view, "officeId-label").text() mustBe messages(officeOfExit)
        getElementById(view, "officeId-hint").text() mustBe messages(hint)
        getElementById(view, "officeId").attr("value") mustBe ""
      }

      "display presentation office question" in {
        val view = createView()
        getElementById(view, "presentationOfficeId-label").text() mustBe messages(presentationOffice)
        getElementById(view, "presentationOfficeId-hint").text() mustBe messages(presentationOfficeHint)
        getElementById(view, "presentationOfficeId").attr("value") mustBe ""
      }

      // TODO change below code to use getElementById, missing ID in radio input for legend
      "display circumstances code question" in {
        val view = createView()
        getElementByCss(view, "#circumstancesCode>legend>span").text() must be(messages(circumstancesCode))
      }

      "display 'Back' button that links to 'Location of Goods' page" in {

        val backButton = getElementById(createView(), "link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe "/customs-declare-exports/declaration/location-of-goods"
      }

      "display 'Save and continue' button" in {

        val saveButton = getElementByCss(createView(), "#submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }
    }

    "Office of Exit during standard declaration for invalid input" should {

      "display errors when all inputs are empty" in {
        val data = OfficeOfExitStandard("", None, "")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form)

        checkErrorsSummary(view)

        getElementById(view, "officeId-error").text() mustBe messages(officeOfExitEmpty)
        getElementById(view, "error-message-officeId-input").text() mustBe messages(officeOfExitEmpty)

        getElementById(view, "circumstancesCode-error").text() must be(messages(circumstancesCodeEmpty))
        getElementById(view, "error-message-circumstancesCode-input").text() mustBe messages(circumstancesCodeEmpty)
      }

      "display errors when all inputs are incorrect" in {
        val data = OfficeOfExitStandard("123456", Some("654321"), "Yes")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form)

        checkErrorsSummary(view)

        getElementById(view, "officeId-error").text() mustBe messages(officeOfExitLength)
        getElementById(view, "error-message-officeId-input").text() mustBe messages(officeOfExitLength)

        getElementById(view, "presentationOfficeId-error").text() mustBe messages(presentationOfficeLength)
        getElementById(view, "error-message-presentationOfficeId-input").text() mustBe messages(
          presentationOfficeLength
        )
      }

      "display errors when office of exit and presentation office contains special characters" in {
        val data = OfficeOfExitStandard("12#$%^78", Some("87^%$#21"), "Yes")
        val form = OfficeOfExitForms.standardForm.fillAndValidate(data)
        val view = createView(form)

        checkErrorsSummary(view)

        getElementById(view, "officeId-error").text() mustBe messages(officeOfExitSpecialCharacters)
        getElementById(view, "error-message-officeId-input").text() mustBe messages(officeOfExitSpecialCharacters)

        getElementById(view, "presentationOfficeId-error").text() mustBe messages(presentationOfficeSpecialCharacters)
        getElementById(view, "error-message-presentationOfficeId-input").text() mustBe messages(
          presentationOfficeSpecialCharacters
        )
      }
    }
  }
}
