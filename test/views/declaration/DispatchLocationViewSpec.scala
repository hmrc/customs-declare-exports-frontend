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
import forms.declaration.DispatchLocation
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.dispatch_location
import views.tags.ViewTest

@ViewTest
class DispatchLocationViewSpec extends UnitViewSpec2 with CommonMessages with Stubs with Injector {

  private val form: Form[DispatchLocation] = DispatchLocation.form()
  private val dispatchLocationPage = instanceOf[dispatch_location]
  private def createView(form: Form[DispatchLocation] = form, mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    dispatchLocationPage(mode, form)(request, messages)

  "Dispatch Location" should {

    "have correct message keys" in {
      messages must haveTranslationFor("supplementary.dispatchLocation.header")
      messages must haveTranslationFor("supplementary.dispatchLocation.header.hint")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.outsideEU")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.specialFiscalTerritory")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.error.empty")
      messages must haveTranslationFor("supplementary.dispatchLocation.inputText.error.incorrect")
    }
  }

  "Dispatch Location View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display same page title as header" in {
        val viewWithMessage = createView()
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include(messages("declaration.summary.locations.header"))
      }

      "display two radio buttons with description (not selected)" in {

        val view = createView(DispatchLocation.form().fill(DispatchLocation("")))

        val optionOne = view.getElementsByAttributeValue("for", "OutsideEU")
        optionOne.attr("checked") mustBe empty
        optionOne.text() mustBe messages("supplementary.dispatchLocation.inputText.outsideEU")

        val optionTwo = view.getElementsByAttributeValue("for", "SpecialFiscalTerritory")
        optionTwo.attr("checked") mustBe empty
        optionTwo.text() mustBe messages("supplementary.dispatchLocation.inputText.specialFiscalTerritory")
      }

      "display 'Back' button" when {
        "normal mode" in {
          val backButton = createView(mode = Mode.Normal).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe controllers.declaration.routes.DeclarationChoiceController.displayPage().url
        }

        "change mode" in {
          val backButton = createView(mode = Mode.Change).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal).url
        }

        "draft mode" in {
          val backButton = createView(mode = Mode.Draft).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Draft).url
        }

        "amend mode" in {
          val backButton = createView(mode = Mode.Amend).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe controllers.declaration.routes.DeclarationChoiceController.displayPage(Mode.Amend).url
        }

        "change amend mode" in {
          val backButton = createView(mode = Mode.ChangeAmend).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Amend).url
        }
      }

      "display 'Continue' button" in {
        val saveButton = createView().getElementById("submit")
        saveButton.text() mustBe messages(continueCaption)
      }

      "not display 'Save and return' button" in {
        val saveButton = createView().getElementById("submit_and_return")
        saveButton mustBe null
      }
    }
  }

  "Dispatch Location View for invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error if nothing is selected" in {

        val view = createView(DispatchLocation.form().bind(Map[String, String]()))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#dispatchLocation")

        view must containErrorElementWithMessageKey("supplementary.dispatchLocation.inputText.error.empty")
      }

      "display error if incorrect dispatch is selected" in {

        val view = createView(DispatchLocation.form().fillAndValidate(DispatchLocation("12")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#dispatchLocation")

        view must containErrorElementWithMessageKey("supplementary.dispatchLocation.inputText.error.incorrect")
      }
    }
  }

  "Dispatch Location View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display selected first radio button - Outside (EX)" in {

        val view = createView(DispatchLocation.form().fill(DispatchLocation("EX")))

        val optionOne = view.getElementById("OutsideEU")
        optionOne.getElementsByAttribute("checked").size() mustBe 1

        val optionTwo = view.getElementById("SpecialFiscalTerritory")
        optionTwo.attr("checked") mustBe empty
      }

      "display selected second radio button - Fiscal Territory (CO)" in {

        val view = createView(DispatchLocation.form().fill(DispatchLocation("CO")))

        val optionOne = view.getElementById("OutsideEU")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("SpecialFiscalTerritory")
        optionTwo.getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }
}
