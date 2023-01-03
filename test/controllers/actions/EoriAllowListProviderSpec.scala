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
import play.api.Configuration

class EoriAllowListProviderSpec extends UnitWithMocksSpec {

  "EoriAllowListProvider" should {
    "reload correctly from configuration" in {
      val config = Configuration("allowList.eori.0" -> "1234")
      val provider = new EoriAllowListProvider(config)
      provider.get() mustBe a[EoriAllowList]
    }
    "throw exception when there is not configuration key" in {
      val provider = new EoriAllowListProvider(Configuration.empty)
      an[Exception] mustBe thrownBy {
        provider.get()
      }
    }
  }
}
