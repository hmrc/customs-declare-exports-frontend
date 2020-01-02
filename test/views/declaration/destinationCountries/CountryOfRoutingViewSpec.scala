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

package views.declaration.destinationCountries

import controllers.declaration.routes
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.{FirstRoutingCountryPage, NextRoutingCountryPage}
import models.Mode
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.country_of_routing

class CountryOfRoutingViewSpec extends UnitViewSpec with Stubs with ExportsTestData {

  val firstRoutingForm: Form[String] = DestinationCountries.form(FirstRoutingCountryPage)
  val nextRoutingForm: Form[String] = DestinationCountries.form(NextRoutingCountryPage)

  val countryOfRoutingPage = new country_of_routing(mainTemplate)
  val firstRoutingView = countryOfRoutingPage(Mode.Normal, firstRoutingForm, FirstRoutingCountryPage)(journeyRequest(), messages)
  val nextRoutingView = countryOfRoutingPage(Mode.Normal, nextRoutingForm, NextRoutingCountryPage)(journeyRequest(), messages)

  "Routing Country view" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingCountry.title")
      messages must haveTranslationFor("declaration.routingCountry.heading")
    }

    "have page heading" in {

      firstRoutingView.getElementById("section-header").text() must include(messages("declaration.routingCountry.heading"))
    }

    "display back button that links to 'Country of Routing question' page" in {

      val backButton = firstRoutingView.getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.RoutingCountriesController.displayRoutingQuestion(fastForward = true))
    }

    "display 'Save and continue' button" in {

      firstRoutingView.getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "display 'Save and return' button" in {

      firstRoutingView.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
    }
  }

  "First Routing Country view" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.firstRoutingCountry.question")
      messages must haveTranslationFor("declaration.firstRoutingCountry.empty")
    }

    "have page question" in {

      firstRoutingView.getElementById("title").text() mustBe messages("declaration.firstRoutingCountry.question")
    }
  }

  "Next Routing Country view" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingCountry.question")
      messages must haveTranslationFor("declaration.routingCountry.empty")
    }

    "have page question" in {

      nextRoutingView.getElementById("title").text() mustBe messages("declaration.routingCountry.question")
    }
  }
}
