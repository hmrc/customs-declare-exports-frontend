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

package utils.validators

import scala.util.{Success, Try}

object FormFieldValidator {

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

  private val zerosOnlyRegexValue = "[0]+"
  private val noMoreDecimalPlacesThanRegexValue: Int => String = (decimalPlaces: Int) =>
    s"^([0-9]*)([\\.]{0,1}[0-9]{0,$decimalPlaces})$$"
  private val allowedSpecialChars = Set(',', '.', '-', '\'', '/', ' ')

  val isEmpty: String => Boolean = (input: String) => input.isEmpty

  val nonEmpty: String => Boolean = (input: String) => input.nonEmpty

  val noLongerThan: Int => String => Boolean = (length: Int) => (input: String) => input.length <= length

  val noShorterThan: Int => String => Boolean = (length: Int) => (input: String) => input.length >= length

  val hasSpecificLength: Int => String => Boolean = (length: Int) => (input: String) => input.length == length

  val isNumeric: String => Boolean = (input: String) => input.forall(_.isDigit)

  val isAllCapitalLetter: String => Boolean = (input: String) => input.forall(_.isUpper)

  val isAlphabetic: String => Boolean = (input: String) => input.forall(_.isLetter)

  val isAlphanumeric: String => Boolean = (input: String) => input.forall(_.isLetterOrDigit)

  val isAlphanumericWithSpecialCharacters: Set[Char] => String => Boolean = (allowedChars: Set[Char]) =>
    (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedChars)

  val isAlphanumericWithAllowedSpecialCharacters: String => Boolean = (input: String) =>
    input.filter(!_.isLetterOrDigit).forall(allowedSpecialChars)

  val startsWithCapitalLetter: String => Boolean = (input: String) => input.headOption.exists(_.isUpper)

  val isContainedIn: Iterable[String] => String => Boolean =
    (iterable: Iterable[String]) => (input: String) => iterable.exists(_ == input)

  val containsNotOnlyZeros: String => Boolean = (input: String) => !input.matches(zerosOnlyRegexValue)

  val isTailNumeric: String => Boolean = (input: String) =>
    Try(input.tail) match {
      case Success(value) => isNumeric(value)
      case _              => false
  }

  val isDecimalWithNoMoreDecimalPlacesThan: Int => String => Boolean = (decimalPlaces: Int) =>
    (input: String) => input.matches(noMoreDecimalPlacesThanRegexValue(decimalPlaces))

  val validateDecimal: Int => Int => String => Boolean = (totalLength: Int) =>
    (decimalPlaces: Int) =>
      (input: String) =>
        input.split('.') match {
          case Array(a, b) if isNumeric(a) && isNumeric(b) => b.length <= decimalPlaces && (a + b).length <= totalLength
          case Array(a) if isNumeric(a)                    => a.length <= totalLength
          case _                                           => false
  }
}
