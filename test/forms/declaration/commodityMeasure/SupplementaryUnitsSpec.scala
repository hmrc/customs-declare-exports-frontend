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

package forms.declaration.commodityMeasure

import base.UnitSpec
import forms.common.DeclarationPageBaseSpec
import forms.declaration.commodityMeasure.SupplementaryUnits.{form, hasSupplementaryUnits, supplementaryUnits}
import play.api.data.{Form, FormError}

class SupplementaryUnitsSpec extends UnitSpec with DeclarationPageBaseSpec {

  def yesNoForm(hasSupplementaryUnits: String, supplementaryUnits: String): Form[SupplementaryUnits] =
    form.bind(Map(SupplementaryUnits.hasSupplementaryUnits -> hasSupplementaryUnits, SupplementaryUnits.supplementaryUnits -> supplementaryUnits))

  "Form for Supplementary Units page with Yes/No radios" should {

    "have no errors" when {

      "the user selects 'Yes' and enters a valid 'Supplementary Units'" in {
        yesNoForm("Yes", "100").errors must be(empty)
      }

      "the user selects 'No' and does not enter a 'Supplementary Units'" in {
        yesNoForm("No", "").errors must be(empty)
      }
    }

    "have errors" when {

      "no radio is selected" in {
        val expectedErrors = List(FormError(hasSupplementaryUnits, "declaration.supplementaryUnits.yesNo.empty"))
        yesNoForm("", "").errors mustBe expectedErrors
      }

      "the user selects 'Yes' and enters a non-numeric 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.error"))
        yesNoForm("Yes", "abcd").errors mustBe expectedErrors
      }

      "the user selects 'Yes' and enters a 'Supplementary Units' of only zeroes" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.empty"))
        yesNoForm("Yes", "0000").errors mustBe expectedErrors
        yesNoForm("Yes", "00.00").errors mustBe expectedErrors
        yesNoForm("Yes", "000,0").errors mustBe expectedErrors
      }

      "the user selects 'Yes' and enters a too long 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.length"))
        yesNoForm("Yes", "12345678901234567").errors mustBe expectedErrors
      }

      "the user selects 'Yes' and does not enter a 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.empty"))
        yesNoForm("Yes", "").errors mustBe expectedErrors
      }
    }
  }
}
