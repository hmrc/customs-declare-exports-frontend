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

package views.declaration.destinationCountries

import base.Injector
import controllers.declaration.routes
import forms.declaration.RoutingCountryQuestionYesNo
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.routing_country_question

class RoutingCountryQuestionViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector {

  val countryOfDestination = "Poland"
  val form: Form[Boolean] = RoutingCountryQuestionYesNo.formAdd()
  val routingQuestionPage = instanceOf[routing_country_question]
  def createView(mode: Mode = Mode.Normal) =
    routingQuestionPage(mode, form, countryOfDestination)(journeyRequest(), messages)

  "Routing country question page" should {

    val view = createView()

    "have defined translation for used labels" in {

      messages must haveTranslationFor("declaration.routingCountryQuestion.title")
      messages must haveTranslationFor("declaration.routingCountryQuestion.paragraph")
      messages must haveTranslationFor("declaration.routingCountryQuestion.empty")
      messages must haveTranslationFor("tariff.expander.title.clearance")
    }

    "display the section header" in {

      view.getElementById("section-header").text must include(messages("declaration.section.3"))
    }

    "display the page question" in {

      view.getElementsByTag("h1").text mustBe messages("declaration.routingCountryQuestion.title", countryOfDestination)
    }

    "display Yes/No answers" in {

      view.getElementsByAttributeValue("for", "Yes").text.text() mustBe messages("site.yes")
      view.getElementsByAttributeValue("for", "No").text mustBe messages("site.no")
    }

    "display Tariff section text" in {
      val tariffText = view.getElementsByClass("govuk-details__summary-text").first.text
      tariffText.text() mustBe messages("tariff.expander.title.common")
    }

    "display back button that links to 'Declaration Holder' page" in {

      val backButton = view.getElementById("back-link")

      backButton.text mustBe messages("site.back")
      backButton must haveHref(routes.DestinationCountryController.displayPage())
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }
}
