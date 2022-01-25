/*
 * Copyright 2022 HM Revenue & Customs
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

sealed abstract class TransportCode(
  val id: String,
  val value: String,
  val useAltRadioTextForV2: Boolean = false,
  val useAltRadioTextForBorderTransport: Boolean = false
)

object TransportCodes {

  case object AircraftRegistrationNumber extends TransportCode("aircraftRegistrationNumber", "41", true)
  case object EuropeanVesselIDNumber extends TransportCode("europeanVesselIDNumber", "80")
  case object FlightNumber extends TransportCode("flightNumber", "40", true)
  case object NameOfInlandWaterwayVessel extends TransportCode("nameOfInlandWaterwayVessel", "81")
  case object NameOfVessel extends TransportCode("nameOfVessel", "11", false, true)
  case object NotApplicable extends TransportCode("notApplicable", "option_none", false, true)
  case object ShipOrRoroImoNumber extends TransportCode("shipOrRoroImoNumber", "10", false, true)
  case object VehicleRegistrationNumber extends TransportCode("vehicleRegistrationNumber", "30", false, true)
  case object WagonNumber extends TransportCode("wagonNumber", "20", true, true)

  // As used on /departure-transport when journey is Standard or Supplementary_Simplified and /inland-or-border is 'Border'
  val transportCodesForV1 = List(
    ShipOrRoroImoNumber,
    NameOfVessel,
    WagonNumber,
    FlightNumber,
    AircraftRegistrationNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel,
    VehicleRegistrationNumber
  )

  // As used on /departure-transport by default
  val transportCodesForV2 = List(
    VehicleRegistrationNumber,
    ShipOrRoroImoNumber,
    NameOfVessel,
    FlightNumber,
    AircraftRegistrationNumber,
    WagonNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel
  )

  // As used on /departure-transport when journey is Clearance
  val transportCodesForV3 = transportCodesForV1

  // As used on /departure-transport when journey is Clearance and PC is '0019'
  val transportCodesForV3WhenPC0019 = transportCodesForV3 :+ NotApplicable

  // As used on /border-transport
  val transportCodesOnBorderTransport = List(
    ShipOrRoroImoNumber,
    NameOfVessel,
    WagonNumber,
    VehicleRegistrationNumber,
    FlightNumber,
    AircraftRegistrationNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel
  )
}
