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

package forms.declaration.officeOfExit

import forms.Mapping.requiredRadio
import forms.declaration.officeOfExit.OfficeOfExitStandard.AllowedCircumstancesCodeAnswers.{no, yes}
import play.api.data.Forms.text
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class OfficeOfExitStandard(officeId: String, circumstancesCode: String)

object OfficeOfExitStandard {
  implicit val format: OFormat[OfficeOfExitStandard] = Json.format[OfficeOfExitStandard]

  val mapping: Mapping[OfficeOfExitStandard] = Forms.mapping(
    "officeId" -> text()
      .verifying("declaration.officeOfExit.empty", nonEmpty)
      .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
      .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric),
    "circumstancesCode" -> requiredRadio("standard.officeOfExit.circumstancesCode.empty")
      .verifying("standard.officeOfExit.circumstancesCode.error", isContainedIn(Seq(yes, no)))
  )(OfficeOfExitStandard.apply)(OfficeOfExitStandard.unapply)

  def apply(officeOfExit: OfficeOfExit): OfficeOfExitStandard =
    OfficeOfExitStandard(officeId = officeOfExit.officeId, circumstancesCode = officeOfExit.circumstancesCode.getOrElse(""))

  object AllowedCircumstancesCodeAnswers {
    val yes = "Yes"
    val no = "No"
  }
}
