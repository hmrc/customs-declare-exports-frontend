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

case class RoutingQuestion(hasRoutingCountries: String) {

  def toBoolean: Boolean = RoutingQuestion.answerToBoolean(this)
}

object RoutingQuestion {

  case object RoutingQuestionPage extends DeclarationPage

  implicit val format: OFormat[RoutingQuestion] = Json.format[RoutingQuestion]

  val yes = "Yes"
  val no = "No"

  val answerFromBoolean: Boolean => RoutingQuestion =
    (boolean: Boolean) => if (boolean) RoutingQuestion(yes) else RoutingQuestion(no)

  val answerToBoolean: RoutingQuestion => Boolean =
    (routingCountry: RoutingQuestion) => routingCountry.hasRoutingCountries == yes

  val allowedValues: Seq[String] = Seq(yes, no)

  def form(): Form[RoutingQuestion] = Form(
    Forms.mapping(
      "hasRoutingCountries" -> requiredRadio("declaration.routingQuestion.empty")
        .verifying("declaration.routingQuestion.error", isContainedIn(allowedValues))
    )(RoutingQuestion.apply)(RoutingQuestion.unapply)
  )
}
