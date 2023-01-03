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

package models

import base.UnitSpec
import play.api.libs.json.{JsString, JsSuccess, Json}

class PointerSpec extends UnitSpec {

  "PointerSection" should {
    val field = PointerSection("ABC", PointerSectionType.FIELD)
    val sequence = PointerSection("123", PointerSectionType.SEQUENCE)

    "map field to pattern" in {
      field.pattern mustBe "ABC"
    }

    "map sequence to pattern" in {
      sequence.pattern mustBe "$"
    }

    "map field to string" in {
      field.toString mustBe "ABC"
    }

    "map sequence to string" in {
      sequence.toString mustBe "#123"
    }

    "map field from string" in {
      PointerSection("ABC") mustBe field
    }

    "map sequence from string" in {
      PointerSection("#123") mustBe sequence
    }
  }

  "Pointer" should {
    val field1 = PointerSection("ABC", PointerSectionType.FIELD)
    val sequence1 = PointerSection("123", PointerSectionType.SEQUENCE)
    val field2 = PointerSection("000", PointerSectionType.FIELD)
    val sequence2 = PointerSection("321", PointerSectionType.SEQUENCE)
    val pointer = Pointer(List(field1, sequence1, field2, sequence2))

    "map to pattern" in {
      pointer.pattern mustBe "ABC.$.000.$"
    }

    "map to string" in {
      pointer.toString mustBe "ABC.#123.000.#321"
    }

    "serialize to JSON" in {
      Json.toJson(pointer)(Pointer.format) mustBe JsString("ABC.#123.000.#321")
    }

    "deserialize from JSON" in {
      Json.fromJson(JsString("ABC.#123.000.#321"))(Pointer.format) mustBe JsSuccess(pointer)
    }
  }
}
