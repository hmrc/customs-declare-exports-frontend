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

package helpers.views.declaration

trait OfficeOfExitMessages {

  val officeOfExit: String = "declaration.officeOfExit"

  val title: String = officeOfExit + ".title"
  val hint: String = officeOfExit + ".hint"
  val officeOfExitEmpty: String = officeOfExit + ".empty"
  val officeOfExitLength: String = officeOfExit + ".length"
  val officeOfExitSpecialCharacters: String = officeOfExit + ".specialCharacters"

  val officeOfExitStandard: String = "standard.officeOfExit"

  val presentationOffice: String = officeOfExitStandard + ".presentationOffice"
  val presentationOfficeHint: String = officeOfExitStandard + ".presentationOffice.hint"
  val presentationOfficeLength: String = officeOfExitStandard + ".presentationOffice.length"
  val presentationOfficeSpecialCharacters: String = officeOfExitStandard + ".presentationOffice.specialCharacters"
  val circumstancesCode: String = officeOfExitStandard + ".circumstancesCode"
  val circumstancesCodeEmpty: String = officeOfExitStandard + ".circumstancesCode.empty"
  val circumstancesCodeError: String = officeOfExitStandard + ".circumstancesCode.error"
}
