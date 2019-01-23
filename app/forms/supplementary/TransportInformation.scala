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

package forms.supplementary

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.{boolean, optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue
import utils.validators.FormFieldValidator._

case class TransportInformation(
  inlandModeOfTransportCode: Option[String],
  borderModeOfTransportCode: String,
  meansOfTransportOnDepartureType: String,
  meansOfTransportOnDepartureIDNumber: Option[String],
  meansOfTransportCrossingTheBorderType: String,
  meansOfTransportCrossingTheBorderIDNumber: Option[String],
  meansOfTransportCrossingTheBorderNationality: Option[String],
  container: Boolean,
  containerId: Option[String]
) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.consignment.arrivalTransportMeans.modeCode" -> inlandModeOfTransportCode.getOrElse(""),
      "declaration.borderTransportMeans.modeCode" -> borderModeOfTransportCode,
      "declaration.borderTransportMeans.registrationNationalityCode" ->
        allCountries
          .find(country => meansOfTransportCrossingTheBorderNationality.contains(country.countryName))
          .map(_.countryCode)
          .getOrElse(""),
      "declaration.goodsShipment.consignment.containerCode" -> container.toString,
      "declaration.goodsShipment.governmentAgencyGoodsItem.commodity.transportEquipment.id" -> containerId.getOrElse("")
    )
}

object TransportInformation {
  implicit val format = Json.format[TransportInformation]

  val id = "TransportInformation"

  def form(): Form[TransportInformation] = Form(mapping)

  import ModeOfTransportCodes._
  private val allowedModeOfTransportCodes =
    Set(Maritime, Rail, Road, Air, PostalConsignment, FixedTransportInstallations, InlandWaterway, Unknown)

  import MeansOfTransportTypeCodes._
  private val allowedMeansOfTransportTypeCodes =
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

  val mapping = Forms.mapping(
    "inlandModeOfTransportCode" -> optional(
      text()
        .verifying("supplementary.transportInfo.error.incorrect", isContainedIn(allowedModeOfTransportCodes))
    ),
    "borderModeOfTransportCode" -> text()
      .verifying("supplementary.transportInfo.error.empty", _.trim.nonEmpty)
      .verifying("supplementary.transportInfo.error.incorrect", isEmpty or isContainedIn(allowedModeOfTransportCodes)),
    "meansOfTransportOnDepartureType" -> text()
      .verifying("supplementary.transportInfo.error.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.transportInfo.error.incorrect",
        isEmpty or isContainedIn(allowedMeansOfTransportTypeCodes)
      ),
    "meansOfTransportOnDepartureIDNumber" -> optional(
      text()
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.length", noLongerThan(27))
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.specialCharacters", isAlphanumeric)
    ),
    "meansOfTransportCrossingTheBorderType" -> text()
      .verifying("supplementary.transportInfo.error.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.transportInfo.error.incorrect",
        isEmpty or isContainedIn(allowedMeansOfTransportTypeCodes)
      ),
    "meansOfTransportCrossingTheBorderIDNumber" -> optional(
      text()
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.length", noLongerThan(35))
        .verifying("supplementary.transportInfo.meansOfTransport.idNumber.error.specialCharacters", isAlphanumeric)
    ),
    "meansOfTransportCrossingTheBorderNationality" -> optional(
      text()
        .verifying("supplementary.transportInfo.error.incorrect", isContainedIn(allCountries.map(_.countryName)))
    ),
    "container" -> boolean,
    "containerId" -> mandatoryIfTrue(
      "container",
      text()
        .verifying("supplementary.transportInfo.containerId.empty", nonEmpty)
        .verifying("supplementary.transportInfo.containerId.error", isEmpty or (isAlphanumeric and noLongerThan(17)))
    )
  )(TransportInformation.apply)(TransportInformation.unapply)

  object ModeOfTransportCodes {
    val Maritime = "1"
    val Rail = "2"
    val Road = "3"
    val Air = "4"
    val PostalConsignment = "5"
    val FixedTransportInstallations = "7"
    val InlandWaterway = "8"
    val Unknown = "9"
  }

  object MeansOfTransportTypeCodes {
    val IMOShipIDNumber = "10"
    val NameOfVessel = "11"
    val WagonNumber = "20"
    val VehicleRegistrationNumber = "30"
    val IATAFlightNumber = "40"
    val AircraftRegistrationNumber = "41"
    val EuropeanVesselIDNumber = "80"
    val NameOfInlandWaterwayVessel = "81"
  }

}
