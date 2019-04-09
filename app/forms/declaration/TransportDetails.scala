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
import utils.validators.forms.FieldValidator.isContainedIn

case class TransportDetails(meansOfTransportCrossingTheBorderNationality: Option[String], container: Boolean)

object TransportDetails {

 val formId = "transportDetails"

  implicit val formats = Json.format[TransportDetails]

  val formMapping = mapping(
    "meansOfTransportCrossingTheBorderNationality" -> optional(
      text()
        .verifying(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.error.incorrect",
          isContainedIn(allCountries.map(_.countryName))
        )
    ),
    "container" -> boolean
  )(TransportDetails.apply)(TransportDetails.unapply)

}
