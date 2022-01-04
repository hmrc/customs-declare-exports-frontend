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

package services

import base.UnitSpec
import services.model.OfficeOfExit

class OfficeOfExitsSpec extends UnitSpec {

  "OfficeOfExits" should {

    "have 131 entries" in {
      OfficeOfExits.all.length mustBe 134
    }

    "read values from CSV and order by description, alphabetically ascending" in {
      OfficeOfExits.all must contain inOrder (
        OfficeOfExit("GB000411", "Aberdeen Airport"),
        OfficeOfExit("GB000060", "Dover/ Folkestone Eurotunnel Freight"),
        OfficeOfExit("GB003280", "Workington")
      )
    }
  }
}
