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
import forms.MappingHelper.requiredRadioWithArgs
import forms.common.YesNoAnswer.YesNoAnswers.{no, yes}
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import models.FieldMapping
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.text
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.view.Countries.isValidCountryName
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class TransportCountry(countryName: Option[String]) extends Ordered[TransportCountry] {
  override def compare(that: TransportCountry): Int =
    (countryName, that.countryName) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }
}

object TransportCountry extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[TransportCountry] = Json.format[TransportCountry]

  val pointer: String = "meansOfTransportCrossingTheBorderNationality"

  val transportCountry = "transportCountry"
  val hasTransportCountry = "hasTransportCountry"

  val prefix = "declaration.transportInformation.transportCountry"

  def form(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Form[TransportCountry] =
    Form(mapping(transportMode))

  private def mapping(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Mapping[TransportCountry] =
    Forms.mapping(
      hasTransportCountry -> requiredRadioWithArgs(s"$prefix.error.empty", List(transportMode)),
      transportCountry -> mandatoryIfEqual(
        hasTransportCountry,
        yes,
        text
          .verifying(nonEmptyConstraint(transportMode))
          .verifying(s"$prefix.country.error.invalid", input => input.isEmpty or isValidCountryName(input))
      )
    )(form2Model)(model2Form)

  private def nonEmptyConstraint(transportMode: String): Constraint[String] =
    Constraint("constraint.nonEmpty.country") { country =>
      if (country.trim.nonEmpty) Valid else Invalid(List(ValidationError(s"$prefix.country.error.empty", transportMode)))
    }

  private def form2Model: (String, Option[String]) => TransportCountry = { case (hasTransportCountry, countryName) =>
    TransportCountry(if (hasTransportCountry == yes) countryName else None)
  }

  private def model2Form: TransportCountry => Option[(String, Option[String])] =
    _.countryName match {
      case Some(name) => Some((yes, Some(name)))
      case None       => Some((no, None))
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.transportCountry.common"))
}
