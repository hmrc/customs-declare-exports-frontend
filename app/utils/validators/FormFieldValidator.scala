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

object FormFieldValidator {

  implicit class PredicateOps[A](first: A => Boolean) {
    def and(second: A => Boolean): A => Boolean = (arg: A) => first(arg) && second(arg)

    def or(second: A => Boolean): A => Boolean = (arg: A) => first(arg) || second(arg)
  }

  private val numericRegexValue = "[0-9]*"
  private val alphabeticRegexValue = "[a-zA-Z]*"
  private val alphanumericRegexValue = "[a-zA-Z0-9]*"
  private val zeros = "[0]{1,3}"

  val isEmpty: String => Boolean = (input: String) => input.isEmpty

  val noLongerThan: Int => String => Boolean = (length: Int) => (input: String) => input.length <= length

  val noShorterThan: Int => String => Boolean = (length: Int) => (input: String) => input.length >= length

  val isNumeric: String => Boolean = (input: String) => input.matches(numericRegexValue)

  val isAlphabetic: String => Boolean = (input: String) => input.matches(alphabeticRegexValue)

  val isAlphanumeric: String => Boolean = (input: String) => input.matches(alphanumericRegexValue)

  val hasSpecificLength: Int => String => Boolean = (length: Int) => (input: String) => input.length == length

  val forbiddenZero: String => Boolean = (input: String) => !input.matches(zeros)
}
