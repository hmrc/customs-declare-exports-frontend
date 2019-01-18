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
  procedureCode: String, // max 4 alphanumeric characters
  additionalProcedureCodes: Seq[String] // max 99 codes, each is max 3 alphanumeric characters
) {

  def toMetadataProperties(): Map[String, String] = {
    val procedureCodeMapping = Map(
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].currentCode" -> procedureCode
        .substring(0, 2),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].previousCode" -> procedureCode
        .substring(2, 4)
    )

    val additionalProcedureCodesMapping = additionalProcedureCodes.zipWithIndex.map { codeWithIdx =>
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[" + (codeWithIdx._2 + 1) + "].currentCode" -> codeWithIdx._1
    }

    procedureCodeMapping ++ additionalProcedureCodesMapping
  }
}

object ProcedureCodes {
  implicit val format = Json.format[ProcedureCodes]

  private val procedureCodeLength = 4
  private val additionalProcedureCodeLength = 3
  val mapping = Forms.mapping(
    "procedureCode" -> text()
      .verifying("supplementary.procedureCodes.procedureCode.error.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.procedureCodes.procedureCode.error.length",
        isEmpty or hasSpecificLength(procedureCodeLength)
      )
      .verifying("supplementary.procedureCodes.procedureCode.error.specialCharacters", isAlphanumeric),
    "additionalProcedureCodes" -> seq(
      text()
        .verifying(
          "supplementary.procedureCodes.additionalProcedureCode.error.length",
          isEmpty or hasSpecificLength(additionalProcedureCodeLength)
        )
        .verifying("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters", isAlphanumeric)
    ).verifying("supplementary.procedureCodes.additionalProcedureCode.error.singleEmpty", _.exists(_.trim.nonEmpty))
  )(ProcedureCodes.apply)(ProcedureCodes.unapply)

  val id = "ProcedureCodes"

  def form(): Form[ProcedureCodes] = Form(mapping)
}
