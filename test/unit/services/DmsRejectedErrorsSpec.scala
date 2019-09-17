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

package unit.services

import services.DmsRejectedErrors
import services.model.DmsRejectedError
import unit.base.UnitSpec

class DmsRejectedErrorsSpec extends UnitSpec {

  import DmsRejectedErrors._

  "Dms Rejected Errors" should {

    "have 136 errors" in {

      allRejectedErrors.length mustBe 136
    }

    "contain correct values" in {

      allRejectedErrors must contain(DmsRejectedError("CDS40049", "Quota exhausted."))
      allRejectedErrors must contain(DmsRejectedError("CDS40051", "Quota blocked."))
      allRejectedErrors must contain(
        DmsRejectedError(
          "CDS12087",
          "Relation error: VAT Declaring Party Identification (D.E. 3/40), where mandated, must be supplied at either header or item."
        )
      )
      allRejectedErrors must contain(
        DmsRejectedError("CDS12108", "Obligation error: DUCR is mandatory on an Export Declaration.")
      )
    }
  }
}
