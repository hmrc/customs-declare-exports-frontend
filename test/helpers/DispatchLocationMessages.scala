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

package helpers

trait DispatchLocationMessages {

  val dispatchLocation: String = "supplementary.dispatchLocation"

  val header: String = dispatchLocation + ".header"
  val hint: String = dispatchLocation + ".header.hint"
  val outsideEu: String = dispatchLocation + ".inputText.outsideEU"
  val specialFiscalTerritory: String = dispatchLocation + ".inputText.specialFiscalTerritory"
  val errorMessageEmpty: String = dispatchLocation + ".inputText.error.empty"
  val errorMessageIncorrect :String = dispatchLocation + ".inputText.error.incorrect"
}
