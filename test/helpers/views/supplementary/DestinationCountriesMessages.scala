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

package helpers.views.supplementary

trait DestinationCountriesMessages {

  val prefix: String = "supplementary.destinationCountries"

  val title: String = prefix + ".title"
  val countryOfDestination: String = prefix + ".countryOfDestination"
  val countryOfDestinationError: String = prefix + ".countryOfDestination.error"
  val countryOfDestinationEmpty: String = prefix + ".countryOfDestination.empty"
  val countryOfDispatch: String = prefix + ".countryOfDispatch"
  val countryOfDispatchError: String = prefix + ".countryOfDispatch.error"
  val countryOfDispatchEmpty: String = prefix + ".countryOfDispatch.empty"
}
