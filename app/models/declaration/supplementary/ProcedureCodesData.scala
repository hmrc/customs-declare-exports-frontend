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

package models.declaration.supplementary

import forms.MetadataPropertiesConvertable
import forms.supplementary.ProcedureCodes
import play.api.libs.json.Json

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

  def toProcedureCode(): ProcedureCodes = ProcedureCodes(procedureCode, None)

  def containsAdditionalCode(code: String): Boolean = additionalProcedureCodes.contains(code)
}

object ProcedureCodesData {
  implicit val format = Json.format[ProcedureCodesData]

  val formId = "ProcedureCodesData"

  val limitOfCodes = 99
}
