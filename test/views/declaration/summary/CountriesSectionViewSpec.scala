/*
 * Copyright 2021 HM Revenue & Customs
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

import base.Injector
import forms.declaration.countries.Country
import models.{ExportsDeclaration, Mode}
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.countries_section

class CountriesSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val section = instanceOf[countries_section]

  def view(data: ExportsDeclaration) = section(Mode.Change, data)(messages, journeyRequest())

  "Countries section" should {

    "not display empty routing countries" in {

      val data = aDeclaration(withRoutingQuestion(false), withoutRoutingCountries())

      val row = view(data).getElementsByClass("countriesOfRouting-row")
      row must haveSummaryKey(messages("declaration.summary.countries.routingCountries"))
      row must haveSummaryValue("")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.countries.routingCountries.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(Mode.Change))
    }

    "display single routing country" in {

      val country = Country(Some("GB"))
      val data = aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(country)))

      val row = view(data).getElementsByClass("countriesOfRouting-row")

      val expectedCountry = "United Kingdom (GB)"

      row must haveSummaryValue(expectedCountry)
    }

    "display multiple routing countries separated by comma" in {

      val firstCountryCode = Country(Some("GB"))
      val secondCountryCode = Country(Some("PL"))
      val data = aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(firstCountryCode, secondCountryCode)))

      val row = view(data).getElementsByClass("countriesOfRouting-row")

      val firstExpectedCountry = "United Kingdom (GB)"
      val secondExpectedCountry = "Poland (PL)"

      row must haveSummaryValue(s"$firstExpectedCountry, $secondExpectedCountry")

    }
    "display change button for countries of routing" in {

      val row = view(aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(Country(Some("GB")))))).getElementsByClass("countriesOfRouting-row")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.countries.routingCountries.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(Mode.Change))
    }

    "not have routing country section when question not answered" in {

      view(aDeclaration(withoutRoutingQuestion())).getElementsByClass("countriesOfRouting-row") mustBe empty
    }

    "not display empty country of destination when question not asked" in {

      view(aDeclaration(withoutDestinationCountry())).getElementsByClass("countryOfDestination-row") mustBe empty
    }

    "display country of destination" in {

      val country = Country(Some("GB"))
      val data = aDeclaration(withDestinationCountry(country))

      val row = view(data).getElementsByClass("countryOfDestination-row")

      val expectedCountry = "United Kingdom (GB)"

      row must haveSummaryKey(messages("declaration.summary.countries.countryOfDestination"))
      row must haveSummaryValue(expectedCountry)
    }

    "display change button for country of destination" in {

      val country = Country(Some("GB"))
      val data = aDeclaration(withDestinationCountry(country))

      val row = view(data).getElementsByClass("countryOfDestination-row")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.countries.countryOfDestination.change")
      row must haveSummaryActionsHref(controllers.declaration.routes.DestinationCountryController.displayPage(Mode.Change))
    }

    "display country of dispatch" in {

      val country = Country(Some("GB"))
      val data = aDeclaration(withOriginationCountry(country))

      val row = view(data).getElementsByClass("countryOfDispatch-row")

      val expectedCountry = "United Kingdom (GB)"

      row must haveSummaryKey(messages("declaration.summary.countries.countryOfDispatch"))
      row must haveSummaryValue(expectedCountry)
    }

    "not display empty country of dispatch" in {

      val data = aDeclaration(withoutOriginationCountry())

      view(data).getElementsByClass("countryOfDispatch-row") mustBe empty
    }

    "display change button for country of dispatch" in {

      val country = Country(Some("GB"))
      val data = aDeclaration(withOriginationCountry(country))

      val row = view(data).getElementsByClass("countryOfDispatch-row")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.countries.countryOfDispatch.change")
      row must haveSummaryActionsHref(controllers.declaration.routes.OriginationCountryController.displayPage(Mode.Change))
    }

    "not display country of dispatch when question not asked" in {

      val data = aDeclaration()

      view(data).getElementsByClass("countryOfDispatch-row") mustBe empty
    }
  }
}
