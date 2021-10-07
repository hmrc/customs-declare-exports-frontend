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

import base.UnitSpec
import play.api.libs.json.{JsObject, JsString, JsValue}

class DeclarationChoiceSpec extends UnitSpec {
  import DeclarationChoiceSpec._

  "Validation defined in DeclarationChoice mapping" should {

    "attach errors to form" when {
      "provided with  empty input" in {
        val form = DeclarationChoice.form().bind(emptyChoiceJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.type.error")
      }

      "provided with a value not defined in AllowedChoiceValues" in {
        val form = DeclarationChoice.form().bind(incorrectChoiceJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.type.error")
      }
    }

    "not attach any error" when {
      "provided with valid input" in {
        val form = DeclarationChoice.form().bind(correctChoiceJSON, JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

}

object DeclarationChoiceSpec {

  val correctChoiceJSON: JsValue = createChoiceJSON("STANDARD")
  val incorrectChoiceJSON: JsValue = createChoiceJSON("InvalidChoice")
  val emptyChoiceJSON: JsValue = JsString("")

  def createChoiceJSON(choiceValue: String = ""): JsValue = JsObject(Map("type" -> JsString(choiceValue)))
}
