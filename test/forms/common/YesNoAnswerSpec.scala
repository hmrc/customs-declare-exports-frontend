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

package forms.common
import base.UnitSpec
import play.api.libs.json.{JsObject, JsString, JsValue}

class YesNoAnswerSpec extends UnitSpec {
  import YesNoAnswerSpec._
  "Validation defined in YesNoAnswer mapping" should {

    "attach errors to form" when {
      "provided with empty input" in {
        val form = YesNoAnswer.form().bind(emptyYesNoAnswerJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("error.yesNo.required")
      }

      "provided with an incorrect value" in {
        val form = YesNoAnswer.form().bind(incorrectYesNoAnswerJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("error.yesNo.required")
      }
    }

    "not attach any error" when {
      "provided with valid input" in {
        val form = YesNoAnswer.form().bind(correctYesNoAnswerJSON, JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }
}

object YesNoAnswerSpec {

  val correctYesNoAnswerJSON: JsValue = createYesNoJSON("Yes")
  val incorrectYesNoAnswerJSON: JsValue = createYesNoJSON("InvalidChoice")
  val emptyYesNoAnswerJSON: JsValue = createYesNoJSON()

  def createYesNoJSON(answer: String = ""): JsValue = JsObject(Map("yesNo" -> JsString(answer)))
}
