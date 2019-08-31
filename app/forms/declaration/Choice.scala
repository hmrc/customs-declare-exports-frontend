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

package forms.declaration
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isContainedIn, isEmpty, nonEmpty, _}

case class Choice(addItem: String)

object Choice {

  implicit val format = Json.format[Choice]

  object ChoiceAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import ChoiceAnswers._

  val allowedValues: Seq[String] = Seq(yes, no)

  val mapping = Forms.mapping(
    "choice" -> text()
      .verifying("error.required", nonEmpty)
      .verifying("error.required", isEmpty or isContainedIn(allowedValues))
  )(Choice.apply)(Choice.unapply)

  val formId = "AddItem"

  def form(): Form[Choice] = Form(mapping)
}
