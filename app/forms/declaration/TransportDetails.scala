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
import forms.Mapping.requiredRadio
import forms.declaration.TransportCodes._
import play.api.data.Forms.{boolean, mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class TransportDetails(
  meansOfTransportCrossingTheBorderNationality: Option[String],
  container: Boolean,
  meansOfTransportCrossingTheBorderType: String,
  meansOfTransportCrossingTheBorderIDNumber: Option[String],
  paymentMethod: Option[String] = None
)

object TransportDetails {

  val formId = "TransportDetails"

  implicit val formats = Json.format[TransportDetails]

  val formMapping: Mapping[TransportDetails] = mapping(
    "meansOfTransportCrossingTheBorderNationality" -> optional(
      text()
        .verifying(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.error.incorrect",
          isContainedIn(allCountries.map(_.countryName))
        )
    ),
    "container" -> optional(boolean)
      .verifying("supplementary.transportInfo.container.error.empty", _.isDefined)
      .transform(_.get, (b: Boolean) => Some(b)),
    "meansOfTransportCrossingTheBorderType" -> requiredRadio(
      "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.empty"
    ).verifying(
      "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.incorrect",
      isContainedIn(allowedMeansOfTransportTypeCodes)
    ),
    "meansOfTransportCrossingTheBorderIDNumber" -> optional(
      text()
        .verifying("supplementary.meansOfTransportCrossingTheBorderIDNumber.error.length", noLongerThan(35))
        .verifying(
          "supplementary.transportInfo.meansOfTransport.idNumber.invalid",
          isAlphanumericWithAllowedSpecialCharacters
        )
    ),
    "paymentMethod" -> optional(
      text()
        .verifying("standard.transportDetails.paymentMethod.error", isContainedIn(paymentMethods.keys))
    )
  )(TransportDetails.apply)(TransportDetails.unapply)

  def form(): Form[TransportDetails] = Form(TransportDetails.formMapping)
}
