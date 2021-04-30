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
import base.UnitSpec
import forms.LightFormMatchers
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.WarehouseIdentification._
import forms.declaration.WarehouseIdentificationYesNoSpec._
import play.api.libs.json.{JsObject, JsString}

class WarehouseIdentificationYesNoSpec extends UnitSpec with LightFormMatchers {

  private def formYesNo() = WarehouseIdentification.form(yesNo = true)

  "Warehouse Identification Form" should {
    "validate - no answer" in {
      val incorrectWarehouseDetails = formData("", "")

      formYesNo().bind(incorrectWarehouseDetails).errors.map(_.message) must contain("declaration.warehouse.identification.answer.error")
    }

    "validate - more than 35 characters after type code" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCode + createRandomAlphanumericString(36))

      formYesNo().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - missing identification number" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCode)

      formYesNo().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(
        "declaration.warehouse.identification.identificationNumber.error"
      )
    }

    "validate - missing warehouse type" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseId)

      formYesNo().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - invalid warehouse type" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCodeInvalid + warehouseId)

      formYesNo().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate correct empty identification" in {
      val correctWarehouseDetails = formData(YesNoAnswers.no, "")

      formYesNo().bind(correctWarehouseDetails) mustBe errorless
    }

    "validate correct ware house type and number" in {
      val correctWarehouseDetails = formData(YesNoAnswers.yes, "R" + warehouseId)

      formYesNo().bind(correctWarehouseDetails) mustBe errorless
    }

    "validate lowercase ware house type and number" in {
      val id = "r" + warehouseId.toLowerCase
      val correctWarehouseDetails = formData(YesNoAnswers.yes, id)

      val boundForm = formYesNo().bind(correctWarehouseDetails)
      boundForm mustBe errorless
      boundForm.value.flatMap(_.identificationNumber) mustBe Some(id.toUpperCase)
    }

    "validate max length" in {
      val correctWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCode + createRandomAlphanumericString(35))

      formYesNo().bind(correctWarehouseDetails) mustBe errorless
    }
  }
}

object WarehouseIdentificationYesNoSpec {
  private val warehouseTypeCode = "R"
  private val warehouseTypeCodeInvalid = "A"
  private val warehouseId = "1234567GB"

  val correctWarehouseDetails = WarehouseIdentification(Some(warehouseTypeCode + warehouseId))

  def formData(inWarehouse: String, identifier: String) =
    JsObject(Map(inWarehouseKey -> JsString(inWarehouse), warehouseIdKey -> JsString(identifier)))

  def warehouseIdentification(id: String) = JsObject(Map("identificationNumber" -> JsString(id)))

  def identificationNumberError = "declaration.warehouse.identification.identificationNumber.error"
}
