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
import forms.declaration.DispatchLocation
import helpers.views.declaration.{CommonMessages, DispatchLocationMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.declaration.dispatch_location
import views.tags.ViewTest

@ViewTest
class DispatchLocationViewSpec extends AppViewSpec with DispatchLocationMessages with CommonMessages {

  private val form: Form[DispatchLocation] = DispatchLocation.form()
  private val dispatchLocationPage = app.injector.instanceOf[dispatch_location]
  private def createView(form: Form[DispatchLocation] = form, mode: Mode = Mode.Normal): Html =
    dispatchLocationPage(mode, form)(fakeRequest, messages)

  "Dispatch Location View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(header))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Locations")
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("")))

      val optionOne = view.getElementById("OutsideEU")
      optionOne.attr("checked") must be("")

      val optionOneLabel = view.getElementById("OutsideEU-label")
      optionOneLabel.text() must be(messages(outsideEu))

      val optionTwo = view.getElementById("SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = view.getElementById("SpecialFiscalTerritory-label")
      optionTwoLabel.text() must be(messages(specialFiscalTerritory))
    }

    "display 'Back' button" when {
      "normal mode" in {
        val backButton = createView(mode = Mode.Normal).getElementById("link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be(controllers.routes.ChoiceController.displayPage().url)
      }

      "draft mode" in {
        val backButton = createView(mode = Mode.Draft).getElementById("link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be(controllers.declaration.routes.SummaryController.displayPage(Mode.Draft).url)
      }

      "amend mode" in {
        val backButton = createView(mode = Mode.Amend).getElementById("link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be(controllers.declaration.routes.SummaryController.displayPage(Mode.Amend).url)
      }
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

  "Dispatch Location View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(DispatchLocation.form().bind(Map[String, String]()))

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, messages(errorMessageEmpty), "#dispatchLocation")

      view.select("#error-message-dispatchLocation-input").text() must be(messages(errorMessageEmpty))
    }

    "display error if incorrect dispatch is selected" in {

      val view = createView(DispatchLocation.form().fillAndValidate(DispatchLocation("12")))

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, messages(errorMessageIncorrect), "#dispatchLocation")

      view.select("#error-message-dispatchLocation-input").text() must be(messages(errorMessageIncorrect))
    }
  }

  "Dispatch Location View when filled" should {

    "display selected first radio button - Outside (EX)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("EX")))

      val optionOne = view.getElementById("OutsideEU")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("")
    }

    "display selected second radio button - Fiscal Territory (CO)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("CO")))

      val optionOne = view.getElementById("OutsideEU")
      optionOne.attr("checked") must be("")

      val optionTwo = view.getElementById("SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("checked")
    }
  }
}
