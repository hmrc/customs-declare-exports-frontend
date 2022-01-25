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
import forms.declaration.TransportCodes.transportCodesForV3WhenPC0019
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator.{isContainedIn, noLongerThan, _}

case class DepartureTransport(meansOfTransportOnDepartureType: Option[String], meansOfTransportOnDepartureIDNumber: Option[String])

object DepartureTransport extends DeclarationPage {

  implicit val formats = Json.format[DepartureTransport]

  val radioButtonGroupId = "departureTransportType"

  val prefix = "declaration.transportInformation.meansOfTransport.departure"

  def form(transportCodes: Seq[TransportCode]): Form[DepartureTransport] = Form(mappingFor(transportCodes))

  private def mappingFor(transportCodes: Seq[TransportCode]): Mapping[DepartureTransport] =
    mapping(
      radioButtonGroupId -> mappingForRadioDepartureType(transportCodes),
      mappingForInputDepartureNumber(transportCodes(0)),
      mappingForInputDepartureNumber(transportCodes(1)),
      mappingForInputDepartureNumber(transportCodes(2)),
      mappingForInputDepartureNumber(transportCodes(3)),
      mappingForInputDepartureNumber(transportCodes(4)),
      mappingForInputDepartureNumber(transportCodes(5)),
      mappingForInputDepartureNumber(transportCodes(6)),
      mappingForInputDepartureNumber(transportCodes(7))
    )(form2Model)(model2Form(transportCodes))

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
    case (departureType, v1, v2, v3, v4, v5, v6, v7, v8) =>
      DepartureTransport(departureType, List(v1, v2, v3, v4, v5, v6, v7, v8).flatten.headOption.orElse(Some("")))
  }

  private def model2Form(transportCodes: Seq[TransportCode]): DepartureTransport => Option[
    (Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])
  ] =
    implicit departureTransport =>
      Some(
        (
          departureTransport.meansOfTransportOnDepartureType,
          model2Ref(transportCodes(0)),
          model2Ref(transportCodes(1)),
          model2Ref(transportCodes(2)),
          model2Ref(transportCodes(3)),
          model2Ref(transportCodes(4)),
          model2Ref(transportCodes(5)),
          model2Ref(transportCodes(6)),
          model2Ref(transportCodes(7))
        )
    )

  private def model2Ref(transportCode: TransportCode)(implicit departureTransport: DepartureTransport): Option[String] =
    if (!departureTransport.meansOfTransportOnDepartureType.contains(transportCode.value)) None
    else departureTransport.meansOfTransportOnDepartureIDNumber

  private def mappingForRadioDepartureType(transportCodes: Seq[TransportCode]): Mapping[Option[String]] = {
    val isV3WhenPC0019 = transportCodes == transportCodesForV3WhenPC0019

    optional(text.verifying(s"$prefix.error.incorrect", isContainedIn(transportCodes.map(_.value))))
      .verifying(s"$prefix.error.empty${if (isV3WhenPC0019) ".v3" else ""}", isPresent)
  }

  private def mappingForInputDepartureNumber(transportCode: TransportCode): (String, Mapping[Option[String]]) =
    transportCode.id -> mandatoryIfEqual(
      radioButtonGroupId,
      transportCode.value,
      text
        .verifying(s"$prefix.error.empty.input", nonEmpty)
        .verifying(s"$prefix.error.length", isEmpty or noLongerThan(35))
        .verifying(s"$prefix.error.invalid", isEmpty or (noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters))
    )

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.departureTransport.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
