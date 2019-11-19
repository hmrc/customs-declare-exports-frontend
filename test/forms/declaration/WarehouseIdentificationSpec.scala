/*
 * Copyright 2019 HM Revenue & Customs
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

import base.TestHelper
import forms.LightFormMatchers
import helpers.views.declaration.WarehouseIdentificationMessages
import play.api.libs.json.{JsObject, JsString, JsValue}
import unit.base.UnitSpec

class WarehouseIdentificationSpec extends UnitSpec with WarehouseIdentificationMessages with LightFormMatchers {

  import WarehouseIdentification._

  "Warehouse Identification Form" should {
    "validate identification number - more than 35 characters" in {
      val incorrectWarehouseDetails: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(36))))

      form().bind(incorrectWarehouseDetails).errors.map(_.message) must contain(identificationNumberError)
    }

    "validate correct identification number" in {
      val incorrectWarehouseDetails: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(34))))

      form().bind(incorrectWarehouseDetails) mustBe errorless
    }
  }
}

object WarehouseIdentificationSpec {
  val correctWarehouseDetails = WarehouseIdentification(Some(warehouseTypeCode + warehouseId))

  val correctWarehouseIdentificationJSON: JsValue =
    JsObject(Map("identificationNumber" -> JsString(warehouseTypeCode + warehouseId)))
  val emptyWarehouseIdentificationJSON: JsValue =
    JsObject(Map("identificationNumber" -> JsString("")))

  private val warehouseTypeCode = "R"
  private val warehouseId = "1234567GB"
}
