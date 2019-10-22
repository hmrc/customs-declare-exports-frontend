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

import base.Injector
import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfig
import forms.Choice
import helpers.views.declaration.{ChoiceMessages, CommonMessages}
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.Mode.Test
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.choice_page
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with ChoiceMessages with CommonMessages with Stubs with Injector {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = new choice_page(mainTemplate, instanceOf[AppConfig])
  private def createView(form: Form[Choice] = form, messages: Messages = stubMessages()): Document =
    choicePage(form)(request, messages)

  "Choice View on empty page" should {

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display radio buttons with description (not selected)" in {

      val view = createView(Choice.form().fill(Choice("")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Simplified declaration")
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

      val page = new choice_page(mainTemplate, supplementaryAppConfig)

      val view = page(Choice.form().fill(Choice("SMP")))(request, messages)
      ensureSupplementaryLabelIsCorrect(view)

      ensureRadioIsChecked(view, "Supplementary declaration")
    }

    "display 'Back' button that links to 'Make an export declaration' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.getElementById("link-back") must haveHref(controllers.routes.StartController.displayStartPage())
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("value", "#value")

      view.select("#error-message-value-input").text() mustBe messages(choiceEmpty)
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("value" -> "incorrect")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("value", "#value")

      view.select("#error-message-value-input").text() mustBe messages(choiceError)
    }
  }

  "Choice View when filled" should {

    "display selected radio button - Supplementary (SMP)" in {
      val view = createView(Choice.form().fill(Choice("SMP")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Simplified declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Standard (STD)" in {
      val view = createView(Choice.form().fill(Choice("STD")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Simplified declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Simplified (SIM)" in {
      val view = createView(Choice.form().fill(Choice("SIM")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsChecked(view, "Simplified declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Cancel declaration (CAN)" in {
      val view = createView(Choice.form().fill(Choice("CAN")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Simplified declaration")
      ensureRadioIsChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - View recent declarations (SUB)" in {
      val view = createView(Choice.form().fill(Choice("SUB")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Simplified declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsChecked(view, "Submissions")
      ensureRadioIsUnChecked(view, "Continue declaration")
    }

    "display selected radio button - Continue saved declaration (Con)" in {
      val view = createView(Choice.form().fill(Choice("CON")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "Supplementary declaration")
      ensureRadioIsUnChecked(view, "Standard declaration")
      ensureRadioIsUnChecked(view, "Simplified declaration")
      ensureRadioIsUnChecked(view, "Cancel declaration")
      ensureRadioIsUnChecked(view, "Submissions")
      ensureRadioIsChecked(view, "Continue declaration")
    }
  }
  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 6
    view.getElementById("Standard declaration-label").text() mustBe standardDec
    view.getElementById("Supplementary declaration-label").text() mustBe supplementaryDec
    view.getElementById("Simplified declaration-label").text() mustBe simplifiedDec
    view.getElementById("Submissions-label").text() mustBe recentDec
    view.getElementById("Continue declaration-label").text() mustBe continueDec
    view.getElementById("Cancel declaration-label").text() mustBe cancelDec
  }

  private def ensureSupplementaryLabelIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 1
    view.getElementById("Supplementary declaration-label").text() mustBe supplementaryDec
  }

  private def ensureRadioIsChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe "checked"
  }

  private def ensureRadioIsUnChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe empty
  }
}
