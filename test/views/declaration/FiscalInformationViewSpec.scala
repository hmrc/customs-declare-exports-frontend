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

import controllers.util.SaveAndReturn
import forms.declaration.FiscalInformation
import helpers.views.declaration.{CommonMessages, FiscalInformationMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.fiscal_information
import views.tags.ViewTest

@ViewTest
class FiscalInformationViewSpec extends ViewSpec with FiscalInformationMessages with CommonMessages {

  private val form: Form[FiscalInformation] = FiscalInformation.form()
  private val fiscalInformationPage = app.injector.instanceOf[fiscal_information]
  private def createView(form: Form[FiscalInformation] = form): Html =
    fiscalInformationPage(Mode.Normal, itemId, form)(fakeRequest, messages)

  "Fiscal Information View on empty page" should {

    "display page title" in {
      createView().getElementById("title").text() must be(messages(title))
    }

    "display section header" in {
      createView().getElementById("section-header").text() must be(messages(header))
    }

    "display two radio buttons with description (not selected)" in {
      val view = createView(FiscalInformation.form().fill(FiscalInformation("")))

      val optionOne = view.getElementById("Yes")
      optionOne.attr("checked") must be("")

      val optionOneLabel = view.getElementById("Yes-label")
      optionOneLabel.text() must be(messages(yes))

      val optionTwo = view.getElementById("No")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = view.getElementById("No-label")
      optionTwoLabel.text() must be(messages(no))
    }

    "display 'Back' button that links to 'Warehouse' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/procedure-codes")
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() must be(messages(saveAndReturnCaption))
      saveButton.attr("name") must be(SaveAndReturn.toString)
    }

  }

  "Fiscal Information View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(FiscalInformation.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageEmpty), "#onwardSupplyRelief")

      view.select("#error-message-onwardSupplyRelief-input").text() must be(messages(errorMessageEmpty))
    }

    "display error if incorrect fiscal information is selected" in {

      val view = createView(FiscalInformation.form().fillAndValidate(FiscalInformation("Incorrect")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageIncorrect), "#onwardSupplyRelief")

      view.select("#error-message-onwardSupplyRelief-input").text() must be(messages(errorMessageIncorrect))
    }

  }

  "Dispatch Border Transport View when filled" should {

    "display selected first radio button - Yes" in {

      val view = createView(FiscalInformation.form().fill(FiscalInformation("Yes")))

      val optionOne = view.getElementById("Yes")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("No")
      optionTwo.attr("checked") must be("")
    }

    "display selected second radio button - No" in {

      val view = createView(FiscalInformation.form().fill(FiscalInformation("No")))

      val optionOne = view.getElementById("Yes")
      optionOne.attr("checked") must be("")

      val optionTwo = view.getElementById("No")
      optionTwo.attr("checked") must be("checked")
    }
  }
}
