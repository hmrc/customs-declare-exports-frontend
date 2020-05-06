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

class OfficeOfExitOutsideUKSpec extends WordSpec with MustMatchers {

  "Supplementary declaration mapping" should {

    "return form with errors" when {

      "office of exit is empty" in {

        val data = OfficeOfExitOutsideUK("")
        val errors = OfficeOfExitOutsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError("officeId", "declaration.officeOfExitOutsideUk.empty"))
      }

      "office of exit is too short" in {

        val data = OfficeOfExitOutsideUK("1234")
        val errors = OfficeOfExitOutsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError("officeId", "declaration.officeOfExitOutsideUk.format"))
      }

      "office of exit is too long" in {

        val data = OfficeOfExitOutsideUK("123456789")
        val errors = OfficeOfExitOutsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError("officeId", "declaration.officeOfExitOutsideUk.format"))
      }

      "office of exit contain special characters" in {

        val data = OfficeOfExitOutsideUK("12!@#$%^")
        val errors = OfficeOfExitOutsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors.head must be(FormError("officeId", "declaration.officeOfExitOutsideUk.format"))
      }
    }

    "return form without errors" when {

      "office of exit is correct" in {

        val data = OfficeOfExitOutsideUK("GB345678")
        val errors = OfficeOfExitOutsideUK.form().fillAndValidate(data).errors

        errors.length must be(0)
      }
    }
  }

}
