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

import forms.declaration.DispatchLocation.AllowedDispatchLocations.OutsideEU
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DispatchLocationSpec extends WordSpec with MustMatchers {
  import DispatchLocationSpec._

  "DispatchLocation mapping used for binding data" should {

    "return form with errors" when {
      "provided with empty input" in {
        val form = DispatchLocation.form().bind(emptyDispatchLocationJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.dispatchLocation.inputText.error.empty")
      }

      "provided with a value not defined in AllowedDispatchLocations" in {
        val form = DispatchLocation.form().bind(incorrectDispatchLocationJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.dispatchLocation.inputText.error.incorrect")
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = DispatchLocation.form().bind(correctDispatchLocationJSON)

        form.hasErrors must be(false)
      }
    }
  }

}

object DispatchLocationSpec {
  val correctDispatchLocation = DispatchLocation(OutsideEU)
  val incorrectDispatchLocation = DispatchLocation("11")
  val emptyDispatchLocation = DispatchLocation("")

  val correctDispatchLocationJSON: JsValue = JsObject(Map("dispatchLocation" -> JsString(OutsideEU)))
  val incorrectDispatchLocationJSON: JsValue = JsObject(Map("dispatchLocation" -> JsString("11")))
  val emptyDispatchLocationJSON: JsValue = JsObject(Map("dispatchLocation" -> JsString("")))
}
