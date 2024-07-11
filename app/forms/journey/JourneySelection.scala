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

package forms.journey

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import models.DeclarationType.{allDeclarationTypesExcluding, STANDARD}
import play.api.data.{Form, Forms, Mapping}
import utils.validators.forms.FieldValidator.isContainedIn

object JourneySelection extends DeclarationPage {

  val NonStandardDeclarationType = "NonStandardDeclarationType"
  val StandardDeclarationType = STANDARD.toString

  val standardOrOtherJourneys = List(StandardDeclarationType, NonStandardDeclarationType)
  val nonStandardJourneys = allDeclarationTypesExcluding(STANDARD).map(_.toString)

  def mapping(acceptedJourneys: Seq[String]): Mapping[String] = Forms.single(
    "type" ->
      requiredRadio("declaration.type.error")
        .verifying("declaration.type.error", isContainedIn(acceptedJourneys))
  )

  def form(acceptedJourneys: Seq[String]): Form[String] = Form(mapping(acceptedJourneys))
}
