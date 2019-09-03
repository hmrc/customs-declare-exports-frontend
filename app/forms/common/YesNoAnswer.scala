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

package forms.common

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class YesNoAnswer(answer: String)

object YesNoAnswer {

  implicit val format = Json.format[YesNoAnswer]

  object YesNoAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import YesNoAnswers._

  val allowedValues: Seq[String] = Seq(yes, no)

  val mapping: Mapping[YesNoAnswer] = Forms.single(
    "yesNo" -> optional(
      text()
        .verifying("error.yesNo.required", isContainedIn(allowedValues))
    ).verifying("error.yesNo.required", _.isDefined)
      .transform[YesNoAnswer](answer => YesNoAnswer(answer.getOrElse("")), yesNoAnswer => Some(yesNoAnswer.answer))
  )

  val formId = "YesNo"

  def form(): Form[YesNoAnswer] = Form(mapping)
}
