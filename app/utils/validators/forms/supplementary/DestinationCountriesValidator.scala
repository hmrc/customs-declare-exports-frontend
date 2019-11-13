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
import models.DeclarationType
import models.requests.JourneyRequest
import play.api.data.Forms.{seq, text}
import play.api.data.{Form, Forms}
import play.api.mvc.AnyContent
import services.Countries.allCountries
import utils.validators.forms.FieldValidator.areAllElementsUnique
import utils.validators.forms.{Invalid, Valid, ValidationResult, Validator}

object DestinationCountriesValidator extends Validator[DestinationCountries] {

  override def validateOnAddition(element: DestinationCountries)(implicit request: JourneyRequest[AnyContent]): ValidationResult =
    Form(mappingWithValidationForAddition)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  override def validateOnSaveAndContinue(element: DestinationCountries)(implicit request: JourneyRequest[AnyContent]): ValidationResult =
    Form(if (request.isType(DeclarationType.SIMPLIFIED)) simplifiedMapping else standardAndSuppMapping)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  val mappingWithValidationForAddition = Forms.mapping(
    "countryOfDispatch" -> text(),
    "countriesOfRouting" -> seq(
      text()
        .verifying("declaration.destinationCountries.countriesOfRouting.empty", _.trim.nonEmpty)
        .verifying(
          "declaration.destinationCountries.countriesOfRouting.error",
          input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
        )
    ).verifying("supplementary.duplication", areAllElementsUnique)
      .verifying("supplementary.limit", countries => countries.size <= DestinationCountries.limit),
    "countryOfDestination" -> text()
  )(DestinationCountries.apply)(DestinationCountries.unapply)

  val simplifiedMapping = Forms.mapping(
    "countryOfDispatch" -> text(),
    "countriesOfRouting" -> seq(
      text()
        .verifying(
          "declaration.destinationCountries.countriesOfRouting.error",
          input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
        )
    ).verifying("supplementary.duplication", areAllElementsUnique)
      .verifying("supplementary.limit", countries => countries.size <= DestinationCountries.limit),
    "countryOfDestination" -> text()
      .verifying("declaration.destinationCountries.countryOfDestination.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDestination.error",
        input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
      )
  )(DestinationCountries.apply)(DestinationCountries.unapply)

  val standardAndSuppMapping = Forms.mapping(
    "countryOfDispatch" -> text()
      .verifying("declaration.destinationCountries.countryOfDispatch.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDispatch.error",
        input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
      ),
    "countriesOfRouting" -> seq(
      text()
        .verifying(
          "declaration.destinationCountries.countriesOfRouting.error",
          input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
        )
    ).verifying("supplementary.duplication", areAllElementsUnique)
      .verifying("supplementary.limit", countries => countries.size <= DestinationCountries.limit),
    "countryOfDestination" -> text()
      .verifying("declaration.destinationCountries.countryOfDestination.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDestination.error",
        input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
      )
  )(DestinationCountries.apply)(DestinationCountries.unapply)

}
