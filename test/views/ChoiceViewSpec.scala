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

package views

import forms.Choice
import helpers.views.declaration.{ChoiceMessages, CommonMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.choice_page
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends ViewSpec with ChoiceMessages with CommonMessages {

  private val form: Form[Choice] = Choice.form()
  private def createView(form: Form[Choice] = form): Html = choice_page(appConfig, form)

  "Choice View" should {

    "have proper labels for messages" in {

      assertMessage(title, "What do you want to do?")
      assertMessage(supplementaryDec, "Supplementary declaration")
      assertMessage(standardDec, "Standard declaration")
      assertMessage(arrivalDec, "Arrival")
      assertMessage(departureDec, "Departure")
      assertMessage(cancelDec, "Cancel declaration")
      assertMessage(recentDec, "View recent declarations")
    }

    "have proper labels for error messages" in {

      assertMessage(choiceEmpty, "Please, choose what do you want to do")
      assertMessage(choiceError, "Please, choose valid option")
    }
  }

  "Choice View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header with hint" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display six radio buttons with description (not selected)" in {

      val view = createView(Choice.form().fill(Choice("")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }

    "display \"Back\" button that links to \"Make an export declaration\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("start")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(Choice.form().bind(Map[String, String]()))

      getElementByCss(view, "#error-message-choice-input").text() must be(messages(choiceEmpty))
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("choice" -> "incorrect")))

      getElementByCss(view, "#error-message-choice-input").text() must be(messages(choiceError))
    }
  }

  "Choice View when filled" should {

    "display selected first radio button - Supplementary (SMP)" in {

      val view = createView(Choice.form().fill(Choice("SMP")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("checked")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }

    "display selected second radio button - Standard (STD)" in {

      val view = createView(Choice.form().fill(Choice("STD")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("checked")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }

    "display selected third radio button - Arrival (EAL)" in {

      val view = createView(Choice.form().fill(Choice("EAL")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("checked")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }

    "display selected fourth radio button - Departure (EDL)" in {

      val view = createView(Choice.form().fill(Choice("EDL")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("checked")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }

    "display selected fifth radio button - Cancel declaration (CAN)" in {

      val view = createView(Choice.form().fill(Choice("CAN")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("checked")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }

    "display selected sixth radio button - View recent declarations (SUB)" in {

      val view = createView(Choice.form().fill(Choice("SUB")))

      val optionOne = getElementById(view, "Supplementary declaration")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be(messages(supplementaryDec))

      val optionTwo = getElementById(view, "Standard declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be(messages(standardDec))

      val optionThree = getElementById(view, "Arrival")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be(messages(arrivalDec))

      val optionFour = getElementById(view, "Departure")
      optionFour.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be(messages(departureDec))

      val optionFive = getElementById(view, "Cancel declaration")
      optionFive.attr("checked") must be("")

      getElementByCss(view, "#choice>div:nth-child(6)>label").text() must be(messages(cancelDec))

      val optionSix = getElementById(view, "Submissions")
      optionSix.attr("checked") must be("checked")

      getElementByCss(view, "#choice>div:nth-child(7)>label").text() must be(messages(recentDec))
    }
  }
}
