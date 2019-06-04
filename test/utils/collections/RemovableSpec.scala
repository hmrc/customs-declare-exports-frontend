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

import org.scalatest.{MustMatchers, WordSpec}

class RemovableSpec extends WordSpec with MustMatchers {

  "RemovableSeq on removeByIdx" should {
    import Removable.RemovableSeq

    "throw an Exception" when {

      "provided with 0 index on empty sequence" in {
        val collection = Seq.empty[String]
        val indexToRemove = 0
        assertIndexOutOfBoundsExceptionBeingThrown(collection, indexToRemove)
      }

      "provided with -1 index" in {
        val collection = Seq("a", "b", "c", "d", "e")
        val indexToRemove = -1
        assertIndexOutOfBoundsExceptionBeingThrown(collection, indexToRemove)
      }

      "provided with index equal to sequence length" in {
        val collection = Seq("a", "b", "c", "d", "e")
        val indexToRemove = collection.length
        assertIndexOutOfBoundsExceptionBeingThrown(collection, indexToRemove)
      }

      "provided with index bigger than sequence length" in {
        val collection = Seq("a", "b", "c", "d", "e")
        val indexToRemove = collection.length + 1
        assertIndexOutOfBoundsExceptionBeingThrown(collection, indexToRemove)
      }

      def assertIndexOutOfBoundsExceptionBeingThrown(collection: Seq[_], indexToRemove: Int): Unit =
        an[IndexOutOfBoundsException] should be thrownBy collection.removeByIdx(indexToRemove)
    }

    "return empty sequence" when {

      "removing element from single-element collection" in {
        val collection = Seq("single-element")
        val indexToRemove = 0

        collection.removeByIdx(indexToRemove) must equal(Seq.empty[String])
      }
    }

    "return a new sequence without one element" when {

      "removing the first element in the sequence" in {
        val collection = Seq("a", "b", "c", "d", "e")
        val indexToRemove = 0
        val expectedOutput = Seq("b", "c", "d", "e")

        collection.removeByIdx(indexToRemove) must equal(expectedOutput)
      }

      "removing the last element in the sequence" in {
        val collection = Seq("a", "b", "c", "d", "e")
        val indexToRemove = collection.length - 1
        val expectedOutput = Seq("a", "b", "c", "d")

        collection.removeByIdx(indexToRemove) must equal(expectedOutput)
      }

      "removing an element from the middle of the sequence" in {
        val collection = Seq("a", "b", "c", "d", "e")
        val indexToRemove = 2
        val expectedOutput = Seq("a", "b", "d", "e")

        collection.removeByIdx(indexToRemove) must equal(expectedOutput)
      }
    }

  }

}
