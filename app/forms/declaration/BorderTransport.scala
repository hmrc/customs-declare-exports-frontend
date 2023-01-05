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

import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import forms.declaration.TransportCodes._
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class BorderTransport(meansOfTransportCrossingTheBorderType: String, meansOfTransportCrossingTheBorderIDNumber: String)

object BorderTransport extends DeclarationPage {

  implicit val formats: OFormat[BorderTransport] = Json.format[BorderTransport]

  val radioButtonGroupId = "borderTransportType"

  val prefix = "declaration.transportInformation.meansOfTransport.crossingTheBorder"

  def form: Form[BorderTransport] = Form(mapping)

  def transportReferenceMapping(transportCode: TransportCode): (String, Mapping[Option[String]]) =
    transportCode.id -> mandatoryIfEqual(
      radioButtonGroupId,
      transportCode.value,
      text
        .verifying(s"$prefix.IDNumber.error.empty", nonEmpty)
        .verifying(s"$prefix.IDNumber.error.length", isEmpty or noLongerThan(35))
        .verifying(s"$prefix.IDNumber.error.invalid", isAlphanumericWithAllowedSpecialCharacters)
    )

  private def mapping: Mapping[BorderTransport] =
    Forms.mapping(
      radioButtonGroupId -> requiredRadio(s"$prefix.error.empty")
        .verifying(s"$prefix.error.incorrect", isContainedIn(transportCodesOnBorderTransport.map(_.value))),
      transportReferenceMapping(ShipOrRoroImoNumber),
      transportReferenceMapping(NameOfVessel),
      transportReferenceMapping(WagonNumber),
      transportReferenceMapping(VehicleRegistrationNumber),
      transportReferenceMapping(FlightNumber),
      transportReferenceMapping(AircraftRegistrationNumber),
      transportReferenceMapping(EuropeanVesselIDNumber),
      transportReferenceMapping(NameOfInlandWaterwayVessel)
    )(form2Model)(model2Form)

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

  private def model2Form: BorderTransport => Option[
    (String, Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])
  ] =
    implicit borderTransport =>
      Some(
        (
          borderTransport.meansOfTransportCrossingTheBorderType,
          model2Ref(ShipOrRoroImoNumber),
          model2Ref(NameOfVessel),
          model2Ref(WagonNumber),
          model2Ref(VehicleRegistrationNumber),
          model2Ref(FlightNumber),
          model2Ref(AircraftRegistrationNumber),
          model2Ref(EuropeanVesselIDNumber),
          model2Ref(NameOfInlandWaterwayVessel)
        )
      )

  private def form2Ref(refs: Option[String]*): String = refs.map(_.getOrElse("")).mkString

  private def model2Ref(transportCode: TransportCode)(implicit model: BorderTransport): Option[String] =
    if (transportCode.value != model.meansOfTransportCrossingTheBorderType) None
    else Some(model.meansOfTransportCrossingTheBorderIDNumber)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.borderTransport.common"))
}
