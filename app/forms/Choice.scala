/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.QueryStringBindable
import utils.validators.forms.FieldValidator.isContainedIn

case class Choice(value: String)

object Choice {
  implicit val format: OFormat[Choice] = Json.format[Choice]

  val choiceId = "Choice"

  import AllowedChoiceValues._
  private val correctChoices = Set(CreateDec, Movements, ContinueDec, CancelDec, Dashboard, Inbox)

  val choiceMapping: Mapping[Choice] = Forms.single(
    "value" -> optional(
      text()
        .verifying("choicePage.input.error.incorrectValue", isContainedIn(correctChoices))
    ).verifying("choicePage.input.error.empty", _.isDefined)
      .transform[Choice](choice => Choice(choice.getOrElse("")), choice => Some(choice.value))
  )

  def form: Form[Choice] = Form(choiceMapping)

  object AllowedChoiceValues {
    val CreateDec = "CRT"
    val Movements = "MVT"
    val ContinueDec = "CON"
    val CancelDec = "CAN"
    val Dashboard = "SUB"
    val Inbox = "MSG"
  }

  implicit val queryStringBindable: QueryStringBindable[Choice] = new QueryStringBindable[Choice] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Choice]] =
      QueryStringBindable.bindableString.bind(key, params).map { case Right(choice) =>
        correctChoices
          .find(_ == choice)
          .map(Choice.apply)
          .map(choice => Right(choice))
          .getOrElse(Left("Unrecognized option"))
      }
    override def unbind(key: String, value: Choice): String = s"$key=${value.value}"
  }
}
