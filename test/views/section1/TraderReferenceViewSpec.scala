/*
 * Copyright 2024 HM Revenue & Customs
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

package views.section1

import base.{Injector, MockAuthAction}
import controllers.section1.routes.DucrChoiceController
import forms.section1.TraderReference.form
import forms.section1.TraderReference
import models.DeclarationType.{DeclarationType, STANDARD}
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.html.section1.trader_reference
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class TraderReferenceViewSpec extends PageWithButtonsSpec with Injector with MockAuthAction {

  private val page = instanceOf[trader_reference]

  override val typeAndViewInstance: (DeclarationType, (JourneyRequest[_], Messages) => HtmlFormat.Appendable) = (STANDARD, page(form)(_, _))

  private val dummyTraderReference = "dummyTraderRef"
  private val view = page(form)(request, messages)
  private val viewWithData = page(form.fill(TraderReference(dummyTraderReference)))(request, messages)

  "Trader Reference view" should {

    "not have View declaration summary link" in {
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    "display title" in {
      view.getElementsByTag("h1").first().text() mustBe messages("declaration.traderReference.title")
    }

    "display body text" in {
      view.getElementsByClass("govuk-body").first.text() mustBe messages("declaration.traderReference.body")
    }

    "display hint text" in {
      view.getElementsByClass("govuk-hint").first.text() mustBe messages("declaration.traderReference.hintText")
    }

    "display input with data from cache" in {
      viewWithData.getElementById(TraderReference.traderReferenceKey).attr("value") mustBe dummyTraderReference
    }

    "display empty input" in {
      view.getElementById(TraderReference.traderReferenceKey).attr("value") mustBe empty
    }

    "display the first tariff expander" should {
      "in non-Clearance journeys" in {

        val expectedBodyTextListMessageKeys = List(
          "declaration.ducr.expander.bullet1",
          "declaration.ducr.expander.bullet2",
          "declaration.ducr.expander.bullet3",
          "declaration.ducr.expander.bullet4"
        )

        expectedBodyTextListMessageKeys.foreach { messageKey =>
          view.getElementsByClass("govuk-list").get(0) must containMessage(messageKey)
        }
      }
      "in a Clearance journey" in {
        val expectedBodyTextListMessageKeys = List(
          "declaration.ducr.expander.bullet1",
          "declaration.ducr.expander.bullet2",
          "declaration.ducr.expander.bullet3",
          "declaration.ducr.expander.bullet4"
        )

        expectedBodyTextListMessageKeys.foreach { messageKey =>
          view.getElementsByClass("govuk-list").get(0) must containMessage(messageKey)
        }
      }
    }

    "display back button linking to Do You Have A DUCR? page" in {
      val backLink = view.getElementById("back-link")
      backLink must containMessage(backToPreviousQuestionCaption)
      backLink must haveHref(DucrChoiceController.displayPage.url)
    }

    checkAllSaveButtonsAreDisplayed(view)
  }
}
