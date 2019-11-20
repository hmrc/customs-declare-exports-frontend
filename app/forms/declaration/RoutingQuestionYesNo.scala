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

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class RoutingQuestionYesNo(answer: Boolean) {

  def extractValue(): Option[String] = RoutingQuestionYesNo.unapplyToString(this)
}

object RoutingQuestionYesNo {

  case object RoutingQuestionPage extends DeclarationPage

  case object RemoveCountryPage extends DeclarationPage

  implicit val format: OFormat[RoutingQuestionYesNo] = Json.format[RoutingQuestionYesNo]

  val yes = "Yes"
  val no = "No"

  def apply(answer: String): RoutingQuestionYesNo = RoutingQuestionYesNo(if (answer == yes) true else false)

  def unapplyToString(routingQuestion: RoutingQuestionYesNo): Option[String] = if (routingQuestion.answer) Some(yes) else Some(no)

  val allowedValues: Seq[String] = Seq(yes, no)

  def form(): Form[RoutingQuestionYesNo] = Form(
    Forms.mapping(
      "answer" -> requiredRadio("declaration.routingQuestion.empty")
        .verifying("declaration.routingQuestion.error", isContainedIn(allowedValues))
    )(RoutingQuestionYesNo.apply)(RoutingQuestionYesNo.unapplyToString)
  )
}
