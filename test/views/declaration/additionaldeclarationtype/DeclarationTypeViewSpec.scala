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
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec.AllowedAdditionalDeclarationTypes._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes._
import forms.declaration.additionaldeclarationtype.{
  AdditionalDeclarationType,
  AdditionalDeclarationTypeStandardDec,
  AdditionalDeclarationTypeSupplementaryDec
}
import helpers.views.declaration.{CommonMessages, DeclarationTypeMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additionaldeclarationtype.declaration_type
import views.tags.ViewTest

@ViewTest
class DeclarationTypeViewSpec
    extends UnitViewSpec with ExportsTestData with DeclarationTypeMessages with CommonMessages with Stubs
    with Injector {

  private val formStandard: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeStandardDec.form()
  private val formSupplementary: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeSupplementaryDec.form()
  private val declarationTypePage = new declaration_type(mainTemplate)
  private def createView(form: Form[AdditionalDeclarationType], journeyType: String): Document =
    declarationTypePage(Mode.Normal, form)(journeyRequest(journeyType), messages)

  "Declaration Type View on empty page" should {

    "display page title" when {

      "used for Standard Declaration journey" in {

        createView(formStandard, StandardDec).select("title").text() mustBe messages(title)
      }

      "used for Supplementary Declaration journey" in {

        createView(formSupplementary, SupplementaryDec).select("title").text() mustBe messages(title)
      }
    }

    "display 'Back' button that links to 'Dispatch Location' page" when {

      "used for Standard Declaration journey" in {
        val backButton = createView(formStandard, StandardDec).getElementById("link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DispatchLocationController.displayPage().url
      }

      "used for Supplementary Declaration journey" in {

        val backButton = createView(formSupplementary, SupplementaryDec).getElementById("link-back")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DispatchLocationController.displayPage().url
      }
    }

    "display 'Save and continue' button" when {

      "used for Standard Declaration journey" in {
        val view: Document = createView(formStandard, StandardDec)
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "used for Supplementary Declaration journey" in {
        val view: Document = createView(formSupplementary, SupplementaryDec)
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }
    }

    "display 'Save and return' button" when {

      "used for Standard Declaration journey" in {
        val view: Document = createView(formStandard, StandardDec)
        val button = view.getElementById("submit_and_return")
        button.text() mustBe messages(saveAndReturnCaption)
        button must haveAttribute("name", SaveAndReturn.toString)
      }

      "used for Supplementary Declaration journey" in {
        val view: Document = createView(formSupplementary, SupplementaryDec)
        val button = view.getElementById("submit_and_return")
        button.text() mustBe messages(saveAndReturnCaption)
        button must haveAttribute("name", SaveAndReturn.toString)
      }
    }

    "display header with hint" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard, StandardDec)

        view.getElementById("title").text() mustBe messages(headerStandardDec)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary, SupplementaryDec)

        view.getElementById("title").text() mustBe messages(headerSupplementaryDec)
      }
    }

    "display two radio buttons with description (not selected)" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType("")), StandardDec)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe ""

        val optionOneLabel = view.getElementById("PreLodged-label")
        optionOneLabel.text() mustBe messages(preLodged)

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe ""

        val optionTwoLabel = view.getElementById("Frontier-label")
        optionTwoLabel.text() mustBe messages(frontier)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType("")), SupplementaryDec)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe ""

        val optionOneLabel = view.select("#additionalDeclarationType>div:nth-child(2)>label")
        optionOneLabel.text() mustBe messages(simplified)

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe ""

        val optionTwoLabel = view.select("#additionalDeclarationType>div:nth-child(3)>label")
        optionTwoLabel.text() mustBe messages(standard)
      }
    }

  }

  "Declaration Type View with invalid input" should {

    "display error if nothing is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.bind(Map[String, String]()), StandardDec)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageEmpty)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.bind(Map[String, String]()), SupplementaryDec)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageEmpty)
      }
    }

    "display error if incorrect declaration is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.fillAndValidate(AdditionalDeclarationType("#")), StandardDec)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageIncorrect)
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.fillAndValidate(AdditionalDeclarationType("#")), SupplementaryDec)

        checkErrorsSummary(view)
        view must haveFieldErrorLink("additionalDeclarationType", "#additionalDeclarationType")

        view.select("#error-message-additionalDeclarationType-input").text() mustBe messages(errorMessageIncorrect)
      }
    }

  }

  "Declaration Type View with entered data" should {

    "display selected first radio button" when {

      "used for Standard Declaration journey - Pre-lodged (D)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType(PreLodged)), StandardDec)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe "checked"

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe ""
      }

      "used for Supplementary Declaration journey - Simplified (Y)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType(Simplified)), SupplementaryDec)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe "checked"

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe ""
      }
    }

    "display selected second radio button" when {

      "used for Standard Declaration journey - Frontier (A)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType(Frontier)), StandardDec)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe ""

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe "checked"
      }

      "used for Supplementary Declaration journey - Standard (Z)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType(Standard)), SupplementaryDec)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe ""

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe "checked"
      }
    }

  }

}
