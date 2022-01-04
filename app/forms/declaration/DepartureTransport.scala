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
import forms.declaration.TransportCodes._
import models.DeclarationType
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class DepartureTransport(meansOfTransportOnDepartureType: Option[String], meansOfTransportOnDepartureIDNumber: Option[String])

object DepartureTransport extends DeclarationPage {

  implicit val formats = Json.format[DepartureTransport]

  val meansOfTransportOnDepartureTypeKey = "meansOfTransportOnDepartureType"
  val meansOfTransportOnDepartureIDNumberKey = "meansOfTransportOnDepartureIDNumber"

  private val meansOfTransportOnDepartureTypeOptional =
    optional(
      text()
        .verifying(
          "declaration.transportInformation.meansOfTransport.departure.error.incorrect",
          isContainedIn(allowedMeansOfTransportTypeCodes + OptionNone)
        )
    ).verifying("declaration.transportInformation.meansOfTransport.departure.error.empty.optional", isPresent)

  private val meansOfTransportOnDepartureTypeRequired =
    optional(
      text()
        .verifying("declaration.transportInformation.meansOfTransport.departure.error.incorrect", isContainedIn(allowedMeansOfTransportTypeCodes))
    ).verifying("declaration.transportInformation.meansOfTransport.departure.error.empty", isPresent)

  private def meansOfTransportOnDepartureIDNumberMapping(transportType: String) =
    s"${meansOfTransportOnDepartureIDNumberKey}_$transportType" -> mandatoryIfEqual(
      meansOfTransportOnDepartureTypeKey,
      transportType,
      text()
        .verifying("declaration.transportInformation.meansOfTransport.reference.error.empty", nonEmpty)
        .verifying("declaration.transportInformation.meansOfTransport.reference.error.length", isEmpty or noLongerThan(35))
        .verifying(
          "declaration.transportInformation.meansOfTransport.reference.error.invalid",
          isEmpty or (noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters)
        )
    )

  private val requiredMapping = mapping(
    meansOfTransportOnDepartureTypeKey -> meansOfTransportOnDepartureTypeRequired,
    meansOfTransportOnDepartureIDNumberMapping(IMOShipIDNumber),
    meansOfTransportOnDepartureIDNumberMapping(NameOfVessel),
    meansOfTransportOnDepartureIDNumberMapping(WagonNumber),
    meansOfTransportOnDepartureIDNumberMapping(VehicleRegistrationNumber),
    meansOfTransportOnDepartureIDNumberMapping(IATAFlightNumber),
    meansOfTransportOnDepartureIDNumberMapping(AircraftRegistrationNumber),
    meansOfTransportOnDepartureIDNumberMapping(EuropeanVesselIDNumber),
    meansOfTransportOnDepartureIDNumberMapping(NameOfInlandWaterwayVessel)
  )(form2Model)(model2Form)

  private val optionalMapping = mapping(
    meansOfTransportOnDepartureTypeKey -> meansOfTransportOnDepartureTypeOptional,
    meansOfTransportOnDepartureIDNumberMapping(IMOShipIDNumber),
    meansOfTransportOnDepartureIDNumberMapping(NameOfVessel),
    meansOfTransportOnDepartureIDNumberMapping(WagonNumber),
    meansOfTransportOnDepartureIDNumberMapping(VehicleRegistrationNumber),
    meansOfTransportOnDepartureIDNumberMapping(IATAFlightNumber),
    meansOfTransportOnDepartureIDNumberMapping(AircraftRegistrationNumber),
    meansOfTransportOnDepartureIDNumberMapping(EuropeanVesselIDNumber),
    meansOfTransportOnDepartureIDNumberMapping(NameOfInlandWaterwayVessel)
  )(form2Model)(model2Form)

  private def form2Model: (
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String]
  ) => DepartureTransport = {

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
      DepartureTransport(
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

  private def form2Ref(refs: Option[String]*): Option[String] = Some(refs.map(_.getOrElse("")).mkString)

  private def model2Form: DepartureTransport => Option[
    (Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])
  ] =
    implicit model =>
      Some(
        (
          model.meansOfTransportOnDepartureType,
          model2Ref(IMOShipIDNumber),
          model2Ref(NameOfVessel),
          model2Ref(WagonNumber),
          model2Ref(VehicleRegistrationNumber),
          model2Ref(IATAFlightNumber),
          model2Ref(AircraftRegistrationNumber),
          model2Ref(EuropeanVesselIDNumber),
          model2Ref(NameOfInlandWaterwayVessel)
        )
    )

  private def model2Ref(transportType: String)(implicit model: DepartureTransport): Option[String] =
    if (model.meansOfTransportOnDepartureType.contains(transportType)) model.meansOfTransportOnDepartureIDNumber else None

  def form(declarationType: DeclarationType): Form[DepartureTransport] = declarationType match {
    case DeclarationType.CLEARANCE => Form(optionalMapping)
    case _                         => Form(requiredMapping)
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.departureTransport.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
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
  val OptionNone = "option_none"

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
