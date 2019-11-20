/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.declaration.routes
import forms.declaration.RoutingQuestionYesNo
import models.Mode
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.routing_country_question

class RoutingCountryQuestionViewSpec extends UnitViewSpec with Stubs with ExportsTestData {

  val countryOfDestination = "Poland"
  val form: Form[Boolean] = RoutingQuestionYesNo.form()

  val routingQuestionPage = new routing_country_question(mainTemplate)
  val view = routingQuestionPage(Mode.Normal, form, countryOfDestination)(journeyRequest(), messages)

  "Routing country question page" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingQuestion.title")
      messages must haveTranslationFor("declaration.routingQuestion.heading")
      messages must haveTranslationFor("declaration.routingQuestion.question")
      messages must haveTranslationFor("declaration.routingQuestion.empty")
      messages must haveTranslationFor("declaration.routingQuestion.error")
    }

    "have section header" in {

      view.getElementById("section-header").text() must include(messages("declaration.routingQuestion.heading"))
    }

    "have page question" in {

      view.getElementById("title").text() mustBe messages("declaration.routingQuestion.question")
    }

    "have Yes/No answers" in {

      view.getElementById("Yes-label").text() mustBe messages("site.yes")
      view.getElementById("No-label").text() mustBe messages("site.no")
    }

    "display back button that links to 'Declaration Holder' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.DestinationCountryController.displayPage())
    }

    "display 'Save and continue' button" in {

      view.getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "display 'Save and return' button" in {

      view.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
    }
  }
}
