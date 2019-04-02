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

import forms.declaration.DispatchLocation
import helpers.views.declaration.{CommonMessages, DispatchLocationMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.dispatch_location
import views.tags.ViewTest

@ViewTest
class DispatchLocationViewSpec extends ViewSpec with DispatchLocationMessages with CommonMessages {

  private val form: Form[DispatchLocation] = DispatchLocation.form()
  private def createView(form: Form[DispatchLocation] = form): Html =
    dispatch_location(form)(appConfig, fakeRequest, messages)

  "Dispatch Location View" should {

    "have proper messages for labels" in {

      assertMessage(header, "1/1 Where have the goods been sent?")
      assertMessage(hint, "Hint text if needed here")
      assertMessage(outsideEu, "Outside the EU")
      assertMessage(
        specialFiscalTerritory,
        "Special fiscal territory of the EU or a country with which the EU has formed a customs union"
      )
    }

    "have proper messages for error labels" in {

      assertMessage(errorMessageEmpty, "Please, choose dispatch location")
      assertMessage(errorMessageIncorrect, "Please, choose valid dispatch location")
    }
  }

  "Dispatch Location View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be("Dispatch Location")
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Locations")
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(header))
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(DispatchLocation.form().fill(DispatchLocation("")))

      val optionOne = getElementById(view, "OutsideEU")
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementByCss(view, "#dispatchLocation>div:nth-child(2)>label")
      optionOneLabel.text() must be(messages(outsideEu))

      val optionTwo = getElementById(view, "SpecialFiscalTerritory")
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementByCss(view, "#dispatchLocation>div:nth-child(3)>label")
      optionTwoLabel.text() must be(messages(specialFiscalTerritory))
    }

    "display \"Back\" button that links to \"What do you want to do ?\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/choice")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Dispatch Location View for invalid input" should {

    "display error if nothing is selected" in {

      val view = createView(DispatchLocation.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageEmpty), "#dispatchLocation")

      getElementByCss(view, "#error-message-dispatchLocation-input").text() must be(messages(errorMessageEmpty))
    }

    "display error if incorrect dispatch is selected" in {

      val view = createView(DispatchLocation.form().fillAndValidate(DispatchLocation("12")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(errorMessageIncorrect), "#dispatchLocation")

      getElementByCss(view, "#error-message-dispatchLocation-input").text() must be(messages(errorMessageIncorrect))
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
