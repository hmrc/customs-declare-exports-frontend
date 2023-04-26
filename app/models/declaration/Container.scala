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

import forms.declaration.Seal
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class Container(sequenceId: Int, id: String, seals: Seq[Seal]) extends DiffTools[Container] with ExplicitlySequencedObject[Container] {

  override def createDiff(original: Container, pointerString: ExportsFieldPointer, sequenceId: Option[Int]): ExportsDeclarationDiff =
    createDiff(original.seals, seals, combinePointers(pointerString, Seal.pointer, sequenceId))

  override def updateSequenceId(sequenceId: Int): Container = copy(sequenceId = sequenceId)
}

object Container extends FieldMapping with EsoFactory[Container] {
  implicit val format: OFormat[Container] = Json.format[Container]

  override val pointer: ExportsFieldPointer = "containers"
  val idPointer: ExportsFieldPointer = "id"

  val maxNumberOfItems = 9999
  override val seqIdKey: String = "Containers"
}
