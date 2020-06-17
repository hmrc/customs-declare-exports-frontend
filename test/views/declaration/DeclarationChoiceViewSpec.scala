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

package views.declaration

import base.Injector
import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfig
import forms.Choice
import forms.Choice.AllowedChoiceValues.CreateDec
import forms.declaration.DeclarationChoice
import helpers.views.declaration.CommonMessages
import models.{DeclarationType, Mode}
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
import views.html.components.gds.{errorSummary, saveAndContinue}
import views.html.declaration.declaration_choice
import views.tags.ViewTest

@ViewTest
class DeclarationChoiceViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationChoice] = DeclarationChoice.form()
  private val choicePage = instanceOf[declaration_choice]
  private def createView(form: Form[DeclarationChoice] = form, messages: Messages = stubMessages()): Document =
    choicePage(Mode.Normal, form)(request, messages)

  "Declaration Choice View on empty page" should {

    "display same page title as header" in {
      val viewWithMessage = createView(messages = validatedMessages(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display radio buttons with description (not selected)" in {

      val view = createView(DeclarationChoice.form())
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "SUPPLEMENTARY")
      ensureRadioIsUnChecked(view, "STANDARD")
      ensureRadioIsUnChecked(view, "SIMPLIFIED")
      ensureRadioIsUnChecked(view, "OCCASIONAL")
      ensureRadioIsUnChecked(view, "CLEARANCE")
    }

    "display 'Back' button that links to 'Choice' page" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages(backToSelectionCaption)
      backButton.getElementById("back-link") must haveHref(controllers.routes.ChoiceController.displayPage(Some(Choice(CreateDec))))
    }

    "display 'Continue' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages(continueCaption)
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(DeclarationChoice.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#type")

      view must containErrorElementWithMessage("declaration.type.error")
    }

    "display error when choice is incorrect" in {

      val view = createView(DeclarationChoice.form().bind(Map("type" -> "incorrect")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#type")

      view must containErrorElementWithMessage("declaration.type.error")
    }
  }

  "Choice View when filled" should {

    "display selected radio button - Create (SUPPLEMENTARY)" in {
      val view = createView(DeclarationChoice.form().fill(DeclarationChoice(DeclarationType.SUPPLEMENTARY)))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "SUPPLEMENTARY")
      ensureRadioIsUnChecked(view, "STANDARD")
      ensureRadioIsUnChecked(view, "SIMPLIFIED")
      ensureRadioIsUnChecked(view, "OCCASIONAL")
      ensureRadioIsUnChecked(view, "CLEARANCE")
    }

  }

  "Choice View for available declarations" should {

    "display choices that matches configuration" in {

      val config: Config =
        ConfigFactory.parseString("""
                                    |list-of-available-journeys="CRT"
                                    |list-of-available-declarations="STANDARD"
                                    |google-analytics.token=N/A
                                    |google-analytics.host=localhostGoogle
                                  """.stripMargin)

      val conf: Configuration = Configuration(config)
      val runMode: RunMode = new RunMode(conf, Test)
      val servicesConfig = new ServicesConfig(conf, runMode)
      val appConfig = new AppConfig(conf, Environment.simple(), servicesConfig, "AppName")

      val page = new declaration_choice(
        gdsMainTemplate,
        instanceOf[GovukButton],
        instanceOf[GovukRadios],
        instanceOf[errorSummary],
        instanceOf[saveAndContinue],
        instanceOf[FormWithCSRF],
        appConfig
      )

      val view = page(Mode.Normal, DeclarationChoice.form)(request, messages)

      view.getElementsByTag("label").size mustBe 1
      view.getElementsByAttributeValue("for", "STANDARD").text() mustBe "declaration.type.standard"

      // CEDS-2290 - "Or" divider should be filtered out if not configured
      view.getElementsByClass("govuk-radios__divider").size() mustBe (0)
    }
  }

  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 5
    view.getElementsByAttributeValue("for", "STANDARD").text() mustBe "declaration.type.standard"
    view.getElementsByAttributeValue("for", "SUPPLEMENTARY").text() mustBe "declaration.type.supplementary"
    view.getElementsByAttributeValue("for", "SIMPLIFIED").text() mustBe "declaration.type.simplified"
    view.getElementsByAttributeValue("for", "OCCASIONAL").text() mustBe "declaration.type.occasional"
    view.getElementsByAttributeValue("for", "CLEARANCE").text() mustBe "declaration.type.clearance"
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
