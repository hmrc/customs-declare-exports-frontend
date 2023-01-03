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

package controllers.actions

import base.UnitWithMocksSpec

class EoriAllowListSpec extends UnitWithMocksSpec {

  "Eori allow list" when {
    "has empty" should {
      "allow everyone" in {
        val allowList = new EoriAllowList(Seq.empty)
        allowList.allows("12345") mustBe true
        allowList.allows("0987") mustBe true
      }
    }
    "has elements" should {
      val allowList = new EoriAllowList(Seq("12345"))
      "allow listed eori" in {
        allowList.allows("12345") mustBe true
      }
      "disallow not listed eori" in {
        allowList.allows("0987") mustBe false
      }
    }
  }
}
