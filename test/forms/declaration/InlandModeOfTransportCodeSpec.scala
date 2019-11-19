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

import forms.LightFormMatchers
import helpers.views.declaration.WarehouseIdentificationMessages
import play.api.libs.json.{JsObject, JsString, JsValue}
import unit.base.UnitSpec

class InlandModeOfTransportCodeSpec extends UnitSpec with WarehouseIdentificationMessages with LightFormMatchers {

  import InlandModeOfTransportCode._

  "InlandModeOfTransportCode Form" should {
    "validate inland mode transport code - wrong choice" in {
      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString("Incorrect more")))

      form().bind(incorrectTransportCode).errors.map(_.message) must contain(inlandTransportModeError)
    }

    "validate inland mode transport code - correct choice" in {
      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString(TransportCodes.Rail)))

      form().bind(incorrectTransportCode) mustBe errorless
    }
  }
}

object InlandModeOfTransportCodeSpec {
  val correctInlandModeOfTransportCode =
    InlandModeOfTransportCode(Some(inlandModeOfTransportCode))
  val correctInlandModeOfTransportCodeJSON: JsValue =
    JsObject(Map("inlandModeOfTransportCode" -> JsString(inlandModeOfTransportCode)))
  val emptyInlandModeOfTransportCodeJSON: JsValue =
    JsObject(Map("inlandModeOfTransportCode" -> JsString("")))
  private val inlandModeOfTransportCode = TransportCodes.Rail
}
