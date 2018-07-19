/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

case class RepresentativesAddress(
  fullName: String,
  buildingAndStreet: String,
  buildingAndStreetSecondPart: Option[String],
  townOrCity: String,
  county: Option[String],
  postcode: String,
  country: String
)

object RepresentativesAddress {
  implicit val format = Json.format[RepresentativesAddress]
}

class RepresentativesAddressFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[RepresentativesAddress] =
    Form(
      mapping(
        "fullName" -> text("nameAndAddress.error.required.fullName"),
        "buildingAndStreet" -> text("nameAndAddress.error.required.buildingAndStreet"),
        "buildingAndStreetSecondPart" -> optional(text()),
        "townOrCity" -> text("nameAndAddress.error.required.townOrCity"),
        "county" -> optional(text()),
        "postcode" -> text("nameAndAddress.error.required.postcode"),
        "country" -> text("nameAndAddress.error.required.country")
      )(RepresentativesAddress.apply)(RepresentativesAddress.unapply)
    )
}
