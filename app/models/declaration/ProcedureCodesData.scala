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

import controllers.helpers.AmendmentInstance
import forms.section5.procedurecodes.ProcedureCode
import models.AmendmentRow.{forAddedValue, forRemovedValue, pointerToSelector}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExportItem.itemsPrefix
import models.declaration.ProcedureCodesData.{additionalProcedureCodesPointer, keyForAPCs, keyForPC, osrProcedureCode, procedureCodesPointer}
import models.{AmendmentOp, FieldMapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{ExportsDeclarationDiff, combinePointers, compareStringDifference}

case class ProcedureCodesData(procedureCode: Option[ProcedureCodesData.ProcedureCode], additionalProcedureCodes: Seq[String])
    extends DiffTools[ProcedureCodesData] with AmendmentOp {

  override def createDiff(original: ProcedureCodesData, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.procedureCode, procedureCode, combinePointers(pointerString, procedureCodesPointer, sequenceId)),
      compareStringDifference(
        original.additionalProcedureCodes,
        additionalProcedureCodes,
        combinePointers(pointerString, additionalProcedureCodesPointer, sequenceId)
      )
    ).flatten

  lazy val hasOsrProcedureCode: Boolean = procedureCode.fold(false)(_ == osrProcedureCode)

  lazy val toProcedureCode: ProcedureCode = ProcedureCode(procedureCode.getOrElse(""))

  def containsAPC(code: String): Boolean = additionalProcedureCodes.contains(code)

  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    Seq(
      Option.when(procedureCode.nonEmpty)(convertToLeafPointer(pointer, procedureCodesPointer)),
      Option.when(additionalProcedureCodes.nonEmpty)(convertToLeafPointer(pointer, additionalProcedureCodesPointer))
    ).flatten

  def valueRemoved(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    Seq(convertToLeafPointer(pointer, procedureCodesPointer), convertToLeafPointer(pointer, additionalProcedureCodesPointer))
}

object ProcedureCodesData extends FieldMapping {
  implicit val format: OFormat[ProcedureCodesData] = Json.format[ProcedureCodesData]

  type ProcedureCode = String

  val pointer: ExportsFieldPointer = "procedureCodes"
  val procedureCodesPointer: ExportsFieldPointer = "procedure.code"
  val additionalProcedureCodesPointer: ExportsFieldPointer = "additionalProcedureCodes"

  lazy val keyForPC = s"$itemsPrefix.procedureCode"
  lazy val keyForAPC = s"$itemsPrefix.additionalProcedureCode"
  lazy val keyForAPCs = s"$itemsPrefix.additionalProcedureCodes"

  val formId = "ProcedureCodesData"

  val lowValueDeclaration = "3LV" // Additional Procedure code

  // Onward Supply Relief
  val osrProcedureCode = "1042"
  val osrProcedureCodes = Set(osrProcedureCode)

  // Export Inventory Cleansing Record
  val eicrProcedureCodes = Set("0019")

  // Warehouse identifier required
  val warehouseRequiredProcedureCodes = Set("07", "71", "78")
  def isWarehouseRequiredCode(code: String): Boolean = warehouseRequiredProcedureCodes.exists(code.endsWith)

  val limitOfCodes = 99
}
