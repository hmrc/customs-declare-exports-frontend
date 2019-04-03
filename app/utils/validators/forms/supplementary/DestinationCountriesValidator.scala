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

import forms.declaration.destinationCountries.DestinationCountriesStandard
import play.api.data.Forms.{seq, text}
import play.api.data.{Form, Forms}
import services.Countries.allCountries
import utils.validators.forms.FieldValidator.areAllElementsUnique
import utils.validators.forms.{Invalid, Valid, ValidationResult, Validator}

object DestinationCountriesValidator extends Validator[DestinationCountriesStandard] {

  override def validateOnAddition(element: DestinationCountriesStandard): ValidationResult =
    Form(mappingWithValidationForAddition)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  override def validateOnSaveAndContinue(element: DestinationCountriesStandard): ValidationResult =
    Form(mappingWithValidation)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  val mappingWithValidationForAddition = Forms.mapping(
    "countryOfDispatch" -> text(),
    "countriesOfRouting" -> seq(
      text()
        .verifying("declaration.destinationCountries.countriesOfRouting.empty", _.trim.nonEmpty)
        .verifying(
          "declaration.destinationCountries.countriesOfRouting.error",
          input => input.isEmpty || allCountries.exists(country => country.countryName == input)
        )
    ).verifying("supplementary.duplication", areAllElementsUnique)
      .verifying("supplementary.limit", countries => countries.size <= DestinationCountriesStandard.limit),
    "countryOfDestination" -> text()
  )(DestinationCountriesStandard.apply)(DestinationCountriesStandard.unapply)

  val mappingWithValidation = Forms.mapping(
    "countryOfDispatch" -> text()
      .verifying("declaration.destinationCountries.countryOfDispatch.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDispatch.error",
        input => input.isEmpty || allCountries.exists(country => country.countryName == input)
      ),
    "countriesOfRouting" -> seq(
      text()
        .verifying("declaration.destinationCountries.countriesOfRouting.empty", _.trim.nonEmpty)
        .verifying(
          "declaration.destinationCountries.countriesOfRouting.error",
          input => input.isEmpty || allCountries.exists(country => country.countryName == input)
        )
    ).verifying("supplementary.duplication", areAllElementsUnique)
      .verifying("supplementary.limit", countries => countries.size <= DestinationCountriesStandard.limit)
      .verifying("declaration.destinationCountries.countriesOfRouting.empty", _.nonEmpty),
    "countryOfDestination" -> text()
      .verifying("declaration.destinationCountries.countryOfDestination.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDestination.error",
        input => input.isEmpty || allCountries.exists(country => country.countryName == input)
      )
  )(DestinationCountriesStandard.apply)(DestinationCountriesStandard.unapply)

}
