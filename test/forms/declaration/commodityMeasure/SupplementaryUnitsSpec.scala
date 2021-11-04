/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.declaration.commodityMeasure

import base.UnitSpec
import forms.common.DeclarationPageBaseSpec
import forms.declaration.commodityMeasure.SupplementaryUnits.{hasSupplementaryUnits, supplementaryUnits}
import play.api.data.{Form, FormError}

class SupplementaryUnitsSpec extends UnitSpec with DeclarationPageBaseSpec {

  def form(hasSupplementaryUnits: String, supplementaryUnits: String): Form[SupplementaryUnits] =
    SupplementaryUnits.form.bind(
      Map(SupplementaryUnits.hasSupplementaryUnits -> hasSupplementaryUnits, SupplementaryUnits.supplementaryUnits -> supplementaryUnits)
    )

  "Supplementary Units form" should {

    "have no errors" when {

      "provided with valid 'Supplementary Units' when the user selects 'Yes'" in {
        form("Yes", "100").errors must be(empty)
      }

      "provided with no 'Supplementary Units' when the user selects 'No'" in {
        form("No", "").errors must be(empty)
      }
    }

    "have errors" when {

      "no radio is selected" in {
        val expectedErrors = List(FormError(hasSupplementaryUnits, "declaration.supplementaryUnits.empty"))
        form("", "").errors mustBe expectedErrors
      }

      "provided with non-numeric 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.amount.error"))
        form("Yes", "abcd").errors mustBe expectedErrors
      }

      "provided with no 'Supplementary Units' when the user selects 'Yes'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.amount.empty"))
        form("Yes", "").errors mustBe expectedErrors
      }
    }
  }
}
