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

package helpers.views.components

trait DateMessages {

  val date: String = "dateTime.date"

  val dateFormatError: String = date + ".error.format"
  val dateOutOfRangeError: String = date + ".error.outOfRange"
  val dayEmptyFieldError: String = date + ".day.error.empty"
  val monthEmptyFieldError: String = date + ".month.error.empty"
  val yearEmptyFieldError: String = date + ".year.error.empty"
}
