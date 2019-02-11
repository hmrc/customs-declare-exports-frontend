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

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class ProcedureCode(
  procedureCode: Option[String], // max 4 alphanumeric characters
  additionalProcedureCode: Option[String] // max 99 codes, each is max 3 alphanumeric characters
)

object ProcedureCode {
  implicit val format = Json.format[ProcedureCode]

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
  )(ProcedureCode.apply)(ProcedureCode.unapply)

  def form(): Form[ProcedureCode] = Form(mapping)
}

case class ProcedureCodesData(procedureCode: Option[String], additionalProcedureCodes: Seq[String])
    extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] = {
    val procedureCodeMapping = Map(
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].currentCode" -> procedureCode
        .map(_.substring(0, 2))
        .getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].previousCode" -> procedureCode
        .map(_.substring(2, 4))
        .getOrElse("")
    )

    val additionalProcedureCodesMapping = additionalProcedureCodes.zipWithIndex.map { codeWithIdx =>
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[" + (codeWithIdx._2 + 1) + "].currentCode" -> codeWithIdx._1
    }

    procedureCodeMapping ++ additionalProcedureCodesMapping
  }

  def toProcedureCode(): ProcedureCode = ProcedureCode(procedureCode, None)

  def containsAdditionalCode(code: String): Boolean = additionalProcedureCodes.contains(code)
}

object ProcedureCodesData {
  implicit val format = Json.format[ProcedureCodesData]

  val formId = "ProcedureCodesData"

  val limitOfCodes = 99
}
