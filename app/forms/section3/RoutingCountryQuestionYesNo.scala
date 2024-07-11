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

package forms.section3

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import forms.common.{Countries, Country, YesNoAnswer}
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.data.{Form, Forms}
import utils.validators.forms.FieldValidator.isContainedIn

object RoutingCountryQuestionYesNo {

  case object RoutingCountryQuestionPage extends DeclarationPage

  case object RemoveCountryPage extends DeclarationPage

  case object ChangeCountryPage extends DeclarationPage

  def formFirst(cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] =
    form("declaration.routingCountryQuestion.empty", cachedCountries)

  def formAdd(cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] =
    form("declaration.routingCountryQuestion.add.empty", cachedCountries)

  def formRemove(cachedCountries: Seq[Country] = Seq.empty): Form[Boolean] =
    form("declaration.routingCountryQuestion.remove.empty", cachedCountries)

  private def form(errorMessage: String, cachedCountries: Seq[Country]): Form[Boolean] = Form(
    Forms.mapping(
      "answer" -> requiredRadio(errorMessage)
        .verifying("declaration.routingCountryQuestion.empty", isContainedIn(YesNoAnswer.allowedValues))
        .verifying(s"declaration.routingCountries.limit", answer => answer == YesNoAnswers.no || cachedCountries.length < Countries.limit)
    )(answer => answer == YesNoAnswers.yes)(answer => if (answer) Some(YesNoAnswers.yes) else Some(YesNoAnswers.no))
  )
}
