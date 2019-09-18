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

package unit.services.model

import services.model.RejectionReason
import unit.base.UnitSpec

class RejectionReasonSpec extends UnitSpec {

  import services.model.RejectionReason.allRejectedErrors

  "Rejection reason model" should {

    "create correct error based on the list" in {

      val errorCode = "ErrorCode"
      val errorDescription = "Error description"
      val error = List(errorCode, errorDescription)

      RejectionReason.apply(error) mustBe RejectionReason(errorCode, errorDescription)
    }

    "throw an exception when input is incorrect" in {

      intercept[IllegalArgumentException](RejectionReason.apply(List.empty))
    }

    "have 136 errors" in {

      allRejectedErrors.length mustBe 136
    }

    "contain correct values" in {

      allRejectedErrors must contain(RejectionReason("CDS40049", "Quota exhausted."))
      allRejectedErrors must contain(RejectionReason("CDS40051", "Quota blocked."))
      allRejectedErrors must contain(
        RejectionReason(
          "CDS12087",
          "Relation error: VAT Declaring Party Identification (D.E. 3/40), where mandated, must be supplied at either header or item."
        )
      )
      allRejectedErrors must contain(
        RejectionReason("CDS12108", "Obligation error: DUCR is mandatory on an Export Declaration.")
      )
    }

    "correctly read multiline values" in {

      val expectedMessages =
        """Sequence error: The referred declaration does not comply with one of the following conditions:
          |- The AdditionalMessage.declarationReference must refer to an existing declaration (Declaration.reference),
          |- have been accepted,
          |- not be invalidated.""".stripMargin
      val expectedRejectionReason = RejectionReason("CDS12015", expectedMessages)

      allRejectedErrors must contain(expectedRejectionReason)
    }
  }
}
