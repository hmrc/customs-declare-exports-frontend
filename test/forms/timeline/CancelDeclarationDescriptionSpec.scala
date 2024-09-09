/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.timeline

import base.UnitSpec
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}

class CancelDeclarationDescriptionSpec extends UnitSpec {

  private val validMrn = "123456789012345678"
  def getBoundedForm(lrn: String = "lrn", mrn: String = validMrn, description: String = "description", reason: String = "1") =
    CancelDeclarationDescription.form.bind(
      JsObject(
        Map(
          "functionalReferenceId" -> JsString(lrn),
          "mrn" -> JsString(mrn),
          "statementDescription" -> JsString(description),
          "changeReason" -> JsString(reason)
        )
      ),
      Form.FromJsonMaxChars
    )

  "Validation defined in CancelDeclaration mapping" should {

    "attach errors to form" when {

      "provided with empty description" in {
        val form = getBoundedForm(description = "")

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.statementDescription.error.empty")
      }

      "provided with too long description" in {
        val form = getBoundedForm(description = "A23456789B" * 60)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.statementDescription.error.length")
      }

      "provided with invalid description" in {
        val form = getBoundedForm(description = "~23456789@")

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.statementDescription.error.invalid")
      }

      "provided with missing reason" in {
        val form = getBoundedForm(reason = "")

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.changeReason.error.wrongValue")
      }

      "provided with invalid reason" in {
        val form = getBoundedForm(reason = "17")

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.changeReason.error.wrongValue")
      }
    }

    "not attach any error" when {
      "provided with valid input" in {
        val form = getBoundedForm()

        form.errors mustBe empty
      }
    }
  }
}
