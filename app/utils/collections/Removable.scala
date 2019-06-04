/*
 * Copyright 2019 HM Revenue & Customs
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

package utils.collections

object Removable {
  implicit class RemovableSeq[A](seq: Seq[A]) extends Seq[A] {

    /**
      * Removes the element at the specified position in this sequence.
      * Shifts any subsequent elements to the left (subtracts one from their indices).
      * Returns new sequence without the element at the specified position.
      *
      * @param index - the index of the element to be removed
      * @return new sequence without the element at the specified position
      * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= length)
      */
    def removeByIdx(index: Int): RemovableSeq[A] = {
      if (index < 0 || index >= length) throw new IndexOutOfBoundsException

      val (start, _ +: end ) = seq.splitAt(index)
      start ++ end
    }

    override def length: Int = seq.length
    override def apply(idx: Int): A = seq.apply(idx)
    override def iterator: Iterator[A] = seq.iterator
  }
}
