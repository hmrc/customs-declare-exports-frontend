/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section2.representative

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class RepresentativeStatus(statusCode: Option[String])

object RepresentativeStatus extends DeclarationPage {
  implicit val format: OFormat[RepresentativeStatus] = Json.format[RepresentativeStatus]

  import StatusCodes._
  private val representativeStatusCodeAllowedValues =
    Set(Declarant, DirectRepresentative, IndirectRepresentative)

  val formId = "RepresentativeStatus"

  val mapping = Forms
    .mapping(
      "statusCode" -> requiredRadio("declaration.representative-status.error")
        .verifying("declaration.representative-status.error", isContainedIn(representativeStatusCodeAllowedValues))
    )(status => RepresentativeStatus(Some(status)))(model => model.statusCode)

  def form: Form[RepresentativeStatus] = Form(mapping)

  object StatusCodes {
    val Declarant = "1"
    val DirectRepresentative = "2"
    val IndirectRepresentative = "3"
  }
}
