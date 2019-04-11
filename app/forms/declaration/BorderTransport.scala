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

import play.api.data.Forms.{mapping, optional, text}
import utils.validators.forms.FieldValidator.{isAlphanumeric, isContainedIn, isEmpty, noLongerThan}
import TransportCodes._
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class BorderTransport(
  borderModeOfTransportCode: String,
  meansOfTransportOnDepartureType: String,
  meansOfTransportOnDepartureIDNumber: Option[String]
)
object BorderTransport {
  val formId = "BorderTransport"

  implicit val formats = Json.format[BorderTransport]

  val formMapping = mapping(
    "borderModeOfTransportCode" -> optional(
      text()
        .verifying(
          "supplementary.transportInfo.borderTransportMode.error.incorrect",
          isEmpty or isContainedIn(allowedModeOfTransportCodes)
        )
    ).verifying("supplementary.transportInfo.borderTransportMode.error.empty", _.isDefined)
      .transform[String](
        optValue => optValue.getOrElse(""),
        borderModeOfTransportCode => Some(borderModeOfTransportCode)
      ),
    "meansOfTransportOnDepartureType" -> optional(
      text()
        .verifying(
          "supplementary.transportInfo.meansOfTransport.departure.error.incorrect",
          isEmpty or isContainedIn(allowedMeansOfTransportTypeCodes)
        )
    ).verifying("supplementary.transportInfo.meansOfTransport.departure.error.empty", _.isDefined)
      .transform[String](
        optValue => optValue.getOrElse(""),
        meansOfTransportOnDepartureType => Some(meansOfTransportOnDepartureType)
      ),
    "meansOfTransportOnDepartureIDNumber" -> optional(
      text()
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.length", noLongerThan(27))
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.specialCharacters", isAlphanumeric)
    )
  )(BorderTransport.apply)(BorderTransport.unapply)
}

object TransportCodes {

  val Maritime = "1"
  val Rail = "2"
  val Road = "3"
  val Air = "4"
  val PostalConsignment = "5"
  val FixedTransportInstallations = "7"
  val InlandWaterway = "8"
  val Unknown = "9"

  val IMOShipIDNumber = "10"
  val NameOfVessel = "11"
  val WagonNumber = "20"
  val VehicleRegistrationNumber = "30"
  val IATAFlightNumber = "40"
  val AircraftRegistrationNumber = "41"
  val EuropeanVesselIDNumber = "80"
  val NameOfInlandWaterwayVessel = "81"

  val allowedModeOfTransportCodes =
    Set(Maritime, Rail, Road, Air, PostalConsignment, FixedTransportInstallations, InlandWaterway, Unknown)

  val allowedMeansOfTransportTypeCodes =
    Set(
      IMOShipIDNumber,
      NameOfVessel,
      WagonNumber,
      VehicleRegistrationNumber,
      IATAFlightNumber,
      AircraftRegistrationNumber,
      EuropeanVesselIDNumber,
      NameOfInlandWaterwayVessel
    )
}
