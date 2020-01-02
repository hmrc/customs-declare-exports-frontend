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
import forms.declaration.RoutingQuestionYesNo
import models.Mode
import play.api.data.Form
import services.cache.ExportsTestData
import services.model.Country
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.remove_routing_country

class RemoveRoutingCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestData {

  val country = Country("Poland", "PL")
  val form: Form[Boolean] = RoutingQuestionYesNo.form()

  val removeRoutingCountryPage = new remove_routing_country(mainTemplate)
  val view = removeRoutingCountryPage(Mode.Normal, form, country)(journeyRequest(), messages)

  "Remove routing country page" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingCountries.remove.title")
      messages must haveTranslationFor("declaration.routingCountries.remove.heading")
      messages must haveTranslationFor("declaration.routingCountries.remove.question")
      messages must haveTranslationFor("declaration.routingCountries.remove.countryHeader")
    }

    "display page question" in {

      view.getElementById("title").text() mustBe messages("declaration.routingCountries.remove.question")
    }

    "display page header" in {

      view.getElementById("section-header").text() must include(messages("declaration.routingCountries.remove.heading"))
    }

    "display country header" in {

      view.getElementById("country-header").text() mustBe messages("declaration.routingCountries.remove.countryHeader")
    }

    "display country to remove" in {

      view.getElementById("country-to-remove").text() mustBe country.asString()
    }

    "display Yes/No radio options" in {

      view.getElementById("Yes-label").text() mustBe messages("site.yes")
      view.getElementById("No-label").text() mustBe messages("site.no")
    }

    "display back button that links to 'Countries summary' page" in {

      val backButton = view.getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.RoutingCountriesSummaryController.displayPage())
    }

    "display 'Save and continue' button" in {

      view.getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "display 'Save and return' button" in {

      view.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
    }
  }
}
