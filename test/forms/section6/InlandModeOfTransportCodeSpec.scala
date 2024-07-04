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

import forms.LightFormMatchers
import forms.common.DeclarationPageBaseSpec
import play.api.libs.json.{JsObject, JsString, JsValue}

class InlandModeOfTransportCodeSpec extends DeclarationPageBaseSpec with LightFormMatchers {

  import InlandModeOfTransportCode._

  "InlandModeOfTransportCode Form" should {
    "validate inland mode transport code - wrong choice" in {
      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString("Incorrect more")))

      form.bind(incorrectTransportCode, JsonBindMaxChars).errors.map(_.message) must contain(
        "declaration.warehouse.inlandTransportDetails.error.incorrect"
      )
    }

    "validate inland mode transport code - correct choice" in {
      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString(InlandModeOfTransportCodeSpec.inlandModeOfTransportCode)))

      form.bind(incorrectTransportCode, JsonBindMaxChars) mustBe errorless
    }
  }

  "InlandModeOfTransportCode" when {
    testTariffContentKeysNoSpecialisation(InlandModeOfTransportCode, "tariff.declaration.inlandTransportDetails")
  }
}

object InlandModeOfTransportCodeSpec {
  private val inlandModeOfTransportCode = ModeOfTransportCode.Rail.value
  val correctInlandModeOfTransportCode =
    InlandModeOfTransportCode(Some(ModeOfTransportCode.Rail))
  val correctInlandModeOfTransportCodeJSON: JsValue =
    JsObject(Map("inlandModeOfTransportCode" -> JsString(inlandModeOfTransportCode)))
  val emptyInlandModeOfTransportCodeJSON: JsValue =
    JsObject(Map("inlandModeOfTransportCode" -> JsString("")))
}
