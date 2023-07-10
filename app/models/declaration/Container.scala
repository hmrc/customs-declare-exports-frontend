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
import models.AmendmentRow.{forAddedValue, forRemovedValue}
import models.DeclarationMeta.sequenceIdPlaceholder
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.Container.{keyForAmend, keyForContainerId, keyForSeals}
import models.{AmendmentOp, FieldMapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class Container(sequenceId: Int = sequenceIdPlaceholder, id: String, seals: Seq[Seal])
    extends DiffTools[Container] with ExplicitlySequencedObject[Container] with AmendmentOp {

  override def createDiff(original: Container, pointerString: ExportsFieldPointer, sequenceId: Option[Int]): ExportsDeclarationDiff =
    Seq(compareStringDifference(original.id, id, combinePointers(pointerString, Container.idPointer, sequenceId))).flatten ++
      createDiff(original.seals, seals, combinePointers(pointerString, Seal.pointer, sequenceId))

  override def updateSequenceId(sequenceId: Int): Container = copy(sequenceId = sequenceId)

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    if (seals.isEmpty) forAddedValue(pointer, messages(keyForContainerId), id)
    else {
      val newValue = s"${messages(keyForContainerId)}: ${id}<br/>${messages(keyForSeals)}:<br/>${seals.map(_.id).mkString("<br/>")}"
      forAddedValue(pointer, messages(keyForAmend), newValue)
    }

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    if (seals.isEmpty) forRemovedValue(pointer, messages(keyForContainerId), id)
    else {
      val newValue = s"${messages(keyForContainerId)}: ${id}<br/>${messages(keyForSeals)}:<br/>${seals.map(_.id).mkString("<br/>")}"
      forRemovedValue(pointer, messages(keyForAmend), newValue)
    }
}

object Container extends FieldMapping {

  val keyForAmend = "declaration.summary.container.information"
  val keyForContainerId = "declaration.summary.container.id"
  val keyForSeals = "declaration.summary.container.securitySeals"

  implicit val format: OFormat[Container] = Json.format[Container]

  override val pointer: ExportsFieldPointer = "containers"
  val idPointer: ExportsFieldPointer = "id"

  val maxNumberOfItems = 9999
}
