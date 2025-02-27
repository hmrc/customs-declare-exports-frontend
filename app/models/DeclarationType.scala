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

package models

import play.api.libs.json._
import utils.EnumJson

object DeclarationType extends Enumeration {
  type DeclarationType = Value

  implicit val format: Format[DeclarationType.Value] = EnumJson.format(DeclarationType)

  val STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL, CLEARANCE = Value
  val allDeclarationTypes: Seq[Value] = List(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL, CLEARANCE)

  def allDeclarationTypesExcluding(types: DeclarationType*): Seq[DeclarationType.Value] = allDeclarationTypes diff types

  val nonClearanceJourneys: Seq[Value] = allDeclarationTypesExcluding(CLEARANCE)
  val standardAndSupplementary: Seq[Value] = List(STANDARD, SUPPLEMENTARY)
  val occasionalAndSimplified: Seq[Value] = List(OCCASIONAL, SIMPLIFIED)

  def isStandardOrSupplementary(declaration: ExportsDeclaration): Boolean = standardAndSupplementary.contains(declaration.`type`)
  def isOccasionalOrSimplified(declaration: ExportsDeclaration): Boolean = occasionalAndSimplified.contains(declaration.`type`)

  def isStandardOrSupplementary(declarationType: DeclarationType): Boolean = standardAndSupplementary.contains(declarationType)
}
