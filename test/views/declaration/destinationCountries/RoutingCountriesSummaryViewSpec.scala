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

import base.Injector
import controllers.declaration.routes
import forms.declaration.RoutingQuestionYesNo
import models.Mode
import play.api.data.Form
import services.cache.ExportsTestData
import services.model.Country
import unit.tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.routing_countries_summary

class RoutingCountriesSummaryViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector {

  val countries = Seq(Country("France", "FR"), Country("Poland", "PL"))
  val form: Form[Boolean] = RoutingQuestionYesNo.formAdd()

  val routingCountriesSummaryPage = instanceOf[routing_countries_summary]
  val view = routingCountriesSummaryPage(Mode.Normal, form, countries)(journeyRequest(), messages)

  "Routing Countries Summary" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingCountries.summary.title")
      messages must haveTranslationFor("declaration.routingCountries.summary.heading")
      messages must haveTranslationFor("declaration.routingCountries.summary.header")
      messages must haveTranslationFor("declaration.routingCountries.summary.question")
      messages must haveTranslationFor("declaration.routingCountries.summary.table.code")
    }

    "display page heading" in {

      view.getElementById("section-header").text() must include(messages("declaration.routingCountries.summary.heading"))
    }

    "display page title for the table" in {

      view.getElementsByClass(Styles.gdsPageLegend).text() mustBe messages("declaration.routingCountries.summary.header")
    }

    "display page question" in {

      view.getElementsByClass("govuk-fieldset__legend--m").text() mustBe messages("declaration.routingCountries.summary.question")
    }

    "have Yes/No answers" in {

      view.getElementsByAttributeValue("for", "Yes").text().text() mustBe messages("site.yes")
      view.getElementsByAttributeValue("for", "No").text() mustBe messages("site.no")
    }

    "display back button that links to 'Destination country' page" in {

      val backButton = view.getElementById("back-link")

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
