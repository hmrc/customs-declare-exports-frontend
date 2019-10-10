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

package forms.declaration.additionaldeclarationtype

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class AdditionalDeclarationTypeSupplementaryDecSpec extends WordSpec with MustMatchers {
  import AdditionalDeclarationTypeSupplementaryDecSpec._

  "AdditionalDeclarationType mapping user for binding data" should {

    "return form with errors" when {
      "provided with empty input" in {
        val form =
          AdditionalDeclarationTypeSupplementaryDec.form().bind(emptyAdditionalDeclarationTypeSupplementaryDecJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarationType.inputText.error.empty")
      }

      "provided with a value not defined in AllowedAdditionalDeclarationTypes" in {
        val form =
          AdditionalDeclarationTypeSupplementaryDec.form().bind(incorrectAdditionalDeclarationTypeSupplementaryDecJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarationType.inputText.error.incorrect")
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form =
          AdditionalDeclarationTypeSupplementaryDec.form().bind(correctAdditionalDeclarationTypeSupplementaryDecJSON)

        form.hasErrors must be(false)
      }
    }
  }

}

object AdditionalDeclarationTypeSupplementaryDecSpec {
  val correctAdditionalDeclarationTypeSupplementaryDec = AdditionalDeclarationType(Simplified)
  val incorrectAdditionalDeclarationTypeSupplementaryDec = AdditionalDeclarationType("1")
  val emptyAdditionalDeclarationTypeSupplementaryDec = AdditionalDeclarationType("")

  val correctAdditionalDeclarationTypeSupplementaryDecJSON: JsValue = JsObject(Map("additionalDeclarationType" -> JsString(Simplified)))
  val incorrectAdditionalDeclarationTypeSupplementaryDecJSON: JsValue = JsObject(Map("additionalDeclarationType" -> JsString("1")))
  val emptyAdditionalDeclarationTypeSupplementaryDecJSON: JsValue = JsObject(Map("additionalDeclarationType" -> JsString("")))
}
