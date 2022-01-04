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

package utils.validators.forms

import base.UnitSpec
import utils.validators.forms.FieldValidator._

class FieldValidatorSpec extends UnitSpec {

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

  "FieldValidator isEmpty" should {
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

  "FieldValidator noLongerThan" should {
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

  "FieldValidator noShorterThan" should {
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

  "FieldValidator hasSpecificLength" should {
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

  "FieldValidator isInRange" should {

    "return false" when {

      "provided with Int just below lower limit" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = lowerLimit - 1

        isInRange(lowerLimit, upperLimit)(input) must be(false)
      }

      "provided with Int just above upper limit" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = upperLimit + 1

        isInRange(lowerLimit, upperLimit)(input) must be(false)
      }

      "provided with Int smaller than lower limit" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = -3

        isInRange(lowerLimit, upperLimit)(input) must be(false)
      }

      "provided with Int bigger than upper limit" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = 1245

        isInRange(lowerLimit, upperLimit)(input) must be(false)
      }
    }

    "return true" when {

      "provided with Int at the lower limit" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = lowerLimit

        isInRange(lowerLimit, upperLimit)(input) must be(true)
      }

      "provided with Int at the upper limit" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = upperLimit

        isInRange(lowerLimit, upperLimit)(input) must be(true)
      }

