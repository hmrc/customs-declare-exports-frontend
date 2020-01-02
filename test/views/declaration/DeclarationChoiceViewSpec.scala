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
import config.AppConfig
import forms.Choice
import forms.Choice.AllowedChoiceValues.CreateDec
import forms.declaration.DeclarationChoice
import helpers.views.declaration.CommonMessages
import models.DeclarationType
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declaration_choice
import views.tags.ViewTest

@ViewTest
class DeclarationChoiceViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationChoice] = DeclarationChoice.form()
  private val choicePage = new declaration_choice(mainTemplate, instanceOf[AppConfig])
  private def createView(form: Form[DeclarationChoice] = form, messages: Messages = stubMessages()): Document =
    choicePage(form)(request, messages)

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

      backButton.text() mustBe messages(backCaption)
      backButton.getElementById("back-link") must haveHref(controllers.routes.ChoiceController.displayPage(Some(Choice(CreateDec))))
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(DeclarationChoice.form().bind(Map[String, String]()))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("type", "#type")

      view.select("#error-message-type-input").text() mustBe messages("error.required")
    }

    "display error when choice is incorrect" in {

      val view = createView(DeclarationChoice.form().bind(Map("type" -> "incorrect")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("type", "#type")

      view.select("#error-message-type-input").text() mustBe messages("declaration.type.error")
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
  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 5
    view.getElementById("STANDARD-label").text() mustBe "declaration.type.standard"
    view.getElementById("SUPPLEMENTARY-label").text() mustBe "declaration.type.supplementary"
    view.getElementById("SIMPLIFIED-label").text() mustBe "declaration.type.simplified"
    view.getElementById("OCCASIONAL-label").text() mustBe "declaration.type.occasional"
    view.getElementById("CLEARANCE-label").text() mustBe "declaration.type.clearance"
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
