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
import org.jsoup.nodes.Element
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.choice_page
import views.tags.ViewTest

import scala.collection.immutable

@ViewTest
class ChoiceViewSpec extends ViewSpec with ChoiceMessages with CommonMessages {

  private val form: Form[Choice] = Choice.form()
  private def createView(form: Form[Choice] = form): Html = choice_page(appConfig, form)

  "Choice View" should {

    "have proper labels for messages" in {

      assertMessage(title, "What do you want to do?")
      assertMessage(supplementaryDec, "Supplementary declaration")
      assertMessage(standardDec, "Standard declaration")
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

    "display four radio buttons with description (not selected)" in {

      val view = createView(Choice.form().fill(Choice("")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
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

      getElementByCss(view, "#error-message-value-input").text() must be(messages(choiceEmpty))
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("value" -> "incorrect")))

      getElementByCss(view, "#error-message-value-input").text() must be(messages(choiceError))
    }
  }

  "Choice View when filled" should {

    "display selected first radio button - Supplementary (SMP)" in {
      val view = createView(Choice.form().fill(Choice("SMP")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")

    }

    "display selected second radio button - Standard (STD)" in {
      val view = createView(Choice.form().fill(Choice("STD")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")

    }

    "display selected fifth radio button - Cancel declaration (CAN)" in {
      val view = createView(Choice.form().fill(Choice("CAN")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
    }

    "display selected sixth radio button - View recent declarations (SUB)" in {
      val view = createView(Choice.form().fill(Choice("SUB")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsChecked(view, "Submissions")
    }
  }
  private def ensureAllLabelTextIsCorrect(view: Html): Unit = {
    val labels: immutable.Seq[Element] = getElementsByTag(view, "label")
    labels.forall(elems => elems.getElementsContainingText(messages(supplementaryDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(standardDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(cancelDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(recentDec)).isEmpty) must be(false)
  }

  private def ensureRadioIsChecked(view: Html, elementId: String): Unit = {
    val option = getElementById(view, elementId)
    option.attr("checked") must be("checked")
  }

  private def ensureRadioIsUnChecked(view: Html, elementId: String): Unit = {
    val option = getElementById(view, elementId)
    option.attr("checked") must be("")
  }
}
