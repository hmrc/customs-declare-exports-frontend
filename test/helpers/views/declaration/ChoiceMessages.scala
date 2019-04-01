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

trait ChoiceMessages {

  val movementChoice: String = "movement.choice"

  // TODO: description is used as title
  val title: String = movementChoice + ".description"

  val supplementaryDec: String = "declaration.choice.SMP"
  val standardDec: String = "declaration.choice.STD"
  val arrivalDec: String = movementChoice + ".EAL"
  val departureDec: String = movementChoice + ".EDL"
  val cancelDec: String = "declaration.choice.CAN"
  val recentDec: String = "declaration.choice.SUB"
  val choiceEmpty: String = "choicePage.input.error.empty"
  val choiceError: String = "choicePage.input.error.incorrectValue"
}
