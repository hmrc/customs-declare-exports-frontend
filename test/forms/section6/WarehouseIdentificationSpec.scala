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

import base.TestHelper.createRandomAlphanumericString
import forms.common.{DeclarationPageBaseSpec, LightFormMatchers}
import forms.section6.WarehouseIdentification.warehouseIdKey
import forms.section6.WarehouseIdentificationSpec.{msgPrefix, warehouseId, warehouseTypeCode, warehouseTypeCodeInvalid}
import play.api.libs.json.{JsObject, JsString}

class WarehouseIdentificationSpec extends DeclarationPageBaseSpec with LightFormMatchers {

  private def formData(identifier: String): JsObject = JsObject(Map(warehouseIdKey -> JsString(identifier)))

  private val form = WarehouseIdentification.form(yesNo = false)

  private val identificationNumberError = s"$msgPrefix.format"

  "Warehouse Identification Form" should {
    "validate - no answer" in {
      val incorrectWarehouseDetails = formData("")

      form.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(s"$msgPrefix.empty")
    }

    "validate - more than 36 characters after type code" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCode + createRandomAlphanumericString(36))

      form.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(s"$msgPrefix.length")
    }

    "validate - with non-alphanumeric" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCode + "1$3")

      form.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(s"$msgPrefix.invalid")
    }

    "validate - invalid identification number" when {

      "missing identification number" in {
        val incorrectWarehouseDetails = formData(warehouseTypeCode)

        form.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
      }

      "missing warehouse type" in {
        val incorrectWarehouseDetails = formData(warehouseId)

        form.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
      }

      "invalid warehouse type" in {
        val incorrectWarehouseDetails = formData(warehouseTypeCodeInvalid + warehouseId)

        form.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
      }

    }

    "validate correct ware house type and number" in {
      val correctWarehouseDetails = formData("R" + warehouseId)

      form.bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }

    "validate lowercase ware house type and number" in {
      val id = "r" + warehouseId.toLowerCase
      val correctWarehouseDetails = formData(id)

      val boundForm = form.bind(correctWarehouseDetails, JsonBindMaxChars)
      boundForm mustBe errorless
      boundForm.value.flatMap(_.identificationNumber) mustBe Some(id.toUpperCase)
    }

    "validate max length" in {
      val correctWarehouseDetails = formData(warehouseTypeCode + createRandomAlphanumericString(35))

      form.bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }
  }

  "WarehouseIdentification" when {
    testTariffContentKeys(WarehouseIdentification, "tariff.declaration.warehouseIdentification")
  }
}

object WarehouseIdentificationSpec {

  val msgPrefix = "declaration.warehouse.identification.identificationNumber"

  val warehouseId = "1234567GB"
  val warehouseTypeCode = "R"
  val warehouseTypeCodeInvalid = "A"

  val warehouseDetails = WarehouseIdentification(Some(warehouseTypeCode + warehouseId))
}
