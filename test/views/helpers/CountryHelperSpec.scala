/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi

import java.util.Locale

class CountryHelperSpec extends UnitSpec with Injector {

  val countryHelper = instanceOf[CountryHelper]
  implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  "CountryHelper getShortNameForCountry" should {
    "get the short name for a country when it is available" in {
      countryHelper.getShortNameForCountry(
        Country("United States of America (the), Including Puerto Rico", "US")
      ) mustBe "The United States of America"
    }

    "get the full name for a country when a short name is not available" in {
      countryHelper.getShortNameForCountry(
        Country("New Caledonia, Including Loyalty Islands (Lifou, Mare and Ouvea)", "NC")
      ) mustBe "New Caledonia, Including Loyalty Islands (Lifou, Mare and Ouvea)"
    }
  }

  "CountryHelper getShortNameForCountryCode" should {
    "get the short name for a country when it is available" in {
      countryHelper.getShortNameForCountryCode("US").get mustBe "The United States of America"
    }

    "get the full name for a country when a short name is not available" in {
      countryHelper.getShortNameForCountryCode("NC").get mustBe "New Caledonia, Including Loyalty Islands (Lifou, Mare and Ouvea)"
    }
  }
}
