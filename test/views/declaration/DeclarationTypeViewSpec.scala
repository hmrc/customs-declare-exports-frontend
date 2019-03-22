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

import forms.declaration.AdditionalDeclarationType
import helpers.views.declaration.{CommonMessages, DeclarationTypeMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.declaration.declaration_type
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class DeclarationTypeViewSpec extends ViewSpec with DeclarationTypeMessages with CommonMessages {

  private val form: Form[AdditionalDeclarationType] = AdditionalDeclarationType.form()
  private def createView(form: Form[AdditionalDeclarationType] = form): Html =
    declaration_type(appConfig, form)(fakeRequest, messages)

  "Declaration Type View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Declaration Type")
      assertMessage(header, "1/2 What type of declaration are you making?")
      assertMessage(hint, "Hint text if needed here")
      assertMessage(simplified, "Pre-authorised simplified declaration")
      assertMessage(standard, "Entry in Declarants Records (EIDR)")
    }

    "have proper messages for error labels" in {

      assertMessage(errorMessageEmpty, "Please, choose declaration type")
      assertMessage(errorMessageIncorrect, "Please, choose valid declaration type")
    }
  }

  "Declaration Type View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(header))
      getElementByCss(view, "legend>span").text() must be(messages(hint))
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(AdditionalDeclarationType.form().fill(AdditionalDeclarationType("")))

      val optionOne = getElementById(view, "Simplified")
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(2)>label")
      optionOneLabel.text() must be(messages(simplified))

      val optionTwo = getElementById(view, "Standard")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(3)>label")
      optionTwoLabel.text() must be(messages(standard))
    }

    "display \"Back\" button that links to \"Dispatch Location\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/dispatch-location")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Declaration Type View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(AdditionalDeclarationType.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageEmpty), "#additionalDeclarationType")

      getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(
        messages(errorMessageEmpty)
      )
    }

    "display error if incorrect declaration is selected" in {

      val view = createView(AdditionalDeclarationType.form().fillAndValidate(AdditionalDeclarationType("X")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageIncorrect), "#additionalDeclarationType")

      getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(
        messages(errorMessageIncorrect)
      )
    }
  }

  "Declaration Type View when filled" should {

    "display selected first radio button - Simplified (Y)" in {

      val view = createView(AdditionalDeclarationType.form().fill(AdditionalDeclarationType("Y")))

      val optionOne = getElementById(view, "Simplified")
      optionOne.attr("checked") must be("checked")

      val optionTwo = getElementById(view, "Standard")
      optionTwo.attr("checked") must be("")
    }

    "display selected second radio button - Standard (Z)" in {

      val view = createView(AdditionalDeclarationType.form().fill(AdditionalDeclarationType("Z")))

      val optionOne = getElementById(view, "Simplified")
      optionOne.attr("checked") must be("")

      val optionTwo = getElementById(view, "Standard")
      optionTwo.attr("checked") must be("checked")
    }
  }
}
