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

package forms.section4

import forms.common.DeclarationPageBaseSpec
import play.api.libs.json.{JsObject, JsString, JsValue}

class NatureOfTransactionSpec extends DeclarationPageBaseSpec {
  "NatureOfTransaction" when {
    testTariffContentKeysNoSpecialisation(NatureOfTransaction, "tariff.declaration.natureOfTransaction")
  }
}

object NatureOfTransactionSpec {
  val correctNatureOfTransaction = NatureOfTransaction("1")

  val correctNatureOfTransactionJSON: JsValue = JsObject(Map("natureType" -> JsString("1")))

  val emptyNatureOfTransactionJSON: JsValue = JsObject(Map("natureType" -> JsString("")))

  val incorrectNatureOfTransactionJSON: JsValue = JsObject(Map("natureType" -> JsString("123")))
}
