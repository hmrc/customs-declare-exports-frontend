/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.declaration.routes.StandardOrOtherJourneyController
import forms.declaration.DeclarationChoice
import forms.declaration.DeclarationChoice.nonStandardJourneys
import models.DeclarationType._
import org.jsoup.nodes.Document
import play.api.data.Form
import views.helpers.CommonMessages
import views.html.declaration.declaration_choice
import views.common.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class DeclarationChoiceViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val form: Form[String] = DeclarationChoice.form(nonStandardJourneys)
  private val page = instanceOf[declaration_choice]

  private def createView(frm: Form[String] = form): Document =
    page(frm)(request, messages)

  "Declaration Choice View on empty page" should {
    val view = createView()

    "display 'Back' button that links to 'Standard Or Other Journey' page" in {
      val backButton = view.getElementById("back-link")
      backButton.text() mustBe messages(backToPreviousQuestionCaption)
      backButton.getElementById("back-link") must haveHref(StandardOrOtherJourneyController.displayPage)
    }

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display page title" in {
      view.getElementsByTag("h1").text mustBe messages("declaration.type.description.other")
    }

    "display radio buttons with description (not selected)" in {
      ensureAllLabelTextIsCorrect(view)
      ensureRadiosAreUnChecked(view, allDeclarationTypesExcluding(STANDARD))
    }

    "display the expected inset text" in {
      val insetText = view.getElementsByClass("govuk-inset-text")
      insetText.first.getElementsByClass("govuk-heading-s").text mustBe messages("declaration.type.insetText.header")

      val paragraphs = insetText.first.getElementsByClass("govuk-body")
      paragraphs.size mustBe 2

      paragraphs.first.text mustBe messages("declaration.type.insetText.p1", messages("declaration.type.insetText.linkText"))

      val href =
        "https://www.gov.uk/guidance/declare-commercial-goods-youre-taking-out-of-great-britain-in-your-accompanied-baggage-or-small-vehicles"
      paragraphs.first.getElementsByTag("a").attr("href") mustBe href

      paragraphs.last.text mustBe messages("declaration.type.insetText.p2")
    }

    "display the expected tariff details" in {
      val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
      tariffTitle.text mustBe messages(s"tariff.expander.title.common")

      val tariffDetails = view.getElementsByClass("govuk-details__text").first

      val expectedText = messages("tariff.declaration.others.text", messages("tariff.declaration.others.linkText.0"))
      val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
      actualText mustBe removeLineBreakIfAny(expectedText)
    }

    "display 'Continue' button on page" in {
      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages(continueCaption)
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {
      val view = createView(form.bind(Map[String, String]()))
      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${SIMPLIFIED.toString}")
      view must containErrorElementWithMessageKey("declaration.type.error")
    }

    "display error when choice is incorrect" in {
      val view = createView(form.bind(Map("type" -> "incorrect")))
      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${SIMPLIFIED.toString}")
      view must containErrorElementWithMessageKey("declaration.type.error")
    }
  }

  "Choice View when filled" should {
    "display selected radio button" in {
      allDeclarationTypesExcluding(STANDARD).foreach { declarationType =>
        val view = createView(form.fill(declarationType.toString))
        ensureAllLabelTextIsCorrect(view)

        view.getElementById(declarationType.toString).getElementsByAttribute("checked").size() mustBe 1
        ensureRadiosAreUnChecked(view, allDeclarationTypesExcluding(STANDARD, declarationType))
      }
    }
  }

  private def ensureAllLabelTextIsCorrect(view: Document): Unit = {
    view.getElementsByTag("label").size mustBe 4
    allDeclarationTypesExcluding(STANDARD).foreach { declarationType =>
      val journey = declarationType.toString
      view.getElementsByAttributeValue("for", journey) must containMessageForElements(s"declaration.type.${journey.toLowerCase}")
    }
  }

  private def ensureRadiosAreUnChecked(view: Document, journeyTypes: Seq[DeclarationType]): Unit =
    journeyTypes.foreach { journeyType =>
      view.getElementById(journeyType.toString).attr("checked") mustBe empty
    }
}
