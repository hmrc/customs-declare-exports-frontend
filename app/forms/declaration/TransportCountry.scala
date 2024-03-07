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

package forms.declaration

import connectors.CodeListConnector
import forms.DeclarationPage
import forms.declaration.TransportCountry.keyForAmend
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue}
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.Countries
import services.Countries.isValidCountryCode
import utils.validators.forms.FieldValidator._

case class TransportCountry(countryName: Option[String]) extends Ordered[TransportCountry] with Amendment {

  override def compare(that: TransportCountry): Int =
    (countryName, that.countryName) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }

  def value: String = countryName.getOrElse("")

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    countryName.fold("")(forAddedValue(pointer, messages(keyForAmend), _))

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(keyForAmend), value, newValue.value)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    countryName.fold("")(forRemovedValue(pointer, messages(keyForAmend), _))
}

object TransportCountry extends DeclarationPage with FieldMapping {

  implicit val format: OFormat[TransportCountry] = Json.format[TransportCountry]

  val pointer: String = "meansOfTransportCrossingTheBorderNationality"

  private val keyForAmend = "declaration.summary.transport.registrationCountry"

  val transportCountry = "transport-country"

  val prefix = "declaration.transportInformation.transportCountry"

  def form(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Form[TransportCountry] =
    Form(mapping(transportMode))

  private def mapping(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Mapping[TransportCountry] =
    Forms.mapping(
      transportCountry -> text
        .verifying(nonEmptyConstraint(transportMode))
        .verifying(s"$prefix.country.error.invalid", input => input.isEmpty or isValidCountryCode(input))
    )(country => TransportCountry(Some(country)))(country => country.countryName.map(convertToCode))

  // TODO Remove when transport country field migrated
  private def convertToCode(nameOrCode: String)(implicit messages: Messages, codeListConnector: CodeListConnector): String =
    if (Countries.isValidCountryCode(nameOrCode)) nameOrCode else Countries.findByName(nameOrCode).map(_.countryCode).getOrElse("")

  private def nonEmptyConstraint(transportMode: String): Constraint[String] =
    Constraint("constraint.nonEmpty.country") { country =>
      if (country.trim.nonEmpty) Valid else Invalid(List(ValidationError(s"$prefix.country.error.empty", transportMode)))
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.transportCountry.common"))
}
