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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class ProcedureCodes(
  procedureCode: Option[String], // max 4 alphanumeric characters
  additionalProcedureCode: Option[String] // max 99 codes, each is max 3 alphanumeric characters
)

object ProcedureCodes {
  implicit val format = Json.format[ProcedureCodes]

  private val procedureCodeLength = 4
  private val additionalProcedureCodeLength = 3


  val mapping = Forms.mapping(
    "procedureCode" -> optional(
      text()
        .verifying("supplementary.procedureCodes.procedureCode.error.empty", _.trim.nonEmpty)
        .verifying(
          "supplementary.procedureCodes.procedureCode.error.length",
          isEmpty or hasSpecificLength(procedureCodeLength)
        )
        .verifying("supplementary.procedureCodes.procedureCode.error.specialCharacters", isAlphanumeric)
    ),
    "additionalProcedureCode" -> optional(
      text()
        .verifying(
          "supplementary.procedureCodes.additionalProcedureCode.error.length",
          isEmpty or hasSpecificLength(additionalProcedureCodeLength)
        )
        .verifying("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters", isAlphanumeric)
    )
  )(ProcedureCodes.apply)(ProcedureCodes.unapply)

  def form(): Form[ProcedureCodes] = Form(mapping)
}
