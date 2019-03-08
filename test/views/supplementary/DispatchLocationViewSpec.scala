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

import forms.supplementary.DispatchLocation
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.dispatch_location
import views.tags.ViewTest

@ViewTest
class DispatchLocationViewSpec extends ViewSpec {

  private val form: Form[DispatchLocation] = DispatchLocation.form()

  private val prefix = s"${basePrefix}dispatchLocation."

  private val formName = "dispatchLocation"
  private val header = Item(prefix, "header")
  private val hint = Item(prefix + "header.", "hint")
  private val outsideEu = Item(prefix + "inputText.", "outsideEU")
  private val specialFiscalTerritory = Item(prefix + "inputText.", "specialFiscalTerritory")
  private val errorMessageEmpty = Item(prefix + "inputText.", "error.empty")
  private val errorMessageIncorrect = Item(prefix + "inputText.", "error.incorrectValue")

  private def createView(form: Form[DispatchLocation] = form): Html =
    dispatch_location(appConfig, form)(fakeRequest, messages)

  "Dispatch Location View" should {

    "have proper messages for labels" in {

      assertMessage(header.withPrefix, "1/1 Where are the goods being dispatched to?")
      assertMessage(hint.withPrefix, "Hint text if needed here")
      assertMessage(outsideEu.withPrefix, "Outside the EU")
      assertMessage(
        specialFiscalTerritory.withPrefix,
        "Fiscal territory of the EU or country with which the EU has formed a customs union"
      )
    }

    "have proper messages for error labels" in {

      assertMessage(errorMessageEmpty.withPrefix, "Please, choose dispatch location")
      assertMessage(errorMessageIncorrect.withPrefix, "Please, choose valid dispatch location")
    }
  }

  "Dispatch Location View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be("Dispatch Location")
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(header.withPrefix))
      getElementByCss(view, "legend>span").text() must be(messages(hint.withPrefix))
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("")))

      val optionOne = getElementById(view, "OutsideEU")
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementByCss(view, "#dispatchLocation>div:nth-child(2)>label")
      optionOneLabel.text() must be(messages(outsideEu.withPrefix))

      val optionTwo = getElementById(view, "SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementByCss(view, "#dispatchLocation>div:nth-child(3)>label")
      optionTwoLabel.text() must be(messages(specialFiscalTerritory.withPrefix))
    }

    "display \"Back\" button that links to \"What do you want to do ?\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/choice")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Dispatch Location View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(DispatchLocation.form().withError(formName, messages(errorMessageEmpty.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageEmpty.withPrefix), "#dispatchLocation")

      getElementByCss(view, "#error-message-dispatchLocation-input").text() must be(
        messages(errorMessageEmpty.withPrefix)
      )
    }
  }

  "Dispatch Location View when filled" should {

    "display selected first radio button - Outside (EX)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("EX")))

      val optionOne = getElementById(view, "OutsideEU")
      optionOne.attr("checked") must be("checked")

      val optionTwo = getElementById(view, "SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("")
    }

    "display selected second radio button - Fiscal Territory (CO)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("CO")))

      val optionOne = getElementById(view, "OutsideEU")
      optionOne.attr("checked") must be("")

      val optionTwo = getElementById(view, "SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("checked")
    }
  }
}
