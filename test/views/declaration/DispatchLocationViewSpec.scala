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
import controllers.util.SaveAndReturn
import forms.declaration.DispatchLocation
import helpers.views.declaration.{CommonMessages, DispatchLocationMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.dispatch_location
import views.tags.ViewTest

@ViewTest
class DispatchLocationViewSpec extends UnitViewSpec with DispatchLocationMessages with CommonMessages with Stubs with Injector {

  private val form: Form[DispatchLocation] = DispatchLocation.form()
  private val dispatchLocationPage = new dispatch_location(mainTemplate)
  private def createView(form: Form[DispatchLocation] = form, mode: Mode = Mode.Normal): Document =
    dispatchLocationPage(mode, form)(request, messages)

  "Dispatch Location" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.dispatchLocation.header")
      messages must haveTranslationFor("supplementary.dispatchLocation.header.hint")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.outsideEU")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.specialFiscalTerritory")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.error.empty")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.error.incorrect")
    }
  }

  "Dispatch Location View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(header)
    }

    "display section header" in {

      createView().getElementById("section-header").text() must include(messages("declaration.summary.locations.header"))
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("")))

      val optionOne = view.getElementById("OutsideEU")
      optionOne.attr("checked") mustBe empty

      val optionOneLabel = view.getElementById("OutsideEU-label")
      optionOneLabel.text() mustBe messages(outsideEu)

      val optionTwo = view.getElementById("SpecialFiscalTerritory")
      optionTwo.attr("checked") mustBe empty

      val optionTwoLabel = view.getElementById("SpecialFiscalTerritory-label")
      optionTwoLabel.text() mustBe messages(specialFiscalTerritory)
    }

    "display 'Back' button" when {
      "normal mode" in {
        val backButton = createView(mode = Mode.Normal).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe controllers.declaration.routes.DeclarationChoiceController.displayPage().url
      }

      "draft mode" in {
        val backButton = createView(mode = Mode.Draft).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Draft).url
      }

      "amend mode" in {
        val backButton = createView(mode = Mode.Amend).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Amend).url
      }
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() mustBe messages(saveAndReturnCaption)
      saveButton.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Dispatch Location View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(DispatchLocation.form().bind(Map[String, String]()))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("dispatchLocation", "#dispatchLocation")

      view.select("#error-message-dispatchLocation-input").text() mustBe messages(errorMessageEmpty)
    }

    "display error if incorrect dispatch is selected" in {

      val view = createView(DispatchLocation.form().fillAndValidate(DispatchLocation("12")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("dispatchLocation", "#dispatchLocation")

      view.select("#error-message-dispatchLocation-input").text() mustBe messages(errorMessageIncorrect)
    }
  }

  "Dispatch Location View when filled" should {

    "display selected first radio button - Outside (EX)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("EX")))

      val optionOne = view.getElementById("OutsideEU")
      optionOne.attr("checked") mustBe "checked"

      val optionTwo = view.getElementById("SpecialFiscalTerritory")
      optionTwo.attr("checked") mustBe empty
    }

    "display selected second radio button - Fiscal Territory (CO)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("CO")))

      val optionOne = view.getElementById("OutsideEU")
      optionOne.attr("checked") mustBe empty

      val optionTwo = view.getElementById("SpecialFiscalTerritory")
      optionTwo.attr("checked") mustBe "checked"
    }
  }
}
