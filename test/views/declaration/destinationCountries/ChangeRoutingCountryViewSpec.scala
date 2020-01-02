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
import views.html.declaration.destinationCountries.change_routing_country

class ChangeRoutingCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestData {

  val countryToChange = "GB"
  val firstRoutingForm: Form[String] = DestinationCountries.form(FirstRoutingCountryPage)
  val nextRoutingForm: Form[String] = DestinationCountries.form(NextRoutingCountryPage)

  val changeRoutingCountryPage = new change_routing_country(mainTemplate)
  val firstRoutingView = changeRoutingCountryPage(Mode.Normal, firstRoutingForm, FirstRoutingCountryPage, countryToChange)(journeyRequest(), messages)
  val nextRoutingView = changeRoutingCountryPage(Mode.Normal, firstRoutingForm, NextRoutingCountryPage, countryToChange)(journeyRequest(), messages)

  "Change routing country pages" should {

    "have page heading" in {

      firstRoutingView.getElementById("section-header").text() must include(messages("declaration.routingCountry.heading"))
      nextRoutingView.getElementById("section-header").text() must include(messages("declaration.routingCountry.heading"))
    }

    "have page question during changing first routing country" in {

      firstRoutingView.getElementById("title").text() mustBe messages("declaration.firstRoutingCountry.question")
    }

    "have page question during changing next routing country" in {

      nextRoutingView.getElementById("title").text() mustBe messages("declaration.routingCountry.question")
    }

    "display back button that links to 'Countries summary' for first routing country page" in {

      val backButton = firstRoutingView.getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.RoutingCountriesSummaryController.displayPage())
    }

    "display back button that links to 'Countries summary' for next routing country page" in {

      val backButton = firstRoutingView.getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.RoutingCountriesSummaryController.displayPage())
    }

    "display 'Save and continue' button" in {

      firstRoutingView.getElementById("submit").text() mustBe messages("site.save_and_continue")
      nextRoutingView.getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "display 'Save and return' button" in {

      firstRoutingView.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
      nextRoutingView.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
    }
  }
}
