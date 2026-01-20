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

package forms.section6

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.TransportCodeService
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class BorderTransport(meansOfTransportCrossingTheBorderType: String, meansOfTransportCrossingTheBorderIDNumber: String)

object BorderTransport extends DeclarationPage {

  implicit val formats: OFormat[BorderTransport] = Json.format[BorderTransport]

  val radioButtonGroupId = "borderTransportType"

  val prefix = "declaration.transportInformation.meansOfTransport.crossingTheBorder"

  def form(implicit tcs: TransportCodeService): Form[BorderTransport] =
    Form(
      mapping(
        radioButtonGroupId -> requiredRadio(s"$prefix.error.empty")
          .verifying(s"$prefix.error.incorrect", isContainedIn(tcs.transportCodesOnBorderTransport.map(_.value))),
        transportReferenceMapping(tcs.ShipOrRoroImoNumber),
        transportReferenceMapping(tcs.NameOfVessel),
        transportReferenceMapping(tcs.WagonNumber),
        transportReferenceMapping(tcs.VehicleRegistrationNumber),
        transportReferenceMapping(tcs.FlightNumber),
        transportReferenceMapping(tcs.AircraftRegistrationNumber),
        transportReferenceMapping(tcs.EuropeanVesselIDNumber),
        transportReferenceMapping(tcs.NameOfInlandWaterwayVessel),
        //add mapping for new value created
      )(form2Model)(model2Form(tcs))
    )

  private def transportReferenceMapping(transportCode: TransportCode): (String, Mapping[Option[String]]) =
    transportCode.id -> mandatoryIfEqual(
      radioButtonGroupId,
      transportCode.value,
      text
        .verifying(s"$prefix.IDNumber.error.empty", nonEmpty)
        .verifying(s"$prefix.IDNumber.error.length", isEmpty or noLongerThan(35))
        .verifying(s"$prefix.IDNumber.error.invalid", isAlphanumericWithAllowedSpecialCharacters)
    )

  private def form2Model: (
    String,
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String]
  ) => BorderTransport = {
    case (
          transportType,
          shipIdNumber,
          nameOfVessel,
          wagonNumber,
          vehicleRegistrationNumber,
          flightNumber,
          aircraftRegistrationNumber,
          europeanVesselIDNumber,
          nameOfInlandWaterwayVessel
        ) =>
      BorderTransport(
        transportType,
        form2Ref(
          shipIdNumber,
          nameOfVessel,
          wagonNumber,
          vehicleRegistrationNumber,
          flightNumber,
          aircraftRegistrationNumber,
          europeanVesselIDNumber,
          nameOfInlandWaterwayVessel
        )
      )
  }

  // scalastyle:off
  private def model2Form(tcs: TransportCodeService): BorderTransport => Option[
    (String, Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])
  ] =
    implicit borderTransport =>
      Some(
        (
          borderTransport.meansOfTransportCrossingTheBorderType,
          model2Ref(tcs.ShipOrRoroImoNumber),
          model2Ref(tcs.NameOfVessel),
          model2Ref(tcs.WagonNumber),
          model2Ref(tcs.VehicleRegistrationNumber),
          model2Ref(tcs.FlightNumber),
          model2Ref(tcs.AircraftRegistrationNumber),
          model2Ref(tcs.EuropeanVesselIDNumber),
          model2Ref(tcs.NameOfInlandWaterwayVessel)
        )
      )
  // scalastyle:on

  private def form2Ref(refs: Option[String]*): String = refs.map(_.getOrElse("")).mkString

  private def model2Ref(transportCode: TransportCode)(implicit model: BorderTransport): Option[String] =
    if (transportCode.value != model.meansOfTransportCrossingTheBorderType) None
    else Some(model.meansOfTransportCrossingTheBorderIDNumber)
}
