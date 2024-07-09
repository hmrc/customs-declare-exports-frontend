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

package forms.section6

import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.TransportCodeService
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class DepartureTransport(meansOfTransportOnDepartureType: Option[String], meansOfTransportOnDepartureIDNumber: Option[String])

object DepartureTransport extends DeclarationPage {

  implicit val formats: OFormat[DepartureTransport] = Json.format[DepartureTransport]

  val radioButtonGroupId = "departureTransportType"

  val prefix = "declaration.transportInformation.meansOfTransport.departure"

  def form(transportCodes: TransportCodes)(implicit transportCodeService: TransportCodeService): Form[DepartureTransport] = {
    val isV3WhenPC0019 = transportCodes == transportCodeService.transportCodesForV3WhenPC0019

    def mappingForRadioDepartureType(transportCodes: TransportCodes): Mapping[Option[String]] =
      optional(text.verifying(s"$prefix.error.incorrect", isContainedIn(transportCodes.asList.map(_.value))))
        .verifying(s"$prefix.error.empty${if (isV3WhenPC0019) ".v3" else ""}", isSome)

    Form(
      mapping(
        radioButtonGroupId -> mappingForRadioDepartureType(transportCodes),
        mappingForInputDepartureNumber(transportCodes.code1),
        mappingForInputDepartureNumber(transportCodes.code2),
        mappingForInputDepartureNumber(transportCodes.code3),
        mappingForInputDepartureNumber(transportCodes.code4),
        mappingForInputDepartureNumber(transportCodes.code5),
        mappingForInputDepartureNumber(transportCodes.code6),
        mappingForInputDepartureNumber(transportCodes.code7),
        mappingForInputDepartureNumber(transportCodes.code8)
      )(form2Model)(model2Form(transportCodes))
    )
  }

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
  ) => DepartureTransport = { case (departureType, v1, v2, v3, v4, v5, v6, v7, v8) =>
    DepartureTransport(departureType, List(v1, v2, v3, v4, v5, v6, v7, v8).flatten.headOption.orElse(Some("")))
  }

  // scalastyle:off
  private def model2Form(transportCodes: TransportCodes): DepartureTransport => Option[
    (Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])
  ] =
    implicit departureTransport =>
      Some(
        (
          departureTransport.meansOfTransportOnDepartureType,
          model2Ref(transportCodes.code1),
          model2Ref(transportCodes.code2),
          model2Ref(transportCodes.code3),
          model2Ref(transportCodes.code4),
          model2Ref(transportCodes.code5),
          model2Ref(transportCodes.code6),
          model2Ref(transportCodes.code7),
          model2Ref(transportCodes.code8)
        )
      )
  // scalastyle:on

  private def model2Ref(transportCode: TransportCode)(implicit departureTransport: DepartureTransport): Option[String] =
    if (!departureTransport.meansOfTransportOnDepartureType.contains(transportCode.value)) None
    else departureTransport.meansOfTransportOnDepartureIDNumber

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
