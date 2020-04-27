/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.declaration.AllowedOfficeOfExitAnswers.allowedCodes
import play.api.data.Forms.{text, _}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class OfficeOfExit(officeId: Option[String], answer: String)

object OfficeOfExit extends DeclarationPage {
  implicit val format = Json.format[OfficeOfExit]

  val formId = "OfficeOfExit"

  val mapping: Mapping[OfficeOfExit] = Forms.mapping(
    "officeId" -> optional(
      text()
        .verifying("declaration.officeOfExit.empty", nonEmpty)
        .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
        .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric)
    ),
    "answer" -> requiredRadio("declaration.officeOfExit.answer.empty")
      .verifying("declaration.officeOfExit.answer.empty", isContainedIn(allowedCodes))
  )(OfficeOfExit.apply)(OfficeOfExit.unapply)

  def form(): Form[OfficeOfExit] = Form(mapping)
}

object AllowedOfficeOfExitAnswers {
  val yes = "Yes"
  val no = "No"

  val allowedCodes = Seq(yes, no)
}
