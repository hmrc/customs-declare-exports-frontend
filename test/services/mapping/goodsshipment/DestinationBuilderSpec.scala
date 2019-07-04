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

package services.mapping.goodsshipment

import forms.ChoiceSpec
import forms.declaration.DestinationCountriesSpec
import forms.declaration.destinationCountries.DestinationCountries
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class DestinationBuilderSpec extends WordSpec with Matchers {

  "DestinationBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.Destination instance" when {
      "submitting a supplementary journey" when {

        "countryOfDestination has been supplied" in {
          implicit val cacheMap: CacheMap =
            CacheMap(
              "CacheID",
              Map(
                DestinationCountries.formId -> DestinationCountriesSpec.correctDestinationCountriesJSON
              )
            )
          val destination = DestinationBuilder.build(cacheMap, ChoiceSpec.supplementaryChoice)
          destination.getCountryCode.getValue should be("PL")
        }
        "countryOfDestination has not been supplied" in {
          implicit val cacheMap: CacheMap =
            CacheMap(
              "CacheID",
              Map(
                DestinationCountries.formId -> DestinationCountriesSpec.emptyDestinationCountriesJSON
              )
            )
          DestinationBuilder.build(cacheMap, ChoiceSpec.supplementaryChoice) should be(null)
        }
      }

      "submitting a standard journey" when {
        "countryOfDestination has been supplied" in {
          implicit val cacheMap: CacheMap =
            CacheMap(
              "CacheID",
              Map(DestinationCountries.formId -> Json.toJson(DestinationCountries("GB", Seq("PT"), "PL")))
            )
          val destination = DestinationBuilder.build(cacheMap, ChoiceSpec.standardChoice)
          destination.getCountryCode.getValue should be("PL")
        }
        "countryOfDestination has not been supplied" in {
          implicit val cacheMap: CacheMap =
            CacheMap(
              "CacheID",
              Map(DestinationCountries.formId -> Json.toJson(DestinationCountries("", Seq(""), "")))
            )
          DestinationBuilder.build(cacheMap, ChoiceSpec.standardChoice) should be(null)
        }
      }
    }
  }
}
