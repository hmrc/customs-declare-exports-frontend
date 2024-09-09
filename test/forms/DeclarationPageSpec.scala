/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import models.DeclarationType._
import base.UnitSpec

class DeclarationPageSpec extends UnitSpec {

  "DeclarationPageSpec" when {
    "getJourneyTypeSpecialisation method is called" which {

      val COMMON_KEY = "common"
      val CLEARANCE_KEY = "clearance"

      "when passed a DeclarationType of STANDARD, SUPPLEMENTARY, SIMPLIFIED or OCCASIONAL " should {
        "return the message specialisation string 'common'" in {
          DeclarationPage.getJourneyTypeSpecialisation(STANDARD) mustBe COMMON_KEY
          DeclarationPage.getJourneyTypeSpecialisation(SUPPLEMENTARY) mustBe COMMON_KEY
          DeclarationPage.getJourneyTypeSpecialisation(SIMPLIFIED) mustBe COMMON_KEY
          DeclarationPage.getJourneyTypeSpecialisation(OCCASIONAL) mustBe COMMON_KEY
        }
      }

      "when passed a DeclarationType of CLEARANCE " should {
        "return the message specialisation string 'clearance'" in {
          DeclarationPage.getJourneyTypeSpecialisation(CLEARANCE) mustBe CLEARANCE_KEY
        }
      }
    }
  }
}
