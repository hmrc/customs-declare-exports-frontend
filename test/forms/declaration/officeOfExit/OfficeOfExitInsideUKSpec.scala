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

class OfficeOfExitInsideUKSpec extends WordSpec with MustMatchers {

  "Standard declaration mapping" should {

    "return form with errors" when {

      "all inputs are empty" in {

        val data = OfficeOfExitInsideUK(Some(""), "")
        val errors = OfficeOfExitInsideUK.form().fillAndValidate(data).errors

        errors.length must be(2)
        errors(0) must be(FormError("officeId", "declaration.officeOfExit.empty"))
        errors(1) must be(FormError("isUkOfficeOfExit", "declaration.officeOfExit.isUkOfficeOfExit.empty"))
      }

      "office of exit too short" in {

        val data = OfficeOfExitInsideUK(Some("123456"), "Yes")
        val errors = OfficeOfExitInsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors(0) must be(FormError("officeId", "declaration.officeOfExit.length"))
      }

      "office of exit too long" in {

        val data = OfficeOfExitInsideUK(Some("123456789"), "Yes")
        val errors = OfficeOfExitInsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors(0) must be(FormError("officeId", "declaration.officeOfExit.length"))
      }

      "office of exit contains special characters" in {
        val data = OfficeOfExitInsideUK(Some("12!@#$78"), "Yes")
        val errors = OfficeOfExitInsideUK.form().fillAndValidate(data).errors

        errors.length must be(1)
        errors(0) must be(FormError("officeId", "declaration.officeOfExit.specialCharacters"))
      }
    }
  }
}
