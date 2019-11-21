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

package forms.declaration.destinationCountries

import forms.declaration.RoutingQuestionYesNo
import forms.declaration.destinationCountries.DestinationCountries._
import play.api.data.FormError
import unit.base.UnitSpec

class DestinationCountriesSpec extends UnitSpec {

  "Destination Countries" should {

    "contains object to represent every page and contain correct Id" in {

      OriginationCountryPage.id mustBe "originationCountry"
      DestinationCountryPage.id mustBe "destinationCountry"
      FirstRoutingCountryPage.id mustBe "firstRoutingCountry"
      NextRoutingCountryPage.id mustBe "routingCountry"
    }

    "validate form with no value" in {

      val result = DestinationCountries.form(OriginationCountryPage).fillAndValidate("")

      result.errors mustBe Seq(FormError("country", "declaration.originationCountry.empty"))
    }

    "validate form with incorrect value" in {

      val result = DestinationCountries.form(OriginationCountryPage).fillAndValidate("incorrect")

      result.errors mustBe Seq(FormError("country", "declaration.originationCountry.error"))
    }

    "check if the country is duplicated" in {

      val form = DestinationCountries.form(OriginationCountryPage).fillAndValidate("PL")
      val cachedCountries = Seq("PL")

      val result = DestinationCountries.validateCountryDuplication(form, cachedCountries)

      result.errors mustBe Seq(FormError("country", "declaration.routingCountries.duplication"))
    }

    "validate if country limit is reached" in {

      val cachedCountries = Seq.fill(99)("PL")
      val form = RoutingQuestionYesNo.form()

      val result = DestinationCountries.validateCountriesLimit(form, cachedCountries)

      result.errors mustBe Seq(FormError("country", "declaration.routingCountries.limit"))
    }
  }
}
