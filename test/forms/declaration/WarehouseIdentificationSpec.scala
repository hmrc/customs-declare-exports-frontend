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

package forms.declaration

import base.TestHelper.createRandomAlphanumericString
import forms.LightFormMatchers
import forms.declaration.WarehouseIdentificationSpec._
import play.api.libs.json.{JsObject, JsString, JsValue}
import unit.base.UnitSpec

class WarehouseIdentificationSpec extends UnitSpec with LightFormMatchers {

  import WarehouseIdentification._

  "Warehouse Identification Form" should {
    "validate - more than 35 characters after type code" in {
      val incorrectWarehouseDetails = warehouseIdentification(warehouseTypeCode + createRandomAlphanumericString(36))

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - missing identification number" in {
      val incorrectWarehouseDetails = warehouseIdentification(warehouseTypeCode)

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain("declaration.warehouse.identification.identificationNumber.error")
    }

    "validate - missing warehouse type" in {
      val incorrectWarehouseDetails = warehouseIdentification(warehouseId)

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - invalid warehouse type" in {
      val incorrectWarehouseDetails = warehouseIdentification(warehouseTypeCodeInvalid + warehouseId)

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate correct empty identification" in {
      val correctWarehouseDetails = warehouseIdentification("")

      form().bind(correctWarehouseDetails) mustBe errorless
    }

    "validate correct ware house type and number" in {
      val correctWarehouseDetails: JsValue = warehouseIdentification("R" + warehouseId)

      form().bind(correctWarehouseDetails) mustBe errorless
    }

    "validate max length" in {
      val correctWarehouseDetails = warehouseIdentification(warehouseTypeCode + createRandomAlphanumericString(35))

      form().bind(correctWarehouseDetails) mustBe errorless
    }
  }
}

object WarehouseIdentificationSpec {
  private val warehouseTypeCode = "R"
  private val warehouseTypeCodeInvalid = "A"
  private val warehouseId = "1234567GB"

  val correctWarehouseDetails = WarehouseIdentification(Some(warehouseTypeCode + warehouseId))

  def warehouseIdentification(id: String) = JsObject(Map("identificationNumber" -> JsString(id)))

  def identificationNumberError = "declaration.warehouse.identification.identificationNumber.error"
}
