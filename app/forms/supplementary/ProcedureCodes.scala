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

package forms.supplementary

import play.api.data.Forms.{seq, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class ProcedureCodes(
  procedureCode: String,      // max 4 alphanumeric characters
  additionalProcedureCodes: Seq[String]     // max 99 codes, each is max 3 alphanumeric characters
) {

  def toMetadataProperties(): Map[String, String] = ???
}

object ProcedureCodes {
  implicit val format = Json.format[ProcedureCodes]

  private val procedureCodeMaxLength = 4
  private val additionalProcedureCodeMaxLength = 3
  val mapping = Forms.mapping(
    "procedureCode" -> text()
    .verifying("supplementary.procedureCodes.procedureCode.error.empty", _.trim.nonEmpty)
    .verifying("supplementary.procedureCodes.procedureCode.error.length", noLongerThan(_, procedureCodeMaxLength))
    .verifying("supplementary.procedureCodes.procedureCode.error.specialCharacters", isAlphanumeric(_)),
//    "additionalProcedureCodes" -> seq(text())
//        .verifying("supplementary.procedureCodes.additionalProcedureCode.error.singleEmpty", _.forall(_.trim.nonEmpty))
//        .verifying("supplementary.procedureCodes.additionalProcedureCode.error.length", _.forall(noLongerThan(_, additionalProcedureCodeMaxLength)))
//        .verifying("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters", _.forall(isAlphanumeric(_)))

  "additionalProcedureCodes" -> seq(text()
    .verifying("supplementary.procedureCodes.additionalProcedureCode.error.singleEmpty", _.trim.nonEmpty)
    .verifying("supplementary.procedureCodes.additionalProcedureCode.error.length", noLongerThan(_, additionalProcedureCodeMaxLength))
    .verifying("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters", isAlphanumeric(_))
  )
  )(ProcedureCodes.apply)(ProcedureCodes.unapply)

  val id = "ProcedureCodes"

  def form(): Form[ProcedureCodes] = ???

}
