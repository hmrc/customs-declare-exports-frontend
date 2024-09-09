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

package forms.section6

import base.{TestHelper, UnitSpec}
import forms.common.DeclarationPageBaseSpec

class SealSpec extends UnitSpec with DeclarationPageBaseSpec {

  val form = Seal.form

  "Seal object" should {

    "has correct form id" in {

      Seal.formId must be("Seal")
    }

    "has 9999 limit" in {

      Seal.sealsAllowed must be(9999)
    }
  }

  "Seal form" should {

    "has no errors" when {

      "id field contains correct value" in {

        val correctForm = Seal(1, "12345")

        val result = form.fill(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      "id field is empty" in {

        val emptyForm = Map("id" -> "")

        val result = form.bind(emptyForm)
        val error = result.errors.head

        result.errors.length must be(1)
        error.key must be("id")
        error.message must be("declaration.transport.sealId.empty.error")
      }

      "id field is not alphanumeric" in {

        val emptyForm = Map("id" -> "!@#$")

        val result = form.bind(emptyForm)
        val error = result.errors.head

        error.key must be("id")
        error.message must be("declaration.transport.sealId.error.invalid")
      }

      "id field is longer than 20 characters" in {

        val emptyForm = Map("id" -> TestHelper.createRandomAlphanumericString(21))

        val result = form.bind(emptyForm)
        val error = result.errors.head

        error.key must be("id")
        error.message must be("declaration.transport.sealId.error.invalid")
      }

      "id field is longer than 20 characters with invalid charaters" in {

        val emptyForm = Map("id" -> (TestHelper.createRandomAlphanumericString(21) + "!@#$"))

        val result = form.bind(emptyForm)
        val error = result.errors.head

        error.key must be("id")
        error.message must be("declaration.transport.sealId.error.invalid")
      }
    }
  }

  "Seal" when {
    testTariffContentKeys(Seal, "tariff.declaration.containers.seals")
  }
}
