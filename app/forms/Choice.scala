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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class Choice(value: String)

object Choice {
  implicit val format = Json.format[Choice]

  val choiceId = "Choice"

  import AllowedChoiceValues._
  private val correctChoices = Set(SupplementaryDec, StandardDec, CancelDec, Submissions)

  val choiceMapping: Mapping[Choice] = Forms.single(
    "choice" -> optional(
      text()
        .verifying("choicePage.input.error.incorrectValue", isContainedIn(correctChoices))
    ).verifying("choicePage.input.error.empty", _.isDefined)
      .transform[Choice](value => Choice(value.getOrElse("")), choice => Some(choice.value))
  )

  def form(): Form[Choice] = Form(choiceMapping)

  object AllowedChoiceValues {
    val SupplementaryDec = "SMP"
    val StandardDec = "STD"
    val CancelDec = "CAN"
    val Submissions = "SUB"
  }
}
