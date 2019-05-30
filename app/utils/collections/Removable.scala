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
    * Removes an element at given index, providing the collection is defined this index.
      * @param idx - index of the element to remove
      * @return - new Sequence without element at given index
      */
    def removeByIdx(idx: Int): RemovableSeq[A] = {
      val (start, _ :: end) = seq.splitAt(idx)
      start ++ end
    }

    override def length: Int = seq.length
    override def apply(idx: Int): A = seq.apply(idx)
    override def iterator: Iterator[A] = seq.iterator
  }
}
