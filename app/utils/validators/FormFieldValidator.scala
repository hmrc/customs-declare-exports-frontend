/*
 * Copyright 2018 HM Revenue & Customs
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

  private val numericRegexValue = "[0-9]+"
  private val alphabeticRegexValue = "[a-zA-Z]+"
  private val alphanumericRegexValue = "[a-zA-Z0-9]+"

  def noLongerThan(input: String, length: Int): Boolean = input.length <= length

  def isNumeric(input: String): Boolean = input.matches(numericRegexValue)

  def isAlphabetic(input: String): Boolean = input.matches(alphabeticRegexValue)

  def isAlphanumeric(input: String): Boolean = input.matches(alphanumericRegexValue)

}
