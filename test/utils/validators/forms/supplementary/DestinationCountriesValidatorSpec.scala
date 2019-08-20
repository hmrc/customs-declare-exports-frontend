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

package utils.validators.forms.supplementary
import forms.declaration.destinationCountries.DestinationCountries
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import utils.validators.forms.{Invalid, Valid}

class DestinationCountriesValidatorSpec extends WordSpec with MustMatchers {

  "DestinationCountriesValidator validateOnAddition" should {
    "return valid" when {
      "routing countries empty" in {

        val destinationCountries = DestinationCountries("PL", Seq.empty, "FR")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }

      "routing countries populated" in {

        val destinationCountries = DestinationCountries("PL", Seq("YE"), "FR")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }

      "country of dispatch empty" in {

        val destinationCountries = DestinationCountries("", Seq.empty, "FR")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }

      "country of destination empty" in {

        val destinationCountries = DestinationCountries("", Seq.empty, "")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }
    }
    "return invalid" when {
      "routing country does not exist" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "XX"), "FR")

        val expectedValidationResult = Invalid(
          errors = Seq(FormError("countriesOfRouting[1]", "declaration.destinationCountries.countriesOfRouting.error"))
        )

        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(expectedValidationResult)
      }

      "routing country duplicated" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "YE"), "FR")

        val expectedValidationResult =
          Invalid(errors = Seq(FormError("countriesOfRouting", "supplementary.duplication")))

        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(expectedValidationResult)
      }
    }
  }

  "DestinationCountriesValidator validateOnSaveAndContinue" should {
    "return valid" when {
      "routing countries empty" in {

        val destinationCountries = DestinationCountries("PL", Seq.empty, "FR")
        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(Valid)
      }

      "routing countries populated" in {

        val destinationCountries = DestinationCountries("PL", Seq("YE"), "FR")
        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(Valid)
      }
    }

    "return invalid" when {
      "routing country does not exist" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "XX"), "FR")

        val expectedValidationResult = Invalid(
          errors = Seq(FormError("countriesOfRouting[1]", "declaration.destinationCountries.countriesOfRouting.error"))
        )

        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(expectedValidationResult)
      }

      "routing country duplicated" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "YE"), "FR")

        val expectedValidationResult =
          Invalid(errors = Seq(FormError("countriesOfRouting", "supplementary.duplication")))

        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(expectedValidationResult)
      }

      "country of dispatch and destination empty" in {

        val destinationCountries = DestinationCountries("", Seq.empty, "")

        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError("countryOfDispatch", "declaration.destinationCountries.countryOfDispatch.empty"),
            FormError("countryOfDestination", "declaration.destinationCountries.countryOfDestination.empty")
          )
        )

        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(expectedValidationResult)
      }
    }
  }
}
