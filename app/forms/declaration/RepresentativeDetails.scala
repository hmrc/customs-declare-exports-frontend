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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class RepresentativeDetails(details: Option[EntityDetails], statusCode: Option[String])

object RepresentativeDetails {
  implicit val format = Json.format[RepresentativeDetails]

  import StatusCodes._
  private val representativeStatusCodeAllowedValues =
    Set(Declarant, DirectRepresentative, IndirectRepresentative)

  val formId = "RepresentativeDetails"

  val mapping = Forms
    .mapping(
      "details" -> optional(EntityDetails.mapping),
      "statusCode" -> optional(
        text().verifying(
          "supplementary.representative.representationType.error.wrongValue",
          isContainedIn(representativeStatusCodeAllowedValues)
        )
      )
    )(RepresentativeDetails.apply)(RepresentativeDetails.unapply)
    .verifying(
      "supplementary.namedEntityDetails.error",
      details => details.details.nonEmpty || details.statusCode.isEmpty
    )
    .verifying(
      "supplementary.representative.representationType.error.empty",
      details => details.details.isEmpty || details.statusCode.nonEmpty
    )

  def form(): Form[RepresentativeDetails] = Form(mapping)

  def adjustErrors(formWithErrors: Form[RepresentativeDetails]): Form[RepresentativeDetails] = {

    val newErrors = formWithErrors.errors.map { error =>
      if (error.message == "supplementary.namedEntityDetails.error") error.copy(key = "details")
      else if (error.message == "supplementary.representative.representationType.error.empty")
        error.copy(key = "statusCode")
      else error
    }

    formWithErrors.copy(errors = newErrors)
  }

  object StatusCodes {
    val Declarant = "1"
    val DirectRepresentative = "2"
    val IndirectRepresentative = "3"
  }

  def isDefined(representativeDetails: RepresentativeDetails): Boolean =
    representativeDetails.details.isDefined && (representativeDetails.details.get.eori.isDefined || representativeDetails.details.get.address.isDefined)

}
