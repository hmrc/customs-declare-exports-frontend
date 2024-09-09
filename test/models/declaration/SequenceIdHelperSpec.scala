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

import base.UnitSpec
import controllers.helpers.SequenceIdHelper.handleSequencing
import models.DeclarationMeta
import models.DeclarationMeta.sequenceIdPlaceholder
import models.declaration.DeclarationStatus._

import java.time.Instant

class SequenceIdHelperSpec extends UnitSpec {

  case class TestEso(sequenceId: Int) extends ExplicitlySequencedObject[TestEso] {
    override def updateSequenceId(newSequenceId: Int): TestEso = this.copy(sequenceId = newSequenceId)
  }

  implicit val keyProvider: EsoKeyProvider[TestEso] = new EsoKeyProvider[TestEso] {
    override val seqIdKey: String = "test"
  }

  val preSubmitDecMeta = DeclarationMeta(
    status = DRAFT,
    maxSequenceIds = Map(implicitly[EsoKeyProvider[TestEso]].seqIdKey -> 2),
    createdDateTime = Instant.now(),
    updatedDateTime = Instant.now()
  )
  val postSubmitDecMeta = DeclarationMeta(
    status = AMENDMENT_DRAFT,
    maxSequenceIds = Map(implicitly[EsoKeyProvider[TestEso]].seqIdKey -> 2),
    createdDateTime = Instant.now(),
    updatedDateTime = Instant.now()
  )
  val elements = Seq(TestEso(1), TestEso(2))

  "SequenceIdHelper.handleSequencing" should {

    "update elements and meta for pre-submission status" when {

      "an element is added" in {
        val (updatedElements, updatedMeta) = handleSequencing(elements :+ TestEso(sequenceIdPlaceholder), preSubmitDecMeta)

        updatedElements.size mustBe 3
        updatedElements.zipWithIndex.foreach(elemWithIdx => elemWithIdx._1.sequenceId mustBe elemWithIdx._2 + 1)
        updatedMeta.maxSequenceIds(implicitly[EsoKeyProvider[TestEso]].seqIdKey) mustBe 3
      }

      "the last element is removed" in {
        val (updatedElements, updatedMeta) = handleSequencing(elements.filter(_.sequenceId == 1), preSubmitDecMeta)

        updatedElements.size mustBe 1
        updatedElements.zipWithIndex.foreach(elemWithIdx => elemWithIdx._1.sequenceId mustBe elemWithIdx._2 + 1)
        updatedMeta.maxSequenceIds(implicitly[EsoKeyProvider[TestEso]].seqIdKey) mustBe 1
      }

      "an element is added and the first removed" in {
        val (updatedElements, updatedMeta) =
          handleSequencing(elements.filter(_.sequenceId == 2) :+ TestEso(sequenceIdPlaceholder), preSubmitDecMeta)

        updatedElements.size mustBe 2
        updatedElements.zipWithIndex.foreach(elemWithIdx => elemWithIdx._1.sequenceId mustBe elemWithIdx._2 + 1)
        updatedMeta.maxSequenceIds(implicitly[EsoKeyProvider[TestEso]].seqIdKey) mustBe 2
      }
    }

    "update elements and meta for post-submission status" when {

      "an element is added" in {
        val (updatedElements, updatedMeta) = handleSequencing(elements :+ TestEso(sequenceIdPlaceholder), postSubmitDecMeta)

        updatedElements.size mustBe 3
        updatedElements.zipWithIndex.foreach(elemWithIdx => elemWithIdx._1.sequenceId mustBe elemWithIdx._2 + 1)
        updatedMeta.maxSequenceIds(implicitly[EsoKeyProvider[TestEso]].seqIdKey) mustBe 3
      }

      "the last element is removed" in {
        val (updatedElements, updatedMeta) = handleSequencing(elements.filter(_.sequenceId == 1), postSubmitDecMeta)

        updatedElements.size mustBe 1
        updatedElements(0).sequenceId mustBe 1
        updatedMeta.maxSequenceIds(implicitly[EsoKeyProvider[TestEso]].seqIdKey) mustBe 2
      }

      "an element is added and the first removed" in {
        val (updatedElements, updatedMeta) =
          handleSequencing(elements.filter(_.sequenceId == 2) :+ TestEso(sequenceIdPlaceholder), postSubmitDecMeta)

        updatedElements.size mustBe 2
        updatedElements(0).sequenceId mustBe 2
        updatedElements(1).sequenceId mustBe 3
        updatedMeta.maxSequenceIds(implicitly[EsoKeyProvider[TestEso]].seqIdKey) mustBe 3
      }
    }
  }
}
