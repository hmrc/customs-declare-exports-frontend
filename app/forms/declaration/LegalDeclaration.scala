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

package forms.declaration
import play.api.data.Forms.{boolean, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class LegalDeclaration(fullName: String, jobRole: String, email: String, confirmation: Boolean)

object LegalDeclaration {
  implicit val format: OFormat[LegalDeclaration] = Json.format[LegalDeclaration]

  val mapping: Mapping[LegalDeclaration] = Forms.mapping(
    "fullName" -> text()
      .verifying("legal.declaration.fullName.empty", nonEmpty)
      .verifying("legal.declaration.fullName.short", isEmpty or noShorterThan(4))
      .verifying("legal.declaration.fullName.long", isEmpty or noLongerThan(64))
      .verifying("legal.declaration.fullName.error", isEmpty or isAlphanumericWithSpace),
    "jobRole" -> text()
      .verifying("legal.declaration.jobRole.empty", nonEmpty)
      .verifying("legal.declaration.jobRole.short", isEmpty or noShorterThan(4))
      .verifying("legal.declaration.jobRole.long", isEmpty or noLongerThan(64))
      .verifying("legal.declaration.jobRole.error", isEmpty or isAlphanumericWithSpace),
    "email" -> text()
      .verifying("legal.declaration.email.empty", nonEmpty)
      .verifying("legal.declaration.email.long", isEmpty or noLongerThan(64))
      .verifying("legal.declaration.email.error", isEmpty or isValidEmail),
    "confirmation" -> boolean.verifying("legal.declaration.confirmation.missing", isTrue)
  )(LegalDeclaration.apply)(LegalDeclaration.unapply)

  def form(): Form[LegalDeclaration] = Form(mapping)
}
