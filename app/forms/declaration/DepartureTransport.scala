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
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class DepartureTransport(borderModeOfTransportCode: String, meansOfTransportOnDepartureType: String, meansOfTransportOnDepartureIDNumber: String)

object DepartureTransport {
  val formId = "BorderTransport"

  implicit val formats = Json.format[DepartureTransport]

  val formMapping = mapping(
    "borderModeOfTransportCode" -> requiredRadio("supplementary.transportInfo.borderTransportMode.error.empty")
      .verifying("supplementary.transportInfo.borderTransportMode.error.incorrect", isContainedIn(allowedModeOfTransportCodes)),
    "meansOfTransportOnDepartureType" -> requiredRadio("supplementary.transportInfo.meansOfTransport.departure.error.empty")
      .verifying("supplementary.transportInfo.meansOfTransport.departure.error.incorrect", isContainedIn(allowedMeansOfTransportTypeCodes)),
    "meansOfTransportOnDepartureIDNumber" -> text()
      .verifying("supplementary.transportInfo.meansOfTransport.reference.error.empty", nonEmpty)
      .verifying("supplementary.transportInfo.meansOfTransport.reference.error.length", noLongerThan(27))
      .verifying("supplementary.transportInfo.meansOfTransport.reference.error.invalid", isAlphanumericWithAllowedSpecialCharacters)
  )(DepartureTransport.apply)(DepartureTransport.unapply)

  def form(): Form[DepartureTransport] = Form(DepartureTransport.formMapping)

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
