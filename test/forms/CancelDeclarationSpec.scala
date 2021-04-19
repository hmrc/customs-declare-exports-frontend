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

package forms

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString}

class CancelDeclarationSpec extends WordSpec with MustMatchers {

  def formData(lrn: String = "lrn", mrn: String = "123456789012345678", description: String = "description", reason: String = "1") =
    JsObject(
      Map(
        "functionalReferenceId" -> JsString(lrn),
        "mrn" -> JsString(mrn),
        "statementDescription" -> JsString(description),
        "changeReason" -> JsString(reason)
      )
    )

  "Validation defined in CancelDeclaration mapping" should {

    "attach errors to form" when {
      "provided with empty lrn" in {
        val form = CancelDeclaration.form.bind(formData(lrn = ""))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.functionalReferenceId.error.empty")
      }

      "provided with too long lrn" in {
        val form = CancelDeclaration.form.bind(formData(lrn = "1234567890" * 3))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.functionalReferenceId.error.length")
      }

      "provided with invalid lrn" in {
        val form = CancelDeclaration.form.bind(formData(lrn = "inv@l!d"))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.functionalReferenceId.error.specialCharacter")
      }

      "provided with empty mrn" in {
        val form = CancelDeclaration.form.bind(formData(mrn = ""))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.mrn.error.empty")
      }

      "provided with too long mrn" in {
        val form = CancelDeclaration.form.bind(formData(mrn = "1234567890123456789"))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.mrn.error.length")
      }

      "provided with invalid mrn" in {
        val form = CancelDeclaration.form.bind(formData(mrn = "12345678901234567~"))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.mrn.error.wrongFormat")
      }

      "provided with empty description" in {
        val form = CancelDeclaration.form.bind(formData(description = ""))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.statementDescription.error.empty")
      }

      "provided with too long description" in {
        val form = CancelDeclaration.form.bind(formData(description = "A23456789B" * 60))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.statementDescription.error.length")
      }

      "provided with invalid description" in {
        val form = CancelDeclaration.form.bind(formData(description = "~23456789@"))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.statementDescription.error.invalid")
      }

      "provided with missing reason" in {
        val form = CancelDeclaration.form.bind(formData(reason = ""))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.changeReason.error.wrongValue")
      }

      "provided with invalid reason" in {
        val form = CancelDeclaration.form.bind(formData(reason = "17"))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("cancellation.changeReason.error.wrongValue")
      }
    }

    "not attach any error" when {
      "provided with valid input" in {
        val form = CancelDeclaration.form.bind(formData())

        form.errors must be(Seq.empty)
      }
    }
  }

}
