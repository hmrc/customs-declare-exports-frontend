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
import play.api.data.Forms.mapping
import play.api.data.Forms.{boolean, optional, text}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.forms.FieldValidator.{isAlphanumeric, isContainedIn, isEmpty, noLongerThan}
import forms.declaration.TransportCodes._
import play.api.data.Form
import utils.validators.forms.FieldValidator._

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

  val formMapping = mapping(
    "meansOfTransportCrossingTheBorderNationality" -> optional(
      text()
        .verifying(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.error.incorrect",
          isContainedIn(allCountries.map(_.countryName))
        )
    ),
    "container" -> boolean,
    "meansOfTransportCrossingTheBorderType" -> optional(
      text()
        .verifying(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.incorrect",
          isEmpty or isContainedIn(allowedMeansOfTransportTypeCodes)
        )
    ).verifying("supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.empty", _.isDefined)
      .transform[String](
        value => value.getOrElse(""),
        meansOfTransportCrossingTheBorderType => Some(meansOfTransportCrossingTheBorderType)
      ),
    "meansOfTransportCrossingTheBorderIDNumber" -> optional(
      text()
        .verifying("supplementary.meansOfTransportCrossingTheBorderIDNumber.error.length", noLongerThan(35))
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.specialCharacters", isAlphanumeric)
    ),
    "paymentMethod" -> optional(
      text()
        .verifying("standard.transportDetails.paymentMethod.error", isContainedIn(paymentMethods.keys))
    )
  )(TransportDetails.apply)(TransportDetails.unapply)

  def form(): Form[TransportDetails] = Form(TransportDetails.formMapping)

}
