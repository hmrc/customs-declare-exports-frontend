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
import controllers.declaration.routes.{DestinationCountryController, LocationOfGoodsController, OfficeOfExitController, RoutingCountriesController}
import forms.declaration.LocationOfGoods
import forms.declaration.countries.Country
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card3ForRoutesAndLocationsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val declaration =
    aDeclaration(
      withRoutingCountries(List(Country(Some("FR")), Country(Some("IT")))),
      withDestinationCountry(Country(Some("ES"))),
      withGoodsLocation(LocationOfGoods("GBAUEMAEMAEMA")),
      withOfficeOfExit("123")
    )

  private val card3ForRoutesAndLocations = instanceOf[Card3ForRoutesAndLocations]

  "'Routes and locations' section" should {
    val view = card3ForRoutesAndLocations.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.section.3")
    }

    "notify the user when there are no routing countries" in {
      val declaration1 = declaration.copy(locations = declaration.locations.copy(routingCountries = List.empty))
      val row = card3ForRoutesAndLocations.eval(declaration1)(messages).getElementsByClass("routing-countries")

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

    "show the goods location code" in {
      val row = view.getElementsByClass("goods-location-code")

      val call = Some(LocationOfGoodsController.displayPage)
      checkSummaryRow(row, "locations.goodsLocationCode", "GBAUEMAEMAEMA", call, "locations.goodsLocationCode")
    }

    "show the 'RRS01' additional information" when {
      "the goods location code ends with 'GVM'" in {
        val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("GBAUABDABDABDGVM")), withOfficeOfExit("123"))
        val view = card3ForRoutesAndLocations.eval(declaration)(messages)
        val row = view.getElementsByClass("rrs01-additional-information")

        val expectedValue = messages("declaration.summary.locations.rrs01AdditionalInformation.text")
        checkSummaryRow(row, "locations.rrs01AdditionalInformation", expectedValue, None, "ign")
      }
    }

    "NOT show the 'RRS01' additional information" when {
      "the goods location code does not end with 'GVM'" in {
        view.getElementsByClass("locations.rrs01AdditionalInformation").size mustBe 0
      }
    }

    "show the office of exit" in {
      val row = view.getElementsByClass("office-of-exit")

      val call = Some(OfficeOfExitController.displayPage)
      checkSummaryRow(row, "locations.officeOfExit", "123", call, "locations.officeOfExit")
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card3ForRoutesAndLocations.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }
  }
}
