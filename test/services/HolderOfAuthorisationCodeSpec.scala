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
      codes must contain(HolderOfAuthorisationCode("ACE", "Authorised consignee for Union transit")) // First in file
      codes must contain(HolderOfAuthorisationCode("UKCS", "UK Continental shelf")) // Last in file
    }

    "exclude header" in {
      val codes = HolderOfAuthorisationCode.all
      codes mustNot contain(HolderOfAuthorisationCode("Code", "Description"))
    }

    "exclude empty lines" in {
      val codes = HolderOfAuthorisationCode.all
      codes.map(_.code.trim).filter(_.isEmpty) mustBe empty
    }

    "sort by description" in {
      val codes = HolderOfAuthorisationCode.all.filter(code => Set("CVA", "CW1").contains(code.code))
      codes mustBe List(
        HolderOfAuthorisationCode("CW1", "Operation of storage facilities for the customs warehousing of goods in a public customs warehouse type I"),
        HolderOfAuthorisationCode("CVA", "Simplification of the determination of amounts being part of the customs value of goods")
      )
    }

  }
}