      "provided with Int in the middle of allowed range" in {

        val lowerLimit = 4
        val upperLimit = 13
        val input = 10

        isInRange(lowerLimit, upperLimit)(input) must be(true)
      }
    }
  }

  "FieldValidator isNumeric" should {
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

  "FieldValidator isAllCapitalLetter" should {
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

  "FieldValidator on isAlphabetic" should {
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

  "FieldValidator isAlphanumeric" should {
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

  "FieldValidator isAlphanumericWithSpecialCharacters" should {
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

  "FieldValidator isAlphanumericWithAllowedSpecialCharacters" should {
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

  "FieldValidator startsWithCapitalLetter" should {
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

  "FieldValidator isContainedIn" should {
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

  "FieldValidator containsNotOnlyZeros" should {
    "return false" when {
      "string contains only zero" in {
        containsNotOnlyZeros("0") must be(false)
      }

      "string contains only several zeros" in {
        containsNotOnlyZeros("00") must be(false)
        containsNotOnlyZeros("000") must be(false)
      }

      "string contains several zeros separated by periods and commas" in {
        containsNotOnlyZeros("0.0") must be(false)
        containsNotOnlyZeros("000,000") must be(false)
        containsNotOnlyZeros("000,000.00") must be(false)
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

  "FieldValidator isTailNumeric" should {
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

  "FieldValidator hasNoMoreDecimalPlacesThan" should {
    "return false" when {
      "input contains non-numeric characters" in {
        val input = "123A"
        val decimalPlaces = 1
        isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces)(input) must be(false)
      }

      "input contains decimal separator other than '.'" in {
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

  "FieldValidator validateDecimalGreaterThanZero" should {

    val totalDecimalLength = 10
    val decimalPlaces = 5

    "return false" when {

      "input is empty" in {
        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(emptyString) must be(false)
      }

      "input contains letters" in {
        val input = "123.asd213"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input contains two or more dots" in {
        val firstInput = "123.123.123"
        val secondInput = "1243.1423.121233.135423.124"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(firstInput) must be(false)
        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(secondInput) must be(false)
      }

      "input contains no decimal places, but longer than allowed" in {
        val input = "12345678901"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input is longer than length" in {
        val input = "123456.12345"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input contains more decimal places than allowed" in {
        val input = "12.123456"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }

      "input is negative" in {
        val input = "-1234.1234"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(false)
      }
    }

    "return true" when {

      "input without decimal places" in {
        val input = "123456"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(true)
      }

      "input with dot and without decimal places" in {
        val input = "123456."

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(true)
      }

      "input with whole decimal number" in {
        val input = "1234.1234"

        validateDecimalGreaterThanZero(totalDecimalLength)(decimalPlaces)(input) must be(true)
      }
    }
  }

  "FieldValidator containsDuplicates" should {

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

  "FieldValidator areAllElementsUnique" should {

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

  "FieldValidator isValidName" should {

    "return false" when {

      "name is incorrect" in {

        isValidName("Weasfd'); DROP TABLE Students;--") must be(false)
        isValidName("Kasdas D@t") must be(false)
      }
    }

    "return true" when {

      "name is correct" in {

        isValidName("Masdjxz d'Arras") must be(true)
        isValidName("FAsdas Lasjs ur, Jr.") must be(true)
        isValidName("Hasewr Sausage-Vsgh") must be(true)
        isValidName("Okjh Wert") must be(true)
        isValidName("Gds VXz Werty") must be(true)
        isValidName("Vgasjs O'asc") must be(true)
        isValidName("Þósauy Wergas") must be(true)
        isValidName("Awerfds Ní Bashjadnáin") must be(true)
      }
    }
  }

  "FieldValidator isAlphanumericWithSpace" should {

    "return false" when {

      "value is incorrect" in {

        isAlphanumericWithSpace("@test") must be(false)
      }
    }

    "return true" when {

      "value is correct" in {

        isAlphanumericWithSpace("abc 123") must be(true)
        isAlphanumericWithSpace(" ") must be(true)
      }
    }
  }

  "FieldValidator isAlphanumericWithSpaceAndHyphen" should {

    "return false" when {

      "value is incorrect" in {

        isAlphanumericWithSpaceAndHyphen("@test") must be(false)
      }
    }

    "return true" when {

      "value is correct" in {

        isAlphanumericWithSpaceAndHyphen("abc 123") must be(true)
        isAlphanumericWithSpaceAndHyphen("abc-123") must be(true)
        isAlphanumericWithSpaceAndHyphen(" ") must be(true)
        isAlphanumericWithSpaceAndHyphen("-") must be(true)
      }
    }
  }

  "FieldValidator isValidEmail" should {

    "return false" when {

      "email is incorrect" in {

        isValidEmail("@test") must be(false)
        isValidEmail("test@") must be(false)
      }
    }

    "return true" when {

      "email is correct" in {

        isValidEmail("test@test") must be(true)
        isValidEmail("test@test.com") must be(true)
        isValidEmail("#!$%&'*+-/=?^_{}|~@domain.org") must be(true)
      }
    }
  }

  "FieldValidator isTrue" should {

    "return false" when {
      "not true" in {
        isTrue(false) must be(false)
      }
    }

    "return true" when {
      "true" in {
        isTrue(true) must be(true)
      }
    }
  }

  "FieldValidator startsWith" should {

    "return false" when {
      "input does not start with allowed character" in {
        startsWith(Set('A', 'B', 'C'))("The Test") must be(false)
      }

      "input empty" in {
        startsWith(Set('A', 'B', 'C'))("") must be(false)
      }
    }

    "return true" when {
      "input starts with allowed character" in {
        startsWith(Set('A', 'B', 'C'))("A Test") must be(true)
        startsWith(Set('A', 'B', 'C'))("B Test") must be(true)
        startsWith(Set('A', 'B', 'C'))("C Test") must be(true)
      }
    }
  }

  "FieldValidator isValidEori" should {

    "return false" when {

      "provided with Eori" which {

        "starts with a digit" in {

          val inputEori = "7GB1234567890123"

          isValidEori(inputEori) mustBe false
        }

        "contains just a single letter" in {

          val inputEori = "G1234567890123"

          isValidEori(inputEori) mustBe false
        }

        "contains 3 leading letters" in {

          val inputEori = "GBW1234567890123"

          isValidEori(inputEori) mustBe false
        }

        "contains 3rd letter between digits" in {

          val inputEori = "GB12345678W90123"

          isValidEori(inputEori) mustBe false
        }

        "contains less than 10 digits" in {

          val inputEori = "GB123456789"

          isValidEori(inputEori) mustBe false
        }

        "contains more than 15 digits" in {

          val inputEori = "GB1234567890123456"

          isValidEori(inputEori) mustBe false
        }

        "contains special character instead of letter" in {

          val inputEori = "G%1234567890123"

          isValidEori(inputEori) mustBe false
        }

        "contains special character instead of digit" in {

          val inputEori = "GB1234567890#123"

          isValidEori(inputEori) mustBe false
        }
      }
    }

    "return true" when {

      "provided with correct Eori" which {

        "contains 10 digits" in {

          val inputEori = "GB1234567890"

          isValidEori(inputEori) mustBe true
        }

        "contains 15 digits" in {

          val inputEori = "GB123456789012345"

          isValidEori(inputEori) mustBe true
        }
      }

      "provided with lower case letters" in {

        val inputEori = "gb1234567890123"

        isValidEori(inputEori) mustBe true
      }
    }
  }

  "Field validator isValidDucr" should {

    "return true" when {

      "DUCR is valid" in {
        isValidDucr("9GB123456789012-1") mustBe true
        isValidDucr("0GB123456664559-1234567890123456789") mustBe true
        isValidDucr("9GB123456789012-AB12/(1)") mustBe true
        isValidDucr("9gb123456789012-ab12/(1)") mustBe true
      }
    }

    "return false" when {

      "DUCR is invalid" in {
        withClue("Not starting with single year digit") {
          isValidDucr("GB0123456664559-1234567890123456789") mustBe false
        }

        withClue("Country code not present") {
          isValidDucr("000123456789012-1234567890123456789") mustBe false
          isValidDucr("00B123456789012-1234567890123456789") mustBe false
          isValidDucr("0G0123456789012-1234567890123456789") mustBe false
        }

        withClue("EORI value wrong length") {
          isValidDucr("0GB12345678901-01234567890123456789") mustBe false
          isValidDucr("0GB1234567890123-234567890123456789") mustBe false
        }

        withClue("Invalid character present in EORI") {
          isValidDucr("9GB12A456789012-AB12")
          isValidDucr("91B12-456664559-AB12") mustBe false
          isValidDucr("9GB12$456664559-AB12") mustBe false
          isValidDucr("9GB12ö456664559-AB12") mustBe false
        }

        withClue("Missing trader ref") {
          isValidDucr("9GB123456789012") mustBe false
          isValidDucr("9GB123456789012-") mustBe false
        }

        withClue("Trader ref too long") {
          isValidDucr("0GB123456664559-12345678901234567890") mustBe false
        }

        withClue("Invalid character present in trader ref") {
          isValidDucr("91B123456664559-AB12$") mustBe false
          isValidDucr("9GB123456664559-AB12-") mustBe false
          isValidDucr("9GB123456664559-AB12ö") mustBe false
        }
      }
    }
  }
}
