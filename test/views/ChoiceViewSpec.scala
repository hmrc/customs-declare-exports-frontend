/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.Choice
import forms.Choice.AllowedChoiceValues.CreateDec
import helpers.views.declaration.CommonMessages
import org.jsoup.nodes.Document
import org.scalatest.Matchers._
import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukButton, GovukRadios}
import uk.gov.hmrc.play.views.html.helpers.FormWithCSRF
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.choice_page
import views.html.components.gds.{errorSummary, link, paragraphBody, saveAndContinue}
import views.tags.ViewTest
import base.ExportsTestData._
import config.SfusConfig
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with BeforeAndAfterEach {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = instanceOf[choice_page]
  private val sfusConfig = mock[SfusConfig]
  private def createView(form: Form[Choice] = form): Document =
    choicePage(form, allJourneys, sfusConfig)(request, messages)

  override def beforeEach(): Unit =
    super.beforeEach()

  override protected def afterEach(): Unit = {
    reset(sfusConfig)
    super.afterEach()
  }

  private val dummyUploadLink = "dummyUploadLink"
  private val dummyInboxLink = "dummyInboxLink"

  private def withSfusInboxEnabled(messaging: Boolean = true) = {
    when(sfusConfig.isSfusSecureMessagingEnabled).thenReturn(messaging)
    when(sfusConfig.sfusUploadLink).thenReturn(dummyUploadLink)
    when(sfusConfig.sfusInboxLink).thenReturn(dummyInboxLink)
  }

  "Choice View on empty page" should {

    "display same page title as header" in {
      withSfusInboxEnabled()
      val viewWithMessage = createView()
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display radio buttons with description (not selected)" in {
      withSfusInboxEnabled()
      val view = createView(Choice.form().fill(Choice("")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display only Create radio button with description" in {
      val page = new choice_page(
        gdsMainTemplate,
        instanceOf[GovukButton],
        instanceOf[GovukRadios],
        instanceOf[errorSummary],
        instanceOf[saveAndContinue],
        instanceOf[paragraphBody],
        instanceOf[link],
        instanceOf[FormWithCSRF]
      )

      withSfusInboxEnabled()
      val view = page(Choice.form().fill(Choice("CRT")), Seq(CreateDec), sfusConfig)(request, messages)
      ensureCreateLabelIsCorrect(view)

      ensureRadioIsChecked(view, "CRT")
    }

    "not display 'Back' button" in {
      withSfusInboxEnabled()
      val backButton = createView().getElementById("back-link")

      backButton mustBe null
    }

    "display 'Continue' button on page" in {
      withSfusInboxEnabled()
      val view = createView()

      val saveButton = view.getElementsByClass("govuk-button")
      saveButton.text() mustBe messages(continueCaption)
    }
  }

  "Choice View" when {
    "secure messaging flag is enabled" should {
      "display SFUS link description text" in {
        withSfusInboxEnabled()
        val h3s = createView().getElementsByTag("h3")
        h3s.size mustBe 1
        h3s.first().text() mustBe messages("declaration.choice.linkDescription")
      }

      "display SFUS upload documents link" in {
        withSfusInboxEnabled()
        val link = createView().getElementById("sfusUploadLink")

        link.text() mustBe messages("declaration.choice.link.sfusUpload.txt")
        link.attr("href") mustBe dummyUploadLink
      }

      "display SFUS message inbox link" in {
        withSfusInboxEnabled()
        val link = createView().getElementById("sfusInboxLink")

        link.text() mustBe messages("declaration.choice.link.sfusInbox.txt")
        link.attr("href") mustBe dummyInboxLink
      }
    }

    "secure messaging flag is disabled" should {
      "not display SFUS link description text" in {
        withSfusInboxEnabled(false)
        val h3s = createView().getElementsByTag("h3")
        h3s.size mustBe 0
      }

      "not display SFUS upload documents link" in {
        withSfusInboxEnabled(false)
        createView().getElementById("sfusUploadLink") mustBe null
      }

      "not display SFUS inbox link" in {
        withSfusInboxEnabled(false)
        createView().getElementById("sfusInboxLink") mustBe null
      }
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {
      withSfusInboxEnabled()
      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#CRT")

      view must containErrorElementWithMessageKey("choicePage.input.error.empty")
    }

    "display error when choice is incorrect" in {
      withSfusInboxEnabled()
      val view = createView(Choice.form().bind(Map("value" -> "incorrect")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#CRT")

      view must containErrorElementWithMessageKey("choicePage.input.error.incorrectValue")
    }
  }

  "Choice View when filled" should {

    "display selected radio button - Create (CRT)" in {
      withSfusInboxEnabled()
      val view = createView(Choice.form().fill(Choice("CRT")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display selected radio button - Cancel a declaration (CAN)" in {
      withSfusInboxEnabled()
      val view = createView(Choice.form().fill(Choice("CAN")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display selected radio button - View recent declarations (SUB)" in {
      withSfusInboxEnabled()
      val view = createView(Choice.form().fill(Choice("SUB")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
    }

    "display selected radio button - Continue saved declaration (Con)" in {
      withSfusInboxEnabled()
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
    view.getElementsByAttributeValue("for", "CRT") must containMessageForElements("declaration.choice.CRT")
    view.getElementsByAttributeValue("for", "SUB") must containMessageForElements("declaration.choice.SUB")
    view.getElementsByAttributeValue("for", "CAN") must containMessageForElements("declaration.choice.CAN")
    view.getElementsByAttributeValue("for", "CON") must containMessageForElements("declaration.choice.CON")
  }

  private def ensureCreateLabelIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 1
    view.getElementsByAttributeValue("for", "CRT") must containMessageForElements("declaration.choice.CRT")
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
