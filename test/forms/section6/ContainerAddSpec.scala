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

package forms.section6

import base.TestHelper
import forms.common.DeclarationPageBaseSpec

class ContainerAddSpec extends DeclarationPageBaseSpec {

  private val form = ContainerAdd.form

  "ContainerAdd" should {

    "have max container Id length equal to 17" in {
      ContainerAdd.maxContainerIdLength must be(17)
    }

    "have no errors" when {
      "id field contains correct value" in {
        val correctForm = ContainerAdd(Some("12345"))

        val result = form.fill(correctForm)
        result.errors must be(empty)
      }
    }

    "have errors" when {

      "id field is empty" in {
        val emptyForm = Map("id" -> "")

        val result = form.bind(emptyForm)
        val error = result.errors.head

        result.errors.length must be(1)
        error.key must be("id")
        error.message must be("declaration.transportInformation.containerId.empty")
      }

      "id field is not alphanumeric" in {
        val emptyForm = Map("id" -> "!@#$")

        val result = form.bind(emptyForm)
        val error = result.errors.head

        error.key must be("id")
        error.message must be("declaration.transportInformation.containerId.error.invalid")
      }

      "id field is longer than allowed" in {
        val emptyForm = Map("id" -> TestHelper.createRandomAlphanumericString(ContainerAdd.maxContainerIdLength + 1))

        val result = form.bind(emptyForm)
        val error = result.errors.head

        error.key must be("id")
        error.message must be("declaration.transportInformation.containerId.error.length")
      }

      "id field is not alphanumeric and too long" in {
        val emptyForm = Map("id" -> (TestHelper.createRandomAlphanumericString(ContainerAdd.maxContainerIdLength + 1) + "!@#$"))

        val result = form.bind(emptyForm)
        val error = result.errors.head

        error.key must be("id")
        error.message must be("declaration.transportInformation.containerId.error.invalid")
      }
    }
  }

  "ContainerAdd" when {
    testTariffContentKeys(ContainerAdd, "tariff.declaration.container.change")
  }
}
