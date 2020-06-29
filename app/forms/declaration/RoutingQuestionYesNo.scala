/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.countries.{Countries, Country}
import play.api.data.{Form, Forms}
import utils.validators.forms.FieldValidator.isContainedIn

object RoutingQuestionYesNo {

  case object RoutingQuestionPage extends DeclarationPage

  case object RemoveCountryPage extends DeclarationPage

  case object ChangeCountryPage extends DeclarationPage

  val yes = "Yes"
  val no = "No"

  val allowedValues: Seq[String] = Seq(yes, no)

  def formFirst(cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] = form("declaration.routingQuestion.empty", cachedCountries)
  def formAdd(cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] = form("declaration.routingQuestion.add.empty", cachedCountries)
  def formRemove(cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] = form("declaration.routingQuestion.remove.empty", cachedCountries)

  private def form(errorMessage: String, cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] = Form(
    Forms.mapping(
      "answer" -> requiredRadio(errorMessage)
        .verifying("declaration.routingQuestion.error", isContainedIn(allowedValues))
        .verifying(s"declaration.routingCountries.limit", answer => answer == no || cachedCountries.length < Countries.limit)
    )(answer => if (answer == yes) true else false)(answer => if (answer) Some(yes) else Some(no))
  )
}
