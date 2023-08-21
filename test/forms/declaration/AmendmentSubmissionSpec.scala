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

package forms.declaration

import base.{TestHelper, UnitWithMocksSpec}
import play.api.data.Form
import play.api.libs.json._

class AmendmentSubmissionSpec extends UnitWithMocksSpec {

  import AmendmentSubmission._

  private def form(isCancellation: Boolean = false): Form[AmendmentSubmission] = AmendmentSubmission.form(isCancellation)

  "Amendment Submission Form" should {

    "not return errors" when {
      "all form data is provided and valid" in {
        form().bind(validFormData, JsonBindMaxChars).errors mustBe empty
      }
    }

    "return errors for full name" when {

      "name missing" in {
        form().bind(formDataWith(name = ""), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.fullName.empty")
      }

      "name too short" in {
        form().bind(formDataWith(name = "Al"), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.fullName.short")
      }

      "name too long" in {
        form()
          .bind(formDataWith(name = TestHelper.createRandomAlphanumericString(65)), JsonBindMaxChars)
          .errors
          .map(_.message) must contain("amendment.submission.fullName.long")
      }

      "name invalid" in {
        form().bind(formDataWith(name = "Prince!"), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.fullName.error")
      }
    }

    "return errors for job role" when {

      "job role missing" in {
        form().bind(formDataWith(role = ""), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.jobRole.empty")
      }

      "job role too short" in {
        form().bind(formDataWith(role = "CEO"), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.jobRole.short")
      }

      "job role too long" in {
        form()
          .bind(formDataWith(role = TestHelper.createRandomAlphanumericString(65)), JsonBindMaxChars)
          .errors
          .map(_.message) must contain("amendment.submission.jobRole.long")
      }

      "job role invalid" in {
        form().bind(formDataWith(role = "Prince!"), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.jobRole.error")
      }
    }

    "return errors for email" when {

      "email missing" in {
        form().bind(formDataWith(email = ""), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.email.empty")
      }

      "email too long" in {
        form()
          .bind(formDataWith(email = TestHelper.createRandomAlphanumericString(65)), JsonBindMaxChars)
          .errors
          .map(_.message) must contain("amendment.submission.email.long")
      }

      "email invalid" in {
        form().bind(formDataWith(email = "not.an.email.address"), JsonBindMaxChars).errors.map(_.message) must contain(
          "amendment.submission.email.error"
        )
      }
    }

    "return errors for reason on an amendment submission" when {

      "text-box field is empty" in {
        form().bind(formDataWith(reason = ""), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.reason.empty")
      }

      "input contains certain special characters" in {
        form().bind(formDataWith(reason = "[^<>\"&]*$"), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.reason.error")
      }

      "input is too long" in {
        form().bind(formDataWith(reason = "a" * 513), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.reason.long")
      }
    }

    "return errors for reason on an amendment cancellation" when {

      "text-box field is empty" in {
        form(true).bind(formDataWith(reason = ""), JsonBindMaxChars).errors.map(_.message) must contain("amendment.cancellation.reason.empty")
      }

      "input contains certain special characters" in {
        form(true).bind(formDataWith(reason = "[^<>\"&]*$"), JsonBindMaxChars).errors.map(_.message) must contain(
          "amendment.cancellation.reason.error"
        )
      }

      "input is too long" in {
        form(true).bind(formDataWith(reason = "a" * 513), JsonBindMaxChars).errors.map(_.message) must contain("amendment.cancellation.reason.long")
      }
    }

    "return errors for confirmation" when {
      "not selected" in {
        form().bind(formDataWith(checked = false), JsonBindMaxChars).errors.map(_.message) must contain("amendment.submission.confirmation.missing")
      }
    }
  }

  private val validFormData = formDataWith()

  private def formDataWith(
    name: String = "O'Neil Some-Name, Jr.",
    role: String = "Traveling-Secretary for the N.Y. Yankees' Chairman",
    email: String = "some@email.com",
    reason: String = "Some reason",
    checked: Boolean = true
  ): JsObject =
    Json.obj(
      nameKey -> JsString(name),
      jobRoleKey -> JsString(role),
      emailKey -> JsString(email),
      reasonKey -> JsString(reason),
      confirmationKey -> (if (checked) JsTrue else JsFalse)
    )
}
