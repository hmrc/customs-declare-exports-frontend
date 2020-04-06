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
import forms.common.Eori
import models.DeclarationType.DeclarationType
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json

case class DeclarantDetails(details: EntityDetails)

object DeclarantDetails extends DeclarationPage {
  implicit val format = Json.format[DeclarantDetails]

  val id = "DeclarantDetails"

  val defaultEntityMapping: Mapping[EntityDetails] = Forms
    .mapping("eori" -> Eori.mapping("declaration.declarant"))(eori => EntityDetails(Some(eori), None))(entityDetails => entityDetails.eori)

  val defaultMapping = Forms.mapping("details" -> defaultEntityMapping)(DeclarantDetails.apply)(DeclarantDetails.unapply)

  def form(declarationType: DeclarationType): Form[DeclarantDetails] = Form(defaultMapping)
}
