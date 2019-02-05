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

package forms.supplementary

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DestinationCountriesSpec extends WordSpec with MustMatchers {
  import DestinationCountriesSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val destinationCountries = correctDestinationCountries
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.destination.countryCode" -> "PL",
        "declaration.goodsShipment.exportCountry.id" -> "PL"
      )

      destinationCountries.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object DestinationCountriesSpec {
  val correctDestinationCountries =
    DestinationCountries(countryOfDestination = Some("Poland"), countryOfDispatch = "Poland")
  val emptyDestinationCountries =
    DestinationCountries(countryOfDestination = None, countryOfDispatch = "")
  val incorrectDestinationCountries =
    DestinationCountries(countryOfDestination = Some("Country"), countryOfDispatch = "Country")

  val correctDestinationCountriesJSON: JsValue = JsObject(
    Map("countryOfDestination" -> JsString("Poland"), "countryOfDispatch" -> JsString("Poland"))
  )
  val emptyDestinationCountriesJSON: JsValue = JsObject(
    Map("countryOfDestination" -> JsString(""), "countryOfDispatch" -> JsString(""))
  )
  val incorrectDestinationCountriesJSON: JsValue = JsObject(
    Map("countryOfDestination" -> JsString("Country"), "countryOfDispatch" -> JsString("Country"))
  )
}
