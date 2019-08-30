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

import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfig
import forms.Choice
import helpers.views.declaration.{ChoiceMessages, CommonMessages}
import play.api.Mode.Test
import play.api.data.Form
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import views.declaration.spec.AppViewSpec
import views.html.{choice_page, main_template}
import views.tags.ViewTest

import scala.collection.JavaConversions._

@ViewTest
class ChoiceViewSpec extends AppViewSpec with ChoiceMessages with CommonMessages {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = app.injector.instanceOf[choice_page]
  private def createView(form: Form[Choice] = form): Html = choicePage(form)

  "Choice View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(title))
    }

    "display header with hint" in {

      createView().select("legend>h1").text() must be(messages(title))
    }

    "display four radio buttons with description (not selected)" in {

      val view = createView(Choice.form().fill(Choice("")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display only Supplementary radio button with description" in {

      val supplementaryConfig: Config =
        ConfigFactory.parseString("""
        |list-of-available-journeys="SMP"
        |google-analytics.token=N/A
        |google-analytics.host=localhostGoogle
      """.stripMargin)

      val conf: Configuration = Configuration(supplementaryConfig)
      val runMode: RunMode = new RunMode(conf, Test)
      val servicesConfig = new ServicesConfig(conf, runMode)
      val supplementaryAppConfig = new AppConfig(conf, Environment.simple(), servicesConfig, "AppName")

      val mainTemplate = app.injector.instanceOf[main_template]

      val page = new choice_page(mainTemplate, supplementaryAppConfig)

      val view = page(Choice.form().fill(Choice("SMP")))(messages = messages, request = fakeRequest)
      ensureSupplementaryLabelIsCorrect(view)

      ensureRadioIsChecked(view, "Supplementary declaration")
    }

    "display 'Back' button that links to 'Make an export declaration' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(controllers.routes.StartController.displayStartPage().url)
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(Choice.form().bind(Map[String, String]()))

      view.select("#error-message-value-input").text() must be(messages(choiceEmpty))
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("value" -> "incorrect")))

      view.select("#error-message-value-input").text() must be(messages(choiceError))
    }
  }

  "Choice View when filled" should {

    "display selected radio button - Supplementary (SMP)" in {
      val view = createView(Choice.form().fill(Choice("SMP")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Standard (STD)" in {
      val view = createView(Choice.form().fill(Choice("STD")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Cancel declaration (CAN)" in {
      val view = createView(Choice.form().fill(Choice("CAN")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - View recent declarations (SUB)" in {
      val view = createView(Choice.form().fill(Choice("SUB")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Contimue saved declaration (Con)" in {
      val view = createView(Choice.form().fill(Choice("CON")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsChecked(view, "Continue declaration")
    }
  }
  private def ensureAllLabelTextIsCorrect(view: Html): Unit = {
    val labels = view.getElementsByTag("label").toList
    labels.forall(elems => elems.getElementsContainingText(messages(supplementaryDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(standardDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(cancelDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(recentDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(continueDec)).isEmpty) must be(false)
  }

  private def ensureSupplementaryLabelIsCorrect(view: Html): Unit = {
    val labels = view.getElementsByTag("label").toList
    labels.forall(elems => elems.getElementsContainingText(messages(supplementaryDec)).isEmpty) must be(false)
    labels.forall(elems => elems.getElementsContainingText(messages(standardDec)).isEmpty) must be(true)
    labels.forall(elems => elems.getElementsContainingText(messages(cancelDec)).isEmpty) must be(true)
    labels.forall(elems => elems.getElementsContainingText(messages(recentDec)).isEmpty) must be(true)
  }

  private def ensureRadioIsChecked(view: Html, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") must be("checked")
  }

  private def ensureRadioIsUnChecked(view: Html, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") must be("")
  }
}
