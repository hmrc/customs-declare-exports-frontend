/*
 * Copyright 2018 HM Revenue & Customs
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

package services

import base.CustomExportsBaseSpec

class CountriesSpec extends CustomExportsBaseSpec {

  val countries = new Countries(appConfig)

  "Countries" should {
    "give all countries with codes in alphabetical order of country name with filtering according to permitted MDG values" in {
      countries.all.contains(Country("Afghanistan", "AF")) must be(true)
      countries.all.contains(Country("Curaçao", "CW")) must be(true)
      countries.all.contains(Country("Réunion", "RE")) must be(true)
      countries.all.contains(Country("Zimbabwe", "ZW")) must be(true)
      countries.all.contains(Country("Åland Islands", "AX")) must be(true)
    }
  }
}
