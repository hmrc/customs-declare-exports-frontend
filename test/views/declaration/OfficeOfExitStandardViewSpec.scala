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
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitStandard}
import helpers.views.declaration.{CommonMessages, OfficeOfExitMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.office_of_exit_standard
import views.tags.ViewTest

@ViewTest
class OfficeOfExitStandardViewSpec extends ViewSpec with OfficeOfExitMessages with CommonMessages {

  private val form: Form[OfficeOfExitStandard] = OfficeOfExit.standardForm()
  private def createView(form: Form[OfficeOfExitStandard] = form): Html =
    office_of_exit_standard(form)(fakeRequest, appConfig, messages)

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

      assertMessage(presentationOfficeEmpty, "Enter the office of presentation reference")
      assertMessage(presentationOfficeLength, "The code must be 8 characters")
      assertMessage(presentationOfficeSpecialCharacters, "Enter a reference in the correct format")
      assertMessage(circumstancesCodeEmpty, "Please provide an answer on this question")
    }

    "Office of Exit View on empty page for standard" should {

      "display page title" in {

        getElementByCss(createView(), "title").text() must be(messages(title))
      }

      "display section header" in {

        getElementById(createView(), "section-header").text() must be("Locations")
      }

      "display office of exit question" in {
        val view = createView()

        getElementByCss(view, "form>div:nth-child(4)>label>span.bold-small")
          .text() must be(messages(officeOfExit))
        getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint")
          .text() must be(messages(hint))
        getElementById(view, "officeId").attr("value") must be("")
      }

      "display presentation office question" in {
        val view = createView()

        getElementByCss(view, "form>div:nth-child(5)>label>span.bold-small")
          .text() must be(messages(presentationOffice))
        getElementByCss(view, "form>div:nth-child(5)>label>span.form-hint")
          .text() must be(messages(presentationOfficeHint))
        getElementById(view, "presentationOfficeId").attr("value") must be("")
      }

      "display circumstances code question" in {
        val view = createView()

        getElementByCss(view, "#circumstancesCode>legend>span")
          .text() must be(messages(circumstancesCode))
      }

      "display 'Back' button that links to 'Location of Goods' page" in {

        val backButton = getElementById(createView(), "link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be("/customs-declare-exports/declaration/location-of-goods")
      }

      "display 'Save and continue' button" in {

        val saveButton = getElementByCss(createView(), "#submit")
        saveButton.text() must be(messages(saveAndContinueCaption))
      }
    }

    "Office of Exit during standard declaration for invalid input" should {

      "display errors when all inputs are empty" in {
        val data = OfficeOfExitStandard("", "", "")
        val form = OfficeOfExit.standardForm.fillAndValidate(data)
        val view = createView(form)

        checkErrorsSummary(view)

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(1)>a")
          .text() must be(messages(officeOfExitEmpty))
        getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitEmpty))

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(2)>a")
          .text() must be(messages(presentationOfficeEmpty))
        getElementByCss(view, "#error-message-presentationOfficeId-input").text() must be(
          messages(presentationOfficeEmpty)
        )

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(3)>a")
          .text() must be(messages(circumstancesCodeEmpty))
        getElementByCss(view, "#error-message-circumstancesCode-input").text() must be(messages(circumstancesCodeEmpty))
      }

      "display errors when all inputs are incorrect" in {
        val data = OfficeOfExitStandard("123456", "654321", "Yes")
        val form = OfficeOfExit.standardForm.fillAndValidate(data)
        val view = createView(form)

        checkErrorsSummary(view)

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(1)>a")
          .text() must be(messages(officeOfExitLength))
        getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitLength))

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(2)>a")
          .text() must be(messages(presentationOfficeLength))
        getElementByCss(view, "#error-message-presentationOfficeId-input").text() must be(
          messages(presentationOfficeLength)
        )
      }

      "display errors when office of exit and presentation office contains special characters" in {
        val data = OfficeOfExitStandard("12#$%^78", "87^%$#21", "Yes")
        val form = OfficeOfExit.standardForm.fillAndValidate(data)
        val view = createView(form)

        checkErrorsSummary(view)

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(1)>a")
          .text() must be(messages(officeOfExitSpecialCharacters))
        getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitSpecialCharacters))

        getElementByCss(view, "form>div.error-summary.error-summary--show>ul>li:nth-child(2)>a")
          .text() must be(messages(presentationOfficeSpecialCharacters))
        getElementByCss(view, "#error-message-presentationOfficeId-input").text() must be(
          messages(presentationOfficeSpecialCharacters)
        )
      }
    }
  }
}
