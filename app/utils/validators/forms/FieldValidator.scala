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

import java.util.regex.Pattern

import scala.util.{Success, Try}

object FieldValidator {

  implicit class PredicateOpsForFunctions[A](first: A => Boolean) {
    def and(second: A => Boolean): A => Boolean = (arg: A) => first(arg) && second(arg)

    def and(second: Boolean): A => Boolean = (arg: A) => first(arg) && second

    def or(second: A => Boolean): A => Boolean = (arg: A) => first(arg) || second(arg)

    def or(second: Boolean): A => Boolean = (arg: A) => first(arg) || second
  }

  implicit class PredicateOpsForBooleans[A](first: Boolean) {
    def and(second: A => Boolean): A => Boolean = (arg: A) => first && second(arg)

    def and(second: Boolean): Boolean = first && second

    def or(second: A => Boolean): A => Boolean = (arg: A) => first || second(arg)

    def or(second: Boolean): Boolean = first || second
  }

  private def noMoreDecimalPlacesThanRegexValue(decimalPlaces: Int): Pattern =
    Pattern.compile(s"^([0-9]*)([\\.]?[0-9]{0,$decimalPlaces})$$")

  private val allowedSpecialChars = Set(',', '.', '-', '\'', '/', ' ')
  private val allowedNewLineChars = Set('\r', '\n')
  private val allowedHyphenChar = Set('-')

  val isPresent: Option[_] => Boolean = _.nonEmpty

  val isMissing: Option[_] => Boolean = _.isEmpty

  val isEmpty: String => Boolean = (input: String) => input.isEmpty

  val nonEmpty: String => Boolean = (input: String) => input.trim.nonEmpty

  val isTrue: Boolean => Boolean = (input: Boolean) => input

  val noLongerThan: Int => String => Boolean = (length: Int) => (input: String) => input.length <= length

  val noLongerThanAfterTrim: Int => String => Boolean = (length: Int) => (input: String) => input.trim.length <= length

  val noShorterThan: Int => String => Boolean = (length: Int) => (input: String) => input.length >= length

  val hasSpecificLength: Int => String => Boolean = (length: Int) => (input: String) => input.length == length

  val hasSpecificLengths: Seq[Int] => String => Boolean = (lengths: Seq[Int]) => (input: String) => lengths.contains(input.length)

  val lengthInRange: Int => Int => String => Boolean = (min: Int) => (max: Int) => (input: String) => input.length >= min && input.length <= max

  val isInRange: (Int, Int) => Int => Boolean = (min: Int, max: Int) => (input: Int) => input >= min && input <= max

  val isNumeric: String => Boolean = (input: String) => input.forall(_.isDigit)

  val isAllCapitalLetter: String => Boolean = (input: String) => input.forall(_.isUpper)

  val isAlphabetic: String => Boolean = (input: String) => input.forall(_.isLetter)

  val isAlphanumeric: String => Boolean = (input: String) => input.trim.forall(_.isLetterOrDigit)

  val isNotAlphanumericWithSpace: String => Boolean = (input: String) => !isAlphanumericWithSpace(input)

  val isNotAlphanumeric: String => Boolean = (input: String) => !isAlphanumeric(input)

  val isAlphanumericWithSpace: String => Boolean = (input: String) => isAlphanumericWithSpecialCharacters(Set(' '))(input)

  val isAlphanumericWithSpaceAndHyphen: String => Boolean = (input: String) => isAlphanumericWithSpecialCharacters(Set(' ', '-'))(input)

  val isValidAddressField: String => Boolean = (input: String) =>
    isAlphanumericWithSpecialCharacters(Set(' ', '\'', ',', '-', '&', '.', ',', '/', '(', ')'))(input)

  val isValidFieldForAddresses: String => Boolean = (input: String) => isAlphanumericWithSpecialCharacters(Set(' ', '\'', ',', '-', '&'))(input)

  val isValidMucr: String => Boolean = (input: String) => isAlphanumericWithSpecialCharacters(Set('/', '-', ':'))(input)

  val isAlphanumericWithSpecialCharacters: Set[Char] => String => Boolean = (allowedChars: Set[Char]) =>
    (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedChars)

