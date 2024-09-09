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
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

trait ImplicitlySequencedObject

trait IsoData[E <: DiffTools[E] with ImplicitlySequencedObject] {
  this: DiffTools[_] =>
  val subPointer: String
  val elements: Seq[E]

  def createDiffWithEmpty(originalIsEmpty: Boolean, pointerString: ExportsFieldPointer): ExportsDeclarationDiff =
    if (originalIsEmpty) createDiff(Seq.empty, elements, combinePointers(pointerString, subPointer))
    else createDiff(elements, Seq.empty, combinePointers(pointerString, subPointer))
}
