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

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain("declaration.warehouse.identification.identificationNumber.error")
    }

    "validate - more than 35 characters after type code" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCode + createRandomAlphanumericString(36))

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - missing identification number" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCode)

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain("declaration.warehouse.identification.identificationNumber.error")
    }

    "validate - missing warehouse type" in {
      val incorrectWarehouseDetails = formData(warehouseId)

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - invalid warehouse type" in {
      val incorrectWarehouseDetails = formData(warehouseTypeCodeInvalid + warehouseId)

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate correct ware house type and number" in {
      val correctWarehouseDetails = formData("R" + warehouseId)

      form().bind(correctWarehouseDetails) mustBe errorless
    }

    "validate lowercase ware house type and number" in {
      val id = "r" + warehouseId.toLowerCase
      val correctWarehouseDetails = formData(id)

      val boundForm = form().bind(correctWarehouseDetails)
      boundForm mustBe errorless
      boundForm.value.flatMap(_.identificationNumber) mustBe Some(id.toUpperCase)
    }

    "validate max length" in {
      val correctWarehouseDetails = formData(warehouseTypeCode + createRandomAlphanumericString(35))

      form().bind(correctWarehouseDetails) mustBe errorless
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

  def identificationNumberError = "declaration.warehouse.identification.identificationNumber.error"
}
