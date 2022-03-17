/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.common.{DeclarationPageBaseSpec, YesNoAnswer}
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.libs.json.{JsObject, JsString, JsValue}

class DeclarantIsExporterSpec extends DeclarationPageBaseSpec {

  import DeclarantIsExporterSpec._

  "DeclarantIsExporter mapping used for binding data" should {

    "return form with errors" when {

      "provided with empty input" in {

        val form = DeclarantIsExporter.form().bind(emptyJSON, JsonBindMaxChars)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarant.exporter.error")
      }

      "provided with invalid input" in {

        val form = DeclarantIsExporter.form().bind(invalidJSON, JsonBindMaxChars)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarant.exporter.error")
      }

      "provided with valid input" in {

        val form = DeclarantIsExporter.form().bind(validJSON, JsonBindMaxChars)

        form.hasErrors mustBe false
      }
    }
  }

}

object DeclarantIsExporterSpec {

  private val answerKey = YesNoAnswer.formId

  val emptyJSON: JsValue = JsObject(Map(answerKey -> JsString("")))
  val invalidJSON: JsValue = JsObject(Map(answerKey -> JsString("invalid")))
  val validJSON: JsValue = JsObject(Map(answerKey -> JsString(YesNoAnswers.yes)))
}
