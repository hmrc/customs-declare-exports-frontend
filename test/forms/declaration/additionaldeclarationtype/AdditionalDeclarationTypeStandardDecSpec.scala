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

package forms.declaration.additionaldeclarationtype

import base.UnitSpec
import .AllowedAdditionalDeclarationTypes._
import play.api.libs.json.{JsObject, JsString, JsValue}

class AdditionalDeclarationTypeStandardDecSpec extends UnitSpec {
  import AdditionalDeclarationTypeStandardDecSpec._

  "AdditionalDeclarationType mapping user for binding data" should {

    "return form with errors" when {
      "provided with empty input" in {
        val form = AdditionalDeclarationTypeStandardDec.form().bind(emptyAdditionalDeclarationTypeStandardDecJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarationType.inputText.error.empty")
      }

      "provided with a value not defined in AllowedAdditionalDeclarationTypes" in {
        val form = AdditionalDeclarationTypeStandardDec.form().bind(incorrectAdditionalDeclarationTypeStandardDecJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarationType.inputText.error.incorrect")
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = AdditionalDeclarationTypeStandardDec.form().bind(correctAdditionalDeclarationTypeStandardDecJSON, JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

}

object AdditionalDeclarationTypeStandardDecSpec {
  val correctAdditionalDeclarationTypeStandardDec = AdditionalDeclarationType.STANDARD_PRE_LODGED

  val correctAdditionalDeclarationTypeStandardDecJSON: JsValue = JsObject(Map("additionalDeclarationType" -> JsString(PreLodged)))
  val incorrectAdditionalDeclarationTypeStandardDecJSON: JsValue = JsObject(Map("additionalDeclarationType" -> JsString("1")))
  val emptyAdditionalDeclarationTypeStandardDecJSON: JsValue = JsObject(Map("additionalDeclarationType" -> JsString("")))

}
