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

import play.api.data.Forms.{boolean, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class AmendmentSubmission(fullName: String, jobRole: String, email: String, reason: String, confirmation: Boolean)

object AmendmentSubmission {

  implicit val format: OFormat[AmendmentSubmission] = Json.format[AmendmentSubmission]

  val nameKey = "fullName"
  val jobRoleKey = "jobRole"
  val emailKey = "email"
  val reasonKey = "reason"
  val confirmationKey = "confirmation"

  val reasonMaxLength = 512

  def mapping(isCancellation: Boolean): Mapping[AmendmentSubmission] = {
    val key = if (isCancellation) "cancellation" else "submission"
    Forms.mapping(
      nameKey -> text()
        .verifying("amendment.submission.fullName.empty", nonEmpty)
        .verifying("amendment.submission.fullName.short", isEmpty or noShorterThan(4))
        .verifying("amendment.submission.fullName.long", isEmpty or noLongerThan(64))
        .verifying("amendment.submission.fullName.error", isEmpty or isValidName),
      jobRoleKey -> text()
        .verifying("amendment.submission.jobRole.empty", nonEmpty)
        .verifying("amendment.submission.jobRole.short", isEmpty or noShorterThan(4))
        .verifying("amendment.submission.jobRole.long", isEmpty or noLongerThan(64))
        .verifying("amendment.submission.jobRole.error", isEmpty or isValidName),
      emailKey -> text()
        .verifying("amendment.submission.email.empty", nonEmpty)
        .verifying("amendment.submission.email.long", isEmpty or noLongerThan(64))
        .verifying("amendment.submission.email.error", isEmpty or isValidEmail),
      reasonKey -> text()
        .verifying(s"amendment.$key.reason.empty", nonEmpty)
        .verifying(s"amendment.$key.reason.long", isEmpty or noLongerThan(reasonMaxLength))
        .verifying(s"amendment.$key.reason.error", isEmpty or isValidAmendmentReason),
      confirmationKey -> boolean.verifying("amendment.submission.confirmation.missing", isTrue)
    )(AmendmentSubmission.apply)(AmendmentSubmission.unapply)
  }

  def form(isCancellation: Boolean): Form[AmendmentSubmission] = Form(mapping(isCancellation))
}
