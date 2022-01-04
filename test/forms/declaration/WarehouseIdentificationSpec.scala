/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.declaration

import base.TestHelper.createRandomAlphanumericString
import forms.LightFormMatchers
import forms.common.DeclarationPageBaseSpec
import forms.declaration.WarehouseIdentification.warehouseIdKey
import forms.declaration.WarehouseIdentificationSpec._
import play.api.libs.json.{JsObject, JsString}

class WarehouseIdentificationSpec extends DeclarationPageBaseSpec with LightFormMatchers {

  private def form() = WarehouseIdentification.form(yesNo = false)

  "Warehouse Identification Form" should {
    "validate - no answer" in {
      val incorrectWarehouseDetails = formData("")

      form().bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(s"$msgPrefix.empty")
    }

    "validate - more than 36 characters after type code" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCode + createRandomAlphanumericString(36))

      form().bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(s"$msgPrefix.length")
    }

    "validate - with non-alphanumeric" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCode + "1$3")

      form().bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(s"$msgPrefix.invalid")
    }

    "validate - invalid identification number" when {

      "missing identification number" in {
        val incorrectWarehouseDetails = formData(warehouseTypeCode)

        form().bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
      }

      "missing warehouse type" in {
        val incorrectWarehouseDetails = formData(warehouseId)

        form().bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
      }

      "invalid warehouse type" in {
        val incorrectWarehouseDetails = formData(warehouseTypeCodeInvalid + warehouseId)

        form().bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
      }

    }

    "validate correct ware house type and number" in {
      val correctWarehouseDetails = formData("R" + warehouseId)

      form().bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }

    "validate lowercase ware house type and number" in {
      val id = "r" + warehouseId.toLowerCase
      val correctWarehouseDetails = formData(id)

      val boundForm = form().bind(correctWarehouseDetails, JsonBindMaxChars)
      boundForm mustBe errorless
      boundForm.value.flatMap(_.identificationNumber) mustBe Some(id.toUpperCase)
    }

    "validate max length" in {
      val correctWarehouseDetails = formData(warehouseTypeCode + createRandomAlphanumericString(35))

      form().bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }
  }

  "WarehouseIdentification" when {
    testTariffContentKeys(WarehouseIdentification, "tariff.declaration.warehouseIdentification")
  }
}

object WarehouseIdentificationSpec {
  private val warehouseTypeCode = "R"
  private val warehouseTypeCodeInvalid = "A"
  private val warehouseId = "1234567GB"

  val correctWarehouseDetails = WarehouseIdentification(Some(warehouseTypeCode + warehouseId))

  def formData(identifier: String) =
    JsObject(Map(warehouseIdKey -> JsString(identifier)))

  val msgPrefix = "declaration.warehouse.identification.identificationNumber"

  val identificationNumberError = s"$msgPrefix.format"
}
