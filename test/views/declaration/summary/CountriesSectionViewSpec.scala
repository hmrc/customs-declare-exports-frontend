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

package views.declaration.summary

import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.countries_section

class CountriesSectionViewSpec extends UnitViewSpec with ExportsTestData {

  "Countries section" should {

    "not display empty routing countries" in {

      val data = aDeclaration(withRoutingQuestion(false), withoutRoutingCountries())

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("countriesOfRouting-label").text() mustBe messages("declaration.summary.countries.routingCountries")
      view.getElementById("countriesOfRouting").text() mustBe empty
    }

    "display single routing country" in {

      val countryCode = "GB"
      val data = aDeclaration(withRoutingQuestion(true), withRoutingCountries(Seq(countryCode)))

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      val expectedCountry = "United Kingdom (GB)"

      view.getElementById("countriesOfRouting-label").text() mustBe messages("declaration.summary.countries.routingCountries")
      view.getElementById("countriesOfRouting").text() mustBe expectedCountry
    }

    "display multiple routing countries separated by comma" in {

      val firstCountryCode = "GB"
      val secondCountryCode = "PL"
      val data = aDeclaration(withRoutingQuestion(true), withRoutingCountries(Seq(firstCountryCode, secondCountryCode)))

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      val firstExpectedCountry = "United Kingdom (GB)"
      val secondExpectedCountry = "Poland (PL)"

      view.getElementById("countriesOfRouting-label").text() mustBe messages("declaration.summary.countries.routingCountries")
      view.getElementById("countriesOfRouting").text() mustBe s"$firstExpectedCountry, $secondExpectedCountry"

    }
    "display change button for countries of routing" in {

      val view = countries_section(Mode.Normal, aDeclaration(withRoutingQuestion(true), withRoutingCountries(Seq("GB"))))(messages, journeyRequest())

      val List(change, accessibleChange) = view.getElementById("countriesOfRouting-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.countries.routingCountries.change")

      view.getElementById("countriesOfRouting-change") must haveHref(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage())
    }

    "not have routing country section when question not answered" in {
      val view = countries_section(Mode.Normal, aDeclaration(withoutRoutingQuestion()))(messages, journeyRequest())

      view.getElementById("countriesOfRouting-label") mustBe null
      view.getElementById("countriesOfRouting") mustBe null
    }

    "not display empty country of destination when question not asked" in {
      val view = countries_section(Mode.Normal, aDeclaration(withoutDestinationCountry()))(messages, journeyRequest())

      view.getElementById("countryOfDestination-label") mustBe null
      view.getElementById("countryOfDestination") mustBe null
    }

    "display country of destination" in {

      val countryCode = "GB"
      val data = aDeclaration(withDestinationCountry(countryCode))

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      val expectedCountry = "United Kingdom (GB)"

      view.getElementById("countryOfDestination-label").text() mustBe messages("declaration.summary.countries.countryOfDestination")
      view.getElementById("countryOfDestination").text() mustBe expectedCountry
    }

    "display change button for country of destination" in {

      val countryCode = "GB"
      val data = aDeclaration(withDestinationCountry(countryCode))

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      val List(change, accessibleChange) = view.getElementById("countryOfDestination-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.countries.countryOfDestination.change")

      view.getElementById("countryOfDestination-change") must haveHref(controllers.declaration.routes.DestinationCountryController.displayPage())
    }

    "display country of dispatch" in {

      val countryCode = "GB"
      val data = aDeclaration(withOriginationCountry(countryCode))

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      val expectedCountry = "United Kingdom (GB)"

      view.getElementById("countryOfDispatch-label").text() mustBe messages("declaration.summary.countries.countryOfDispatch")
      view.getElementById("countryOfDispatch").text() mustBe expectedCountry
    }

    "not display empty country of dispatch" in {

      val data = aDeclaration(withoutOriginationCountry())

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("countryOfDispatch-label") mustBe null
      view.getElementById("countryOfDispatch") mustBe null
    }

    "display change button for country of dispatch" in {

      val countryCode = "GB"
      val data = aDeclaration(withOriginationCountry(countryCode))

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      val List(change, accessibleChange) = view.getElementById("countryOfDispatch-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.countries.countryOfDispatch.change")

      view.getElementById("countryOfDispatch-change") must haveHref(controllers.declaration.routes.OriginationCountryController.displayPage())
    }

    "not display country of dispatch when question not asked" in {

      val data = aDeclaration()

      val view = countries_section(Mode.Normal, data)(messages, journeyRequest())

      view.getElementById("countryOfDispatch-label") mustBe null
      view.getElementById("countryOfDispatch") mustBe null
      view.getElementById("countryOfDispatch-change") mustBe null
    }
  }
}
