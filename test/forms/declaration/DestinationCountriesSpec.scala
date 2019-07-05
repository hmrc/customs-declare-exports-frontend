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

package forms.declaration

import forms.declaration.destinationCountries.DestinationCountries
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}

object DestinationCountriesSpec {
  val correctDestinationCountries = DestinationCountries("PL", "PL")

  val correctDestinationCountriesJSON: JsValue = JsObject(
    Map(
      "countryOfDestination" -> JsString("PL"),
      "countriesOfRouting" -> JsArray(),
      "countryOfDispatch" -> JsString("PL")
    )
  )
  val emptyDestinationCountriesJSON: JsValue = JsObject(
    Map("countryOfDestination" -> JsString(""), "countriesOfRouting" -> JsArray(), "countryOfDispatch" -> JsString(""))
  )

  val emptyDestinationCountrySupplementaryJSON: JsValue = JsObject(
    Map(
      "countryOfDestination" -> JsString(""),
      "countriesOfRouting" -> JsArray(),
      "countryOfDispatch" -> JsString("MW")
    )
  )

  val incorrectDestinationCountriesJSON: JsValue = JsObject(
    Map(
      "countryOfDestination" -> JsString("Country"),
      "countriesOfRouting" -> Json.arr(JsString("Country")),
      "countryOfDispatch" -> JsString("Country")
    )
  )
}
