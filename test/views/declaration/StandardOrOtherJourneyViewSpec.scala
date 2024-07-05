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
import controllers.routes.ChoiceController
import forms.declaration.DeclarationChoice
import forms.declaration.DeclarationChoice.{standardOrOtherJourneys, NonStandardDeclarationType}
import models.DeclarationType._
import org.jsoup.nodes.Document
import play.api.data.Form
import views.helpers.CommonMessages
import views.html.declaration.standard_or_other_journey
import views.common.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class StandardOrOtherJourneyViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val form: Form[String] = DeclarationChoice.form(standardOrOtherJourneys)
  private val page = instanceOf[standard_or_other_journey]

  private def createView(frm: Form[String] = form): Document =
    page(frm)(request, messages)

  "'Standard Or Other Journey' View on empty page" should {
    val view = createView()

    "display 'Back' button that links to 'Choice' page" in {
      val backButton = view.getElementById("back-link")
      backButton.text() mustBe messages(backCaption)
      backButton.getElementById("back-link") must haveHref(ChoiceController.displayPage)
    }

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display the expected page title" in {
      view.getElementsByTag("h1").text() mustBe messages("declaration.type.description")
    }

    "display the expected radio buttons" in {
      view.getElementsByClass("govuk-radios__divider").text mustBe messages("site.radio.divider")
      val radios = view.getElementsByClass("govuk-radios__item")
      radios.size mustBe 2

      val radio1 = radios.get(0).children()
      radio1.size mustBe 3

      assert(radio1.get(0).hasClass("govuk-radios__input"))
      radio1.get(0).attr("value") mustBe "STANDARD"

      assert(radio1.get(1).hasClass("govuk-radios__label"))
      radio1.get(1).attr("for") mustBe "STANDARD"
      radio1.get(1).text mustBe messages("declaration.type.standard")

      assert(radio1.get(2).hasClass("govuk-radios__hint"))
      radio1.get(2).text mustBe messages("declaration.type.standard.hint")

      val radio2 = radios.get(1).children()
      radio2.size mustBe 3

      assert(radio2.get(0).hasClass("govuk-radios__input"))
      radio2.get(0).attr("value") mustBe NonStandardDeclarationType

      assert(radio2.get(1).hasClass("govuk-radios__label"))
      radio2.get(1).attr("for") mustBe NonStandardDeclarationType
      radio2.get(1).text mustBe messages("declaration.type.other")

      assert(radio2.get(2).hasClass("govuk-radios__hint"))
      radio2.get(2).text mustBe messages("declaration.type.other.hint")
    }

    "display the expected warning text" in {
      view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("site.warning")
      view.getElementsByClass("govuk-warning-text__text") must containMessageForElements("declaration.type.warning")
    }

    "display the expected tariff details" in {
      val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
      tariffTitle.text mustBe messages(s"tariff.declaration.type.title")

      val tariffDetails = view.getElementsByClass("govuk-details__text").get(0).children()
      tariffDetails.size mustBe 2

      tariffDetails.first.text mustBe messages("tariff.declaration.type.text.1")
      tariffDetails.last.text mustBe messages("tariff.declaration.type.text.2", messages("tariff.declaration.type.linkText.2"))
    }

    "display 'Continue' button on page" in {
      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages(continueCaption)
    }
  }

  "'Standard Or Other Journey' View for invalid input" should {

    "display error when no choice is made" in {
      val view = createView(form.bind(Map[String, String]()))
      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${STANDARD.toString}")
      view must containErrorElementWithMessageKey("declaration.type.error")
    }

    "display error when choice is incorrect" in {
      val view = createView(form.bind(Map("type" -> "incorrect")))
      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${STANDARD.toString}")
      view must containErrorElementWithMessageKey("declaration.type.error")
    }
  }

  "'Standard Or Other Journey' View when filled" should {
    "display selected radio button" in {
      standardOrOtherJourneys.foreach { declarationType =>
        val view = createView(form.fill(declarationType))
        view.getElementById(declarationType).getElementsByAttribute("checked").size() mustBe 1
        view.getElementById(standardOrOtherJourneys.filterNot(_ == declarationType).head).attr("checked") mustBe empty
      }
    }
  }
}
