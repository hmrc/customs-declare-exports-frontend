/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import connectors.{CodeLinkConnector, Tag}
import forms.section6.{TransportCode, TransportCodes}

import javax.inject.{Inject, Singleton}

@Singleton
class TransportCodeService @Inject() (implicit codeLinkConnector: CodeLinkConnector) {

  val AircraftRegistrationNumber = TransportCode(Tag.AircraftRegistrationNumber, true)
  val EuropeanVesselIDNumber = TransportCode(Tag.EuropeanVesselIDNumber)
  val FlightNumber = TransportCode(Tag.FlightNumber, true)
  val NameOfInlandWaterwayVessel = TransportCode(Tag.NameOfInlandWaterwayVessel)
  val NameOfVessel = TransportCode(Tag.NameOfVessel, false, true)
  val NotApplicable = TransportCode(Tag.NotApplicable, false, true)
  val ShipOrRoroImoNumber = TransportCode(Tag.ShipOrRoroImoNumber, false, true)
  val VehicleRegistrationNumber = TransportCode(Tag.VehicleRegistrationNumber, false, true)
  val WagonNumber = TransportCode(Tag.WagonNumber, true, true)
  val NotProvided = TransportCode(Tag.NotProvided, false, false)

  // As used on /departure-transport when journey is Standard or Supplementary_Simplified and /inland-or-border is 'Border'
  val transportCodesForV1 = TransportCodes(
    ShipOrRoroImoNumber,
    NameOfVessel,
    WagonNumber,
    FlightNumber,
    AircraftRegistrationNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel,
    VehicleRegistrationNumber,
    NotProvided
  )

  // As used on /departure-transport by default
  val transportCodesForV2 = TransportCodes(
    VehicleRegistrationNumber,
    ShipOrRoroImoNumber,
    NameOfVessel,
    FlightNumber,
    AircraftRegistrationNumber,
    WagonNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel,
    NotProvided
  )

  // As used on /departure-transport when journey is Clearance
  val transportCodesForV3 = transportCodesForV1

  // As used on /departure-transport when journey is Clearance and PC is '0019'
  val transportCodesForV3WhenPC0019 = TransportCodes(
    ShipOrRoroImoNumber,
    NameOfVessel,
    WagonNumber,
    FlightNumber,
    AircraftRegistrationNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel,
    VehicleRegistrationNumber,
    NotProvided,
    Some(NotApplicable)
  )

  // As used on /border-transport
  val transportCodesOnBorderTransport = List(
    ShipOrRoroImoNumber,
    NameOfVessel,
    WagonNumber,
    VehicleRegistrationNumber,
    FlightNumber,
    AircraftRegistrationNumber,
    EuropeanVesselIDNumber,
    NameOfInlandWaterwayVessel,
    NotProvided
  )
}
