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

package forms.declaration.countries

import forms.common.DeclarationPageBaseSpec
import forms.declaration.countries.Countries._
import models.DeclarationType._
import play.api.data.FormError
import unit.base.JourneyTypeTestRunner

class DestinationCountriesSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  "Destination Countries" should {

    "contains object to represent every page and contain correct Id" in {

      OriginationCountryPage.id mustBe "originationCountry"
      DestinationCountryPage.id mustBe "destinationCountry"
      FirstRoutingCountryPage.id mustBe "firstRoutingCountry"
      NextRoutingCountryPage.id mustBe "routingCountry"
    }
  }

  onEveryDeclarationJourney() { request =>
    "Destination Countries" should {

      s"validate form with incorrect value for ${request.declarationType}" in {

        val result = Countries.form(OriginationCountryPage)(request).fillAndValidate(Country(Some("incorrect")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.originationCountry.error"))
      }

      s"validate form with invalid selection of GB for ${request.declarationType}" in {

        val result = Countries.form(DestinationCountryPage)(request).fillAndValidate(Country(Some("GB")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.destinationCountry.error.uk"))
      }

      s"check if the country is duplicated for ${request.declarationType}" in {

        val cachedCountries = Seq(Country(Some("PL")))
        val result = Countries.form(NextRoutingCountryPage, cachedCountries)(request).fillAndValidate(Country(Some("PL")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.routingCountries.duplication"))
      }

      s"validate if country limit is reached for ${request.declarationType}" in {

        val cachedCountries = Seq.fill(99)(Country(Some("PL")))
        val result = Countries.form(NextRoutingCountryPage, cachedCountries)(request).fillAndValidate(Country(Some("GB")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.routingCountries.limit"))
      }
    }
  }

  onJourney(OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY) { request =>
    "Destination Countries" should {

      "return error" when {

        s"there is no value for ${request.declarationType}" in {

          val result = Countries.form(DestinationCountryPage)(request).fillAndValidate(Country(Some("")))

          result.errors mustBe Seq(FormError("countryCode", "declaration.destinationCountry.empty"))
        }
      }
    }
  }

  onJourney(CLEARANCE) { request =>
    "Destination Countries" should {

      "return no errors" when {

        s"field is not provided for ${request.declarationType}" in {

          val result = Countries.form(DestinationCountryPage)(request).fillAndValidate(Country(None))

          result.errors mustBe empty
        }
      }
    }
  }

  "DestinationCountryPage" when {
    testTariffContentKeys(DestinationCountryPage, "tariff.declaration.destinationCountry")
  }
}
