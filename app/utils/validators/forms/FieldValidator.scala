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
    Pattern.compile(s"^([0-9]*)([\\.]{0,1}[0-9]{0,$decimalPlaces})$$")

  private val allowedSpecialChars = Set(',', '.', '-', '\'', '/', ' ')

  private val allowedHyphenChar = Set('-')

  val isEmpty: String => Boolean = (input: String) => input.isEmpty

  val nonEmpty: String => Boolean = (input: String) => input.trim.nonEmpty

  val noLongerThan: Int => String => Boolean = (length: Int) => (input: String) => input.length <= length

  val noShorterThan: Int => String => Boolean = (length: Int) => (input: String) => input.length >= length

  val hasSpecificLength: Int => String => Boolean = (length: Int) => (input: String) => input.length == length

  val lengthInRange: Int => Int => String => Boolean = (min: Int) =>
    (max: Int) => (input: String) => input.length >= min && input.length <= max

  val isInRange: (Int, Int) => Int => Boolean = (min: Int, max: Int) => (input: Int) => input >= min && input <= max

  val isNumeric: String => Boolean = (input: String) => input.forall(_.isDigit)

  val isAllCapitalLetter: String => Boolean = (input: String) => input.forall(_.isUpper)

  val isAlphabetic: String => Boolean = (input: String) => input.forall(_.isLetter)

  val isAlphanumeric: String => Boolean = (input: String) => input.forall(_.isLetterOrDigit)

  val isAlphanumericWithSpecialCharacters: Set[Char] => String => Boolean = (allowedChars: Set[Char]) =>
    (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedChars)

  val isAlphanumericWithAllowedSpecialCharacters: String => Boolean = (input: String) =>
    input.filter(!_.isLetterOrDigit).forall(allowedSpecialChars)

  val isAlphanumericWithAllowedHyphenCharacter: String => Boolean = (input: String) =>
    input.filter(!_.isLetterOrDigit).forall(allowedHyphenChar)

  val startsWithCapitalLetter: String => Boolean = (input: String) => input.headOption.exists(_.isUpper)

  val isContainedIn: Iterable[String] => String => Boolean =
    (iterable: Iterable[String]) => (input: String) => iterable.exists(_ == input)

  val containsNotOnlyZeros: String => Boolean = (input: String) => input.isEmpty || input.exists(char => char != '0')

  val isTailNumeric: String => Boolean = (input: String) =>
    Try(input.tail) match {
      case Success(value) => isNumeric(value)
      case _              => false
  }

  def isDecimalWithNoMoreDecimalPlacesThan(decimalPlaces: Int): String => Boolean = {
    val pattern = noMoreDecimalPlacesThanRegexValue(decimalPlaces)
    input => pattern.matcher(input).matches()
  }


  val validateDecimal: Int => Int => String => Boolean = (totalLength: Int) =>
    (decimalPlaces: Int) =>
      (input: String) =>
        input.split('.') match {
          case Array(a, b) if isNumeric(a) && isNumeric(b) => b.length <= decimalPlaces && (a + b).length <= totalLength
          case Array(a) if isNumeric(a)                    => a.length <= totalLength
          case _                                           => false
  }

  val containsDuplicates: Iterable[_] => Boolean = (input: Iterable[_]) => input.toSet.size != input.size

  val areAllElementsUnique: Iterable[_] => Boolean = (input: Iterable[_]) => input.toSet.size == input.size

  val ofPattern: String => String => Boolean = (pattern: String) => {
    val compiledPattern = Pattern.compile(pattern)
    input => compiledPattern.matcher(input).matches()
  }

  private val namePattern = Pattern.compile("[\\p{IsLatin} ,.'-]+")

  val isValidName: String => Boolean = (name: String) => namePattern.matcher(name).matches()
}
