/*
 * Copyright 2022 HM Revenue & Customs
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

package services.model

import base.UnitWithMocksSpec

class FieldNamePointerSpec extends UnitWithMocksSpec {

  "Apply" should {

    "create correct pointer based on the list" in {

      val pattern = "some-pattern"
      val fieldName = "some-fieldname"
      val error = List(pattern, fieldName)

      FieldNamePointer.apply(error) mustBe FieldNamePointer(pattern, Some(fieldName))
    }

    "create pointer with missing fieldname" in {

      val pattern = "some-pattern"
      val fieldName = ""
      val error = List(pattern, fieldName)

      FieldNamePointer.apply(error) mustBe FieldNamePointer(pattern, None)
    }

    "throw an exception when input is incorrect" in {

      intercept[IllegalArgumentException](FieldNamePointer.apply(List.empty))
    }
  }

  "All Pointers" should {

    "have 2 pointers" in {

      FieldNamePointer.allFieldNamePointers.length mustBe 2
    }

  }

}
