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

  def mandatoryForm(supplementaryUnits: String): Form[SupplementaryUnits] =
    SupplementaryUnits
      .form(false)
      .bind(Map(SupplementaryUnits.supplementaryUnits -> supplementaryUnits))

  def yesNoForm(hasSupplementaryUnits: String, supplementaryUnits: String): Form[SupplementaryUnits] =
    SupplementaryUnits
      .form(true)
      .bind(Map(SupplementaryUnits.hasSupplementaryUnits -> hasSupplementaryUnits, SupplementaryUnits.supplementaryUnits -> supplementaryUnits))

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
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.error"))
        yesNoForm("Yes", "0000").errors mustBe expectedErrors
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

  "Form for Supplementary Units page with single input field" should {

    "have no errors" when {
      "the user enters a valid 'Supplementary Units'" in {
        mandatoryForm("100").errors must be(empty)
      }
    }

    "have errors" when {

      "the user enters a non-numeric 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.error"))
        mandatoryForm("abcd").errors mustBe expectedErrors
      }

      "the user enters a 'Supplementary Units' of only zeroes" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.error"))
        mandatoryForm("0000").errors mustBe expectedErrors
      }

      "the user enters a too long 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.length"))
        mandatoryForm("12345678901234567").errors mustBe expectedErrors
      }

      "the user does not enter no 'Supplementary Units'" in {
        val expectedErrors = List(FormError(supplementaryUnits, "declaration.supplementaryUnits.quantity.empty"))
        mandatoryForm("").errors mustBe expectedErrors
      }
    }
  }
}
