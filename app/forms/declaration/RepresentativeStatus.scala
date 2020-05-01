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
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class RepresentativeStatus(statusCode: Option[String])

object RepresentativeStatus extends DeclarationPage {
  implicit val format = Json.format[RepresentativeStatus]

  import StatusCodes._
  private val representativeStatusCodeAllowedValues =
    Set(Declarant, DirectRepresentative, IndirectRepresentative)

  val formId = "RepresentativeStatus"

  val optionalMapping = Forms
    .mapping(
      "statusCode" -> optional(text().verifying("declaration.representative-status.error", isContainedIn(representativeStatusCodeAllowedValues)))
    )(RepresentativeStatus.apply)(RepresentativeStatus.unapply)

  val requiredMapping = Forms
    .mapping(
      "statusCode" -> requiredRadio("declaration.representative-status.error")
        .verifying("declaration.representative-status.error", isContainedIn(representativeStatusCodeAllowedValues))
    )(status => RepresentativeStatus(Some(status)))(model => model.statusCode)

  def formOptional(): Form[RepresentativeStatus] = Form(optionalMapping)
  def formRequired(): Form[RepresentativeStatus] = Form(requiredMapping)

  object StatusCodes {
    val Declarant = "1"
    val DirectRepresentative = "2"
    val IndirectRepresentative = "3"
  }

}
