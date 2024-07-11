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

package forms.section1

import base.FormSpec
import forms.common.DeclarationPageBaseSpec
import forms.section1.AdditionalDeclarationTypePage.{form, radioButtonGroupId}

class AdditionalDeclarationTypePageSpec extends FormSpec with DeclarationPageBaseSpec {

  "Form for AdditionalDeclarationType" should {

    "have no errors" when {
      "provided with one of the expected values" in {
        val errors = AdditionalDeclarationType.values.toList.flatMap { additionalType =>
          form.fillAndValidate(additionalType).errors
        }
        errors must be(empty)
      }
    }

    "have errors" when {
      "not provided with any value" in {
        val incorrectForm = Map(radioButtonGroupId -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(radioButtonGroupId))
        errorMessages must be(List(s"declaration.declarationType.radio.error.empty"))
      }
    }
  }

  "Form for AdditionalDeclarationType" when {
    testTariffContentKeys(AdditionalDeclarationTypePage, "tariff.declaration.type")
  }
}
