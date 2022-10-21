/*
 * Copyright 2022 HM Revenue & Customs
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
import config.{AppConfig, AppConfigSpec}
import forms.Choice
import forms.Choice.AllowedChoiceValues.CreateDec
import forms.declaration.DeclarationChoice
import models.DeclarationType
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.{Configuration, Environment}
import tools.Stubs
import uk.gov.hmrc.govukfrontend.views.html.components.{FormWithCSRF, GovukButton, GovukDetails, GovukRadios}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.components.gds.{errorSummary, exportsInsetText, externalLink, paragraphBody, saveAndContinue}
import views.html.declaration.declaration_choice
import views.tags.ViewTest

@ViewTest
class DeclarationChoiceViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationChoice] = DeclarationChoice.form
  private val choicePage = instanceOf[declaration_choice]
  private def createView(form: Form[DeclarationChoice] = form): Document =
    choicePage(form)(request, messages)

  "Declaration Choice View on empty page" should {

    "display same page title as header" in {
      val viewWithMessage = createView()
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display radio buttons with description (not selected)" in {
      val view = createView(DeclarationChoice.form)
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "SUPPLEMENTARY")
      ensureRadioIsUnChecked(view, "STANDARD")
      ensureRadioIsUnChecked(view, "SIMPLIFIED")
      ensureRadioIsUnChecked(view, "OCCASIONAL")
      ensureRadioIsUnChecked(view, "CLEARANCE")
    }

    "display 'Back' button that links to 'Choice' page" in {
      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages(backToPreviousQuestionCaption)
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
      val view = createView(DeclarationChoice.form.bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${DeclarationType.STANDARD.toString}")

      view must containErrorElementWithMessageKey("declaration.type.error")
    }

    "display error when choice is incorrect" in {
      val view = createView(DeclarationChoice.form.bind(Map("type" -> "incorrect")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${DeclarationType.STANDARD.toString}")

      view must containErrorElementWithMessageKey("declaration.type.error")
    }
  }

  "Choice View when filled" should {
    "display selected radio button - Create (SUPPLEMENTARY)" in {
      val view = createView(DeclarationChoice.form.fill(DeclarationChoice(DeclarationType.SUPPLEMENTARY)))
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
        ConfigFactory.parseString(AppConfigSpec.configBareMinimum + """
                                    |list-of-available-journeys="CRT"
                                    |list-of-available-declarations="STANDARD"
                                    |google-analytics.token=N/A
                                    |google-analytics.host=localhostGoogle
                                  """.stripMargin)

      val conf: Configuration = Configuration(config)
      val servicesConfig = new ServicesConfig(conf)
      val appConfig = new AppConfig(conf, Environment.simple(), servicesConfig, "AppName")

      val page = new declaration_choice(
        gdsMainTemplate,
        instanceOf[GovukDetails],
        instanceOf[GovukButton],
        instanceOf[GovukRadios],
        instanceOf[errorSummary],
        instanceOf[exportsInsetText],
        instanceOf[paragraphBody],
        instanceOf[externalLink],
        instanceOf[saveAndContinue],
        instanceOf[FormWithCSRF],
        appConfig
      )

      val view = page(DeclarationChoice.form)(request, messages)

      view.getElementsByTag("label").size mustBe 1
      view.getElementsByAttributeValue("for", "STANDARD") must containMessageForElements("declaration.type.standard")
      view.getElementsByClass("govuk-radios__divider").size() mustBe 1
    }
  }

  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 5
    view.getElementsByAttributeValue("for", "STANDARD") must containMessageForElements("declaration.type.standard")
    view.getElementsByAttributeValue("for", "SUPPLEMENTARY") must containMessageForElements("declaration.type.supplementary")
    view.getElementsByAttributeValue("for", "SIMPLIFIED") must containMessageForElements("declaration.type.simplified")
    view.getElementsByAttributeValue("for", "OCCASIONAL") must containMessageForElements("declaration.type.occasional")
    view.getElementsByAttributeValue("for", "CLEARANCE") must containMessageForElements("declaration.type.clearance")
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
