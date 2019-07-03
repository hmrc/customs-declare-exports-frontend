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

import org.scalatest.{Matchers, WordSpec}
import services.model.{OfficeOfExit, SupervisingCustomsOffice}

class SupervisingCustomsOfficesSpec extends WordSpec with Matchers {

  "SupervisingCustomsOffice" should {

    "have 143 entries" in {
      SupervisingCustomsOffices.all.length shouldBe 143
    }

    "read values from CSV and order by description, alphabetically ascending" in {
      SupervisingCustomsOffices.all should contain inOrder(
        SupervisingCustomsOffice("GBABD001", "Aberdeen, Ruby House"),
        SupervisingCustomsOffice("GBLBA001", "Leeds, Peter Bennett House (Customs Authorisations & Reviews Team)"),
        SupervisingCustomsOffice("GBYRK001", "York, Swinson House")
      )
    }
  }
}
