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
import models.DeclarationType
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.{of, optional}
import play.api.libs.json._
import utils.validators.forms.FieldValidator.isPresent

case class TransportLeavingTheBorder(code: Option[ModeOfTransportCode] = None) {
  def getCodeValue: String = code.getOrElse(ModeOfTransportCode.Empty).value
}

object TransportLeavingTheBorder extends DeclarationPage {
  implicit val format = Json.format[TransportLeavingTheBorder]

  val classicMapping: Mapping[TransportLeavingTheBorder] = Forms.mapping(
    "transportLeavingTheBorder" ->
      optional(of(ModeOfTransportCode.classicFormatter("declaration.transportInformation.borderTransportMode.error.incorrect")))
        .verifying("declaration.transportInformation.borderTransportMode.error.empty", isPresent)
  )(TransportLeavingTheBorder.apply)(TransportLeavingTheBorder.unapply)

  val clearanceMapping: Mapping[TransportLeavingTheBorder] = Forms.mapping(
    "transportLeavingTheBorder" ->
      optional(of(ModeOfTransportCode.clearanceJourneyFormatter("declaration.transportInformation.borderTransportMode.error.incorrect")))
        .verifying("declaration.transportInformation.borderTransportMode.error.empty.optional", isPresent)
  )(TransportLeavingTheBorder.apply)(TransportLeavingTheBorder.unapply)

  def form(declarationType: DeclarationType): Form[TransportLeavingTheBorder] = declarationType match {
    case DeclarationType.CLEARANCE => Form(clearanceMapping)
    case _                         => Form(classicMapping)
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.transportLeavingTheBorder.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
