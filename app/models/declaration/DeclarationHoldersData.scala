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

import forms.common.YesNoAnswer
import forms.declaration.declarationHolder.DeclarationHolder
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class DeclarationHoldersData(holders: Seq[DeclarationHolder], isRequired: Option[YesNoAnswer])
    extends DiffTools[DeclarationHoldersData] with IsoData[DeclarationHolder] {
  // isRequired field is not used to generate the WCO XML
  override val subPointer: ExportsFieldPointer = DeclarationHolder.pointer
  override val elements: Seq[DeclarationHolder] = holders
  def createDiff(original: DeclarationHoldersData, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    createDiff(original.holders, holders, combinePointers(pointerString, subPointer, None))
  def containsHolder(holder: DeclarationHolder): Boolean = holders.contains(holder)
}

object DeclarationHoldersData extends FieldMapping {
  implicit val format = Json.format[DeclarationHoldersData]

  val pointer: ExportsFieldPointer = "declarationHoldersData"

  def apply(holders: Seq[DeclarationHolder]): DeclarationHoldersData =
    new DeclarationHoldersData(holders, None)

  val formId = "DeclarationHoldersData"

  val limitOfHolders = 99
}
