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

package util.validators

import org.scalatest.{MustMatchers, WordSpec}
import utils.validators.FormFieldValidator._

class FormFieldValidatorSpec extends WordSpec with MustMatchers {

  "FormFieldValidator on noLongerThan" should {

    "return false" when {
      "provided with negative length value" in {
        val input = "Any String"
        val length = -1
        noLongerThan(length)(input) must be(false)
      }

      "provided with String longer than provided length value" in {
        val input = "Any String"
        val length = 1
        noLongerThan(length)(input) must be(false)
      }
    }

    "return true" when {
      "provided with String shorter than provided length value" in {
        val input = "Any String"
        val length = 20
        noLongerThan(length)(input) must be(true)
      }

      "provided with String with length equal to provided value" in {
        val input = "Any String"
        val length = 10
        noLongerThan(length)(input) must be(true)
      }

      "provided with empty String and length value equal 0" in {
        val input = ""
        val length = 0
        noLongerThan(length)(input) must be(true)
      }
    }
  }

  "FormFieldValidator on isNumeric" should {

    "return false" when {
      "provided with alphabetic character" in {
        val input = "A"
        isNumeric(input) must be(false)
      }

      "provided with special character" in {
        val input = "$"
        isNumeric(input) must be(false)
      }

      "provided with several numeric and an alphabetic character" in {
        val input = "1234567A"
        isNumeric(input) must be(false)
      }

      "provided with several numeric and a special character" in {
        val input = "1234567&"
        isNumeric(input) must be(false)
      }
    }

    "return true" when {
      "provided with single numeric character" in {
        val input = "1"
        isNumeric(input) must be(true)
      }

      "provided with multiple numeric characters" in {
        val input = "1234567890"
        isNumeric(input) must be(true)
      }

      "provided with empty String" in {
        val input = ""
        isNumeric(input) must be(true)
      }
    }
  }

  "FormFieldValidator on isAlphabetic" should {

    "return false" when {
      "provided with numeric character" in {
        val input = "1"
        isAlphabetic(input) must be(false)
      }

      "provided with special character" in {
        val input = "@"
        isAlphabetic(input) must be(false)
      }

      "provided with several alphabetic and a numeric character" in {
        val input = "ABCDEFG7"
        isAlphabetic(input) must be(false)
      }

      "provided with several alphabetic and a special character" in {
        val input = "ABCDEFG#"
        isAlphabetic(input) must be(false)
      }
    }

    "return true" when {
      "provided with single alphabetic character" in {
        val input = "A"
        isAlphabetic(input) must be(true)
      }

      "provided with multiple alphabetic characters" in {
        val input = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        isAlphabetic(input) must be(true)
      }

      "provided with empty String" in {
        val input = ""
        isAlphabetic(input) must be(true)
      }
    }
  }

  "FormFieldValidator on isAlphaNumeric" should {

    "return false" when {
      "provided with special character" in {
        val input = "%"
        isAlphanumeric(input) must be(false)
      }

      "provided with several alphanumeric and a special character" in {
        val input = "ABC123*"
        isAlphanumeric(input) must be(false)
      }
    }

    "return true" when {
      "provided with single numeric character" in {
        val input = "8"
        isAlphanumeric(input) must be(true)
      }

      "provided with single alphabetic character" in {
        val input = "A"
        isAlphanumeric(input) must be(true)
      }

      "provided with both numeric and alphabetic characters" in {
        val input = "ABCD2358"
        isAlphanumeric(input) must be(true)
      }

      "provided with empty String" in {
        val input = ""
        isAlphanumeric(input) must be(true)
      }
    }
  }

  "FormFieldValidator on hasNoMoreDecimalPlacesThan" should {

    "return false" when {
      "input contains non-numeric characters" in {
        val input = "123A"
        val decimalPlaces = 1
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(false)
      }

      "input contains decimal separator other than \".\"" in {
        val input = "123,1"
        val decimalPlaces = 1
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(false)
      }

      "input contains double decimal separator" in {
        val input = "123.4.5"
        val decimalPlaces = 4
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(false)
      }

      "input contains more digits after decimal place than required" in {
        val input = "123.45"
        val decimalPlaces = 1
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(false)
      }
    }

    "return true" when {
      "input contains less digits after decimal place than required" in {
        val input = "123.4"
        val decimalPlaces = 3
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(true)
      }

      "input contains the exact number of digits after decimal place to what is required" in {
        val input = "123.456"
        val decimalPlaces = 3
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(true)
      }

      "input contains no decimal separator" in {
        val input = "12345"
        val decimalPlaces = 3
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(true)
      }

      "input contains no digit before decimal place" in {
    val input = ".123"
    val decimalPlaces = 3
    isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(true)
  }
    }
  }

}
