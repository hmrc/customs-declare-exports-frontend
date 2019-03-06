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

package views.supplementary

import forms.supplementary.AdditionalDeclarationType
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.declaration_type
import views.tags.ViewTest

@ViewTest
class DeclarationTypeViewSpec extends ViewSpec {

  private val form: Form[AdditionalDeclarationType] = AdditionalDeclarationType.form()

  private val prefix = s"${basePrefix}declarationType."
  private val formName = "additionalDeclarationType"

  private val title = Item(prefix, "title")
  private val header = Item(prefix, "header")
  private val hint = Item(prefix + "header.", "hint")
  private val simplified = Item(prefix + "inputText.", "simplified")
  private val standard = Item(prefix + "inputText.", "standard")
  private val errorMessage = Item(prefix + "inputText.", "errorMessage")

  private def createView(form: Form[AdditionalDeclarationType] = form): Html = declaration_type(appConfig, form)(fakeRequest, messages)

  "Declaration Type View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "Declaration Type")
      assertMessage(header.withPrefix, "1/2 What type of declaration are you making?")
      assertMessage(hint.withPrefix, "Hint text if needed here")
      assertMessage(simplified.withPrefix, "Pre-authorized simplified declaration")
      assertMessage(standard.withPrefix, "Entry in Declarants Records (EIDR)")
    }

    "have proper messages for error labels" in {

      assertMessage(errorMessage.withPrefix, "Please, choose declaration type")
    }
  }

  "Declaration Type View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title.withPrefix))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(header.withPrefix))
      getElementByCss(view, "legend>span").text() must be(messages(hint.withPrefix))
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(AdditionalDeclarationType.form().fill(AdditionalDeclarationType("")))

      val optionOne = getElementById(view, "Simplified")
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(2)>label")
      optionOneLabel.text() must be(messages(simplified.withPrefix))

      val optionTwo = getElementById(view, "Standard")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementByCss(view, "#additionalDeclarationType>div:nth-child(3)>label")
      optionTwoLabel.text() must be(messages(standard.withPrefix))
    }

    "display \"Back\" button that links to \"Dispatch Location\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/dispatch-location")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Declaration Type View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(AdditionalDeclarationType.form().withError(formName, messages(errorMessage.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessage.withPrefix), "#additionalDeclarationType")

      getElementByCss(view, "#error-message-additionalDeclarationType-input").text() must be(messages(errorMessage.withPrefix))
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
