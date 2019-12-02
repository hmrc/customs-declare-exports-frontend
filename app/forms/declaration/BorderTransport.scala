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

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.declaration.TransportCodes._
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.Countries.allCountries
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class BorderTransport(
  meansOfTransportCrossingTheBorderNationality: Option[String],
  meansOfTransportCrossingTheBorderType: String,
  meansOfTransportCrossingTheBorderIDNumber: String
)

object BorderTransport extends DeclarationPage {

  implicit val formats: OFormat[BorderTransport] = Json.format[BorderTransport]

  def transportReferenceMapping(id: String, transportType: String) = s"borderTransportReference_$id" -> mandatoryIfEqual(
    "borderTransportType",
    transportType,
    text()
      .verifying("declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.error.empty", nonEmpty)
      .verifying("declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.error.length", noLongerThan(35))
      .verifying(
        "declaration.transportInformation.meansOfTransport.CrossingTheBorder.IDNumber.error.invalid",
        isAlphanumericWithAllowedSpecialCharacters
      )
  )

  val formMapping: Mapping[BorderTransport] = mapping(
    "borderTransportNationality" -> optional(
      text()
        .verifying(
          "declaration.transportInformation.meansOfTransport.crossingTheBorder.nationality.error.incorrect",
          isContainedIn(allCountries.map(_.countryName))
        )
    ),
    "borderTransportType" -> requiredRadio("declaration.transportInformation.meansOfTransport.crossingTheBorder.error.empty")
      .verifying(
        "declaration.transportInformation.meansOfTransport.crossingTheBorder.error.incorrect",
        isContainedIn(allowedMeansOfTransportTypeCodes)
      ),
    transportReferenceMapping("IMOShipIDNumber", IMOShipIDNumber),
    transportReferenceMapping("nameOfVessel", NameOfVessel),
    transportReferenceMapping("wagonNumber", WagonNumber),
    transportReferenceMapping("vehicleRegistrationNumber", VehicleRegistrationNumber),
    transportReferenceMapping("IATAFlightNumber", IATAFlightNumber),
    transportReferenceMapping("aircraftRegistrationNumber", AircraftRegistrationNumber),
    transportReferenceMapping("europeanVesselIDNumber", EuropeanVesselIDNumber),
    transportReferenceMapping("nameOfInlandWaterwayVessel", NameOfInlandWaterwayVessel)
  )(form2Model)(model2Form)

  def form(): Form[BorderTransport] = Form(BorderTransport.formMapping)

  private def form2Model: (
    Option[String],
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
        nationality,
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
        nationality,
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
    (
      Option[String],
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String]
    )
  ] =
    implicit model =>
      Some(
        model.meansOfTransportCrossingTheBorderNationality,
        model.meansOfTransportCrossingTheBorderType,
        model2Ref(IMOShipIDNumber),
        model2Ref(NameOfVessel),
        model2Ref(WagonNumber),
        model2Ref(VehicleRegistrationNumber),
        model2Ref(IATAFlightNumber),
        model2Ref(AircraftRegistrationNumber),
        model2Ref(EuropeanVesselIDNumber),
        model2Ref(NameOfInlandWaterwayVessel)
    )

  private def form2Ref(refs: Option[String]*) = refs.map(_.getOrElse("")).mkString

  private def model2Ref(transportType: String)(implicit model: BorderTransport) =
    if (model.meansOfTransportCrossingTheBorderType.equals(transportType)) {
      Some(model.meansOfTransportCrossingTheBorderIDNumber)
    } else {
      None
    }
}
