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

package forms

import forms.Choice.AllowedChoiceValues._
import base.UnitSpec
import play.api.libs.json.{JsObject, JsString, JsValue}

class ChoiceSpec extends UnitSpec {
  import ChoiceSpec._

  "Validation defined in Choice mapping" should {

    "attach errors to form" when {
      "provided with  empty input" in {
        val form = Choice.form.bind(emptyChoiceJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("choicePage.input.error.empty")
      }

      "provided with a value not defined in AllowedChoiceValues" in {
        val form = Choice.form.bind(incorrectChoiceJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("choicePage.input.error.incorrectValue")
      }
    }

    "not attach any error" when {
      "provided with valid input" in {
        val form = Choice.form.bind(correctChoiceJSON, JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

}

object ChoiceSpec {
  val incorrectChoice = Choice("InvalidChoice")
  val emptyChoice = Choice("")

  val correctChoiceJSON: JsValue = createChoiceJSON(CreateDec)
  val incorrectChoiceJSON: JsValue = createChoiceJSON("InvalidChoice")
  val emptyChoiceJSON: JsValue = createChoiceJSON()

  def createChoiceJSON(choiceValue: String = ""): JsValue = JsObject(Map("value" -> JsString(choiceValue)))
}
