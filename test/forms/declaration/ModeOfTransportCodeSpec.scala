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

package forms.declaration

import base.UnitWithMocksSpec

class ModeOfTransportCodeSpec extends UnitWithMocksSpec {

  "ModeOfTransportCode classicFormatter" should {
    val errorMessageKey = "error.message"
    val formatter = ModeOfTransportCode.classicFormatter(errorMessageKey)

    "return Right" when {
      "provided with allowed code" in {
        val input = Map("clientKey" -> "1")

        formatter.bind("clientKey", input) mustBe Right(ModeOfTransportCode.Maritime)
      }
    }

    "return Left" when {

      "provided with empty String" in {
        val input = Map("clientKey" -> "")

        val result = formatter.bind("clientKey", input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe errorMessageKey
      }

      "provided with a code other than allowed" in {
        val input = Map("clientKey" -> "123")

        val result = formatter.bind("clientKey", input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe errorMessageKey
      }

      "provided with no-code value" in {
        val input = Map("clientKey" -> "no-code")

        val result = formatter.bind("clientKey", input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe errorMessageKey
      }
    }
  }

  "ModeOfTransportCode clearanceJourneyFormatter" should {
    val errorMessageKey = "error.message"
    val formatter = ModeOfTransportCode.clearanceJourneyFormatter(errorMessageKey)

    "return Right" when {

      "provided with allowed code" in {
        val input = Map("clientKey" -> "1")
        formatter.bind("clientKey", input) mustBe Right(ModeOfTransportCode.Maritime)
      }

      "provided with no-code value" in {
        val input = Map("clientKey" -> "no-code")
        formatter.bind("clientKey", input) mustBe Right(ModeOfTransportCode.Empty)
      }
    }

    "return Left" when {

      "provided with empty String" in {
        val input = Map("clientKey" -> "")

        val result = formatter.bind("clientKey", input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe errorMessageKey
      }

      "provided with a code other than allowed" in {
        val input = Map("clientKey" -> "123")

        val result = formatter.bind("clientKey", input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe errorMessageKey
      }
    }
  }
}
