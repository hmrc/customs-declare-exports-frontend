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
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class OfficeOfExitOutsideUk(officeId: String)

object OfficeOfExitOutsideUk extends DeclarationPage {
  implicit val format = Json.format[OfficeOfExitOutsideUk]

  val formId = "OfficeOfExitOutsideUk"

  val mapping: Mapping[OfficeOfExitOutsideUk] = Forms.mapping(
    "officeId" -> text()
      .verifying("declaration.officeOfExitOutsideUk.empty", nonEmpty)
      .verifying("declaration.officeOfExitOutsideUk.format", isEmpty or isValidOfficeOfExit)
  )(OfficeOfExitOutsideUk.apply)(OfficeOfExitOutsideUk.unapply)

  def form(): Form[OfficeOfExitOutsideUk] = Form(mapping)
}
