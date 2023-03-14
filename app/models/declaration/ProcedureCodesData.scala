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

package models.declaration

import forms.declaration.procedurecodes.ProcedureCode
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class ProcedureCodesData(procedureCode: Option[String], additionalProcedureCodes: Seq[String]) extends DiffTools[ProcedureCodesData] {
  override def createDiff(original: ProcedureCodesData, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(
        original.procedureCode,
        procedureCode,
        combinePointers(pointerString, ProcedureCodesData.procedureCodesPointer, sequenceId)
      ),
      compareStringDifference(
        original.additionalProcedureCodes,
        additionalProcedureCodes,
        combinePointers(pointerString, ProcedureCodesData.additionalProcedureCodesPointer, sequenceId)
      )
    ).flatten

  def toProcedureCode(): ProcedureCode = ProcedureCode(procedureCode.getOrElse(""))

  def containsAdditionalCode(code: String): Boolean = additionalProcedureCodes.contains(code)
}

object ProcedureCodesData extends FieldMapping {
  implicit val format = Json.format[ProcedureCodesData]

  val pointer: ExportsFieldPointer = "procedureCodes"
  val procedureCodesPointer: ExportsFieldPointer = "procedure.code"
  val additionalProcedureCodesPointer: ExportsFieldPointer = "additionalProcedureCodes"

  val formId = "ProcedureCodesData"

  // Onward Supply Relief
  val osrProcedureCodes = Set("1042")

  // Export Inventory Cleansing Record
  val eicrProcedureCodes = Set("0019")

  // Warehouse identifier required
  val warehouseRequiredProcedureCodes = Set("07", "71", "78")
  def isWarehouseRequiredCode(code: String): Boolean = warehouseRequiredProcedureCodes.exists(code.endsWith)

  val limitOfCodes = 99
}
