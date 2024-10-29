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

package controllers.helpers

import models.{DeclarationMeta, ExportsDeclaration}
import models.DeclarationMeta.sequenceIdPlaceholder
import models.declaration.DeclarationStatus.preSubmissionStatuses
import models.declaration.{EsoKeyProvider, ExplicitlySequencedObject}

object SequenceIdHelper {

  def handleSequencing[T <: ExplicitlySequencedObject[T]](elements: Seq[T], declarationMeta: DeclarationMeta)(
    implicit keyProvider: EsoKeyProvider[T]
  ): (Seq[T], DeclarationMeta) = {

    val (updatedElements, updatedMeta) =
      if (elements.isEmpty && preSubmissionStatuses.contains(declarationMeta.status)) {
        val maxSequenceIds = declarationMeta.maxSequenceIds.updated(keyProvider.seqIdKey, sequenceIdPlaceholder)
        (List.empty[T], declarationMeta.copy(maxSequenceIds = maxSequenceIds))
      } else
        elements.zipWithIndex.foldLeft((Seq.empty[T], declarationMeta)) { (sequenceAndMeta: (Seq[T], DeclarationMeta), elementAndIdx: (T, Int)) =>
          val sequenceOfElems = sequenceAndMeta._1
          val meta = sequenceAndMeta._2

          def getUpdatedElementsAndMeta(nextSeqId: Int, element: T): (Seq[T], DeclarationMeta) = {
            val updatedElement = element.updateSequenceId(nextSeqId)
            val updatedMeta = meta.copy(maxSequenceIds = meta.maxSequenceIds.updated(keyProvider.seqIdKey, nextSeqId))
            (sequenceOfElems :+ updatedElement, updatedMeta)
          }

          elementAndIdx match {
            // Prior to submission we will sequence elems using their index, ensuring no unnecessary gaps on eventual submission.
            case (element, idx) if preSubmissionStatuses.contains(declarationMeta.status) =>
              getUpdatedElementsAndMeta(idx + 1, element)

            // After submission we will sequence using sequenceIds to correspond with DMS.
            case (element, _) =>
              // Presence of the placeholder means we have a new element.
              if (element.sequenceId == sequenceIdPlaceholder) {
                val sequenceId = declarationMeta.maxSequenceIds.get(keyProvider.seqIdKey).fold(0) { seqId =>
                  if (seqId == sequenceIdPlaceholder) 0 else seqId
                }
                getUpdatedElementsAndMeta(sequenceId + 1, element)
              }
              // Absence of placeholder means no new elements added and existing seqIds are retained.
              else (sequenceOfElems :+ element, meta)
          }
        }

    (updatedElements, updatedMeta)
  }

  def valueOfEso[T <: ExplicitlySequencedObject[T]](declaration: ExportsDeclaration)(implicit provider: EsoKeyProvider[T]): Option[Int] =
    declaration.declarationMeta.maxSequenceIds.get(provider.seqIdKey)
}
