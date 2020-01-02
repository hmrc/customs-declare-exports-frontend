/*
 * Copyright 2020 HM Revenue & Customs
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

class HolderOfAuthorisationCodeSpec extends UnitSpec {

  "Holder of Authorisation Code" should {
    "read from file" in {
      val codes = HolderOfAuthorisationCode.all
      codes must contain(HolderOfAuthorisationCode("ACE")) // First in file
      codes must contain(HolderOfAuthorisationCode("UKCS")) // Last in file
    }

    "exclude header" in {
      val codes = HolderOfAuthorisationCode.all
      codes mustNot contain(HolderOfAuthorisationCode("Code"))
    }

    "exclude empty lines" in {
      val codes = HolderOfAuthorisationCode.all
      codes.map(_.value.trim).filter(_.isEmpty) mustBe empty
    }

    "sort by value" in {
      val codes = HolderOfAuthorisationCode.all.filter(code => Set("ACE", "UKCS").contains(code.value))
      codes mustBe List(HolderOfAuthorisationCode("ACE"), HolderOfAuthorisationCode("UKCS"))
    }

    "contain values for No Deal (see CEDS-1773)" in {
      val codes = HolderOfAuthorisationCode.all
      codes must contain(HolderOfAuthorisationCode("NIRE"))
      codes must contain(HolderOfAuthorisationCode("RORO"))
    }
  }
}
