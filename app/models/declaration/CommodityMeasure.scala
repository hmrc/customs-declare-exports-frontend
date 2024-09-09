/*
 * Copyright 2024 HM Revenue & Customs
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

import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.declaration.CommodityMeasure.{grossMassPointer, netMassPointer, supplementaryUnitsPointer}
import models.declaration.ExportItem.itemsPrefix
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class CommodityMeasure(
  supplementaryUnits: Option[String],
  supplementaryUnitsNotRequired: Option[Boolean],
  grossMass: Option[String],
  netMass: Option[String]
) extends DiffTools[CommodityMeasure] {

  // supplementaryUnitsNotRequired is not used to build WCO XML payload
  def createDiff(original: CommodityMeasure, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.supplementaryUnits, supplementaryUnits, combinePointers(pointerString, supplementaryUnitsPointer, sequenceId)),
      compareStringDifference(original.netMass, netMass, combinePointers(pointerString, netMassPointer, sequenceId)),
      compareStringDifference(original.grossMass, grossMass, combinePointers(pointerString, grossMassPointer, sequenceId))
    ).flatten
}

object CommodityMeasure extends FieldMapping {
  implicit val format: OFormat[CommodityMeasure] = Json.format[CommodityMeasure]

  val pointer: ExportsFieldPointer = "commodityMeasure"
  val supplementaryUnitsPointer: ExportsFieldPointer = "supplementaryUnits"
  val netMassPointer: ExportsFieldPointer = "netMass"
  val grossMassPointer: ExportsFieldPointer = "grossMass"

  lazy val keyForGrossMass = s"$itemsPrefix.grossWeight"
  lazy val keyForNetMass = s"$itemsPrefix.netWeight"
  lazy val keyForSupplementaryUnits = s"$itemsPrefix.supplementaryUnits"
}
