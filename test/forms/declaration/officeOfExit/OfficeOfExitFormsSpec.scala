/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.declaration.officeOfExit

import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError

class OfficeOfExitFormsSpec extends WordSpec with MustMatchers {

  trait SetUp {
    val officeFieldId = "officeId"
  }

  trait SupplementarySetUp extends SetUp {
    val supplementaryForm = OfficeOfExitForms.supplementaryForm
  }

  trait StandardSetUp extends SetUp {
    val standardForm = OfficeOfExitForms.standardForm

    val circumstancesCodeFieldId = "circumstancesCode"
  }

  "Supplementary declaration mapping" should {

    "return form with errors" when {

      "office of exit is empty" in new SupplementarySetUp {

        val data = OfficeOfExitSupplementary("")
        val errors = supplementaryForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError(officeFieldId, "declaration.officeOfExit.empty"))
      }

      "office of exit is too short" in new SupplementarySetUp {

        val data = OfficeOfExitSupplementary("1234")
        val errors = supplementaryForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError(officeFieldId, "declaration.officeOfExit.length"))
      }

      "office of exit is too long" in new SupplementarySetUp {

        val data = OfficeOfExitSupplementary("123456789")
        val errors = supplementaryForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError(officeFieldId, "declaration.officeOfExit.length"))
      }

      "office of exit contain special characters" in new SupplementarySetUp {

        val data = OfficeOfExitSupplementary("12!@#$%^")
        val errors = supplementaryForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError(officeFieldId, "declaration.officeOfExit.specialCharacters"))
      }
    }

    "return form without errors" when {

      "office of exit is correct" in new SupplementarySetUp {

        val data = OfficeOfExitSupplementary("12345678")
        val errors = supplementaryForm.fillAndValidate(data).errors

        errors.length must be(0)
      }
    }
  }

  "Standard declaration mapping" should {

    "return form with errors" when {

      "all inputs are empty" in new StandardSetUp {

        val data = OfficeOfExitStandard("", "")
        val errors = standardForm.fillAndValidate(data).errors

        errors.length must be(2)
        errors(0) must be(FormError(officeFieldId, "declaration.officeOfExit.empty"))
        errors(1) must be(FormError(circumstancesCodeFieldId, "standard.officeOfExit.circumstancesCode.error"))
      }

      "office of exit too short" in new StandardSetUp {

        val data = OfficeOfExitStandard("123456", "Yes")
        val errors = standardForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors(0) must be(FormError(officeFieldId, "declaration.officeOfExit.length"))
      }

      "office of exit too long" in new StandardSetUp {

        val data = OfficeOfExitStandard("123456789", "Yes")
        val errors = standardForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors(0) must be(FormError(officeFieldId, "declaration.officeOfExit.length"))
      }

      "office of exit contains special characters" in new StandardSetUp {
        val data = OfficeOfExitStandard("12!@#$78", "Yes")
        val errors = standardForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors(0) must be(FormError(officeFieldId, "declaration.officeOfExit.specialCharacters"))
      }

      "circumstances code is incorrect" in new StandardSetUp {

        val data = OfficeOfExitStandard("12345678", "Incorrect")
        val errors = standardForm.fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError(circumstancesCodeFieldId, "standard.officeOfExit.circumstancesCode.error"))
      }
    }
  }
}
