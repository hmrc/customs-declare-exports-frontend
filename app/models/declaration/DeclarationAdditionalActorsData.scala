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

import forms.declaration.DeclarationAdditionalActors
import models.ExportsFieldPointer.ExportsFieldPointer
import models.{ExportsDeclaration, FieldMapping}
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class DeclarationAdditionalActorsData(actors: Seq[DeclarationAdditionalActors])
    extends DiffTools[DeclarationAdditionalActorsData] with IsoData[DeclarationAdditionalActors] {

  override val subPointer: ExportsFieldPointer = DeclarationAdditionalActors.pointer
  override val elements: Seq[DeclarationAdditionalActors] = actors

  def createDiff(
    original: DeclarationAdditionalActorsData,
    pointerString: ExportsFieldPointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    createDiff(original.actors, actors, combinePointers(pointerString, subPointer, sequenceId))
}

object DeclarationAdditionalActorsData extends FieldMapping {

  val pointer: ExportsFieldPointer = "declarationAdditionalActorsData"

  private lazy val parties = s"${ExportsDeclaration.pointer}.${Parties.pointer}"

  lazy val eoriPointerForAmend = s"$parties.$pointer.${DeclarationAdditionalActors.pointer}.${DeclarationAdditionalActors.eoriPointer}"
  lazy val partyTypePointerForAmend = s"$parties.$pointer.${DeclarationAdditionalActors.pointer}.${DeclarationAdditionalActors.partyTypePointer}"

  implicit val format = Json.format[DeclarationAdditionalActorsData]

  val maxNumberOfActors = 99

  val formId = "DeclarationAdditionalActorsData"
}
