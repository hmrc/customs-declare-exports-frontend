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

package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Choice(choice: String)

object Choice {
  implicit val format = Json.format[Choice]

  import AllowedChoiceValues._
  private val correctChoice = Set(SupplementaryDec, StandardDec, Arrival, Departure, CancelDec)

  val choiceMapping =
    mapping("choice" -> text().verifying("Incorrect value", correctChoice.contains(_)))(Choice.apply)(Choice.unapply)

  val choiceId = "Choice"

  def form(): Form[Choice] = Form(choiceMapping)

  object AllowedChoiceValues {
    val SupplementaryDec = "SMP"
    val StandardDec = "STD"
    val Arrival = "EAL"
    val Departure = "EDL"
    val CancelDec = "CAN"
  }
}
