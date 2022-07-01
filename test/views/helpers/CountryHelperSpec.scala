/*
 * Copyright 2022 HM Revenue & Customs
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

package views.helpers

import base.{Injector, UnitSpec}
import models.codes.Country

class CountryHelperSpec extends UnitSpec with Injector {

  val countryHelper = instanceOf[CountryHelper]

  "CountryHelper getShortNameForCountry" should {
    "get the short name for a country when it is available" in {
      countryHelper.getShortNameForCountry(
        Country("United States of America (the), Including Puerto Rico", "US")
      ) mustBe "the United States of America"
    }

    "get the full name for a country when a short name is not available" in {
      countryHelper.getShortNameForCountry(
        Country("Taiwan - Separate customs territory of Taiwan, Penghu, Kinmen and Matsu", "TW")
      ) mustBe "Taiwan - Separate customs territory of Taiwan, Penghu, Kinmen and Matsu"
    }
  }
}
