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

import forms.declaration.FiscalInformation
import helpers.views.declaration.{CommonMessages, FiscalInformationMessages}
import play.api.data.Form
import play.twirl.api.Html
import utils.FakeRequestCSRFSupport._
import views.declaration.spec.ViewSpec
import views.html.declaration.fiscal_information
import views.tags.ViewTest

@ViewTest
class FiscalInformationViewSpec extends ViewSpec with FiscalInformationMessages with CommonMessages {

  private val form: Form[FiscalInformation] = FiscalInformation.form()
  private def createView(form: Form[FiscalInformation] = form): Html =
    fiscal_information(form)(fakeRequest.withCSRFToken, appConfig, messages)

  "Fiscal Information View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Onward Supply Relief (OSR)")
      assertMessage(header, "Item 1")
      assertMessage(yes, "Yes")
      assertMessage(no, "No")
    }
    "have proper messages for error labels" in {
      assertMessage(errorMessageEmpty, "Please enter a value")
      assertMessage(errorMessageIncorrect, "Please enter a value")
    }
  }

  "Fiscal Information View on empty page" should {

    "display page title" in {
      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {
      getElementById(createView(), "section-header").text() must be(messages(header))
    }

    "display two radio buttons with description (not selected)" in {
      val view = createView(FiscalInformation.form().fill(FiscalInformation("")))

      val optionOne = getElementById(view, "Yes")
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementById(view, "Yes-label")
      optionOneLabel.text() must be(messages(yes))

      val optionTwo = getElementById(view, "No")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementById(view, "No-label")
      optionTwoLabel.text() must be(messages(no))
    }

    "display 'Back' button that links to 'Warehouse' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/procedure-codes")
    }

    "display 'Save and continue' button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

  }

  "Fiscal Information View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(FiscalInformation.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageEmpty), "#onwardSupplyRelief")

      getElementByCss(view, "#error-message-onwardSupplyRelief-input").text() must be(messages(errorMessageEmpty))
    }

    "display error if incorrect fiscal information is selected" in {

      val view = createView(FiscalInformation.form().fillAndValidate(FiscalInformation("Incorrect")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageIncorrect), "#onwardSupplyRelief")

      getElementByCss(view, "#error-message-onwardSupplyRelief-input").text() must be(messages(errorMessageIncorrect))
    }

  }

  "Dispatch Border Transport View when filled" should {

    "display selected first radio button - Yes" in {

      val view = createView(FiscalInformation.form().fill(FiscalInformation("Yes")))

      val optionOne = getElementById(view, "Yes")
      optionOne.attr("checked") must be("checked")

      val optionTwo = getElementById(view, "No")
      optionTwo.attr("checked") must be("")
    }

    "display selected second radio button - No" in {

      val view = createView(FiscalInformation.form().fill(FiscalInformation("No")))

      val optionOne = getElementById(view, "Yes")
      optionOne.attr("checked") must be("")

      val optionTwo = getElementById(view, "No")
      optionTwo.attr("checked") must be("checked")
    }
  }
}
