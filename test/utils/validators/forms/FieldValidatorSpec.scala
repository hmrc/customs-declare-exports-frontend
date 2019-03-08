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

package utils.validators.forms

import org.scalatest.{MustMatchers, WordSpec}
import utils.validators.forms.FieldValidator._

class FieldValidatorSpec extends WordSpec with MustMatchers {

  val anyString = "Any string"
  val emptyString = ""

  "Predicate operations" should {
    "correctly apply and logic for booleans" in {
      (true and true) must be(true)
      (true and false) must be(false)
      (false and true) must be(false)
      (false and false) must be(false)
    }

    "correctly apply or logic for booleans" in {
      (true or true) must be(true)
      (true or false) must be(true)
      (false or true) must be(true)
      (false or false) must be(false)
    }

    "correctly apply and logic for function and boolean" in {
      val fun = (input: String) => input.contains("a")

      (fun("a") and true) must be(true)
      (fun("a") and false) must be(false)
      (true and fun("a")) must be(true)
      (false and fun("a")) must be(false)

      (fun("b") and true) must be(false)
      (fun("b") and false) must be(false)
      (true and fun("b")) must be(false)
      (false and fun("b")) must be(false)
    }

    "correctly apply or logic for function and boolean" in {
      val fun = (input: String) => input.contains("a")

      (fun("a") or true) must be(true)
      (fun("a") or false) must be(true)
      (true or fun("a")) must be(true)
      (false or fun("a")) must be(true)

      (fun("b") or true) must be(true)
      (fun("b") or false) must be(false)
      (true or fun("b")) must be(true)
      (false or fun("b")) must be(false)
    }

    "correctly apply and logic for two functions" in {
      val fun1 = (input: String) => input.contains("a")
      val fun2 = (input: String) => input.contains("b")

      (fun1("a") and fun2("b")) must be(true)
      (fun1("a") and fun2("a")) must be(false)
      (fun1("b") and fun2("b")) must be(false)
      (fun1("b") and fun2("a")) must be(false)
    }

    "correctly apply or logic for two functions" in {
      val fun1 = (input: String) => input.contains("a")
      val fun2 = (input: String) => input.contains("b")

      (fun1("a") or fun2("b")) must be(true)
      (fun1("a") or fun2("a")) must be(true)
      (fun1("b") or fun2("b")) must be(true)
      (fun1("b") or fun2("a")) must be(false)
    }
  }

  "FormFieldValidator isEmpty" should {
    "return false" when {
      "provided with non empty value" in {
        isEmpty(anyString) must be(false)
      }
    }

    "return true" when {
      "provided with empty value" in {
        isEmpty(emptyString) must be(true)
      }
    }
  }

  "FormFieldValidation nonEmpty" should {
    "return false" when {
      "provided with empty value" in {
        nonEmpty(emptyString) must be(false)
      }
    }

    "return true" when {
      "provided with non-empty value" in {
        nonEmpty(anyString) must be(true)
      }
    }
  }

  "FormFieldValidator noLongerThan" should {
    "return false" when {
      "provided with negative length value" in {
        val length = -1

        noLongerThan(length)(anyString) must be(false)
      }

      "provided with String longer than provided length value" in {
        val length = 1

        noLongerThan(length)(anyString) must be(false)
      }
    }

    "return true" when {
      "provided with String shorter than provided length value" in {
        val length = 20

        noLongerThan(length)(anyString) must be(true)
      }

      "provided with String with length equal to provided value" in {
        val length = 10

        noLongerThan(length)(anyString) must be(true)
      }

      "provided with empty String and length value equal 0" in {
        val length = 0

        noLongerThan(length)(emptyString) must be(true)
      }
    }
  }

  "FormFieldValidation noShorterThan" should {
    "return false" when {
      "provided with shorter string" in {
        val length = 20

        noShorterThan(length)(anyString) must be(false)
      }
    }

    "return true" when {
      "provided with negative length value" in {
        val length = -1

        noShorterThan(length)(anyString) must be(true)
      }
      "provided with string longer than provided length value" in {
        val length = 1

        noShorterThan(length)(anyString) must be(true)
      }

      "provided with string exactly the same length that provided" in {
        val length = 10

        noShorterThan(length)(anyString) must be(true)
      }

      "provided with empty string and length equal 0" in {
        val length = 0

        noShorterThan(length)(emptyString) must be(true)
      }
    }
  }

