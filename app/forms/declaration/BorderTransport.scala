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
import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.declaration.TransportCodes._
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.Countries.allCountries
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class BorderTransport(
  meansOfTransportCrossingTheBorderNationality: Option[String],
  meansOfTransportCrossingTheBorderType: String,
  meansOfTransportCrossingTheBorderIDNumber: String
)

object BorderTransport extends DeclarationPage {

  implicit val formats: OFormat[BorderTransport] = Json.format[BorderTransport]

  val formMapping: Mapping[BorderTransport] = mapping(
    "meansOfTransportCrossingTheBorderNationality" -> optional(
      text()
        .verifying(
          "declaration.transportInformation.meansOfTransport.crossingTheBorder.nationality.error.incorrect",
          isContainedIn(allCountries.map(_.countryName))
        )
    ),
    "meansOfTransportCrossingTheBorderType" -> requiredRadio("declaration.transportInformation.meansOfTransport.crossingTheBorder.error.empty")
      .verifying("declaration.transportInformation.meansOfTransport.crossingTheBorder.error.incorrect", isContainedIn(allowedMeansOfTransportTypeCodes)),
    "meansOfTransportCrossingTheBorderIDNumber" -> text()
      .verifying("declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.error.empty", nonEmpty)
      .verifying("declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.error.length", noLongerThan(35))
      .verifying("declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.error.invalid", isAlphanumericWithAllowedSpecialCharacters)
  )(BorderTransport.apply)(BorderTransport.unapply)

  def form(): Form[BorderTransport] = Form(BorderTransport.formMapping)
}
