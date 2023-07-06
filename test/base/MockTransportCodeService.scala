/*
 * Copyright 2023 HM Revenue & Customs
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

package base

import connectors.CodeLinkConnector
import connectors.Tag._
import org.mockito.ArgumentMatchers.refEq
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.view.TransportCodeService

object MockTransportCodeService extends MockitoSugar {

  val codeLinkConnector = mock[CodeLinkConnector]

  when(codeLinkConnector.getTransportCodeForTag(refEq(AircraftRegistrationNumber))).thenReturn(("AircraftRegistrationNumber", "41"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(EuropeanVesselIDNumber))).thenReturn(("EuropeanVesselIDNumber", "80"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(FlightNumber))).thenReturn(("FlightNumber", "40"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(NameOfInlandWaterwayVessel))).thenReturn(("NameOfInlandWaterwayVessel", "81"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(NameOfVessel))).thenReturn(("NameOfVessel", "11"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(NotApplicable))).thenReturn(("NotApplicable", "option_none"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(ShipOrRoroImoNumber))).thenReturn(("ShipOrRoroImoNumber", "10"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(VehicleRegistrationNumber))).thenReturn(("VehicleRegistrationNumber", "30"))
  when(codeLinkConnector.getTransportCodeForTag(refEq(WagonNumber))).thenReturn(("WagonNumber", "20"))

  val transportCodeService: TransportCodeService = new TransportCodeService()(codeLinkConnector)
}
