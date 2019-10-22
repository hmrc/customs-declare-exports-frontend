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

package views.declaration.additionaldeclarationtype

import base.Injector
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.{
  AdditionalDeclarationType,
  AdditionalDeclarationTypeSimplifiedDec,
  AdditionalDeclarationTypeStandardDec,
  AdditionalDeclarationTypeSupplementaryDec
}
import helpers.views.declaration.{CommonMessages, DeclarationTypeMessages}
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additionaldeclarationtype.declaration_type
import views.tags.ViewTest

@ViewTest
class DeclarationTypeViewSpec extends UnitViewSpec with ExportsTestData with DeclarationTypeMessages with CommonMessages with Stubs with Injector {

  private val formStandard: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeStandardDec.form()
  private val formSupplementary: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeSupplementaryDec.form()
  private val formSimplified: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeSimplifiedDec.form()
  private val declarationTypePage = new declaration_type(mainTemplate)
  private def createView(form: Form[AdditionalDeclarationType], journeyType: DeclarationType, messages: Messages = stubMessages()): Document =
    declarationTypePage(Mode.Normal, form)(journeyRequest(journeyType), messages)

  "Declaration Type View on empty page" should {

    "display page title" when {

      "used for Standard Declaration journey" in {
        val viewWithMessage = createView(formStandard, DeclarationType.STANDARD, realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "used for Supplementary Declaration journey" in {
        val viewWithMessage = createView(formSupplementary, DeclarationType.SUPPLEMENTARY, realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "used for Simplified Declaration journey" in {
        val viewWithMessage = createView(formSimplified, DeclarationType.SIMPLIFIED, realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }
    }

    "display 'Back' button that links to 'Dispatch Location' page" when {

      "used for Standard Declaration journey" in {
        val backButton = createView(formStandard, DeclarationType.STANDARD).getElementById("link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DispatchLocationController.displayPage().url
      }

      "used for Supplementary Declaration journey" in {

        val backButton = createView(formSupplementary, DeclarationType.SUPPLEMENTARY).getElementById("link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DispatchLocationController.displayPage().url
      }

      "used for Simplified Declaration journey" in {
        val backButton = createView(formSimplified, DeclarationType.SIMPLIFIED).getElementById("link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DispatchLocationController.displayPage().url
      }
    }

    "display 'Save and continue' button" when {

      "used for Standard Declaration journey" in {
        val view: Document = createView(formStandard, DeclarationType.STANDARD)
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "used for Supplementary Declaration journey" in {
        val view: Document = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "used for Simplified Declaration journey" in {
        val view: Document = createView(formSimplified, DeclarationType.SIMPLIFIED)
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }
    }

    "display 'Save and return' button" when {

      "used for Standard Declaration journey" in {
        val view: Document = createView(formStandard, DeclarationType.STANDARD)
        val button = view.getElementById("submit_and_return")
        button.text() mustBe messages(saveAndReturnCaption)
        button must haveAttribute("name", SaveAndReturn.toString)
      }

      "used for Supplementary Declaration journey" in {
        val view: Document = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)
        val button = view.getElementById("submit_and_return")
        button.text() mustBe messages(saveAndReturnCaption)
        button must haveAttribute("name", SaveAndReturn.toString)
      }

      "used for Simplified Declaration journey" in {
        val view: Document = createView(formSimplified, DeclarationType.SIMPLIFIED)
        val button = view.getElementById("submit_and_return")
        button.text() mustBe messages(saveAndReturnCaption)
        button must haveAttribute("name", SaveAndReturn.toString)
      }
    }

    "display header with hint" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard, DeclarationType.STANDARD)

        view.getElementById("title").text() mustBe messages(headerStandardDec)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)

        view.getElementById("title").text() mustBe messages(headerSupplementaryDec)
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified, DeclarationType.SIMPLIFIED)

        view.getElementById("title").text() mustBe messages(headerSimplifiedDec)
      }
    }

    "display two radio buttons with description (not selected)" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard, DeclarationType.STANDARD)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionOneLabel = view.getElementById("PreLodged-label")
        optionOneLabel.text() mustBe messages(standardPreLodged)

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty

        val optionTwoLabel = view.getElementById("Frontier-label")
        optionTwoLabel.text() mustBe messages(standardFrontier)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe empty

        val optionOneLabel = view.select("#additionalDeclarationType>div:nth-child(2)>label")
        optionOneLabel.text() mustBe messages(supplementarySimplified)

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe empty

        val optionTwoLabel = view.select("#additionalDeclarationType>div:nth-child(3)>label")
        optionTwoLabel.text() mustBe messages(supplementaryStandard)
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified, DeclarationType.SIMPLIFIED)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionOneLabel = view.getElementById("PreLodged-label")
        optionOneLabel.text() mustBe messages(simplifiedPreLodged)

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty

        val optionTwoLabel = view.getElementById("Frontier-label")
        optionTwoLabel.text() mustBe messages(simplifiedFrontier)
      }
    }

  }

  "Declaration Type View with invalid input" should {

    "display error if nothing is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.bind(Map[String, String]()), DeclarationType.STANDARD)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageEmpty)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.bind(Map[String, String]()), DeclarationType.SUPPLEMENTARY)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageEmpty)
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified.bind(Map[String, String]()), DeclarationType.SIMPLIFIED)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageEmpty)
      }
    }

    "display error if incorrect declaration is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.STANDARD)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageIncorrect)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.SUPPLEMENTARY)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageIncorrect)
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.SIMPLIFIED)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageIncorrect)
      }
    }

  }

  "Declaration Type View with entered data" should {

    "display selected first radio button" when {

      "used for Standard Declaration journey - Pre-lodged (D)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType.STANDARD_PRE_LODGED), DeclarationType.STANDARD)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe "checked"

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty
      }

      "used for Supplementary Declaration journey - Simplified (Y)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED), DeclarationType.SUPPLEMENTARY)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe "checked"

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe empty
      }

      "used for Simplified Declaration journey - Pre-lodged (F)" in {

        val view = createView(formSimplified.fill(AdditionalDeclarationType.SIMPLIFIED_PRE_LODGED), DeclarationType.SIMPLIFIED)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe "checked"

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty
      }
    }

    "display selected second radio button" when {

      "used for Standard Declaration journey - Frontier (A)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType.STANDARD_FRONTIER), DeclarationType.STANDARD)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe "checked"
      }

      "used for Supplementary Declaration journey - Standard (Z)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType.SUPPLEMENTARY_EIDR), DeclarationType.SUPPLEMENTARY)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe "checked"
      }

      "used for Simplified Declaration journey - Frontier (C)" in {

        val view = createView(formSimplified.fill(AdditionalDeclarationType.SIMPLIFIED_FRONTIER), DeclarationType.SIMPLIFIED)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe "checked"
      }
    }

  }

}