  val startsWith: Set[Char] => String => Boolean = (allowedChars: Set[Char]) =>
    (input: String) => input.headOption.exists(firstChar => allowedChars.contains(firstChar))

  val startsWithIgnoreCase: Set[Char] => String => Boolean = (allowedChars: Set[Char]) =>
    (input: String) => input.headOption.exists(firstChar => allowedChars.contains(firstChar.toLower) || allowedChars.contains(firstChar.toUpper))

  val isAlphanumericWithAllowedSpecialCharacters: String => Boolean = (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedSpecialChars)

  val isAlphanumericWithAllowedSpecialCharactersAndNewLine: String => Boolean = (input: String) =>
    input
      .filter(!_.isLetterOrDigit)
      .forall(allowedSpecialChars ++ allowedNewLineChars)

  val isAlphanumericWithAllowedHyphenCharacter: String => Boolean = (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedHyphenChar)

  val startsWithCapitalLetter: String => Boolean = (input: String) => input.headOption.exists(_.isUpper)

  def isContainedIn[T](iterable: Iterable[T]): T => Boolean =
    (input: T) => iterable.exists(_ == input)

  val containsNotOnlyZeros: String => Boolean = (input: String) => {
    val strippedOfPeriodsAndCommas = input.replaceAll("\\.*\\,*", "")
    strippedOfPeriodsAndCommas.isEmpty || strippedOfPeriodsAndCommas.exists(char => char != '0')
  }

  val containsOnlyZeros: String => Boolean = (input: String) => !containsNotOnlyZeros(input)

  val notContainsConsecutiveSpaces: String => Boolean = (input: String) => !input.contains("  ")

  val isTailNumeric: String => Boolean = (input: String) =>
    Try(input.tail) match {
      case Success(value) => isNumeric(value)
      case _              => false
    }

  def isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces: Int): String => Boolean = {
    val pattern = noMoreDecimalPlacesThanRegexValue(decimalPlaces)
    input => pattern.matcher(input).matches()
  }

  val validateDecimalGreaterThanZero: Int => Int => String => Boolean = (totalLength: Int) =>
    (decimalPlaces: Int) =>
      (input: String) =>
        try
          BigDecimal(input) > 0 &&
            BigDecimal(input).scale <= decimalPlaces &&
            input.length <= totalLength
        catch {
          case _: java.lang.NumberFormatException => false
        }

  val containsDuplicates: Iterable[_] => Boolean = (input: Iterable[_]) => input.toSet.size != input.size

  val areAllElementsUnique: Iterable[_] => Boolean = (input: Iterable[_]) => input.toSet.size == input.size

  val ofPattern: String => String => Boolean = (pattern: String) => {
    val compiledPattern = Pattern.compile(pattern)
    input => compiledPattern.matcher(input).matches()
  }

  private val namePattern = Pattern.compile("[\\p{IsLatin} ,.'-]+")
  val isValidName: String => Boolean = (name: String) => namePattern.matcher(name).matches()

  private val emailPattern = Pattern.compile("""^\S+@\S+$""")
  val isValidEmail: String => Boolean = (name: String) => emailPattern.matcher(name).matches()

  private val eoriDigitsAmountMin = 10
  private val eoriDigitsAmountMax = 15
  val isValidEori: String => Boolean = (eori: String) => {
    val (countryCode, number) = eori.splitAt(2)
    countryCode.forall(_.isLetter) && number.forall(_.isDigit) && isInRange(eoriDigitsAmountMin, eoriDigitsAmountMax)(number.length)
  }

  private val officeOfExitPattern = Pattern.compile("^[a-zA-Z]{2}[0-9]{6}$")
  val isValidOfficeOfExit: String => Boolean = (name: String) => officeOfExitPattern.matcher(name).matches()

  private val ducrPattern = Pattern.compile("[0-9]{1}[A-Za-z]{2}[0-9]{12}[-]{1}[/()A-Za-z0-9]{1,19}")
  val isValidDucr: String => Boolean = ducrPattern.matcher(_).matches()

  private val traderReferencePattern = Pattern.compile("[/()A-Z0-9]{1,19}")
  val isValidTraderReference: String => Boolean = traderReferencePattern.matcher(_).matches()
}
