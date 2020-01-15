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
import forms.declaration.TransportCodes._
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class DepartureTransport(meansOfTransportOnDepartureType: String, meansOfTransportOnDepartureIDNumber: String)

object DepartureTransport extends DeclarationPage {

  implicit val formats = Json.format[DepartureTransport]

  val formMapping = mapping(
    "meansOfTransportOnDepartureType" -> requiredRadio("declaration.transportInformation.meansOfTransport.departure.error.empty")
      .verifying("declaration.transportInformation.meansOfTransport.departure.error.incorrect", isContainedIn(allowedMeansOfTransportTypeCodes)),
    "meansOfTransportOnDepartureIDNumber" -> text()
      .verifying("declaration.transportInformation.meansOfTransport.reference.error.empty", nonEmpty)
      .verifying(
        "declaration.transportInformation.meansOfTransport.reference.error.invalid",
        isEmpty or (noLongerThan(27) and isAlphanumericWithAllowedSpecialCharacters)
      )
  )(DepartureTransport.apply)(DepartureTransport.unapply)

  def form(): Form[DepartureTransport] = Form(DepartureTransport.formMapping)

}

object TransportCodes {

  val IMOShipIDNumber = "10"
  val NameOfVessel = "11"
  val WagonNumber = "20"
  val VehicleRegistrationNumber = "30"
  val IATAFlightNumber = "40"
  val AircraftRegistrationNumber = "41"
  val EuropeanVesselIDNumber = "80"
  val NameOfInlandWaterwayVessel = "81"

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