  "FormFieldValidation hasSpecificLength" should {
    "return false" when {
      "provided with string shorter than expected value" in {
        val length = 20

        hasSpecificLength(length)(anyString) must be(false)
      }

      "provided with string longer than expected value" in {
        val length = 5

        hasSpecificLength(length)(anyString) must be(false)
      }
    }

    "return true" when {
      "provided with string has the same length like expected value" in {
        val length = 10

        hasSpecificLength(length)(anyString) must be(true)
      }
    }
  }

  "FormFieldValidator isNumeric" should {
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

  "FormFieldValidator isAllCapitalLetter" should {
    "return false" when {
      "provided with string with numbers" in {
        val input = "ASD123ASD"

        isAllCapitalLetter(input) must be(false)
      }

      "provided with string with lowercase" in {
        val input = "ASDzxcASD"

        isAllCapitalLetter(input) must be(false)
      }
    }

    "return true" when {
      "provided with string with uppercase letters" in {
        val input = "ABCDEF"

        isAllCapitalLetter(input) must be(true)
      }

      "provided with empty string" in {
        isAllCapitalLetter(emptyString) must be(true)
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

  "FormFieldValidator isAlphanumeric" should {
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

  "FormFieldValidation isAlphanumericWithSpecialCharacters" should {
    "return false" when {
      "provided with string contains special characters not present in allowed characters set" in {
        val specialCharacters = Set(' ', '$', '@')
        val inputWithOtherSpecialChars = "as!$ &*3sda"

        isAlphanumericWithSpecialCharacters(specialCharacters)(inputWithOtherSpecialChars) must be(false)
      }

      "provided with string contains special characters but allowed special characters set is empty" in {
        val specialCharacters: Set[Char] = Set()
        val input = "asd!@#$%gh"

        isAlphanumericWithSpecialCharacters(specialCharacters)(input) must be(false)
      }
    }

    "return true" when {
      "provided with string doesn't have special characters" in {
        val specialCharacters = Set(' ', '$', '@')
        val input = "asd213"

        isAlphanumericWithSpecialCharacters(specialCharacters)(input) must be(true)
      }

      "provided with string contains only special characters from the list" in {
        val specialCharacters = Set(' ', '$', '@')
        val input = "A a B$ b$ C@ c@"

        isAlphanumericWithSpecialCharacters(specialCharacters)(input) must be(true)
      }
    }
  }

  "FormFieldValidation isAlphanumericWithAllowedSpecialCharacters" should {
    "return false" when {
      "provided with unsupported special characters" in {
        val input = "%$%$#@"

        isAlphanumericWithAllowedSpecialCharacters(input) must be(false)
      }
    }

    "return true" when {
      "provided with only numeric characters" in {
        val input = "1234"

        isAlphanumericWithAllowedSpecialCharacters(input) must be(true)
      }

      "provided with only alphabetic characters" in {
        isAlphanumericWithAllowedSpecialCharacters(anyString) must be(true)
      }

      "provided with string with both alphabetic and numeric characters" in {
        val input = "123abc"

        isAlphanumericWithAllowedSpecialCharacters(input) must be(true)
      }

      "provided with supported special characters" in {
        val input = "Special characters a,.-'/"

        isAlphanumericWithAllowedSpecialCharacters(input) must be(true)
      }
    }
  }

  "FormFieldValidator startsWithCapitalLetter" should {
    "return false" when {
      "input start with lowercase" in {
        val input = "lowercaseString"

        startsWithCapitalLetter(input) must be(false)
      }

      "string is empty" in {
        startsWithCapitalLetter(emptyString) must be(false)
      }
    }

    "return true" when {
      "string start with capital letter" in {
        val input = "CapitalLetter"

        startsWithCapitalLetter(input) must be(true)
      }
    }
  }

  "FormFieldValidator isContainedIn" should {
    "return false" when {
      "list is empty" in {
        isContainedIn(List())("element") must be(false)
      }

      "list doesn't contain specific element" in {
        isContainedIn(List("A"))("B") must be(false)
      }
    }

    "return true" when {
      "element is on the list" in {
        val list = List("A", "B", "C")

        isContainedIn(list)("A") must be(true)
      }
    }
  }

  "FormFieldValidator containsNotOnlyZeros" should {
    "return false" when {
      "string contains only zero" in {
        containsNotOnlyZeros("0") must be(false)
      }

      "string contains only several zeros" in {
        containsNotOnlyZeros("00") must be(false)
        containsNotOnlyZeros("000") must be(false)
      }
    }

    "return true" when {
      "string is empty" in {
        containsNotOnlyZeros("") must be(true)
      }

      "string contains different digits than 0" in {
        val input = "1230"

        containsNotOnlyZeros(input) must be(true)
      }

      "string contains alphabetic characters" in {
        val input = "asv"

        containsNotOnlyZeros(input) must be(true)
      }

      "string contains alphabetic characters and 0" in {
        val input = "0asd00"

        containsNotOnlyZeros(input) must be(true)
      }
    }
  }

  "FormFieldValidator isTailNumeric" should {
    "return false" when {
      "input is empty" in {
        isTailNumeric("") must be(false)
      }

      "input contains two or more characters but not only numeric" in {
        isTailNumeric("12nd12") must be(false)
      }
    }

    "return true" when {
      "input contains only one character" in {
        isTailNumeric("0") must be(true)
      }

      "input contains all numeric characters starts from 2nd char" in {
        isTailNumeric("a1241") must be(true)
      }
    }
  }

  "FormFieldValidator hasNoMoreDecimalPlacesThan" should {
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

  val totalDecimalLength = 10
  val decimalPlaces = 5

  "FormFieldValidator validate decimal" should {
    "return false" when {
      "input contains letters" in {
        val input = "123.asd213"

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input contains two or more dots" in {
        val firstInput = "123.123.123"
        val secondInput = "1243.1423.121233.135423.124"

        validateDecimal(totalDecimalLength)(decimalPlaces)(firstInput) must be(false)
        validateDecimal(totalDecimalLength)(decimalPlaces)(secondInput) must be(false)
      }

      "input without decimal places, but longer than allowed" in {
        val input = "12345678901"

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input longer than length" in {
        val input = "123456.12345"

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input with more decimal places than allowed" in {
        val input = "12.123456"

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }
    }

    "return true" when {
      "input is empty" in {
        validateDecimal(totalDecimalLength)(decimalPlaces)(emptyString) must be(true)
      }

      "input without decimal places" in {
        val input = "123456"

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(true)
      }

      "input with dot and without decimal places" in {
        val input = "123456."

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(true)
      }

      "input with whole decimal number" in {
        val input = "1234.1234"

        validateDecimal(totalDecimalLength)(decimalPlaces)(input) must be(true)
      }
    }
  }

  "FormFieldValidator containsDuplicates" should {

    "return false" when {
      "input contains no value" in {
        val input = Seq.empty
        containsDuplicates(input) must be(false)
      }

      "input contains single value" in {
        val input = Seq("value")
        containsDuplicates(input) must be(false)
      }

      "input contains only unique values" in {
        val input = Seq("value_1", "value_2", "value_3")
        containsDuplicates(input) must be(false)
      }
    }

    "return true" when {
      "input contains 2 identical values" in {
        val input = Seq("value", "value")
        containsDuplicates(input) must be(true)
      }

      "input contains 3 identical values" in {
        val input = Seq("value", "value", "value")
        containsDuplicates(input) must be(true)
      }

      "input contains 2 identical values mixed with uniques" in {
        val input = Seq("value", "value_1", "value_2", "value", "value_3")
        containsDuplicates(input) must be(true)
      }

      "input contains 2 pairs of identical values" in {
        val input = Seq("value_1", "value_2", "value_2", "value_1")
        containsDuplicates(input) must be(true)
      }
    }
  }

  "FormFieldValidator containsUniques" should {

    "return false" when {
      "input contains 2 identical values" in {
        val input = Seq("value", "value")
        areAllElementsUnique(input) must be(false)
      }

      "input contains 3 identical values" in {
        val input = Seq("value", "value", "value")
        areAllElementsUnique(input) must be(false)
      }

      "input contains 2 identical values mixed with uniques" in {
        val input = Seq("value", "value_1", "value_2", "value", "value_3")
        areAllElementsUnique(input) must be(false)
      }

      "input contains 2 pairs of identical values" in {
        val input = Seq("value_1", "value_2", "value_2", "value_1")
        areAllElementsUnique(input) must be(false)
      }
    }

    "return true" when {
      "input contains no value" in {
        val input = Seq.empty
        areAllElementsUnique(input) must be(true)
      }

      "input contains single value" in {
        val input = Seq("value")
        areAllElementsUnique(input) must be(true)
      }

      "input contains only unique values" in {
        val input = Seq("value_1", "value_2", "value_3")
        areAllElementsUnique(input) must be(true)
      }
    }
  }

}
