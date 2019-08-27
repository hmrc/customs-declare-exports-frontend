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

package services

import unit.base.UnitSpec

class NationalAdditionalCodeSpec extends UnitSpec {

  "National Additional Code" should {
    "read from file" in {
      val codes = NationalAdditionalCode.all
      codes must contain(NationalAdditionalCode("VATE")) // First in file
      codes must contain(NationalAdditionalCode("X99D")) // Last in file
    }

    "exclude header" in {
      val codes = NationalAdditionalCode.all
      codes mustNot contain(NationalAdditionalCode("Code"))
    }

    "sort by value" in {
      val codes = NationalAdditionalCode.all.filter(code => Set("VATE", "X99D").contains(code.value))
      codes mustBe List(NationalAdditionalCode("VATE"), NationalAdditionalCode("X99D"))
    }
  }

}
