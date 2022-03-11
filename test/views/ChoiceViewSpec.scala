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

package views

import base.ExportsTestData._
import base.OverridableInjector
import config.featureFlags.{SecureMessagingInboxConfig, SfusConfig}
import features.SecureMessagingFeatureStatus
import features.SecureMessagingFeatureStatus.SecureMessagingFeatureStatus
import forms.Choice
import org.jsoup.nodes.Document
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.inject.bind
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.choice_page
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with CommonMessages with Stubs with BeforeAndAfterEach {

  private val form: Form[Choice] = Choice.form()

  private val injector =
    new OverridableInjector(bind[SfusConfig].toInstance(mockSfusConfig), bind[SecureMessagingInboxConfig].toInstance(mockSecureMessagingInboxConfig))
  private val choicePage = injector.instanceOf[choice_page]

  private def createView(form: Form[Choice] = form, journeys: Seq[String] = allJourneys): Document = choicePage(form, journeys)(request, messages)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSfusConfig, mockSecureMessagingInboxConfig)
  }

  override protected def afterEach(): Unit = {
    reset(mockSfusConfig, mockSecureMessagingInboxConfig)
    super.afterEach()
  }

  private val dummyUploadLink = controllers.routes.FileUploadController.startFileUpload("").url
  private val dummyInboxLink = "dummyInboxLink"

  private val SFUS = SecureMessagingFeatureStatus.sfus
  private val EXPORTS = SecureMessagingFeatureStatus.exports
  private val DISABLED = SecureMessagingFeatureStatus.disabled

  private def withSecureMessagingFeatureStatus(flag: SecureMessagingFeatureStatus): Unit = {
    flag match {
      case SFUS =>
        when(mockSecureMessagingInboxConfig.isSfusSecureMessagingEnabled).thenReturn(true)
        when(mockSecureMessagingInboxConfig.isExportsSecureMessagingEnabled).thenReturn(false)
      case EXPORTS =>
        when(mockSecureMessagingInboxConfig.isSfusSecureMessagingEnabled).thenReturn(false)
        when(mockSecureMessagingInboxConfig.isExportsSecureMessagingEnabled).thenReturn(true)
      case _ =>
        when(mockSecureMessagingInboxConfig.isSfusSecureMessagingEnabled).thenReturn(false)
        when(mockSecureMessagingInboxConfig.isExportsSecureMessagingEnabled).thenReturn(false)
    }

    when(mockSfusConfig.sfusUploadLink).thenReturn(dummyUploadLink)
    when(mockSecureMessagingInboxConfig.sfusInboxLink).thenReturn(dummyInboxLink)
  }

  "Choice View on empty page" should {

    "display same page title as header" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val viewWithMessage = createView()
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display radio buttons with description (not selected)" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().fill(Choice("")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
      ensureRadioIsUnChecked(view, "MSG")
    }

    "not display 'Back' button" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val backButton = createView().getElementById("back-link")

      backButton mustBe null
    }

    "display 'Continue' button on page" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView()

      val saveButton = view.getElementsByClass("govuk-button")
      saveButton.text() mustBe messages(continueCaption)
    }
  }

  "Choice View" when {

    "available journey types are restricted" should {
      "display only the appropriate radio buttons" in {
        allJourneys.foreach { excludedJourneyKey =>
          val filteredJourneyKeys = allJourneys.filterNot(_.equals(excludedJourneyKey))
          val view = createView(journeys = filteredJourneyKeys)

          filteredJourneyKeys.foreach { journeyKey =>
            view.getElementById(journeyKey) mustNot be(null)
          }

          view.getElementById(excludedJourneyKey) mustBe null
        }
      }
    }

    "secureMessagingInbox flag is set to 'sfus'" should {
      "display SFUS link description text" in {
        withSecureMessagingFeatureStatus(SFUS)
        val h2s = createView().getElementsByTag("h2")

        h2s.size mustBe 2
        h2s.first().text() mustBe messages("declaration.choice.link.sfus.description")
      }

      "display SFUS upload documents link" in {
        withSecureMessagingFeatureStatus(SFUS)
        val link = createView().getElementById("sfusUploadLink")

        link.text() mustBe messages("declaration.choice.link.sfusUpload.txt")
        link.attr("href") mustBe dummyUploadLink
      }

      "display SFUS message inbox link" in {
        withSecureMessagingFeatureStatus(SFUS)
        val link = createView().getElementById("sfusInboxLink")

        link.text() mustBe messages("declaration.choice.link.sfusInbox.txt")
        link.attr("href") mustBe dummyInboxLink
      }
    }

    "secureMessagingInbox flag is set to 'exports'" should {
      "display Exports link description text" in {
        withSecureMessagingFeatureStatus(EXPORTS)
        val h2s = createView().getElementsByTag("h2")

        h2s.size mustBe 2
        h2s.first().text() mustBe messages("declaration.choice.link.exports.description")
      }

      "display SFUS upload documents link" in {
        withSecureMessagingFeatureStatus(EXPORTS)
        val link = createView().getElementById("sfusUploadLink")

        link.text() mustBe messages("declaration.choice.link.sfusUpload.txt")
        link.attr("href") mustBe dummyUploadLink
      }
    }

    "secureMessagingInbox flag is set to 'disabled'" should {
      "not display SFUS or Exports link description text" in {
        withSecureMessagingFeatureStatus(DISABLED)
        val h2s = createView().getElementsByTag("h2")
        h2s.size mustBe 1
      }

      "not display SFUS upload documents link" in {
        withSecureMessagingFeatureStatus(DISABLED)
        createView().getElementById("sfusUploadLink") mustBe null
      }

      "not display SFUS inbox link" in {
        withSecureMessagingFeatureStatus(DISABLED)
        createView().getElementById("sfusInboxLink") mustBe null
      }
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#CRT")

      view must containErrorElementWithMessageKey("choicePage.input.error.empty")
    }

    "display error when choice is incorrect" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().bind(Map("value" -> "incorrect")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#CRT")

      view must containErrorElementWithMessageKey("choicePage.input.error.incorrectValue")
    }
  }

  "Choice View when filled" should {

    "display selected radio button - Create (CRT)" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().fill(Choice("CRT")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
      ensureRadioIsUnChecked(view, "MSG")
    }

    "display selected radio button - Cancel a declaration (CAN)" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().fill(Choice("CAN")))
      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
      ensureRadioIsUnChecked(view, "MSG")
    }

    "display selected radio button - View recent declarations (SUB)" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().fill(Choice("SUB")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
      ensureRadioIsUnChecked(view, "MSG")
    }

    "display selected radio button - Continue saved declaration (Con)" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().fill(Choice("CON")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsChecked(view, "CON")
      ensureRadioIsUnChecked(view, "MSG")
    }

    "display selected radio button - View Messages (Msg)" in {
      withSecureMessagingFeatureStatus(EXPORTS)
      val view = createView(Choice.form().fill(Choice("MSG")))

      ensureAllLabelTextIsCorrect(view)

      ensureRadioIsUnChecked(view, "CRT")
      ensureRadioIsUnChecked(view, "CAN")
      ensureRadioIsUnChecked(view, "SUB")
      ensureRadioIsUnChecked(view, "CON")
      ensureRadioIsChecked(view, "MSG")
    }
  }

  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 5
    view.getElementsByAttributeValue("for", "CRT") must containMessageForElements("declaration.choice.CRT")
    view.getElementsByAttributeValue("for", "SUB") must containMessageForElements("declaration.choice.SUB")
    view.getElementsByAttributeValue("for", "CAN") must containMessageForElements("declaration.choice.CAN")
    view.getElementsByAttributeValue("for", "CON") must containMessageForElements("declaration.choice.CON")
    view.getElementsByAttributeValue("for", "MSG") must containMessageForElements("declaration.choice.MSG")
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
