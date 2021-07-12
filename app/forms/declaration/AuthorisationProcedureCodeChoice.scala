/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.MappingHelper.requiredRadio
import models.declaration.AuthorisationProcedureCode
import models.declaration.AuthorisationProcedureCode.{values, _}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class AuthorisationProcedureCodeChoice(code: AuthorisationProcedureCode)

object AuthorisationProcedureCodeChoice extends DeclarationPage {
  implicit val format = Json.format[AuthorisationProcedureCodeChoice]

  val allowedValues: Seq[String] = values.map(_.toString)

  val formFieldName = "authorisationProcedureCodeChoice"

  val mapping: Mapping[AuthorisationProcedureCodeChoice] = Forms.mapping(
    formFieldName -> requiredRadio("declaration.authorisations.procedureCodeChoice.error.empty")
      .verifying("declaration.authorisations.procedureCodeChoice.error.empty", isContainedIn(allowedValues))
      .transform((x: String) => lookupByValue.getOrElse(x, CodeOther), (x: AuthorisationProcedureCode) => x.toString)
  )(AuthorisationProcedureCodeChoice.apply)(AuthorisationProcedureCodeChoice.unapply)

  def form(): Form[AuthorisationProcedureCodeChoice] = Form(mapping)
}
