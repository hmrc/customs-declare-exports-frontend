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

import base.UnitSpec
import forms.section6.InlandOrBorder._
import play.api.libs.json.{JsNull, Json}

class InlandOrBorderSpec extends UnitSpec {

  "InlandOrBorder mapping" should {

    "return form with errors" when {
      "no option has been selected" in {
        val form = InlandOrBorder.form.bind(Json.obj(fieldId -> JsNull), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.inlandOrBorder.answer.empty")
      }
    }

    "return form without errors" when {

      "the option 'Border' has been selected" in {
        val form = InlandOrBorder.form.bind(Json.obj(fieldId -> "Border"), JsonBindMaxChars)
        form.hasErrors must be(false)
      }

      "the option 'Inland' has been selected" in {
        val form = InlandOrBorder.form.bind(Json.obj(fieldId -> "Inland"), JsonBindMaxChars)
        form.hasErrors must be(false)
      }
    }
  }
}
