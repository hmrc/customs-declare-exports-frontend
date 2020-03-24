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

package forms.declaration.officeOfExit

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.declaration.officeOfExit.AllowedCircumstancesCodeAnswers.allowedCodes
import play.api.data.Forms.{optional, text}
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class OfficeOfExitClearance(officeId: Option[String], circumstancesCode: String)

object OfficeOfExitClearance extends DeclarationPage {
  implicit val format: OFormat[OfficeOfExitClearance] = Json.format[OfficeOfExitClearance]

  val mapping: Mapping[OfficeOfExitClearance] = Forms.mapping(
    "officeId" -> optional(
      text()
        .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
        .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric)
    ),
    "circumstancesCode" -> requiredRadio("standard.officeOfExit.circumstancesCode.empty")
      .verifying("standard.officeOfExit.circumstancesCode.error", isContainedIn(allowedCodes))
  )(OfficeOfExitClearance.apply)(OfficeOfExitClearance.unapply)

  def apply(officeOfExit: OfficeOfExit): OfficeOfExitClearance =
    OfficeOfExitClearance(officeId = officeOfExit.officeId, circumstancesCode = officeOfExit.circumstancesCode.getOrElse(""))
}
