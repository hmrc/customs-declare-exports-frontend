/*
 * Copyright 2020 HM Revenue & Customs
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
import helpers.views.declaration.CommonMessages
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.Mode.Test
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.api.{Configuration, Environment}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukButton, GovukRadios}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.views.html.helpers.FormWithCSRF
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.choice_page
import views.html.components.gds.{errorSummary, saveAndContinue}
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = instanceOf[choice_page]
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

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display only Create radio button with description" in {

      val config: Config =
        ConfigFactory.parseString("""
                                    |list-of-available-journeys="CRT"
                                    |google-analytics.token=N/A
                                    |google-analytics.host=localhostGoogle
                                  """.stripMargin)

      val conf: Configuration = Configuration(config)
      val runMode: RunMode = new RunMode(conf, Test)
      val servicesConfig = new ServicesConfig(conf, runMode)
      val appConfig = new AppConfig(conf, Environment.simple(), servicesConfig, "AppName")

      val page = new choice_page(
        gdsMainTemplate,
        instanceOf[GovukButton],
        instanceOf[GovukRadios],
        instanceOf[errorSummary],
        instanceOf[saveAndContinue],
        instanceOf[FormWithCSRF],
        appConfig
      )

      val view = page(Choice.form().fill(Choice("CRT")))(request, messages)
      ensureCreateLabelIsCorrect(view)

      ensureRadioIsChecked(view, "CRT")
    }

    "display 'Back' button that links to 'Make an export declaration' page" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages(backCaption)
      backButton.getElementById("back-link") must haveHref(controllers.routes.StartController.displayStartPage())
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.getElementsByClass("govuk-button")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#value")

      view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("value" -> "incorrect")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#value")

      view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.incorrectValue")
    }
  }

  "Choice View when filled" should {

    "display selected radio button - Create (CRT)" in {
      val view = createView(Choice.form().fill(Choice("CRT")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display selected radio button - CAN (CAN)" in {
      val view = createView(Choice.form().fill(Choice("CAN")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display selected radio button - View recent declarations (SUB)" in {
      val view = createView(Choice.form().fill(Choice("SUB")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display selected radio button - Continue saved declaration (Con)" in {
      val view = createView(Choice.form().fill(Choice("CON")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsChecked(view, "CON")
    }
  }
  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 4
    view.getElementsByAttributeValue("for", "CRT").text() mustBe "declaration.choice.CRT"
    view.getElementsByAttributeValue("for", "SUB").text() mustBe "declaration.choice.SUB"
    view.getElementsByAttributeValue("for", "CAN").text() mustBe "declaration.choice.CAN"
    view.getElementsByAttributeValue("for", "CON").text() mustBe "declaration.choice.CON"
  }

  private def ensureCreateLabelIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 1
    view.getElementsByAttributeValue("for", "CRT").text() mustBe "declaration.choice.CRT"
  }

  private def ensureRadioIsChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId).getElementsByAttribute("checked")
    option.size() mustBe 1
  }

  private def ensureRadioIsUnChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe empty
  }
}
