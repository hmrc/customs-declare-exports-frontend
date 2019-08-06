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

import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec.AllowedAdditionalDeclarationTypes._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes._
import forms.declaration.additionaldeclarationtype.{
  AdditionalDeclarationType,
  AdditionalDeclarationTypeStandardDec,
  AdditionalDeclarationTypeSupplementaryDec
}
import helpers.views.declaration.{CommonMessages, DeclarationTypeMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.additionaldeclarationtype.declaration_type
import views.tags.ViewTest

@ViewTest
class DeclarationTypeViewSpec extends ViewSpec with DeclarationTypeMessages with CommonMessages {

  private val formStandard: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeStandardDec.form()
  private val formSupplementary: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeSupplementaryDec.form()
  private val declarationTypePage = app.injector.instanceOf[declaration_type]
  private def createView(form: Form[AdditionalDeclarationType], journeyType: String): Html =
    declarationTypePage(form)(fakeJourneyRequest(journeyType), messages)

  "Declaration Type View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Declaration Type")
      assertMessage(headerSupplementaryDec, "1/2 Which of the following best describes you?")
      assertMessage(headerStandardDec, "1/2 Which of the following best describes you?")
      assertMessage(hint, "Hint text if needed here")
      assertMessage(
        simplified,
        "I am completing a supplementary declaration for goods that have been under a Simplified Declaration procedure (SDP)"
      )
      assertMessage(
        standard,
        "I am completing a supplementary declaration for goods that have been under EIDR procedures"
      )
      assertMessage(preLodged, "Pre-lodged")
      assertMessage(frontier, "Frontier")
    }

    "have proper messages for error labels" in {

      assertMessage(errorMessageEmpty, "Please, choose declaration type")
      assertMessage(errorMessageIncorrect, "Please, choose valid declaration type")
    }
  }

  "Declaration Type View on empty page" should {

    "display page title" when {

      "used for Standard Declaration journey" in {

        getElementByCss(createView(formStandard, StandardDec), "title").text() must be(messages(title))
      }

      "used for Supplementary Declaration journey" in {

        getElementByCss(createView(formSupplementary, SupplementaryDec), "title").text() must be(messages(title))
      }
    }

    "display 'Back' button that links to 'Dispatch Location' page" when {

      "used for Standard Declaration journey" in {
        val backButton = getElementById(createView(formStandard, StandardDec), "link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be("/customs-declare-exports/declaration/dispatch-location")

      }

      "used for Supplementary Declaration journey" in {

        val backButton = getElementById(createView(formSupplementary, SupplementaryDec), "link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be("/customs-declare-exports/declaration/dispatch-location")

      }
    }

    "display 'Save and continue' button" when {

      "used for Standard Declaration journey" in {

        getElementByCss(createView(formStandard, StandardDec), "#submit").text() must be(
          messages(saveAndContinueCaption)
        )
      }

      "used for Supplementary Declaration journey" in {

        getElementByCss(createView(formSupplementary, SupplementaryDec), "#submit").text() must be(
          messages(saveAndContinueCaption)
        )
      }
    }

    "display header with hint" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard, StandardDec)

        getElementByCss(view, "legend>h1").text() must be(messages(headerStandardDec))
        getElementByCss(view, "legend>span").text() must be(messages(hint))
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary, SupplementaryDec)

        getElementByCss(view, "legend>h1").text() must be(messages(headerSupplementaryDec))
      }
    }

    "display two radio buttons with description (not selected)" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType("")), StandardDec)

        val optionOne = getElementById(view, "PreLodged")
        optionOne.attr("checked") must be("")

        val optionOneLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(2)>label")
        optionOneLabel.text() must be(messages(preLodged))

        val optionTwo = getElementById(view, "Frontier")
        optionTwo.attr("checked") must be("")

        val optionTwoLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(3)>label")
        optionTwoLabel.text() must be(messages(frontier))
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType("")), SupplementaryDec)

        val optionOne = getElementById(view, "Simplified")
        optionOne.attr("checked") must be("")

        val optionOneLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(2)>label")
        optionOneLabel.text() must be(messages(simplified))

        val optionTwo = getElementById(view, "Standard")
        optionTwo.attr("checked") must be("")

        val optionTwoLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(3)>label")
        optionTwoLabel.text() must be(messages(standard))
      }
    }

  }

  "Declaration Type View with invalid input" should {

    "display error if nothing is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.bind(Map[String, String]()), StandardDec)

        checkErrorsSummary(view)
        checkErrorLink(view, 1, messages(errorMessageEmpty), "#additionalDeclarationType")

        getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(
          messages(errorMessageEmpty)
        )
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.bind(Map[String, String]()), SupplementaryDec)

        checkErrorsSummary(view)
        checkErrorLink(view, 1, messages(errorMessageEmpty), "#additionalDeclarationType")

        getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(
          messages(errorMessageEmpty)
        )
      }
    }

    "display error if incorrect declaration is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.fillAndValidate(AdditionalDeclarationType("#")), StandardDec)

        checkErrorsSummary(view)
        checkErrorLink(view, 1, messages(errorMessageIncorrect), "#additionalDeclarationType")

        getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(
          messages(errorMessageIncorrect)
        )
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.fillAndValidate(AdditionalDeclarationType("#")), SupplementaryDec)

        checkErrorsSummary(view)
        checkErrorLink(view, 1, messages(errorMessageIncorrect), "#additionalDeclarationType")

        getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(
          messages(errorMessageIncorrect)
        )
      }
    }

  }

  "Declaration Type View with entered data" should {

    "display selected first radio button" when {

      "used for Standard Declaration journey - Pre-lodged (D)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType(PreLodged)), StandardDec)

        val optionOne = getElementById(view, "PreLodged")
        optionOne.attr("checked") must be("checked")

        val optionTwo = getElementById(view, "Frontier")
        optionTwo.attr("checked") must be("")
      }

      "used for Supplementary Declaration journey - Simplified (Y)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType(Simplified)), SupplementaryDec)

        val optionOne = getElementById(view, "Simplified")
        optionOne.attr("checked") must be("checked")

        val optionTwo = getElementById(view, "Standard")
        optionTwo.attr("checked") must be("")
      }
    }

    "display selected second radio button" when {

      "used for Standard Declaration journey - Frontier (A)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType(Frontier)), StandardDec)

        val optionOne = getElementById(view, "PreLodged")
        optionOne.attr("checked") must be("")

        val optionTwo = getElementById(view, "Frontier")
        optionTwo.attr("checked") must be("checked")
      }

      "used for Supplementary Declaration journey - Standard (Z)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType(Standard)), SupplementaryDec)

        val optionOne = getElementById(view, "Simplified")
        optionOne.attr("checked") must be("")

        val optionTwo = getElementById(view, "Standard")
        optionTwo.attr("checked") must be("checked")
      }
    }

  }

}
