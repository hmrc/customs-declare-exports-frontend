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
import play.api.libs.json.{JsObject, JsString, JsValue}
import unit.base.UnitSpec

class SupervisingCustomsOfficeSpec extends UnitSpec with LightFormMatchers {

  import SupervisingCustomsOffice._

  "SupervisingCustomsOffice Form" should {
    "validate supervising customs office - invalid" in {
      val incorrectWarehouseOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString("SOMEWRONGCODE")))

      form().bind(incorrectWarehouseOffice).errors.map(_.message) must contain("declaration.warehouse.supervisingCustomsOffice.error")
    }

    "accept a valid supervising customs office" in {
      val incorrectSupervisingCustomsOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString("12345678")))

      form().bind(incorrectSupervisingCustomsOffice) mustBe errorless
    }
  }
}

object SupervisingCustomsOfficeSpec {
  private val office = "12345678"
  val correctSupervisingCustomsOffice = SupervisingCustomsOffice(Some(office))
  val correctSupervisingCustomsOfficeJSON: JsValue =
    JsObject(Map("supervisingCustomsOffice" -> JsString(office)))
  val emptySupervisingCustomsOfficeJSON: JsValue =
    JsObject(Map("supervisingCustomsOffice" -> JsString("")))
}
