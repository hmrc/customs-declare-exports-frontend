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

package views.helpers.summary

import base.Injector
import controllers.declaration.routes.{DestinationCountryController, RoutingCountriesController}
import forms.declaration.countries.Country
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card3ForRouteOfGoodsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val declaration =
    aDeclaration(withRoutingCountries(List(Country(Some("FR")), Country(Some("IT")))), withDestinationCountry(Country(Some("ES"))))

  private val card3ForRouteOfGoods = instanceOf[Card3ForRouteOfGoods]

  "Route Of Goods section" should {
    val view = card3ForRouteOfGoods.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.countries")
    }

    "notify the user when there are no routing countries" in {
      val declaration1 = declaration.copy(locations = declaration.locations.copy(routingCountries = List.empty))
      val row = card3ForRouteOfGoods.eval(declaration1)(messages).getElementsByClass("routing-countries")

      val expectedValue = messages("site.none")
      val call = Some(RoutingCountriesController.submitRoutingCountry)
      checkSummaryRow(row, "countries.routingCountries", expectedValue, call, "countries.routingCountries")
    }

    "show the routing countries" in {
      val row = view.getElementsByClass("routing-countries")

      val call = Some(RoutingCountriesController.submitRoutingCountry)
      checkSummaryRow(row, "countries.routingCountries", "France, Italy", call, "countries.routingCountries")
    }

    "show the destination country" in {
      val row = view.getElementsByClass("destination-country")

      val call = Some(DestinationCountryController.displayPage)
      checkSummaryRow(row, "countries.countryOfDestination", "Spain", call, "countries.countryOfDestination")
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card3ForRouteOfGoods.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }
  }
}
