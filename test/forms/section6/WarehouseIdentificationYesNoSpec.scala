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

import base.TestHelper.createRandomAlphanumericString
import base.UnitWithMocksSpec
import forms.common.LightFormMatchers
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section6.WarehouseIdentification._
import forms.section6.WarehouseIdentificationSpec._
import play.api.libs.json.{JsObject, JsString}

class WarehouseIdentificationYesNoSpec extends UnitWithMocksSpec with LightFormMatchers {

  private def formData(inWarehouse: String, identifier: String): JsObject =
    JsObject(Map(inWarehouseKey -> JsString(inWarehouse), warehouseIdKey -> JsString(identifier)))

  private val formYesNo = WarehouseIdentification.form(yesNo = true)

  private val identificationNumberError = s"$msgPrefix.error"

  "Warehouse Identification Form" should {
    "validate - no answer" in {
      val incorrectWarehouseDetails = formData("", "")

      formYesNo.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(
        "declaration.warehouse.identification.answer.error"
      )
    }

    "validate - more than 35 characters after type code" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCode + createRandomAlphanumericString(36))

      formYesNo.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - missing identification number" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCode)

      formYesNo.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(
        "declaration.warehouse.identification.identificationNumber.error"
      )
    }

    "validate - missing warehouse type" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseId)

      formYesNo.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate - invalid warehouse type" in {
      val incorrectWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCodeInvalid + warehouseId)

      formYesNo.bind(incorrectWarehouseDetails, JsonBindMaxChars).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate correct empty identification" in {
      val correctWarehouseDetails = formData(YesNoAnswers.no, "")

      formYesNo.bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }

    "validate correct ware house type and number" in {
      val correctWarehouseDetails = formData(YesNoAnswers.yes, "R" + warehouseId)

      formYesNo.bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }

    "validate lowercase ware house type and number" in {
      val id = "r" + warehouseId.toLowerCase
      val correctWarehouseDetails = formData(YesNoAnswers.yes, id)

      val boundForm = formYesNo.bind(correctWarehouseDetails, JsonBindMaxChars)
      boundForm mustBe errorless
      boundForm.value.flatMap(_.identificationNumber) mustBe Some(id.toUpperCase)
    }

    "validate max length" in {
      val correctWarehouseDetails = formData(YesNoAnswers.yes, warehouseTypeCode + createRandomAlphanumericString(35))

      formYesNo.bind(correctWarehouseDetails, JsonBindMaxChars) mustBe errorless
    }
  }
}
