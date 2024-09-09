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

import forms.section2.AdditionalActor
import models.ExportsFieldPointer.ExportsFieldPointer
import models.{ExportsDeclaration, FieldMapping}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class AdditionalActors(actors: Seq[AdditionalActor]) extends DiffTools[AdditionalActors] with IsoData[AdditionalActor] {

  override val subPointer: ExportsFieldPointer = AdditionalActor.pointer
  override val elements: Seq[AdditionalActor] = actors

  def createDiff(original: AdditionalActors, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    createDiff(original.actors, actors, combinePointers(pointerString, subPointer, sequenceId))
}

object AdditionalActors extends FieldMapping {

  val pointer: ExportsFieldPointer = "additionalActors"

  private lazy val parties = s"${ExportsDeclaration.pointer}.${Parties.pointer}"

  lazy val eoriPointerForAmend = s"$parties.$pointer.${AdditionalActor.pointer}.${AdditionalActor.eoriPointer}"
  lazy val partyTypePointerForAmend = s"$parties.$pointer.${AdditionalActor.pointer}.${AdditionalActor.partyTypePointer}"

  implicit val format: OFormat[AdditionalActors] = Json.format[AdditionalActors]

  val maxNumberOfActors = 99
}
